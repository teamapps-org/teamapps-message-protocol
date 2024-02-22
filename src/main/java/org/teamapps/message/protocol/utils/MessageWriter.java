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
package org.teamapps.message.protocol.utils;

import org.teamapps.message.protocol.message.Message;
import org.teamapps.message.protocol.file.FileDataWriter;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MessageWriter implements AutoCloseable{

	private DataOutputStream dos;
	private FileDataWriter fileSink;

	public MessageWriter(OutputStream outputStream, FileDataWriter fileSink) {
		this.dos = new DataOutputStream(new BufferedOutputStream(outputStream));
		this.fileSink = fileSink;
	}

	public void writeMessage(Message message) throws IOException {
		byte[] bytes = message.toBytes(fileSink);
		dos.writeInt(bytes.length);
		dos.write(bytes);
	}

	@Override
	public void close() throws Exception {
		dos.close();
	}
}
