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