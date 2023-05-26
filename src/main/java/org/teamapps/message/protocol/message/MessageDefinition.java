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

import org.teamapps.message.protocol.model.ExtendedAttributesUpdater;
import org.teamapps.message.protocol.model.MessageModel;
import org.teamapps.message.protocol.utils.MessageUtils;
import org.teamapps.message.protocol.model.AttributeDefinition;
import org.teamapps.message.protocol.model.EnumDefinition;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageDefinition implements MessageModel {

	public static final String META_RECORD_ID = "recordId";
	public final static String META_CREATION_DATE = "recordCreationDate";
	public final static String META_CREATED_BY = "recordCreatedBy";
	public final static String META_MODIFICATION_DATE = "recordModificationDate";
	public final static String META_MODIFIED_BY = "recordModifiedBy";

	public final static Set<String> META_FIELD_NAMES = Stream.of(META_RECORD_ID, META_CREATION_DATE, META_CREATED_BY, META_MODIFICATION_DATE, META_MODIFIED_BY).collect(Collectors.toSet());
	public final static Set<String> RESERVED_NAMES_LOWER_CASE = Stream.of(META_RECORD_ID, META_CREATION_DATE, META_CREATED_BY, META_MODIFICATION_DATE, META_MODIFIED_BY).map(String::toLowerCase).collect(Collectors.toSet());

	private final String name;
	private final String comment;
	private final Message specificType;
	private final String objectUuid;
	private final short modelVersion;
	private final boolean messageRecord;
	private final List<AttributeDefinition> definitions = new ArrayList<>();
	private final Map<Integer, AttributeDefinition> definitionByKey = new HashMap<>();
	private final Map<String, AttributeDefinition> definitionByName = new HashMap<>();

	public static Message readBase64Message(String msg) {
		try {
			return msg == null ? null : new Message(Base64.getDecoder().decode(msg));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MessageDefinition(String objectUuid, String name, boolean messageRecord, int modelVersion) {
		this(objectUuid, name, null, messageRecord, modelVersion);
	}

	public MessageDefinition(String objectUuid, String name, Message specificType, boolean messageRecord, int modelVersion) {
		this(objectUuid, name, specificType, messageRecord, modelVersion, null);
	}

	public MessageDefinition(String objectUuid, String name, Message specificType, boolean messageRecord, int modelVersion, String comment) {
		this.objectUuid = objectUuid;
		this.name = name;
		this.specificType = specificType;
		this.modelVersion = (short) modelVersion;
		this.messageRecord = messageRecord;
		this.comment = comment;
		if (messageRecord) {
			addInteger(META_RECORD_ID, 16_000);
			addTimestamp(META_CREATION_DATE, 16_001);
			addInteger(META_CREATED_BY, 16_002);
			addTimestamp(META_MODIFICATION_DATE, 16_003);
			addInteger(META_MODIFIED_BY, 16_004);
		}
	}

	public MessageDefinition(byte[] bytes) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)));
	}

	public MessageDefinition(DataInputStream dis) throws IOException {
		this(dis, new DefinitionCache());
	}

	public MessageDefinition(DataInputStream dis, DefinitionCache definitionCache) throws IOException {
		this(MessageUtils.readString(dis), MessageUtils.readString(dis), MessageUtils.readMessageOrNull(dis), dis.readBoolean(), dis.readShort(), MessageUtils.readString(dis));
		definitionCache.addModel(this);
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			AttributeDefinition attributeDefinition = new AbstractAttributeDefinition(this, dis, definitionCache);
			if (attributeDefinition.getKey() < 16_000) {
				addAttribute(attributeDefinition);
			}
		}
	}

	public void write(DataOutputStream dos) throws IOException {
		write(dos, new DefinitionCache());
	}

	public void write(DataOutputStream dos, DefinitionCache definitionCache) throws IOException {
		MessageUtils.writeString(dos, objectUuid);
		MessageUtils.writeString(dos, getName());
		MessageUtils.writeNullableMessage(dos, getSpecificType());
		dos.writeBoolean(messageRecord);
		dos.writeShort(modelVersion);
		MessageUtils.writeString(dos, comment);
		dos.writeInt(definitions.size());
		for (AttributeDefinition attributeDefinition : definitions) {
			attributeDefinition.write(dos, definitionCache);
		}
	}

	@Override
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		write(dos);
		dos.close();
		return bos.toByteArray();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Message getSpecificType() {
		return specificType;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public String getObjectUuid() {
		return objectUuid;
	}

	public ExtendedAttributesUpdater addBoolean(String name, int key) {
		return addAttribute(name, key, AttributeType.BOOLEAN);
	}

	public ExtendedAttributesUpdater addByte(String name, int key) {
		return addAttribute(name, key, AttributeType.BYTE);
	}

	public ExtendedAttributesUpdater addString(String name, int key) {
		return addAttribute(name, key, AttributeType.STRING);
	}

	public ExtendedAttributesUpdater addInteger(String name, int key) {
		return addAttribute(name, key, AttributeType.INT);
	}

	public ExtendedAttributesUpdater addLong(String name, int key) {
		return addAttribute(name, key, AttributeType.LONG);
	}

	public ExtendedAttributesUpdater addFloat(String name, int key) {
		return addAttribute(name, key, AttributeType.FLOAT);
	}

	public ExtendedAttributesUpdater addDouble(String name, int key) {
		return addAttribute(name, key, AttributeType.DOUBLE);
	}

	public ExtendedAttributesUpdater addFile(String name, int key) {
		return addAttribute(name, key, AttributeType.FILE);
	}

	public ExtendedAttributesUpdater addByteArray(String name, int key) {
		return addAttribute(name, key, AttributeType.BYTE_ARRAY);
	}

	public ExtendedAttributesUpdater addIntArray(String name, int key) {
		return addAttribute(name, key, AttributeType.INT_ARRAY);
	}

	public ExtendedAttributesUpdater addLongArray(String name, int key) {
		return addAttribute(name, key, AttributeType.LONG_ARRAY);
	}

	public ExtendedAttributesUpdater addFloatArray(String name, int key) {
		return addAttribute(name, key, AttributeType.FLOAT_ARRAY);
	}

	public ExtendedAttributesUpdater addDoubleArray(String name, int key) {
		return addAttribute(name, key, AttributeType.DOUBLE_ARRAY);
	}

	public ExtendedAttributesUpdater addStringArray(String name, int key) {
		return addAttribute(name, key, AttributeType.STRING_ARRAY);
	}

	public ExtendedAttributesUpdater addTimestamp(String name, int key) {
		return addAttribute(name, key, AttributeType.TIMESTAMP_32);
	}

	public ExtendedAttributesUpdater addLongTimestamp(String name, int key) {
		return addAttribute(name, key, AttributeType.TIMESTAMP_64);
	}

	public ExtendedAttributesUpdater addDateTime(String name, int key) {
		return addAttribute(name, key, AttributeType.DATE_TIME);
	}

	public ExtendedAttributesUpdater addDate(String name, int key) {
		return addAttribute(name, key, AttributeType.DATE);
	}

	public ExtendedAttributesUpdater addTime(String name, int key) {
		return addAttribute(name, key, AttributeType.TIME);
	}

	public ExtendedAttributesUpdater addEnum(String name, EnumDefinition enumDefinition, int key) {
		return addEnum(name, enumDefinition, key, null);
	}

	public ExtendedAttributesUpdater addEnum(String name, EnumDefinition enumDefinition, int key, Message specificType) {
		AbstractAttributeDefinition attributeDefinition = new AbstractAttributeDefinition(this, name, key, enumDefinition, specificType);
		addAttribute(attributeDefinition);
		return attributeDefinition;
	}

	public ExtendedAttributesUpdater addAttribute(String name, int key, AttributeType type) {
		return addAttribute(name, key, type, null);
	}

	public ExtendedAttributesUpdater addAttribute(String name, int key, AttributeType type, Message specificType) {
		AbstractAttributeDefinition attributeDefinition = new AbstractAttributeDefinition(this, name, key, type, specificType);
		addAttribute(attributeDefinition);
		return attributeDefinition;
	}

	public ExtendedAttributesUpdater addAttribute(String name, int key, AttributeType type, Message specificType, String defaultValue, String comment) {
		AbstractAttributeDefinition attributeDefinition = new AbstractAttributeDefinition(this, name, key, type, specificType, defaultValue, comment);
		addAttribute(attributeDefinition);
		return attributeDefinition;
	}

	public ExtendedAttributesUpdater addSingleReference(String name, MessageDefinition referencedObject, int key) {
		return addSingleReference(name, key, null, referencedObject);
	}

	public ExtendedAttributesUpdater addSingleReference(String name, int key, MessageDefinition referencedObject) {
		return addSingleReference(name, key, null, referencedObject);
	}

	public ExtendedAttributesUpdater addSingleReference(String name, int key, Message specificType, MessageDefinition referencedObject) {
		AbstractAttributeDefinition referenceAttributeDefinition = new AbstractAttributeDefinition(this, name, key, specificType, referencedObject, false);
		addAttribute(referenceAttributeDefinition);
		return referenceAttributeDefinition;
	}

	public ExtendedAttributesUpdater addMultiReference(String name, MessageDefinition referencedObject,  int key) {
		return addMultiReference(name, key, null, referencedObject);
	}

	public ExtendedAttributesUpdater addMultiReference(String name, int key, MessageDefinition referencedObject) {
		return addMultiReference(name, key, null, referencedObject);
	}

	public ExtendedAttributesUpdater addMultiReference(String name, int key, Message specificType, MessageDefinition referencedObject) {
		AbstractAttributeDefinition referenceAttributeDefinition = new AbstractAttributeDefinition(this, name, key, specificType, referencedObject, true);
		addAttribute(referenceAttributeDefinition);
		return referenceAttributeDefinition;
	}

	public ExtendedAttributesUpdater addGenericMessage(String name, int key) {
		return addAttribute(name, key, AttributeType.GENERIC_MESSAGE);
	}

	public void addAttribute(AttributeDefinition field) {
		if (definitionByName.containsKey(field.getName()) || definitionByKey.containsKey(field.getKey())) {
			throw new RuntimeException("Object attribute already contains field with this name or key:" + field.getName() + "->" + field.getKey());
		}
		definitions.add(field);
		definitionByKey.put(field.getKey(), field);
		definitionByName.put(field.getName(), field);
	}

	@Override
	public boolean isMessageRecord() {
		return messageRecord;
	}

	@Override
	public short getModelVersion() {
		return modelVersion;
	}

	@Override
	public List<AttributeDefinition> getAttributeDefinitions() {
		return definitions;
	}

	@Override
	public AttributeDefinition getAttributeDefinitionByKey(int key) {
		return definitionByKey.get(key);
	}

	@Override
	public AttributeDefinition getAttributeDefinitionByName(String name) {
		return definitionByName.get(name);
	}

	public String explain(int level, Set<String> printedObjects) {
		printedObjects.add(getObjectUuid());
		StringBuilder sb = new StringBuilder();
		sb.append("\t".repeat(level)).append(getName()).append(", ");
		sb.append("[").append(getObjectUuid()).append("], ");
		for (AttributeDefinition definition : definitions) {
			sb.append("\n");
			sb.append(definition.explain(level + 1, printedObjects));
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return explain(0, new HashSet<>());
	}
}
