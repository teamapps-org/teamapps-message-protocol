package org.teamapps.message.protocol.file;

import java.io.IOException;

public interface FileDataReader {

	FileData readFileData(FileDataType type, String fileName, long length, String descriptor, boolean encrypted, String encryptionKey) throws IOException;

}
