package org.teamapps.protocol.file;

import java.io.IOException;

public interface FileDataWriter {

	FileData writeFileData(FileData fileData) throws IOException;
}
