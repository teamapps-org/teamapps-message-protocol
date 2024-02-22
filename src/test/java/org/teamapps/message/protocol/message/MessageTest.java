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
package org.teamapps.message.protocol.message;

import org.junit.Test;
import org.teamapps.message.protocol.message.Message;
import org.teamapps.message.protocol.message.MessageAttribute;
import org.teamapps.protocol.test.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

import static org.junit.Assert.assertEquals;

public class MessageTest {

	@Test
	public void getRecordId() throws IOException {
		Company company = new Company()
				.setName("firstCompany")
				.setCeo(
						new Employee()
								.setFirstName("John")
								.setLastName("Smith")
				)
				.setType("standard");
		company
				.setRecordId(9)
				.setRecordCreationDate(Instant.ofEpochSecond(System.currentTimeMillis() / 1000));
		byte[] bytes = company.toBytes();
		Message message = new Message(bytes);
		bytes = message.toBytes();
		Company c2 = new Company(bytes);
		assertEquals("firstCompany", c2.getName());
		assertEquals("Smith", c2.getCeo().getLastName());
		assertEquals(company.toString(), c2.toString());
		Company c3 = Company.remap(message);
		assertEquals("Smith", c3.getCeo().getLastName());
	}

	@Test
	public void setRecordId() throws IOException {
		Person1 p1 = new Person1().setName("p1").setEmail("p1@gmail.com");
		Person2 p2 = new Person2().setName("p2").setPhone("+49 151 5413 343");
		byte[] p1Bytes = p1.toBytes();
		Person2 p1Asp2 = new Person2(p1Bytes);
		p1Asp2.setPhone("+49 1234567");
		assertEquals("p1", p1Asp2.getName());
		byte[] bytes = p1Asp2.toBytes();
		Person2 person2 = new Person2(bytes);
		Message message = new Message(bytes);
		assertEquals("p1@gmail.com", Person1.remap(message).getEmail());
	}

	@Test
	public void testEnum() throws IOException {
		Employee employee = new Employee()
				.setFirstName("John")
				.setLastName("Smith")
				.setType(EmployeeType.PART_TIME)
				.setGender(Gender.MALE);

		Employee employee2 = new Employee()
				.setFirstName("Anne")
				.setLastName("Miller")
				.setType(EmployeeType.FULL_TIME)
				.setGender(Gender.FEMALE)
				.setMentor(employee);

		byte[] bytes = employee2.toBytes();
		Message message = new Message(bytes);
		int key = employee2.getAttributeKey("lastName");
		MessageAttribute messageAttribute = message.getAttributes().stream().filter(attrib -> attrib.getAttributeDefinition().getKey() == key).findAny().orElse(null);
		assertEquals("Miller", messageAttribute.getStringAttribute());
		byte[] bytes2 = message.toBytes();
		Employee employee2b = new Employee(bytes2);
		assertEquals(employee2, employee2b);
		assertEquals(Gender.FEMALE, employee2b.getGender());
		assertEquals(Gender.MALE, employee2b.getMentor().getGender());
		assertEquals(EmployeeType.PART_TIME, employee2b.getMentor().getType());
	}

	@Test
	public void testXml() throws Exception {
		Company company = new Company()
				.setName("firstCompany")
				.setCeo(
						new Employee()
								.setFirstName("John")
								.setLastName("Smith")
				)
				.setType("standard");
		company
				.setRecordId(9)
				.setRecordCreationDate(Instant.ofEpochSecond(System.currentTimeMillis() / 1000));

		String xml = company.toXml();
		System.out.println(company.toXml());

		Company c2 = new Company(xml, null);
		System.out.println(company);
		System.out.println(c2);

	}

	@Test
	public void testGenericMessage() throws  Exception {
		Company company = new Company()
				.setName("firstCompany")
				.setCeo(
						new Employee()
								.setFirstName("John")
								.setLastName("Smith")
				)
				.setType("standard");
		company
				.setRecordId(9)
				.setRecordCreationDate(Instant.ofEpochSecond(System.currentTimeMillis() / 1000));

		Employee employee = new Employee()
				.setFirstName("John")
				.setLastName("Smith")
				.setType(EmployeeType.PART_TIME)
				.setGender(Gender.MALE);

		company.setEmbeddedMessage(employee);
		String xml = company.toXml();
		byte[] bytes = company.toBytes();
		Company c1 = new Company(bytes);
		Company c2 = new Company(xml, null);


		assertEquals("Smith", Employee.remap(c1.getEmbeddedMessage()).getLastName());
		assertEquals("Smith", Employee.remap(c2.getEmbeddedMessage()).getLastName());
	}



}
