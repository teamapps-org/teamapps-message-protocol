/*-
 * ========================LICENSE_START=================================
 * TeamApps Protocol Schema
 * ---
 * Copyright (C) 2022 TeamApps.org
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
package org.teamapps.protocol.message;


import org.teamapps.protocol.file.FileData;
import org.teamapps.protocol.file.FileDataReader;
import org.teamapps.protocol.file.FileDataWriter;
import org.teamapps.protocol.model.*;
import org.teamapps.protocol.utils.MessageUtils;

import java.io.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class Message {

	private final MessageModel messageModel;
	private final List<MessageAttribute> attributes = new ArrayList<>();
	private final Map<String, MessageAttribute> attributesByName = new HashMap<>();

	public static String readMessageUuid(byte[] bytes) throws IOException {
		return MessageUtils.readString(bytes, 0);
	}

	public Message(MessageModel messageModel) {
		this.messageModel = messageModel;
	}

	public Message(Message message, ModelCollection modelCollection) {
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

	public MessageModel getModel() {
		return messageModel;
	}

	public String getMessageDefUuid() {
		return messageModel.getObjectUuid();
	}

	public String getMessageDefName() {
		return messageModel.getName();
	}

	public List<MessageAttribute> getAttributes() {
		return attributes;
	}

	public int getAttributeKey(String attributeName) {
		return messageModel.getAttributeDefinitionByName(attributeName).getKey();
	}

	public void write(DataOutputStream dos, FileDataWriter fileDataWriter) throws IOException {
		MessageUtils.writeString(dos, messageModel.getObjectUuid());
		dos.writeShort(messageModel.getModelVersion());
		dos.writeShort(attributes.size());
		for (MessageAttribute field : attributes) {
			field.write(dos, fileDataWriter);
		}
	}

	public byte[] toBytes() throws IOException {
		return toBytes(null);
	}

	public byte[] toBytes(FileDataWriter fileDataWriter) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		write(dos, fileDataWriter);
		dos.close();
		return bos.toByteArray();
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