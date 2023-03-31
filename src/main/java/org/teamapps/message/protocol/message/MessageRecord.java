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
