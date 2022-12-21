package org.teamapps.protocol.message;

import org.junit.Test;
import org.teamapps.protocol.model.MessageModel;
import org.teamapps.protocol.test.Company;

import java.io.IOException;

import static org.junit.Assert.*;

public class MessageDefinitionTest {

	@Test
	public void getObjectUuid() throws IOException {
		MessageModel messageModel = Company.getMessageModel();
		byte[] bytes = messageModel.toBytes();
		MessageDefinition definition = new MessageDefinition(bytes);
		assertEquals(messageModel.toString(), definition.toString());
	}
}