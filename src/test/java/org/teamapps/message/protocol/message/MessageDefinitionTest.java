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

import org.junit.Test;
import org.teamapps.message.protocol.message.MessageDefinition;
import org.teamapps.message.protocol.model.MessageModel;
import org.teamapps.protocol.test.Company;

import java.io.IOException;

import static org.junit.Assert.*;

public class MessageDefinitionTest {

	@Test
	public void getObjectUuid() throws IOException {
		MessageModel messageModel = Company.getMessageModel();
		byte[] bytes = messageModel.toBytes();
		MessageDefinition definition = new MessageDefinition(bytes);
		System.out.println(messageModel.toString());
		assertEquals(messageModel.toString(), definition.toString());
	}
}
