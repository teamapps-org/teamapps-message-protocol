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
package org.teamapps.message.protocol.service;


import org.teamapps.message.protocol.message.MessageDefinition;
import org.teamapps.message.protocol.utils.MessageUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProtocol {

	private final String serviceName;
	private final List<ProtocolServiceMethod> serviceMethods = new ArrayList<>();
	private final List<ProtocolServiceBroadcastMethod> broadcastMethods = new ArrayList<>();

	public ServiceProtocol(String serviceName) {
		this.serviceName = serviceName;
	}

	public ServiceProtocol(byte[] bytes) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)));
	}

	public ServiceProtocol(DataInputStream dis) throws IOException {
		this.serviceName = MessageUtils.readString(dis);
		int serviceMethodCount = dis.readInt();
		for (int i = 0; i < serviceMethodCount; i++) {
			serviceMethods.add(new ProtocolServiceMethod(dis));
		}
		int broadCastMethodCount = dis.readInt();
		for (int i = 0; i < broadCastMethodCount; i++) {
			broadcastMethods.add(new ProtocolServiceBroadcastMethod(dis));
		}
	}

	public ServiceProtocol addMethod(ProtocolServiceMethod method) {
		serviceMethods.add(method);
		return this;
	}

	public ServiceProtocol addMethod(String methodName, MessageDefinition inputMessage, MessageDefinition outputMessage) {
		return addMethod(new ProtocolServiceMethod(methodName, inputMessage, outputMessage));
	}

	public ServiceProtocol addBroadcastMethod(String methodName, MessageDefinition message) {
		broadcastMethods.add(new ProtocolServiceBroadcastMethod(methodName, message));
		return this;
	}

	public String getServiceName() {
		return serviceName;
	}

	public List<ProtocolServiceMethod> getServiceMethods() {
		return serviceMethods;
	}

	public List<ProtocolServiceBroadcastMethod> getBroadcastMethods() {
		return broadcastMethods;
	}

	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		write(dos);
		dos.close();
		return bos.toByteArray();
	}

	public void write(DataOutputStream dos) throws IOException {
		MessageUtils.writeString(dos, serviceName);
		dos.writeInt(serviceMethods.size());
		for (ProtocolServiceMethod serviceMethod : serviceMethods) {
			serviceMethod.write(dos);
		}
		dos.writeInt(broadcastMethods.size());
		for (ProtocolServiceBroadcastMethod broadcastMethod : broadcastMethods) {
			broadcastMethod.write(dos);
		}
	}
}
