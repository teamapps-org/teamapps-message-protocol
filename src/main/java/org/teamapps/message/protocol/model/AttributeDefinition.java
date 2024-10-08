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
package org.teamapps.message.protocol.model;


import org.teamapps.message.protocol.message.AttributeType;
import org.teamapps.message.protocol.message.DefinitionCache;
import org.teamapps.message.protocol.message.MessageDefinition;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

public interface AttributeDefinition extends BaseDefinition {

	MessageModel getParent();

	int getKey();

	String getDefaultValue();

	AttributeType getType();

	MessageModel getReferencedObject();

	EnumDefinition getEnumDefinition();

	void write(DataOutputStream dos) throws IOException;

	void write(DataOutputStream dos, DefinitionCache definitionCache) throws IOException;

	byte[] toBytes() throws IOException;

	String explain(int level, Set<String> printedObjects);

	default boolean isReferenceProperty() {
		return getType() == AttributeType.OBJECT_SINGLE_REFERENCE || getType() == AttributeType.OBJECT_MULTI_REFERENCE;
	}

	default boolean isMultiReference() {
		return getType() == AttributeType.OBJECT_MULTI_REFERENCE;
	}

	default boolean isEnumProperty() {
		return getType() == AttributeType.ENUM;
	}

	default boolean isMetaDataField() {
		return MessageDefinition.META_FIELD_NAMES.contains(getName());
	}
}
