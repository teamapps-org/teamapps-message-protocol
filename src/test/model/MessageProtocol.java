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
import org.teamapps.message.protocol.message.AttributeType;
import org.teamapps.message.protocol.message.MessageDefinition;
import org.teamapps.message.protocol.message.MessageModelCollection;
import org.teamapps.message.protocol.model.EnumDefinition;
import org.teamapps.message.protocol.model.ModelCollection;
import org.teamapps.message.protocol.model.ModelCollectionProvider;
import org.teamapps.message.protocol.service.ServiceProtocol;

public class MessageProtocol implements ModelCollectionProvider {
	@Override
	public ModelCollection getModelCollection() {
		MessageModelCollection modelCollection = new MessageModelCollection("newTestModel", "org.teamapps.protocol.test", 1);

		MessageDefinition employee = modelCollection.createModel("employee", "col.employee", true);
		MessageDefinition company = modelCollection.createModel("company", "col.company", true);
		MessageDefinition person1 = modelCollection.createModel("person1", "col.person", true);

		EnumDefinition employeeType = modelCollection.createEnum("employeeType", "fullTime", "partTime", "seasonal", "temporary");
		EnumDefinition gender = modelCollection.createEnum("gender", "male", "female", "diverse");

		MessageDefinition xmlTest = modelCollection.createModel("xmlTest", "col.xmlTest", true);
		xmlTest.addAttribute("intVal", 1, AttributeType.INT, null, "900", "This is a comment");
		xmlTest.addAttribute("intVal2", 2, AttributeType.INT, null, "123", null);
		xmlTest.addAttribute("stringVal", 3, AttributeType.STRING, null, "TEST", "And this is another comment...");


		employee.addAttribute("firstName", 1, AttributeType.STRING);
		employee.addAttribute("lastName", 2, AttributeType.STRING);
		employee.addAttribute("pic", 3, AttributeType.BYTE_ARRAY);
		employee.addAttribute("vegan", 4, AttributeType.BOOLEAN);
		employee.addAttribute("birthday", 5, AttributeType.DATE);
		employee.addEnum("type", employeeType, 6);
		employee.addEnum("gender", gender, 7);
		employee.addSingleReference("mentor", 8, employee);

		company.addAttribute("name", 1, AttributeType.STRING);
		company.addAttribute("type", 2, AttributeType.STRING);
		company.addSingleReference("ceo", 3, employee);
		company.addMultiReference("employee", 4, employee);
		company.addAttribute("picture", 5, AttributeType.FILE);
		company.addGenericMessage("embeddedMessage", 6);

		person1.addString("name", 1);
		person1.addString("email", 2);

		ServiceProtocol testService = modelCollection.createService("testService");
		testService.addMethod("method1", company, employee);
		testService.addBroadcastMethod("broadcastMethod1", employee);

		return modelCollection;
	}
}
