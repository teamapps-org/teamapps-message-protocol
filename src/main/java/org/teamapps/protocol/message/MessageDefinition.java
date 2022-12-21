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

import org.teamapps.protocol.model.AttributeDefinition;
import org.teamapps.protocol.model.EnumDefinition;
import org.teamapps.protocol.model.MessageModel;
import org.teamapps.protocol.utils.MessageUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageDefinition implements MessageModel {

	public static final String META_RECORD_ID = "recordId";
	public final static String META_CREATION_DATE = "creationDate";
	public final static String META_CREATED_BY = "createdBy";
	public final static String META_MODIFICATION_DATE = "modificationDate";
	public final static String META_MODIFIED_BY = "modifiedBy";

	public final static Set<String> RESERVED_NAMES_LOWER_CASE = Stream.of(META_RECORD_ID, META_CREATION_DATE, META_CREATED_BY, META_MODIFICATION_DATE, META_MODIFIED_BY).map(String::toLowerCase).collect(Collectors.toSet());

	private final String name;
	private final String specificType;
	private final String objectUuid;
	private final short modelVersion;
	private final boolean messageRecord;
	private final List<AttributeDefinition> definitions = new ArrayList<>();
	private final Map<Integer, AttributeDefinition> definitionByKey = new HashMap<>();
	private final Map<String, AttributeDefinition> definitionByName = new HashMap<>();

	public MessageDefinition(String objectUuid, String name, boolean messageRecord, int modelVersion) {
		this(objectUuid, name, null, messageRecord, modelVersion);
	}

	public MessageDefinition(String objectUuid, String name, String specificType, boolean messageRecord, int modelVersion) {
		this.objectUuid = objectUuid;
		this.name = name;
		this.specificType = specificType;
		this.modelVersion = (short) modelVersion;
		this.messageRecord = messageRecord;
		if (messageRecord) {
			addIntAttribute(META_RECORD_ID, 16_000);
			addTimestampAttribute(META_CREATION_DATE, 16_001);
			addIntAttribute(META_CREATED_BY, 16_002);
			addTimestampAttribute(META_MODIFICATION_DATE, 16_003);
			addIntAttribute(META_MODIFIED_BY, 16_004);
		}
	}

	public MessageDefinition(byte[] bytes) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)));
	}

	public MessageDefinition(DataInputStream dis) throws IOException {
		this(dis, new DefinitionCache());
	}

	public MessageDefinition(DataInputStream dis, DefinitionCache definitionCache) throws IOException {
		this(MessageUtils.readString(dis), MessageUtils.readString(dis), MessageUtils.readString(dis), dis.readBoolean(), dis.readShort());
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
		MessageUtils.writeString(dos, getSpecificType());
		dos.writeBoolean(messageRecord);
		dos.writeShort(modelVersion);
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
	public String getSpecificType() {
		return specificType;
	}

	public String getObjectUuid() {
		return objectUuid;
	}

	public void addBooleanAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.BOOLEAN);
	}

	public void addByteAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.BYTE);
	}

	public void addStringAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.STRING);
	}

	public void addIntAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.INT);
	}

	public void addLongAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.LONG);
	}

	public void addFloatAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.FLOAT);
	}

	public void addDoubleAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.DOUBLE);
	}

	public void addFileAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.FILE);
	}

	public void addByteArrayAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.BYTE_ARRAY);
	}

	public void addIntArrayAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.INT_ARRAY);
	}

	public void addLongArrayAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.LONG_ARRAY);
	}

	public void addFloatArrayAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.FLOAT_ARRAY);
	}

	public void addDoubleArrayAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.DOUBLE_ARRAY);
	}

	public void addStringArrayAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.STRING_ARRAY);
	}

	public void addTimestampAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.TIMESTAMP_32);
	}

	public void addLongTimestampAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.TIMESTAMP_64);
	}

	public void addDateTimeAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.DATE_TIME);
	}

	public void addDateAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.DATE);
	}

	public void addTimeAttribute(String name, int key) {
		addAttribute(name, key, AttributeType.TIME);
	}

	public void addEnum(String name, EnumDefinition enumDefinition, int key) {
		addEnum(name, enumDefinition, key, null);
	}

	public void addEnum(String name, EnumDefinition enumDefinition, int key, String specificType) {
		AbstractAttributeDefinition attributeDefinition = new AbstractAttributeDefinition(this, name, key, enumDefinition, specificType);
		addAttribute(attributeDefinition);
	}

	public void addAttribute(String name, int key, AttributeType type) {
		addAttribute(name, key, type, null);
	}

	public void addAttribute(String name, int key, AttributeType type, String specificType) {
		AbstractAttributeDefinition attributeDefinition = new AbstractAttributeDefinition(this, name, key, type, specificType);
		addAttribute(attributeDefinition);
	}

	public void addSingleReference(String name, int key, MessageDefinition referencedObject) {
		addSingleReference(name, key, null, referencedObject);
	}

	public void addSingleReference(String name, int key, String specificType, MessageDefinition referencedObject) {
		AbstractAttributeDefinition referenceAttributeDefinition = new AbstractAttributeDefinition(this, name, key, specificType, referencedObject, false);
		addAttribute(referenceAttributeDefinition);
	}

	public void addMultiReference(String name, int key, MessageDefinition referencedObject) {
		addMultiReference(name, key, null, referencedObject);
	}

	public void addMultiReference(String name, int key, String specificType, MessageDefinition referencedObject) {
		AbstractAttributeDefinition referenceAttributeDefinition = new AbstractAttributeDefinition(this, name, key, specificType, referencedObject, true);
		addAttribute(referenceAttributeDefinition);
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

	public String explain(int level) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t".repeat(level)).append(getName()).append(", ");
		sb.append("[").append(getObjectUuid()).append("], ");
		for (AttributeDefinition definition : definitions) {
			sb.append("\n");
			sb.append(definition.explain(level + 1));
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return explain(0);
	}
}
