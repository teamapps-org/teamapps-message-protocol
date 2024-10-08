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

import org.junit.Test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class ProtectedFileTest {

	@Test
	public void canWrite() {
	}

	@Test
	public void delete() throws IOException {
		File file = Files.createTempFile("temp", ".tmp").toFile();
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
		dos.writeLong(Long.MAX_VALUE);
		dos.close();
		assertTrue(file.exists());
		assertEquals(8, file.length());

		ProtectedFile protectedFile = ProtectedFile.ofFile(file);
		assertTrue(file.exists());
		assertEquals(8, file.length());

		assertThrows(RuntimeException.class, protectedFile::delete);
		assertTrue(file.exists());
		assertEquals(8, file.length());

	}

	@Test
	public void mkdir() {
	}

	@Test
	public void renameTo() {
	}
}
