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

import org.teamapps.message.protocol.model.*;
import org.teamapps.message.protocol.service.ServiceProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageModelCollection implements ModelCollection {

	private final String name;
	private final String namespace;
	private final int version;
	private final List<MessageModel> models = new ArrayList<>();
	private final List<EnumDefinition> enums = new ArrayList<>();
	private final Map<String, MessageModel> modelByKey = new ConcurrentHashMap<>();
	private Map<String, PojoObjectDecoder<? extends Message>> decoderByUuid = new ConcurrentHashMap<>();
	private List<ServiceProtocol> serviceProtocols = new ArrayList<>();

	public MessageModelCollection(String name, String namespace, int version) {
		this.name = name;
		this.namespace = namespace;
		this.version = version;
	}

	public MessageDefinition createModel(String name, String uuid) {
		return createModel(name, uuid, true);
	}

	public MessageDefinition createModel(String name, String uuid, boolean messageRecord) {
		MessageDefinition definition = new MessageDefinition(uuid, name, messageRecord, version);
		addModel(definition);
		return definition;
	}

	public MessageDefinition createModel(String name, String uuid, Message specificType, boolean messageRecord) {
		MessageDefinition definition = new MessageDefinition(uuid, name, specificType, messageRecord, version);
		addModel(definition);
		return definition;
	}

	public MessageDefinition createModel(String name, String uuid, int modelVersion, Message specificType, boolean messageRecord) {
		MessageDefinition definition = new MessageDefinition(uuid, name, specificType, messageRecord, modelVersion);
		addModel(definition);
		return definition;
	}

	public void addModel(MessageModel model) {
		models.add(model);
		modelByKey.put(model.getObjectUuid(), model);
	}

	public EnumDefinition createEnum(String name, String... enumValues) {
		return createEnum(name, Arrays.asList(enumValues));
	}

	public EnumDefinition createEnum(String name, List<String> enumValues) {
		EnumDefinitionImpl definition = new EnumDefinitionImpl(name, enumValues);
		enums.add(definition);
		return definition;
	}

	public ServiceProtocol createService(String serviceName) {
		ServiceProtocol serviceSchema = new ServiceProtocol(serviceName);
		serviceProtocols.add(serviceSchema);
		return serviceSchema;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public short getVersion() {
		return (short) version;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public MessageModel getModel(String uuid) {
		return modelByKey.get(uuid);
	}

	@Override
	public List<MessageModel> getModels() {
		return models;
	}

	@Override
	public List<EnumDefinition> getEnums() {
		return enums;
	}

	@Override
	public ModelRegistry createRegistry() {
		return new MessageModelRegistry(this);
	}

	@Override
	public List<ServiceProtocol> getProtocolServiceSchemas() {
		return serviceProtocols;
	}

	@Override
	public void addMessageDecoder(String uuid, PojoObjectDecoder<? extends Message> decoder) {
		decoderByUuid.put(uuid,decoder);
	}

	@Override
	public PojoObjectDecoder<? extends Message> getMessageDecoder(String uuid) {
		return decoderByUuid.get(uuid);
	}

	@Override
	public boolean containsDecoder(String uuid) {
		return decoderByUuid.containsKey(uuid);
	}

	@Override
	public byte[] toBytes() {
		return new byte[0];
	}
}
