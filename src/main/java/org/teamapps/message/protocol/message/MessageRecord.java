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

import org.teamapps.message.protocol.file.FileDataWriter;
import org.teamapps.message.protocol.model.MessageModel;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface MessageRecord {
	MessageModel getModel();

	String getMessageDefUuid();

	String getMessageDefName();

	List<MessageAttribute> getAttributes();

	byte[] toBytes() throws IOException;

	byte[] toBytes(FileDataWriter fileDataWriter) throws IOException;

	//todo this api should be reconsidered because it changes the object
	byte[] toBytes(FileDataWriter fileDataWriter, boolean updateFileData) throws IOException;

	String toXml() throws IOException;

	int getRecordId();

	MessageAttribute getAttribute(String name);

	MessageRecord setRecordId(int recordId);

	MessageRecord setRecordModificationDate(Instant instant);

}
