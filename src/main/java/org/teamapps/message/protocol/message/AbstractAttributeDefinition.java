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


import org.teamapps.message.protocol.model.AttributeDefinition;
import org.teamapps.message.protocol.model.EnumDefinition;
import org.teamapps.message.protocol.model.MessageModel;
import org.teamapps.message.protocol.utils.MessageUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbstractAttributeDefinition implements AttributeDefinition {
	private final MessageModel parent;
	private final String name;
	private final int key;
	private final AttributeType type;
	private final String defaultValue;
	private final String comment;
	private final Message specificType;
	private final MessageModel referencedObject;
	private final boolean multiReference;
	private final EnumDefinition enumDefinition;

	public AbstractAttributeDefinition(MessageModel parent, String name, int key, AttributeType type, Message specificType){
		this(parent, name, key, type, specificType, null, null);
	}

	public AbstractAttributeDefinition(MessageModel parent, String name, int key, AttributeType type, Message specificType, String defaultValue, String comment) {
		this.parent = parent;
		this.name = name;
		this.key = key;
		this.type = type;
		this.specificType = specificType;
		this.defaultValue = defaultValue;
		this.comment = comment;
		this.referencedObject = null;
		this.multiReference = false;
		this.enumDefinition = null;
	}

	public AbstractAttributeDefinition(MessageModel parent, String name, int key, EnumDefinition enumDefinition, Message specificType) {
		this(parent, name, key, enumDefinition, specificType, null, null);
	}

	public AbstractAttributeDefinition(MessageModel parent, String name, int key, EnumDefinition enumDefinition, Message specificType, String defaultValue, String comment) {
		this.parent = parent;
		this.name = name;
		this.key = key;
		this.type = AttributeType.ENUM;
		this.specificType = specificType;
		this.defaultValue = defaultValue;
		this.comment = comment;
		this.referencedObject = null;
		this.multiReference = false;
		this.enumDefinition = enumDefinition;
	}

	public AbstractAttributeDefinition(MessageModel parent, String name, int key, Message specificType, MessageModel referencedObject, boolean multiReference) {
		this(parent, name, key, specificType, referencedObject, multiReference, null);
	}

	public AbstractAttributeDefinition(MessageModel parent, String name, int key, Message specificType, MessageModel referencedObject, boolean multiReference, String comment) {
		this.parent = parent;
		this.name = name;
		this.key = key;
		this.type = multiReference ? AttributeType.OBJECT_MULTI_REFERENCE : AttributeType.OBJECT_SINGLE_REFERENCE;
		this.specificType = specificType;
		this.defaultValue = null;
		this.comment = comment;
		this.referencedObject = referencedObject;
		this.multiReference = multiReference;
		this.enumDefinition = null;
	}

	public AbstractAttributeDefinition(MessageModel parent, byte[] bytes, DefinitionCache definitionCache) throws IOException {
		this(parent, new DataInputStream(new ByteArrayInputStream(bytes)), definitionCache);
	}

	public AbstractAttributeDefinition(MessageModel parent, DataInputStream dis, DefinitionCache definitionCache) throws IOException {
		this.parent = parent;
		this.name = MessageUtils.readString(dis);
		this.key = dis.readInt();
		this.type = AttributeType.getById(dis.readInt());
		this.specificType = MessageUtils.readMessageOrNull(dis);
		this.defaultValue = MessageUtils.readString(dis);
		this.comment = MessageUtils.readString(dis);

		if (type == AttributeType.OBJECT_SINGLE_REFERENCE || type == AttributeType.OBJECT_MULTI_REFERENCE) {
			this.multiReference = dis.readBoolean();
			if (dis.readBoolean()) {
				String objectUuid = MessageUtils.readString(dis);
				this.referencedObject = definitionCache.getModel(objectUuid);
			} else {
				this.referencedObject = new MessageDefinition(dis, definitionCache);
			}
			this.enumDefinition = null;
		} else if (type == AttributeType.ENUM) {
			if (dis.readBoolean()) {
				String enumName = MessageUtils.readString(dis);
				this.enumDefinition = definitionCache.getEnum(enumName);
			} else {
				String enumName = MessageUtils.readString(dis);
				int values = dis.readInt();
				List<String> enumValues = new ArrayList<>();
				for (int i = 0; i < values; i++) {
					enumValues.add(MessageUtils.readString(dis));
				}
				this.enumDefinition = new EnumDefinitionImpl(enumName, enumValues);
				definitionCache.addEnum(this.enumDefinition);
			}
			this.referencedObject = null;
			this.multiReference = false;
		} else {
			this.enumDefinition = null;
			this.referencedObject = null;
			this.multiReference = false;
		}
	}

	public void write(DataOutputStream dos) throws IOException {
		write(dos, new DefinitionCache());
	}

	public void write(DataOutputStream dos, DefinitionCache definitionCache) throws IOException {
		MessageUtils.writeString(dos, name);
		dos.writeInt(key);
		dos.writeInt(type.getId());
		MessageUtils.writeNullableMessage(dos, specificType);
		MessageUtils.writeString(dos, defaultValue);
		MessageUtils.writeString(dos, comment);
		if (isReferenceProperty()) {
			dos.writeBoolean(multiReference);
			if (definitionCache.containsModel(referencedObject)) {
				dos.writeBoolean(true);
				MessageUtils.writeString(dos, referencedObject.getObjectUuid());
			} else {
				definitionCache.addModel(referencedObject);
				dos.writeBoolean(false);
				referencedObject.write(dos, definitionCache);
			}
		} else if ( type == AttributeType.ENUM) {
			if (definitionCache.containsEnum(enumDefinition)) {
				dos.writeBoolean(true);
				MessageUtils.writeString(dos, enumDefinition.getName());
			} else {
				dos.writeBoolean(false);
				MessageUtils.writeString(dos, enumDefinition.getName());
				List<String> enumValues = enumDefinition.getEnumValues();
				dos.writeInt(enumValues.size());
				for (int i = 0; i < enumValues.size(); i++) {
					MessageUtils.writeString(dos, enumValues.get(i));
				}
				definitionCache.addEnum(enumDefinition);
			}
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
	public MessageModel getParent() {
		return parent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getKey() {
		return key;
	}

	@Override
	public AttributeType getType() {
		return type;
	}

	@Override
	public Message getSpecificType() {
		return specificType;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public boolean isReferenceProperty() {
		return type == AttributeType.OBJECT_SINGLE_REFERENCE || type == AttributeType.OBJECT_MULTI_REFERENCE;
	}

	@Override
	public boolean isEnumProperty() {
		return type == AttributeType.ENUM;
	}

	@Override
	public EnumDefinition getEnumDefinition() {
		return enumDefinition;
	}

	@Override
	public MessageModel getReferencedObject() {
		return referencedObject;
	}

	@Override
	public boolean isMultiReference() {
		return multiReference;
	}

	@Override
	public String explain(int level, Set<String> printedObjects) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t".repeat(level)).append(getName()).append(", ");
		sb.append(getType());
		if (isReferenceProperty()) {
			MessageModel model = getReferencedObject();
			sb.append(" ").append(isMultiReference() ? "multi" : "single").append(" -> ");
			sb.append("\n");
			if (printedObjects.contains(model.getObjectUuid())) {
				sb.append("\t".repeat(level + 1)).append(model.getName()).append(" [").append(model.getObjectUuid()).append("], ").append(" ...");
			} else {
				sb.append(model.explain(level + 1, printedObjects));
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return explain(0, new HashSet<>());
	}
}
