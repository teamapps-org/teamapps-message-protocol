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

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Cleaner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public interface FileData {

	static Cleaner CLEANER = Cleaner.create();

	static FileData create(File file) {
		return new LocalFileData(file.getName(), file.length(), file.getPath());
	}

	static FileData create(File file, String fileName) {
		return new LocalFileData(fileName, file.length(), file.getPath());
	}

	static void deleteFileOnDereference(FileData fileData, File file) {
		CLEANER.register(fileData, () -> {
			if (file != null && file.exists()) {
				file.delete();
			}
		});
	}

	FileDataType getType();

	String getFileName();

	long getLength();

	InputStream getInputStream() throws IOException;

	File getAsFile();

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

	default byte[] toBytes() throws IOException {
		if (getLength() > 1_000_000_000) {
			throw new RuntimeException("File too large for byte array:" + getLength());
		}
		BufferedInputStream bis = new BufferedInputStream(getInputStream());
		return IOUtils.readFully(bis, (int) getLength());
	}

}
