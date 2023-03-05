/*-
 * ========================LICENSE_START=================================
 * TeamApps Message Protocol
 * ---
 * Copyright (C) 2022 - 2023 TeamApps.org
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

public class GenericFileData implements FileData {
	private final FileDataType type;
	private final String fileName;
	private final long length;
	private final String path;
	private final boolean encrypted;
	private final String encryptionKey;

	public GenericFileData(FileDataType type, String fileName, long length, String path) {
		this(type, fileName, length, path, false, null);
	}

	public GenericFileData(FileDataType type, String fileName, long length, String path, boolean encrypted, String encryptionKey) {
		this.type = type;
		this.fileName = fileName;
		this.length = length;
		this.path = path;
		this.encrypted = encrypted;
		this.encryptionKey = encryptionKey;
	}

	@Override
	public FileDataType getType() {
		return type;
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

}
