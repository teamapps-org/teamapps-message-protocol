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
package org.teamapps.message.protocol.model;

import org.teamapps.message.protocol.file.FileDataReader;
import org.teamapps.message.protocol.message.MessageRecord;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public interface PojoObjectDecoder<MESSAGE extends MessageRecord> {

	MESSAGE decode(DataInputStream dis, FileDataReader fileDataReader);

	MESSAGE decode(Element element, FileDataReader fileDataReader);

	MESSAGE decode(String xml, FileDataReader fileDataReader);

	MESSAGE remap(MessageRecord message);

	MESSAGE defaultMessage();

	String getMessageUuid();

	default MESSAGE decode(byte[] bytes, FileDataReader fileDataReader) {
		return decode(new DataInputStream(new ByteArrayInputStream(bytes)), fileDataReader);
	}

}
