package org.teamapps.protocol.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public interface FileData {

	static FileData create(File file) {
		return new LocalFileData(file.getName(), file.length(), file.getPath());
	}

	static FileData create(File file, String fileName) {
		return new LocalFileData(fileName, file.length(), file.getPath());
	}

	FileDataType getType();

	String getFileName();

	long getLength();

	InputStream getInputStream() throws IOException;

	String getDescriptor();

	boolean isEncrypted();

	String getEncryptionKey();

	String getBasePath();

	default String getFileExtension() {
		String name = getFileName();
		int pos = name.lastIndexOf('.');
		return pos > 0 ? name.substring(pos).toLowerCase() : "tmp";
	}

	default File copyToTempFile() throws IOException {
		Path path = Files.createTempFile("tmp", "." + getFileExtension());
		Files.copy(getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		return path.toFile();
	}

	default void copyToFile(File file) throws IOException {
		Files.copy(getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
}
