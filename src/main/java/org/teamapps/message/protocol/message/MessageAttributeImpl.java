/*-
 * ========================LICENSE_START=================================
 * TeamApps Message Protocol
 * ---
 * Copyright (C) 2022 - 2024 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.teamapps.message.protocol.message;

import org.teamapps.message.protocol.file.FileData;
import org.teamapps.message.protocol.file.FileDataReader;
import org.teamapps.message.protocol.file.FileDataWriter;
import org.teamapps.message.protocol.model.*;
import org.teamapps.message.protocol.utils.MessageUtils;
import org.teamapps.message.protocol.xml.XmlUtils;
import org.teamapps.message.protocol.xml.XmlNode;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class MessageAttributeImpl implements MessageAttribute {

	private final AttributeDefinition attributeDefinition;
	private Object value;

	public MessageAttributeImpl(AttributeDefinition attributeDefinition, Object value) {
		this.attributeDefinition = attributeDefinition;
		this.value = value;
	}

	public MessageAttributeImpl(MessageAttributeImpl attribute, AttributeDefinition remappedDefinition, ModelCollection modelCollection) {
		this.attributeDefinition = remappedDefinition;
		if (attributeDefinition.isReferenceProperty()) {
			MessageModel referencedObjectDefinition = attributeDefinition.getReferencedObject();
			PojoObjectDecoder<? extends Message> messageDecoder = modelCollection.getMessageDecoder(referencedObjectDefinition.getObjectUuid());
			if (attributeDefinition.isMultiReference()) {
				List<Message> messages = (List<Message>) attribute.value;
				List<Message> remappedMessages = new ArrayList<>();
				for (Message message : messages) {
					remappedMessages.add(messageDecoder.remap(message));
				}
				this.value = remappedMessages;
			} else {
				Message message = (Message) attribute.value;
				this.value = messageDecoder.remap(message);
			}
		} else {
			this.value = attribute.value;
		}
	}

	public MessageAttributeImpl(DataInputStream dis, MessageModel model, FileDataReader fileDataReader, PojoObjectDecoderRegistry decoderRegistry) throws IOException {
		AttributeType type = AttributeType.getById(dis.readByte());
		int key = dis.readShort();
		AttributeDefinition modelDef = model == null ? null : model.getAttributeDefinitionByKey(key);
		if (modelDef == null) {
			if (type.isReference()) {
				if (type == AttributeType.OBJECT_SINGLE_REFERENCE) {
					Message message = new Message(dis, fileDataReader);
					MessageModel definition = message.getModel();
					this.attributeDefinition = new AbstractAttributeDefinition(model, null, key, null, definition, false);
					value = message;
				} else {
					List<Message> messages = new ArrayList<>();
					int messageCount = dis.readInt();
					MessageModel definition = null;
					for (int i = 0; i < messageCount; i++) {
						Message message = new Message(dis, fileDataReader);
						if (definition == null) {
							definition = message.getModel();
						}
						messages.add(message);
					}
					this.attributeDefinition = new AbstractAttributeDefinition(model, null, key, null, definition, true);
					value = messages;
				}
			} else {
				this.attributeDefinition = new AbstractAttributeDefinition(model, null, key, type, null);
				this.value = readValue(dis, attributeDefinition.getType(), fileDataReader);
			}
		} else {
			this.attributeDefinition = modelDef;
			if (type != attributeDefinition.getType()) {
				throw new RuntimeException("Message parsing error - property type mismatch: " + type + " <-> " + attributeDefinition.getType());
			}
			if (attributeDefinition.getType().isReference()) {
				if (type == AttributeType.OBJECT_SINGLE_REFERENCE) {
					MessageModel referencedObjectDefinition = attributeDefinition.getReferencedObject();
					if (decoderRegistry != null && decoderRegistry.containsDecoder(referencedObjectDefinition.getObjectUuid())) {
						PojoObjectDecoder<? extends Message> messageDecoder = decoderRegistry.getMessageDecoder(referencedObjectDefinition.getObjectUuid());
						value = messageDecoder.decode(dis, fileDataReader);
					} else {
						value = new Message(dis, referencedObjectDefinition, fileDataReader, decoderRegistry);
					}
				} else {
					MessageModel referencedObjectDefinition = attributeDefinition.getReferencedObject();
					List<Message> messages = new ArrayList<>();
					int messageCount = dis.readInt();
					if (decoderRegistry != null && decoderRegistry.containsDecoder(referencedObjectDefinition.getObjectUuid())) {
						PojoObjectDecoder<? extends Message> messageDecoder = decoderRegistry.getMessageDecoder(referencedObjectDefinition.getObjectUuid());
						for (int i = 0; i < messageCount; i++) {
							messages.add(messageDecoder.decode(dis, fileDataReader));
						}
					} else {
						for (int i = 0; i < messageCount; i++) {
							messages.add(new Message(dis, referencedObjectDefinition, fileDataReader, decoderRegistry));
						}
					}
					value = messages;
				}
			} else {
				value = readValue(dis, attributeDefinition.getType(), fileDataReader);
			}
		}
	}

	public MessageAttributeImpl(Element element, AttributeDefinition definition, FileDataReader fileDataReader, PojoObjectDecoderRegistry decoderRegistry) {
		this.attributeDefinition = definition;
		AttributeType type = definition.getType();
		if (type.isReference()) {
			if (type == AttributeType.OBJECT_SINGLE_REFERENCE) {
				MessageModel referencedObjectDefinition = definition.getReferencedObject();
				Element refElement = XmlUtils.readChildElement(element, referencedObjectDefinition.getName());
				if (refElement == null || !referencedObjectDefinition.getName().equals(refElement.getNodeName())) {
					throw new RuntimeException("Wrong element name, expected:" + referencedObjectDefinition.getName() + ", actual:" + refElement.getNodeName());
				}
				if (decoderRegistry != null && decoderRegistry.containsDecoder(referencedObjectDefinition.getObjectUuid())) {
					PojoObjectDecoder<? extends Message> messageDecoder = decoderRegistry.getMessageDecoder(referencedObjectDefinition.getObjectUuid());
					value = messageDecoder.decode(refElement, fileDataReader);
				} else {
					value = new Message(refElement, referencedObjectDefinition, fileDataReader, decoderRegistry);
				}
			} else {
				MessageModel referencedObjectDefinition = definition.getReferencedObject();
				List<Message> messages = new ArrayList<>();
				List<Element> multiChildNodes = XmlUtils.readChildrenElements(element, referencedObjectDefinition.getName());
				int messageCount = multiChildNodes.size();
				if (decoderRegistry != null && decoderRegistry.containsDecoder(referencedObjectDefinition.getObjectUuid())) {
					PojoObjectDecoder<? extends Message> messageDecoder = decoderRegistry.getMessageDecoder(referencedObjectDefinition.getObjectUuid());
					for (int i = 0; i < messageCount; i++) {
						messages.add(messageDecoder.decode(multiChildNodes.get(i), fileDataReader));
					}
				} else {
					for (int i = 0; i < messageCount; i++) {
						messages.add(new Message(multiChildNodes.get(i), referencedObjectDefinition, fileDataReader, decoderRegistry));
					}
				}
				value = messages;
			}
		} else {
			value = readValue(element, type, fileDataReader);
		}
	}

	private Object readValue(DataInputStream dis, AttributeType type, FileDataReader fileDataReader) throws IOException {
		return switch (attributeDefinition.getType()) {
			case BOOLEAN -> dis.readBoolean();
			case BYTE -> dis.readByte();
			case INT, ENUM -> dis.readInt();
			case LONG -> dis.readLong();
			case FLOAT -> dis.readFloat();
			case DOUBLE -> dis.readDouble();
			case STRING -> MessageUtils.readString(dis);
			case BITSET -> MessageUtils.readBitSet(dis);
			case BYTE_ARRAY -> MessageUtils.readByteArray(dis);
			case INT_ARRAY -> MessageUtils.readIntArray(dis);
			case LONG_ARRAY -> MessageUtils.readLongArray(dis);
			case FLOAT_ARRAY -> MessageUtils.readFloatArray(dis);
			case DOUBLE_ARRAY -> MessageUtils.readDoubleArray(dis);
			case STRING_ARRAY -> MessageUtils.readStringArray(dis);
			case FILE -> MessageUtils.readFile(dis, fileDataReader);
			case TIMESTAMP_32 -> MessageUtils.readInstant32(dis);
			case TIMESTAMP_64 -> MessageUtils.readInstant64(dis);
			case DATE_TIME -> MessageUtils.readLocalDateTime(dis);
			case DATE -> MessageUtils.readLocalDate(dis);
			case TIME -> MessageUtils.readLocalTime(dis);
			case GENERIC_MESSAGE -> MessageUtils.readGenericMessage(dis, fileDataReader);
			default ->
					throw new RuntimeException("Message parsing error - property type unknown:" + attributeDefinition.getType());
		};
	}

	private Object readValue(Element element, AttributeType type, FileDataReader fileDataReader) {
		return switch (attributeDefinition.getType()) {
			case BOOLEAN -> XmlUtils.readBoolean(element);
			case BYTE -> XmlUtils.readByte(element);
			case INT, ENUM -> XmlUtils.readInt(element);
			case LONG -> XmlUtils.readLong(element);
			case FLOAT -> XmlUtils.readFloat(element);
			case DOUBLE -> XmlUtils.readDouble(element);
			case STRING -> XmlUtils.readString(element);
			case BITSET -> XmlUtils.readBitSet(element);
			case BYTE_ARRAY -> XmlUtils.readByteArray(element);
			case INT_ARRAY -> XmlUtils.readIntArray(element);
			case LONG_ARRAY -> XmlUtils.readLongArray(element);
			case FLOAT_ARRAY -> XmlUtils.readFloatArray(element);
			case DOUBLE_ARRAY -> XmlUtils.readDoubleArray(element);
			case STRING_ARRAY -> XmlUtils.readStringArray(element);
			case FILE -> XmlUtils.readFile(element, fileDataReader);
			case TIMESTAMP_32 -> XmlUtils.readInstant32(element);
			case TIMESTAMP_64 -> XmlUtils.readInstant64(element);
			case DATE_TIME -> XmlUtils.readLocalDateTime(element);
			case DATE -> XmlUtils.readLocalDate(element);
			case TIME -> XmlUtils.readLocalTime(element);
			case GENERIC_MESSAGE -> XmlUtils.readGenericMessage(element, fileDataReader);
			default ->
					throw new RuntimeException("Message parsing error - property type unknown:" + attributeDefinition.getType());
		};
	}


	@Override
	public void write(DataOutputStream dos, FileDataWriter fileDataWriter, boolean updateFileData) throws IOException {
		dos.writeByte(attributeDefinition.getType().getId());
		dos.writeShort(attributeDefinition.getKey());
		switch (attributeDefinition.getType()) {
			case OBJECT_SINGLE_REFERENCE -> {
				Message referencedObject = getReferencedObject();
				referencedObject.write(dos, fileDataWriter, updateFileData);
			}
			case OBJECT_MULTI_REFERENCE -> {
				List<Message> referencedObjects = getReferencedObjects();
				if (referencedObjects == null || referencedObjects.isEmpty()) {
					dos.writeInt(0);
				} else {
					dos.writeInt(referencedObjects.size());
					for (Message referencedObject : referencedObjects) {
						referencedObject.write(dos, fileDataWriter, updateFileData);
					}
				}
			}
			case BOOLEAN -> dos.writeBoolean(getBooleanAttribute());
			case BYTE -> dos.writeByte(getByteAttribute());
			case INT, ENUM -> dos.writeInt(getIntAttribute());
			case LONG -> dos.writeLong(getLongAttribute());
			case FLOAT -> dos.writeFloat(getFloatAttribute());
			case DOUBLE -> dos.writeDouble(getDoubleAttribute());
			case STRING -> MessageUtils.writeString(dos, getStringAttribute());
			case BITSET -> MessageUtils.writeBitSet(dos, getBitSetAttribute());
			case BYTE_ARRAY -> MessageUtils.writeByteArray(dos, getByteArrayAttribute());
			case INT_ARRAY -> MessageUtils.writeIntArray(dos, getIntArrayAttribute());
			case LONG_ARRAY -> MessageUtils.writeLongArray(dos, getLongArrayAttribute());
			case FLOAT_ARRAY -> MessageUtils.writeFloatArray(dos, getFloatArrayAttribute());
			case DOUBLE_ARRAY -> MessageUtils.writeDoubleArray(dos, getDoubleArrayAttribute());
			case STRING_ARRAY -> MessageUtils.writeStringArray(dos, getStringArrayAttribute());
			case FILE -> {
				//todo value should be final - create a message object with updated fields instead!
				FileData fileData = MessageUtils.writeFile(dos, getFileData(), fileDataWriter);
				if (updateFileData) {
					value = fileData;
				}
			}
			case TIMESTAMP_32 -> MessageUtils.writeInstant32(dos, getTimestampAttribute());
			case TIMESTAMP_64 -> MessageUtils.writeInstant64(dos, getTimestampAttribute());
			case DATE_TIME -> MessageUtils.writeLocalDateTime(dos, getDateTimeAttribute());
			case DATE -> MessageUtils.writeLocalDate(dos, getDateAttribute());
			case TIME -> MessageUtils.writeLocalTime(dos, getTimeAttribute());
			case GENERIC_MESSAGE -> MessageUtils.writeGenericMessage(dos, getGenericMessageAttribute(), fileDataWriter);
		}
	}

	@Override
	public byte[] toBytes() throws IOException {
		return toBytes(null, false);
	}


	@Override
	public byte[] toBytes(FileDataWriter fileDataWriter) throws IOException {
		return toBytes(fileDataWriter, false);
	}

	@Override
	public byte[] toBytes(FileDataWriter fileDataWriter, boolean updateFileData) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		write(dos, fileDataWriter, updateFileData);
		dos.close();
		return bos.toByteArray();
	}

	protected void toXml(XmlNode parentNode, boolean withComments, FileDataWriter fileDataWriter) throws IOException {
		XmlNode xmlNode = withComments ? new XmlNode(attributeDefinition.getName(), attributeDefinition.getDefaultValue(), attributeDefinition.getComment()) : new XmlNode(attributeDefinition.getName());
		parentNode.addChild(xmlNode);
		switch (attributeDefinition.getType()) {
			case OBJECT_SINGLE_REFERENCE -> {
				Message referencedObject = getReferencedObject();
				referencedObject.toXml(xmlNode, withComments, fileDataWriter);
			}
			case OBJECT_MULTI_REFERENCE -> {
				List<Message> referencedObjects = getReferencedObjects();
				if (referencedObjects != null) {
					for (Message referencedObject : referencedObjects) {
						referencedObject.toXml(xmlNode, withComments, fileDataWriter);
					}
				}
			}
			default -> {
				if (value != null) {
					switch (attributeDefinition.getType()) {
						case INT, ENUM, LONG, FLOAT, DOUBLE, STRING -> xmlNode.setValue(value.toString());
						case BOOLEAN -> XmlUtils.writeBoolean(xmlNode, getBooleanAttribute());
						case BYTE -> XmlUtils.writeByte(xmlNode, getByteAttribute());
						case BITSET -> XmlUtils.writeBitSet(xmlNode, getBitSetAttribute());
						case BYTE_ARRAY -> XmlUtils.writeByteArray(xmlNode, getByteArrayAttribute());
						case INT_ARRAY -> XmlUtils.writeIntArray(xmlNode, getIntArrayAttribute());
						case LONG_ARRAY -> XmlUtils.writeLongArray(xmlNode, getLongArrayAttribute());
						case FLOAT_ARRAY -> XmlUtils.writeFloatArray(xmlNode, getFloatArrayAttribute());
						case DOUBLE_ARRAY -> XmlUtils.writeDoubleArray(xmlNode, getDoubleArrayAttribute());
						case STRING_ARRAY -> XmlUtils.writeStringArray(xmlNode, getStringArrayAttribute());
						case FILE -> XmlUtils.writeFile(xmlNode, getFileData(), fileDataWriter);
						case TIMESTAMP_32 -> XmlUtils.writeInstant32(xmlNode, getTimestampAttribute());
						case TIMESTAMP_64 -> XmlUtils.writeInstant64(xmlNode, getTimestampAttribute());
						case DATE_TIME -> XmlUtils.writeLocalDateTime(xmlNode, getDateTimeAttribute());
						case DATE -> XmlUtils.writeLocalDate(xmlNode, getDateAttribute());
						case TIME -> XmlUtils.writeLocalTime(xmlNode, getTimeAttribute());
						case GENERIC_MESSAGE -> XmlUtils.writeGenericMessage(xmlNode, getGenericMessageAttribute(), fileDataWriter);
					}
				}
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	@Override
	public Message getReferencedObject() {
		if (value == null) return null;
		return (Message) value;
	}

	@Override
	public List<Message> getReferencedObjects() {
		if (value == null) return null;
		return (List<Message>) value;
	}

	@Override
	public <TYPE extends Message> TYPE getReferencedObjectAsType() {
		if (value == null) return null;
		return (TYPE) value;
	}

	@Override
	public <TYPE extends Message> List<TYPE> getReferencedObjectsAsType() {
		if (value == null) return null;
		return (List<TYPE>) value;
	}

	@Override
	public boolean getBooleanAttribute() {
		if (value == null) return false;
		return (boolean) value;
	}

	@Override
	public byte getByteAttribute() {
		if (value == null) return 0;
		return (byte) value;
	}

	@Override
	public int getIntAttribute() {
		if (value == null) return 0;
		return (int) value;
	}

	@Override
	public long getLongAttribute() {
		if (value == null) return 0;
		return (long) value;
	}

	@Override
	public float getFloatAttribute() {
		if (value == null) return 0;
		return (Float) value;
	}

	@Override
	public double getDoubleAttribute() {
		if (value == null) return 0;
		return (Double) value;
	}

	@Override
	public String getStringAttribute() {
		if (value == null) return null;
		return (String) value;
	}

	@Override
	public FileData getFileData() {
		if (value == null) return null;
		return (FileData) value;
	}

	@Override
	public String getFileDataFileName() {
		if (value == null) return null;
		return getFileData().getFileName();
	}

	@Override
	public long getFileDataFileLength() {
		if (value == null) return 0;
		return getFileData().getLength();
	}

	@Override
	public BitSet getBitSetAttribute() {
		if (value == null) return null;
		return (BitSet) value;
	}

	@Override
	public byte[] getByteArrayAttribute() {
		if (value == null) return null;
		return (byte[]) value;
	}

	@Override
	public int[] getIntArrayAttribute() {
		if (value == null) return null;
		return (int[]) value;
	}

	@Override
	public long[] getLongArrayAttribute() {
		if (value == null) return null;
		return (long[]) value;
	}

	@Override
	public float[] getFloatArrayAttribute() {
		if (value == null) return null;
		return (float[]) value;
	}

	@Override
	public double[] getDoubleArrayAttribute() {
		if (value == null) return null;
		return (double[]) value;
	}

	@Override
	public String[] getStringArrayAttribute() {
		if (value == null) return null;
		return (String[]) value;
	}

	@Override
	public Instant getTimestampAttribute() {
		if (value == null) return null;
		return (Instant) value;
	}

	@Override
	public LocalDateTime getDateTimeAttribute() {
		if (value == null) return null;
		return (LocalDateTime) value;
	}

	@Override
	public LocalDate getDateAttribute() {
		if (value == null) return null;
		return (LocalDate) value;
	}

	@Override
	public LocalTime getTimeAttribute() {
		if (value == null) return null;
		return (LocalTime) value;
	}

	@Override
	public Message getGenericMessageAttribute() {
		if (value == null) return null;
		return (Message) value;
	}

	@Override
	public String getAsString() {
		return "" + value;
	}

	@Override
	public String explain(int level) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t".repeat(level)).append(attributeDefinition.getName()).append(", ");
		sb.append(attributeDefinition.getType());
		if (attributeDefinition.isReferenceProperty()) {
			MessageModel referenceDefinition = attributeDefinition.getReferencedObject();
			if (attributeDefinition.isMultiReference()) {
				sb.append("\n");
				for (Message referencedObject : getReferencedObjects()) {
					sb.append(referencedObject.explain(level + 1)).append("\n");
				}
			} else {
				sb.append("\n");
				Message referencedObject = getReferencedObject();
				sb.append(referencedObject.explain(level + 1));
			}
		} else if (attributeDefinition.isEnumProperty() && attributeDefinition.getEnumDefinition() != null) {
			int index = getIntAttribute();
			sb.append(": ").append(index > 0 ? attributeDefinition.getEnumDefinition().getEnumValues().get(index - 1) : "null");
		} else {
			sb.append(": ").append(value);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return explain(0);
	}

}
