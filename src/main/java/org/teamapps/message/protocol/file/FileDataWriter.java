package org.teamapps.message.protocol.file;

import java.io.IOException;

public interface FileDataWriter {

	FileData writeFileData(FileData fileData) throws IOException;
}
