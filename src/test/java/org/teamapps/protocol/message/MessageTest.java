package org.teamapps.protocol.message;

import org.junit.Test;
import org.teamapps.protocol.test.*;

import java.io.IOException;
import java.time.Instant;

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

}