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
package org.teamapps.message.protocol.message;

import org.teamapps.message.protocol.builder.MessagePojoBuilder;
import org.teamapps.message.protocol.model.*;
import org.teamapps.message.protocol.service.ServiceProtocol;
import org.teamapps.message.protocol.utils.MessageUtils;

import java.io.*;
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
	private List<ServiceProtocol> serviceProtocols = new ArrayList<>();
	private Map<String, PojoObjectDecoder<? extends Message>> decoderByUuid = new ConcurrentHashMap<>();

	public MessageModelCollection(byte[] bytes) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)));
	}

	public MessageModelCollection(DataInputStream dis) throws IOException {
		this.name = MessageUtils.readString(dis);
		this.namespace = MessageUtils.readString(dis);
		this.version = dis.readInt();
		int modelCount = dis.readInt();
		for (int i = 0; i < modelCount; i++) {
			MessageDefinition model = new MessageDefinition(dis);
			addModel(model);
		}
		int enumCount = dis.readInt();
		for (int i = 0; i < enumCount; i++) {
			EnumDefinitionImpl enumDefinition = new EnumDefinitionImpl(dis);
			enums.add(enumDefinition);
		}
		int serviceProtocolCount = dis.readInt();
		for (int i = 0; i < serviceProtocolCount; i++) {
			serviceProtocols.add(new ServiceProtocol(dis));
		}
	}

	public MessageModelCollection(String name, String namespace, int version) {
		this.name = name;
		this.namespace = namespace;
		this.version = version;
	}

	public MessageDefinition createModel(String name) {
		return createModel(name, name, true);
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
	public void write(DataOutputStream dos) throws IOException {
		MessageUtils.writeString(dos, name);
		MessageUtils.writeString(dos, namespace);
		dos.writeInt(version);
		dos.writeInt(models.size());
		for (MessageModel model : models) {
			model.write(dos);
		}
		dos.writeInt(enums.size());
		for (EnumDefinition enumDefinition : enums) {
			enumDefinition.write(dos);
		}
		dos.writeInt(serviceProtocols.size());
		for (ServiceProtocol serviceProtocol : serviceProtocols) {
			serviceProtocol.write(dos);
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
	public String createModelCode() {
		try {
			return MessagePojoBuilder.createSchemaCode(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
