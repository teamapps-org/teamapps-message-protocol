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
package org.teamapps.message.protocol.file;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class LocalFileData implements FileData {

	private final String fileName;
	private final long length;
	private final String path;
	private final boolean encrypted;
	private final String encryptionKey;

	public LocalFileData(String fileName, long length, String path) {
		this(fileName, length, path, false, null);
	}

	public LocalFileData(String fileName, long length, String path, boolean encrypted, String encryptionKey) {
		this.fileName = fileName;
		this.length = length;
		this.path = path;
		this.encrypted = encrypted;
		this.encryptionKey = encryptionKey;
		if (!new File(path).exists()) {
			throw new RuntimeException("Cannot create local file data for non existing file:" + path);
		}
	}

	@Override
	public FileDataType getType() {
		return FileDataType.LOCAL_FILE;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public long getLength() {
		return length;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(path);
	}

	@Override
	public File getAsFile() {
		return new ProtectedFile(path);
	}

	@Override
	public String getDescriptor() {
		return path;
	}

	@Override
	public boolean isEncrypted() {
		return encrypted;
	}

	@Override
	public String getEncryptionKey() {
		return encryptionKey;
	}

	@Override
	public String getBasePath() {
		return new File(path).getParentFile().getPath();
	}

	@Override
	public File copyToTempFile() throws IOException {
		File fileToCopy = new File(path);
		Path tempFile = Files.createTempFile("tmp", "." + getFileExtension());
		Files.copy(fileToCopy.toPath(), tempFile, StandardCopyOption.REPLACE_EXISTING);
		return tempFile.toFile();
	}

	@Override
	public void copyToFile(File file) throws IOException {
		File fileToCopy = new File(path);
		Files.copy(fileToCopy.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

}
