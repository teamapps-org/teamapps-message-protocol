package org.teamapps.protocol.file;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

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
		FileInputStream inputStream = new FileInputStream(fileToCopy);
		FileChannel inChannel = inputStream.getChannel();
		File tempFile = Files.createTempFile("tmp", "." + getFileExtension()).toFile();
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		FileChannel outChannel = outputStream.getChannel();
		inChannel.transferTo(0, fileToCopy.length(), outChannel);
		inputStream.close();
		outputStream.close();
		return tempFile;
	}

	@Override
	public void copyToFile(File file) throws IOException {
		File fileToCopy = new File(path);
		FileInputStream inputStream = new FileInputStream(fileToCopy);
		FileChannel inChannel = inputStream.getChannel();
		FileOutputStream outputStream = new FileOutputStream(file);
		FileChannel outChannel = outputStream.getChannel();
		inChannel.transferTo(0, fileToCopy.length(), outChannel);
		inputStream.close();
		outputStream.close();
	}
}
