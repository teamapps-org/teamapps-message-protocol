/*-
 * ========================LICENSE_START=================================
 * TeamApps Message Protocol
 * ---
 * Copyright (C) 2022 - 2023 TeamApps.org
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
import org.teamapps.message.protocol.file.FileDataType;
import org.teamapps.message.protocol.file.FileDataWriter;
import org.teamapps.message.protocol.model.*;
import org.teamapps.message.protocol.utils.MessageUtils;
import org.teamapps.message.protocol.xml.XmlBuilder;
import org.teamapps.message.protocol.xml.XmlNode;
import org.teamapps.message.protocol.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.time.*;
import java.util.*;

public class Message implements MessageRecord {

	private final MessageModel messageModel;
	private final List<MessageAttribute> attributes = new ArrayList<>();
	private final Map<String, MessageAttribute> attributesByName = new HashMap<>();

	public Message(MessageModel messageModel) {
		this.messageModel = messageModel;
	}

	public Message(MessageRecord message, ModelCollection modelCollection) {
		this.messageModel = modelCollection.getModel(message.getMessageDefUuid());
		for (MessageAttribute attribute : message.getAttributes()) {
			AttributeDefinition remappedDefinition = messageModel.getAttributeDefinitionByKey(attribute.getAttributeDefinition().getKey());
			MessageAttribute messageAttribute = remappedDefinition == null ? attribute : new MessageAttributeImpl((MessageAttributeImpl) attribute, remappedDefinition, modelCollection);
			attributes.add(messageAttribute);
			attributesByName.put(messageAttribute.getAttributeDefinition().getName(), messageAttribute);
		}
	}

	public Message(byte[] bytes, MessageModel model, FileDataReader fileProvider, PojoObjectDecoderRegistry decoderRegistry) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)), model, fileProvider, decoderRegistry);
	}

	public Message(byte[] bytes, ModelRegistry modelRegistry, FileDataReader fileProvider, PojoObjectDecoderRegistry decoderRegistry) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)), modelRegistry, fileProvider, decoderRegistry);
	}

	public Message(DataInputStream dis, ModelRegistry modelRegistry, FileDataReader fileDataReader, PojoObjectDecoderRegistry decoderRegistry) throws IOException {
		String objectUuid = MessageUtils.readString(dis);
		short modelVersion = dis.readShort();
		this.messageModel = modelRegistry.getModel(objectUuid, modelVersion);
		int attributesCount = dis.readShort();
		for (int i = 0; i < attributesCount; i++) {
			MessageAttributeImpl messageAttribute = new MessageAttributeImpl(dis, messageModel, fileDataReader, decoderRegistry);
			attributes.add(messageAttribute);
			attributesByName.put(messageAttribute.getAttributeDefinition().getName(), messageAttribute);
		}
	}

	public Message(DataInputStream dis, MessageModel model, FileDataReader fileDataReader, PojoObjectDecoderRegistry decoderRegistry) throws IOException {
		this.messageModel = model;
		String objectUuid = MessageUtils.readString(dis);
		if (!model.getObjectUuid().equals(objectUuid)) {
			throw new RuntimeException("Cannot parse message with wrong model:" + objectUuid + ", expected:" + messageModel.getObjectUuid());
		}
		short modelVersion = dis.readShort();
		if (model.getModelVersion() != modelVersion) {
			System.out.println("Wrong model version " + model + ", expected: " + model.getModelVersion());
		}
		int attributesCount = dis.readShort();
		for (int i = 0; i < attributesCount; i++) {
			MessageAttributeImpl messageAttribute = new MessageAttributeImpl(dis, messageModel, fileDataReader, decoderRegistry);
			attributes.add(messageAttribute);
			attributesByName.put(messageAttribute.getAttributeDefinition().getName(), messageAttribute);
		}
	}

	public Message(byte[] bytes) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)), null);
	}

	public Message(byte[] bytes, FileDataReader fileDataReader) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)), fileDataReader);
	}

	public Message(DataInputStream dis, FileDataReader fileDataReader) throws IOException {
		String objectUuid = MessageUtils.readString(dis);
		short modelVersion = dis.readShort();
		messageModel = new MessageDefinition(objectUuid, null, false, modelVersion);
		int attributesCount = dis.readShort();
		for (int i = 0; i < attributesCount; i++) {
			MessageAttributeImpl messageAttribute = new MessageAttributeImpl(dis, messageModel, fileDataReader, null);
			attributes.add(messageAttribute);
			attributesByName.put(messageAttribute.getAttributeDefinition().getName(), messageAttribute);
		}
	}

	public Message(String xml, MessageModel model, FileDataReader fileDataReader, PojoObjectDecoderRegistry decoderRegistry) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		Element xmlNode = document.getDocumentElement();
		this.messageModel = model;
		for (AttributeDefinition definition : this.messageModel.getAttributeDefinitions()) {
			Element childElement = XmlUtils.readChildElement(xmlNode, definition.getName());
			if (childElement != null) {
				MessageAttributeImpl messageAttribute = new MessageAttributeImpl(childElement, definition, fileDataReader, decoderRegistry);
				attributes.add(messageAttribute);
				attributesByName.put(messageAttribute.getAttributeDefinition().getName(), messageAttribute);
			}
		}
	}

	public Message(Element xmlNode, MessageModel model, FileDataReader fileDataReader, PojoObjectDecoderRegistry decoderRegistry) {
		this.messageModel = model;
		for (AttributeDefinition definition : this.messageModel.getAttributeDefinitions()) {
			Element childElement = XmlUtils.readChildElement(xmlNode, definition.getName());
			if (childElement != null) {
				MessageAttributeImpl messageAttribute = new MessageAttributeImpl(childElement, definition, fileDataReader, decoderRegistry);
				attributes.add(messageAttribute);
				attributesByName.put(messageAttribute.getAttributeDefinition().getName(), messageAttribute);
			}
		}
	}

	public static String readMessageUuid(byte[] bytes) throws IOException {
		return MessageUtils.readString(bytes, 0);
	}

	public static Message readXml(String xml, MessageModel model, FileDataReader fileDataReader, PojoObjectDecoderRegistry decoderRegistry) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		return new Message(document.getDocumentElement(), model, fileDataReader, decoderRegistry);
	}

	protected Message setDefaultValues() {
		return setDefaultValues(null);
	}

	protected Message setDefaultValues(ModelCollection modelCollection) {
		for (AttributeDefinition attributeDefinition : messageModel.getAttributeDefinitions()) {
			String name = attributeDefinition.getName();
			if (attributeDefinition.isReferenceProperty()) {
				String refUuid = attributeDefinition.getReferencedObject().getObjectUuid();
				if (modelCollection != null && modelCollection.containsDecoder(refUuid)) {
					Message defaultMessage = modelCollection.getMessageDecoder(attributeDefinition.getReferencedObject().getObjectUuid()).defaultMessage();
					if (attributeDefinition.isMultiReference()) {
						setReferencedObjects(name, new ArrayList<>(Collections.singletonList(defaultMessage)));
					} else {
						setReferencedObject(name, defaultMessage);
					}
				}
			} else {
				String s = attributeDefinition.getDefaultValue();
				if (s != null) {
					switch (attributeDefinition.getType()) {
						case BOOLEAN -> setBooleanAttribute(name, s.equals("1") || s.equals("true"));
						case BYTE -> setByteAttribute(name, (byte) Integer.parseInt(s));
						case INT, ENUM -> setIntAttribute(name, Integer.parseInt(s));
						case LONG -> setLongAttribute(name, Long.parseLong(s));
						case FLOAT -> setFloatAttribute(name, Float.parseFloat(s));
						case DOUBLE -> setDoubleAttribute(name, Double.parseDouble(s));
						case STRING -> setStringAttribute(name, s);
						case BITSET -> {
						}
						case BYTE_ARRAY -> setByteArrayAttribute(name, Base64.getDecoder().decode(s));
						case INT_ARRAY -> {
						}
						case LONG_ARRAY -> {
						}
						case FLOAT_ARRAY -> {
						}
						case DOUBLE_ARRAY -> {
						}
						case STRING_ARRAY -> {
						}
						case FILE -> {
						}
						case TIMESTAMP_32 -> setTimestampAttribute(name, Instant.ofEpochSecond(Integer.parseInt(s)));
						case TIMESTAMP_64 -> setTimestampAttribute(name, Instant.ofEpochMilli(Long.parseLong(s)));
						case DATE_TIME -> setDateTimeAttribute(name, LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(s)), ZoneOffset.UTC));
						case DATE -> setDateAttribute(name, LocalDate.ofEpochDay(Long.parseLong(s)));
						case TIME -> setTimeAttribute(name, LocalTime.ofSecondOfDay(Integer.parseInt(s)));
						case GENERIC_MESSAGE -> {

						}
					}
				}
			}
		}
		return this;
	}

	@Override
	public MessageModel getModel() {
		return messageModel;
	}

	@Override
	public String getMessageDefUuid() {
		return messageModel.getObjectUuid();
	}

	@Override
	public String getMessageDefName() {
		return messageModel.getName();
	}

	@Override
	public List<MessageAttribute> getAttributes() {
		return attributes;
	}

	public int getAttributeKey(String attributeName) {
		return messageModel.getAttributeDefinitionByName(attributeName).getKey();
	}

	public void write(DataOutputStream dos) throws IOException {
		write(dos, null);
	}

	public void write(DataOutputStream dos, FileDataWriter fileDataWriter) throws IOException {
		write(dos, fileDataWriter, false);
	}

	public void write(DataOutputStream dos, FileDataWriter fileDataWriter, boolean updateFileData) throws IOException {
		MessageUtils.writeString(dos, messageModel.getObjectUuid());
		dos.writeShort(messageModel.getModelVersion());
		dos.writeShort(attributes.size());
		for (MessageAttribute field : attributes) {
			field.write(dos, fileDataWriter, updateFileData);
		}
	}

	@Override
	public byte[] toBytes() throws IOException {
		return toBytes(null);
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

	@Override
	public String toXml() throws IOException {
		return toXml(false, null);
	}

	public String toXml(boolean withComments, FileDataWriter fileDataWriter) throws IOException {
		XmlNode xmlNode = toXml(null, withComments, fileDataWriter);
		XmlBuilder xmlBuilder = new XmlBuilder(xmlNode);
		return xmlBuilder.getXml();
	}

	protected XmlNode toXml(XmlNode parentNode, boolean withComments, FileDataWriter fileDataWriter) throws IOException {
		XmlNode xmlNode = withComments ? new XmlNode(getMessageDefName(), null, messageModel.getComment()) : new XmlNode(getMessageDefName());
		if (!withComments) {
			for (MessageAttribute attribute : getAttributes()) {
				MessageAttributeImpl messageAttribute = (MessageAttributeImpl) attribute;
				messageAttribute.toXml(xmlNode, false, fileDataWriter);
			}
		} else {
			for (AttributeDefinition attributeDefinition : messageModel.getAttributeDefinitions()) {
				MessageAttribute attribute = getAttribute(attributeDefinition.getName());
				if (attribute != null) {
					MessageAttributeImpl messageAttribute = (MessageAttributeImpl) attribute;
					messageAttribute.toXml(xmlNode, true, fileDataWriter);
				} else if (attributeDefinition.getComment() != null || attributeDefinition.getDefaultValue() != null) {
					xmlNode.addChild(new XmlNode(attributeDefinition.getName(), attributeDefinition.getDefaultValue(), attributeDefinition.getComment()));
				}
			}
		}

		if (parentNode != null) {
			parentNode.addChild(xmlNode);
			return parentNode;
		} else {
			return xmlNode;
		}
	}

	public void deleteAllEmbeddedFiles() {
		for (MessageAttribute attribute : attributes) {
			AttributeType type = attribute.getAttributeDefinition().getType();
			String propertyName = attribute.getAttributeDefinition().getName();
			if (type == AttributeType.FILE) {
				FileData fileData = getFileData(propertyName);
				if (fileData != null && fileData.getType() == FileDataType.LOCAL_FILE) {
					File file = new File(fileData.getDescriptor());
					if (file.exists()) {
						file.delete();
					}
				}
			} else if (type == AttributeType.OBJECT_SINGLE_REFERENCE) {
				Message referencedObject = getReferencedObject(propertyName);
				if (referencedObject != null) {
					referencedObject.deleteAllEmbeddedFiles();
				}
			} else if (type == AttributeType.OBJECT_MULTI_REFERENCE) {
				List<Message> referencedObjects = getReferencedObjects(propertyName);
				if (referencedObjects != null) {
					referencedObjects.forEach(Message::deleteAllEmbeddedFiles);
				}
			}
		}
	}


	public Message setReferencedObject(String name, Message value) {
		setAttribute(name, value);
		return this;
	}


	public Message setReferencedObjects(String name, List<Message> value) {
		setAttribute(name, value);
		return this;
	}

	public <TYPE extends Message> Message setReferencedObjectAsType(String name, TYPE value) {
		setAttribute(name, value);
		return this;
	}

	public <TYPE extends Message> Message setReferencedObjectsAsType(String name, List<TYPE> value) {
		setAttribute(name, value);
		return this;
	}

	public Message setBooleanAttribute(String name, boolean value) {
		setAttribute(name, value);
		return this;
	}


	public Message setByteAttribute(String name, byte value) {
		setAttribute(name, value);
		return this;
	}


	public Message setIntAttribute(String name, int value) {
		setAttribute(name, value);
		return this;
	}


	public Message setLongAttribute(String name, long value) {
		setAttribute(name, value);
		return this;
	}


	public Message setFloatAttribute(String name, float value) {
		setAttribute(name, value);
		return this;
	}


	public Message setDoubleAttribute(String name, double value) {
		setAttribute(name, value);
		return this;
	}


	public Message setStringAttribute(String name, String value) {
		setAttribute(name, value);
		return this;
	}


	public Message setFileData(String name, FileData value) {
		setAttribute(name, value);
		return this;
	}

	public Message setFileData(String name, File file) {
		setAttribute(name, file != null ? FileData.create(file) : null);
		return this;
	}

	public Message setFileData(String name, File file, String fileName) {
		setAttribute(name, file != null ? FileData.create(file, fileName) : null);
		return this;
	}

	public Message setBitSetAttribute(String name, BitSet value) {
		setAttribute(name, value);
		return this;
	}


	public Message setByteArrayAttribute(String name, byte[] value) {
		setAttribute(name, value);
		return this;
	}


	public Message setIntArrayAttribute(String name, int[] value) {
		setAttribute(name, value);
		return this;
	}


	public Message setLongArrayAttribute(String name, long[] value) {
		setAttribute(name, value);
		return this;
	}


	public Message setFloatArrayAttribute(String name, float[] value) {
		setAttribute(name, value);
		return this;
	}


	public Message setDoubleArrayAttribute(String name, double[] value) {
		setAttribute(name, value);
		return this;
	}


	public Message setStringArrayAttribute(String name, String[] value) {
		setAttribute(name, value);
		return this;
	}

	public Message setStringArrayAsList(String name, List<String> value) {
		String[] v = value != null && !value.isEmpty() ? value.toArray(new String[0]) : null;
		setAttribute(name, v);
		return this;
	}

	public Message setTimestampAttribute(String name, Instant value) {
		setAttribute(name, value);
		return this;
	}

	public Message setDateTimeAttribute(String name, LocalDateTime value) {
		setAttribute(name, value);
		return this;
	}

	public Message setDateAttribute(String name, LocalDate value) {
		setAttribute(name, value);
		return this;
	}

	public Message setTimeAttribute(String name, LocalTime value) {
		setAttribute(name, value);
		return this;
	}

	public Message setGenericMessageAttribute(String name, Message value) {
		setAttribute(name, value);
		return this;
	}

	public Message getReferencedObject(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getReferencedObject();
		} else {
			return null;
		}
	}


	public List<Message> getReferencedObjects(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getReferencedObjects();
		} else {
			return null;
		}
	}


	public <TYPE extends Message> TYPE getReferencedObjectAsType(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getReferencedObjectAsType();
		} else {
			return null;
		}
	}


	public <TYPE extends Message> List<TYPE> getReferencedObjectsAsType(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getReferencedObjectsAsType();
		} else {
			return null;
		}
	}

	public boolean isEmpty(String propertyName) {
		return getAttribute(propertyName) == null;
	}


	public boolean getBooleanAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getBooleanAttribute();
		} else {
			return false;
		}
	}


	public byte getByteAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getByteAttribute();
		} else {
			return 0;
		}
	}


	public int getIntAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getIntAttribute();
		} else {
			return 0;
		}
	}


	public long getLongAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getLongAttribute();
		} else {
			return 0;
		}
	}


	public float getFloatAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getFloatAttribute();
		} else {
			return 0;
		}
	}


	public double getDoubleAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getDoubleAttribute();
		} else {
			return 0;
		}
	}


	public String getStringAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getStringAttribute();
		} else {
			return null;
		}
	}


	public FileData getFileData(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getFileData();
		} else {
			return null;
		}
	}

	public File getFile(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getFileData().getAsFile();
		} else {
			return null;
		}
	}

	public String getFileDataFileName(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getFileDataFileName();
		} else {
			return null;
		}
	}

	public long getFileDataFileLength(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getFileDataFileLength();
		} else {
			return 0;
		}
	}


	public BitSet getBitSetAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getBitSetAttribute();
		} else {
			return null;
		}
	}


	public byte[] getByteArrayAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getByteArrayAttribute();
		} else {
			return null;
		}
	}


	public int[] getIntArrayAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getIntArrayAttribute();
		} else {
			return null;
		}
	}


	public long[] getLongArrayAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getLongArrayAttribute();
		} else {
			return null;
		}
	}


	public float[] getFloatArrayAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getFloatArrayAttribute();
		} else {
			return null;
		}
	}


	public double[] getDoubleArrayAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getDoubleArrayAttribute();
		} else {
			return null;
		}
	}


	public String[] getStringArrayAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getStringArrayAttribute();
		} else {
			return null;
		}
	}

	public List<String> getStringArrayAsList(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return Arrays.asList(property.getStringArrayAttribute());
		} else {
			return Collections.emptyList();
		}
	}

	public Instant getTimestampAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getTimestampAttribute();
		} else {
			return null;
		}
	}

	public LocalDateTime getDateTimeAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getDateTimeAttribute();
		} else {
			return null;
		}
	}

	public LocalDate getDateAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getDateAttribute();
		} else {
			return null;
		}
	}

	public LocalTime getTimeAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getTimeAttribute();
		} else {
			return null;
		}
	}

	public Message getGenericMessageAttribute(String propertyName) {
		MessageAttribute property = getAttribute(propertyName);
		if (property != null) {
			return property.getGenericMessageAttribute();
		} else {
			return null;
		}
	}

	@Override
	public int getRecordId() {
		MessageAttribute property = getAttribute(MessageDefinition.META_RECORD_ID);
		if (property != null) {
			return property.getIntAttribute();
		} else {
			return 0;
		}
	}

	public Message setRecordId(int id) {
		setAttribute(MessageDefinition.META_RECORD_ID, id);
		return this;
	}

	public Instant getRecordCreationDate() {
		MessageAttribute property = getAttribute(MessageDefinition.META_CREATION_DATE);
		if (property != null) {
			return property.getTimestampAttribute();
		} else {
			return null;
		}
	}

	public Message setRecordCreationDate(Instant value) {
		setAttribute(MessageDefinition.META_CREATION_DATE, value);
		return this;
	}

	public Instant getRecordModificationDate() {
		MessageAttribute property = getAttribute(MessageDefinition.META_MODIFICATION_DATE);
		if (property != null) {
			return property.getTimestampAttribute();
		} else {
			return null;
		}
	}

	public Message setRecordModificationDate(Instant value) {
		setAttribute(MessageDefinition.META_MODIFICATION_DATE, value);
		return this;
	}

	public int getRecordCreatedBy() {
		MessageAttribute property = getAttribute(MessageDefinition.META_CREATED_BY);
		if (property != null) {
			return property.getIntAttribute();
		} else {
			return 0;
		}
	}

	public Message setRecordCreatedBy(int userId) {
		setAttribute(MessageDefinition.META_CREATED_BY, userId);
		return this;
	}

	public int getRecordModifiedBy() {
		MessageAttribute property = getAttribute(MessageDefinition.META_MODIFIED_BY);
		if (property != null) {
			return property.getIntAttribute();
		} else {
			return 0;
		}
	}

	public Message setRecordModifiedBy(int userId) {
		setAttribute(MessageDefinition.META_MODIFIED_BY, userId);
		return this;
	}


	public void addReference(String name, Message message) {
		AttributeDefinition attributeDefinition = messageModel.getAttributeDefinitionByName(name);
		if (attributeDefinition == null) {
			throw new RuntimeException("Message model does not contain a field with name:" + name);
		}
		if (attributeDefinition.getType() == AttributeType.OBJECT_SINGLE_REFERENCE) {
			setAttribute(name, message);
		} else if (attributeDefinition.getType() == AttributeType.OBJECT_MULTI_REFERENCE) {
			MessageAttribute messageAttribute = getAttribute(name);
			if (messageAttribute == null) {
				List<Message> messages = new ArrayList<>();
				messages.add(message);
				setAttribute(name, messages);
			} else {
				List<Message> referencedObjects = messageAttribute.getReferencedObjects();
				referencedObjects.add(message);
			}
		}
	}

	public void setAttribute(String name, Object value) {
		if (value == null && isEmpty(name)) {
			return;
		}
		AttributeDefinition attributeDefinition = messageModel.getAttributeDefinitionByName(name);
		if (attributeDefinition == null) {
			throw new RuntimeException("Message model does not contain a field with name:" + name);
		}
		MessageAttribute existingField = attributesByName.get(name);
		if (existingField != null) {
			attributes.remove(existingField);
			if (value != null) {
				MessageAttribute messageAttribute = new MessageAttributeImpl(attributeDefinition, value);
				attributes.add(messageAttribute);
				attributesByName.put(name, messageAttribute);
			} else {
				attributesByName.remove(name);
			}
		} else if (value != null) {
			MessageAttribute messageAttribute = new MessageAttributeImpl(attributeDefinition, value);
			attributes.add(messageAttribute);
			attributesByName.put(name, messageAttribute);
		}
	}

	public void removeField(AttributeDefinition attributeDefinition) {
		MessageAttribute existingField = attributesByName.get(attributeDefinition);
		if (existingField != null) {
			attributes.remove(existingField);
			attributesByName.remove(attributeDefinition);
		}
	}

	@Override
	public MessageAttribute getAttribute(String name) {
		return attributesByName.get(name);
	}

	protected String explain(int level) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t".repeat(level)).append(messageModel.getName()).append(", ");
		sb.append("[").append(messageModel.getObjectUuid()).append("], ");
		for (MessageAttribute property : attributes) {
			sb.append("\n");
			sb.append(property.explain(level + 1));
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return explain(0);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Message message = (Message) o;
		return toString().equals(message.toString());
	}

	@Override
	public int hashCode() {
		return Objects.hash(messageModel, attributes, attributesByName);
	}
}
