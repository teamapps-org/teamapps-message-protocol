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
package org.teamapps.message.protocol.utils;

import org.teamapps.message.protocol.file.FileDataReader;
import org.teamapps.message.protocol.message.Message;
import org.teamapps.message.protocol.message.MessageModelCollection;
import org.teamapps.message.protocol.model.PojoObjectDecoder;

import java.io.*;

public class MessageReader implements AutoCloseable {

	private final DataInputStream dis;
	private final FileDataReader fileDataReader;
	private MessageModelCollection messageModelCollection;

	public MessageReader(InputStream inputStream, FileDataReader fileDataReader, MessageModelCollection messageModelCollection) {
		this.dis = new DataInputStream(new BufferedInputStream(inputStream));
		this.fileDataReader = fileDataReader;
		this.messageModelCollection = messageModelCollection;
	}

	public Message readNextMessage() throws IOException {
		try {
			int length = dis.readInt();
			byte[] bytes = new byte[length];
			dis.readFully(bytes);
			String objectUuid = Message.readMessageUuid(bytes);
			PojoObjectDecoder<? extends Message> messageDecoder = messageModelCollection.getMessageDecoder(objectUuid);
			return messageDecoder.decode(bytes, fileDataReader);
		} catch (EOFException ignore) {
			return null;
		}
	}

	@Override
	public void close() throws Exception {
		dis.close();
	}
}
