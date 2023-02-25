package org.teamapps.message.protocol.file;

import java.io.File;

public class ProtectedFile extends File {

	public static ProtectedFile ofFile(File file) {
		return new ProtectedFile(file.getPath());
	}

	public ProtectedFile(File file) {
		this(file.getPath());
	}

	public ProtectedFile(String pathname) {
		super(pathname);
	}

	@Override
	public boolean canWrite() {
		return false;
	}

	@Override
	public boolean delete() {
		throw new RuntimeException("Modifying protected file not allowed!");
	}

	@Override
	public void deleteOnExit() {
		throw new RuntimeException("Modifying protected file not allowed!");
	}

	@Override
	public boolean mkdir() {
		throw new RuntimeException("Modifying protected file not allowed!");
	}

	@Override
	public boolean mkdirs() {
		throw new RuntimeException("Modifying protected file not allowed!");
	}

	@Override
	public boolean renameTo(File dest) {
		throw new RuntimeException("Modifying protected file not allowed!");
	}

	@Override
	public boolean setLastModified(long time) {
		throw new RuntimeException("Modifying protected file not allowed!");
	}

	@Override
	public boolean setWritable(boolean writable, boolean ownerOnly) {
		throw new RuntimeException("Modifying protected file not allowed!");
	}

	@Override
	public boolean setWritable(boolean writable) {
		throw new RuntimeException("Modifying protected file not allowed!");
	}

	@Override
	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		throw new RuntimeException("Modifying protected file not allowed!");
	}

	@Override
	public boolean setExecutable(boolean executable) {
		throw new RuntimeException("Modifying protected file not allowed!");
	}
}
