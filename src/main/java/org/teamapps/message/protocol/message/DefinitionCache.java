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

import org.teamapps.message.protocol.model.EnumDefinition;
import org.teamapps.message.protocol.model.MessageModel;

import java.util.HashMap;
import java.util.Map;

public class DefinitionCache {

	private final Map<String, MessageModel> modelCache = new HashMap<>();
	private final Map<String, EnumDefinition> enumCache = new HashMap<>();

	public void addModel(MessageModel model) {
		modelCache.put(model.getObjectUuid(), model);
	}

	public boolean containsModel(MessageModel model) {
		return modelCache.containsKey(model.getObjectUuid());
	}

	public MessageModel getModel(String uuid) {
		return modelCache.get(uuid);
	}

	public void addEnum(EnumDefinition enumDefinition) {
		enumCache.put(enumDefinition.getName(), enumDefinition);
	}

	public EnumDefinition getEnum(String name) {
		return enumCache.get(name);
	}

	public boolean containsEnum(EnumDefinition enumDefinition) {
		return enumCache.containsKey(enumDefinition.getName());
	}
}
