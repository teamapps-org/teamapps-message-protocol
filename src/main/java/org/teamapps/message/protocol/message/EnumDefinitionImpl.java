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
import org.teamapps.message.protocol.utils.MessageUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EnumDefinitionImpl implements EnumDefinition {

	private final String name;
	private final List<String> values;

	public EnumDefinitionImpl(String name, List<String> values) {
		this.name = name;
		this.values = values;
	}

	public EnumDefinitionImpl(byte[] bytes) throws IOException {
		this(new DataInputStream(new ByteArrayInputStream(bytes)));
	}

	public EnumDefinitionImpl(DataInputStream dis) throws IOException {
		this.name = MessageUtils.readString(dis);
		this.values = new ArrayList<>();
		int valueCount = dis.readInt();
		for (int i = 0; i < valueCount; i++) {
			values.add(MessageUtils.readString(dis));
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getEnumValues() {
		return values;
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
	public void write(DataOutputStream dos) throws IOException {
		MessageUtils.writeString(dos, name);
		dos.writeInt(values.size());
		for (String value : values) {
			MessageUtils.writeString(dos, value);
		}
	}
}
