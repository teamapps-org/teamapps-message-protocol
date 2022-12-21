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
package org.teamapps.protocol.model;

import org.teamapps.protocol.message.DefinitionCache;
import org.teamapps.protocol.message.MessageDefinition;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MessageModel {

	String getName();

	String getSpecificType();

	String getObjectUuid();

	boolean isMessageRecord();

	short getModelVersion();

	List<AttributeDefinition> getAttributeDefinitions();

	AttributeDefinition getAttributeDefinitionByKey(int key);

	AttributeDefinition getAttributeDefinitionByName(String name);

	byte[] toBytes() throws IOException;

	String explain(int level);

	void write(DataOutputStream dos, DefinitionCache definitionCache) throws IOException;
}
