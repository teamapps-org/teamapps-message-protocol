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
package org.teamapps.message.protocol.model;

import org.teamapps.message.protocol.message.DefinitionCache;
import org.teamapps.message.protocol.service.ServiceProtocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public interface ModelCollection extends PojoObjectDecoderRegistry {
	String getName();

	short getVersion();

	String getNamespace();

	MessageModel getModel(String uuid);

	List<MessageModel> getModels();

	List<EnumDefinition> getEnums();

	ModelRegistry createRegistry();

	List<ServiceProtocol> getProtocolServiceSchemas();

	void write(DataOutputStream dos) throws IOException;

	byte[] toBytes() throws IOException;
}
