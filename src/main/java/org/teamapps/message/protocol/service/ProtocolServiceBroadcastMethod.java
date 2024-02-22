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
package org.teamapps.message.protocol.service;

import org.teamapps.message.protocol.message.MessageDefinition;
import org.teamapps.message.protocol.utils.MessageUtils;

import java.io.*;

public class ProtocolServiceBroadcastMethod {

	private final String methodName;
	private final MessageDefinition message;

	public ProtocolServiceBroadcastMethod(String methodName, MessageDefinition message) {
		this.methodName = methodName;
		this.message = message;
	}

	public ProtocolServiceBroadcastMethod(byte[] bytes) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)));
	}

	public ProtocolServiceBroadcastMethod(DataInputStream dis) throws IOException {
		this.methodName = MessageUtils.readString(dis);
		this.message = new MessageDefinition(dis);
	}

	public MessageDefinition getMessage() {
		return message;
	}

	public String getMethodName() {
		return methodName;
	}

	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		write(dos);
		dos.close();
		return bos.toByteArray();
	}

	public void write(DataOutputStream dos) throws IOException {
		MessageUtils.writeString(dos, methodName);
		message.write(dos);
	}
}
