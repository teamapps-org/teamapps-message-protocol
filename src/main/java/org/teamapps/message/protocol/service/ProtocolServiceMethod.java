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

public class ProtocolServiceMethod {

	private final String methodName;
	private final MessageDefinition inputMessage;
	private final MessageDefinition outputMessage;

	public ProtocolServiceMethod(String methodName, MessageDefinition inputMessage, MessageDefinition outputMessage) {
		this.methodName = methodName;
		this.inputMessage = inputMessage;
		this.outputMessage = outputMessage;
	}

	public ProtocolServiceMethod(byte[] bytes) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)));
	}

	public ProtocolServiceMethod(DataInputStream dis) throws IOException {
		this.methodName = MessageUtils.readString(dis);
		this.inputMessage = new MessageDefinition(dis);
		this.outputMessage = new MessageDefinition(dis);
	}

	public MessageDefinition getInputMessage() {
		return inputMessage;
	}

	public MessageDefinition getOutputMessage() {
		return outputMessage;
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
		inputMessage.write(dos);
		outputMessage.write(dos);
	}
}
