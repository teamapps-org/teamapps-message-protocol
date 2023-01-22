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