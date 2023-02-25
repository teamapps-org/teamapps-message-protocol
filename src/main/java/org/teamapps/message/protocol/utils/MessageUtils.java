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
package org.teamapps.message.protocol.utils;

import org.teamapps.message.protocol.file.*;
import org.teamapps.message.protocol.message.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.BitSet;

public class MessageUtils {

	public static Message readMessageOrNull(DataInputStream dis) throws IOException {
		int len = dis.readInt();
		if (len == 0) {
			return null;
		} else {
			byte[] bytes = new byte[len];
			dis.readFully(bytes);
			return new Message(bytes);
		}
	}

	public static void writeNullableMessage(DataOutputStream dos, Message message) throws IOException {
		if (message == null) {
			dos.writeInt(0);
		} else {
			byte[] bytes = message.toBytes();
			dos.writeInt(bytes.length);
			dos.write(bytes);
		}
	}

	public static FileData readFile(DataInputStream dis, FileDataReader fileProvider) throws IOException {
		long length = dis.readLong();
		if (length == 0) {
			return null;
		} else {
			FileDataType type = FileDataType.getById(readByteAsInt(dis));
			String fileName = readString(dis);
			String descriptor = readString(dis);
			boolean encrypted = dis.readBoolean();
			String encryptionKey = encrypted ? readString(dis) : null;
			if (fileProvider != null) {
				return fileProvider.readFileData(type, fileName, length, descriptor, encrypted, encryptionKey);
			} else {
				return new GenericFileData(type, fileName, length, descriptor, encrypted, encryptionKey);
			}
		}
	}

	public static FileData readFile(ByteBuffer buffer, FileDataReader fileProvider) throws IOException {
		long length = buffer.getLong();
		if (length == 0) {
			return null;
		} else {
			FileDataType type = FileDataType.getById(readByteAsInt(buffer));
			String fileName = readString(buffer);
			String descriptor = readString(buffer);
			boolean encrypted = readBoolean(buffer);
			String encryptionKey = encrypted ? readString(buffer) : null;
			if (fileProvider != null) {
				return fileProvider.readFileData(type, fileName, length, descriptor, encrypted, encryptionKey);
			} else {
				return new GenericFileData(type, fileName, length, descriptor, encrypted, encryptionKey);
			}
		}
	}

	public static FileData writeFile(DataOutputStream dos, FileData fileData, FileDataWriter fileWriter) throws IOException {
		if (fileData == null) {
			dos.writeLong(0);
			return null;
		} else {
			FileData data = fileWriter != null ? fileWriter.writeFileData(fileData) : fileData;
			dos.writeLong(data.getLength());
			writeIntAsByte(dos, data.getType().getId());
			writeString(dos, data.getFileName());
			writeString(dos, data.getDescriptor());
			dos.writeBoolean(data.isEncrypted());
			if (data.isEncrypted()) {
				writeString(dos, data.getEncryptionKey());
			}
			return data;
		}
	}

	public static void writeFile(ByteBuffer buffer, FileData fileData, FileDataWriter fileWriter) throws IOException {
		if (fileData == null) {
			buffer.putLong(0);
		} else {
			FileData data = fileWriter != null ? fileWriter.writeFileData(fileData) : fileData;
			buffer.putLong(data.getLength());
			writeIntAsByte(buffer, data.getType().getId());
			writeString(buffer, data.getFileName());
			writeString(buffer, data.getDescriptor());
			writeBoolean(buffer, data.isEncrypted());
			if (data.isEncrypted()) {
				writeString(buffer, data.getEncryptionKey());
			}
		}
	}

	public static void writeIntAsByte(DataOutputStream dos, int value) throws IOException {
		dos.writeByte(value);
	}

	public static void writeIntAsByte(ByteBuffer buffer, int value) throws IOException {
		buffer.put((byte) value);
	}

	public static int readByteAsInt(DataInputStream dis) throws IOException {
		return dis.readByte();
	}

	public static int readByteAsInt(ByteBuffer buffer) {
		return buffer.get();
	}

	public static void writeString(DataOutputStream dos, String value) throws IOException {
		if (value != null && !value.isEmpty()) {
			byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
			dos.writeInt(bytes.length);
			dos.write(bytes);
		} else {
			dos.writeInt(0);
		}
	}

	public static void writeString(ByteBuffer buffer, String value) {
		if (value != null && !value.isEmpty()) {
			byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
			buffer.putInt(bytes.length);
			buffer.put(bytes);
		} else {
			buffer.putInt(0);
		}
	}

	public static String readString(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		if (length == 0) {
			return null;
		} else {
			byte[] bytes = new byte[length];
			dis.readFully(bytes);
			return new String(bytes, StandardCharsets.UTF_8);
		}
	}

	public static String readString(byte[] bytes, int pos) {
		int length = readInt(bytes, pos);
		if (length == 0) {
			return null;
		} else {
			return new String(bytes, pos + 4, length, StandardCharsets.UTF_8);
		}
	}

	public static int readInt(byte[] bytes, int pos) {
		int value = bytes[pos] & 0xFF;
		int c1 = bytes[pos] & 0xFF;
		int c2 = bytes[pos + 1] & 0xFF;
		int c3 = bytes[pos + 2] & 0xFF;
		int c4 = bytes[pos + 3] & 0xFF;
		return ((c1 << 24) + (c2 << 16) + (c3 << 8) + (c4));
	}

	public static String readString(ByteBuffer buf) {
		int length = buf.getInt();
		if (length == 0) {
			return null;
		} else {
			byte[] bytes = new byte[length];
			buf.get(bytes);
			return new String(bytes, StandardCharsets.UTF_8);
		}
	}


	public static void writeByteArray(DataOutputStream dos, byte[] bytes) throws IOException {
		if (bytes == null) {
			dos.writeInt(0);
		} else {
			dos.writeInt(bytes.length);
			dos.write(bytes);
		}
	}


	public static void writeByteArray(ByteBuffer buf, byte[] bytes) {
		if (bytes == null) {
			buf.putInt(0);
		} else {
			buf.putInt(bytes.length);
			buf.put(bytes);
		}
	}

	public static byte[] readByteArray(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		if (length == 0) {
			return null;
		}
		byte[] bytes = new byte[length];
		dis.readFully(bytes);
		return bytes;
	}


	public static byte[] readByteArray(ByteBuffer buf) {
		int length = buf.getInt();
		if (length == 0) {
			return null;
		}
		byte[] bytes = new byte[length];
		buf.get(bytes);
		return bytes;
	}

	public static void writeBitSet(DataOutputStream dos, BitSet bitSet) throws IOException {
		if (bitSet == null) {
			dos.writeInt(0);
		} else {
			dos.writeInt(bitSet.cardinality());
			for (int id = bitSet.nextSetBit(0); id >= 0; id = bitSet.nextSetBit(id + 1)) {
				dos.writeInt(id);
			}
		}
	}

	public static BitSet readBitSet(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		if (length == 0) {
			return null;
		}
		BitSet bitSet = new BitSet();
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			bitSet.set(dis.readInt());
		}
		return bitSet;
	}

	public static BitSet readBitSet(ByteBuffer buf) {
		int size = buf.getInt();
		if (size == 0) {
			return null;
		}
		BitSet bitSet = new BitSet();
		for (int i = 0; i < size; i++) {
			bitSet.set(buf.getInt());
		}
		return bitSet;
	}

	public static void writeIntArray(DataOutputStream dos, int[] intArray) throws IOException {
		if (intArray == null || intArray.length == 0) {
			dos.writeInt(0);
		} else {
			dos.writeInt(intArray.length);
			for (int value : intArray) {
				dos.writeInt(value);
			}
		}
	}

	public static int[] readIntArray(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		if (length == 0) {
			return null;
		}
		int[] intArray = new int[length];
		for (int i = 0; i < length; i++) {
			intArray[i] = dis.readInt();
		}
		return intArray;
	}

	public static int[] readIntArray(ByteBuffer buf) {
		int length = buf.getInt();
		if (length == 0) {
			return null;
		}
		int[] intArray = new int[length];
		for (int i = 0; i < length; i++) {
			intArray[i] = buf.getInt();
		}
		return intArray;
	}

	public static void writeLongArray(DataOutputStream dos, long[] longArray) throws IOException {
		if (longArray == null || longArray.length == 0) {
			dos.writeInt(0);
		} else {
			dos.writeInt(longArray.length);
			for (long value : longArray) {
				dos.writeLong(value);
			}
		}
	}

	public static long[] readLongArray(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		if (length == 0) {
			return null;
		}
		long[] longArray = new long[length];
		for (int i = 0; i < length; i++) {
			longArray[i] = dis.readLong();
		}
		return longArray;
	}

	public static long[] readLongArray(ByteBuffer buf) {
		int length = buf.getInt();
		if (length == 0) {
			return null;
		}
		long[] longArray = new long[length];
		for (int i = 0; i < length; i++) {
			longArray[i] = buf.getLong();
		}
		return longArray;
	}


	public static void writeFloatArray(DataOutputStream dos, float[] floatArray) throws IOException {
		if (floatArray == null || floatArray.length == 0) {
			dos.writeInt(0);
		} else {
			dos.writeInt(floatArray.length);
			for (float value : floatArray) {
				dos.writeFloat(value);
			}
		}
	}

	public static float[] readFloatArray(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		if (length == 0) {
			return null;
		}
		float[] floatArray = new float[length];
		for (int i = 0; i < length; i++) {
			floatArray[i] = dis.readFloat();
		}
		return floatArray;
	}

	public static float[] readFloatArray(ByteBuffer buf) {
		int length = buf.getInt();
		if (length == 0) {
			return null;
		}
		float[] floatArray = new float[length];
		for (int i = 0; i < length; i++) {
			floatArray[i] = buf.getFloat();
		}
		return floatArray;
	}

	public static void writeDoubleArray(DataOutputStream dos, double[] doubleArray) throws IOException {
		if (doubleArray == null || doubleArray.length == 0) {
			dos.writeInt(0);
		} else {
			dos.writeInt(doubleArray.length);
			for (double value : doubleArray) {
				dos.writeDouble(value);
			}
		}
	}

	public static double[] readDoubleArray(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		if (length == 0) {
			return null;
		}
		double[] doubleArray = new double[length];
		for (int i = 0; i < length; i++) {
			doubleArray[i] = dis.readDouble();
		}
		return doubleArray;
	}

	public static double[] readDoubleArray(ByteBuffer buf) {
		int length = buf.getInt();
		if (length == 0) {
			return null;
		}
		double[] doubleArray = new double[length];
		for (int i = 0; i < length; i++) {
			doubleArray[i] = buf.getDouble();
		}
		return doubleArray;
	}

	public static void writeStringArray(DataOutputStream dos, String[] stringArray) throws IOException {
		if (stringArray == null || stringArray.length == 0) {
			dos.writeInt(0);
		} else {
			dos.writeInt(stringArray.length);
			for (String value : stringArray) {
				writeString(dos, value);
			}
		}
	}

	public static String[] readStringArray(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		if (length == 0) {
			return null;
		}
		String[] stringArray = new String[length];
		for (int i = 0; i < length; i++) {
			stringArray[i] = readString(dis);
		}
		return stringArray;
	}

	public static String[] readStringArray(ByteBuffer buf) {
		int length = buf.getInt();
		if (length == 0) {
			return null;
		}
		String[] stringArray = new String[length];
		for (int i = 0; i < length; i++) {
			stringArray[i] = readString(buf);
		}
		return stringArray;
	}

	public static Instant readInstant32(DataInputStream dis) throws IOException {
		int value = dis.readInt();
		return value == 0 ? null : Instant.ofEpochSecond(value);
	}


	public static void writeInstant32(DataOutputStream dos, Instant instant) throws IOException {
		if (instant == null) {
			dos.writeInt(0);
		} else {
			dos.writeInt((int) instant.getEpochSecond());
		}
	}

	public static Instant readInstant64(DataInputStream dis) throws IOException {
		long value = dis.readLong();
		return value == 0 ? null : Instant.ofEpochMilli(value);
	}

	public static void writeInstant64(DataOutputStream dos, Instant instant) throws IOException {
		if (instant == null) {
			dos.writeLong(0);
		} else {
			dos.writeLong(instant.toEpochMilli());
		}
	}

	public static LocalDateTime readLocalDateTime(DataInputStream dis) throws IOException {
		long value = dis.readLong();
		return value == 0 ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC);
	}

	public static void writeLocalDateTime(DataOutputStream dos, LocalDateTime localDateTime) throws IOException {
		if (localDateTime == null) {
			dos.writeLong(0);
		} else {
			dos.writeLong(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
		}
	}

	public static LocalDate readLocalDate(DataInputStream dis) throws IOException {
		long value = dis.readLong();
		return value == 0 ? null : LocalDate.ofEpochDay(value);
	}

	public static void writeLocalDate(DataOutputStream dos, LocalDate localDate) throws IOException {
		if (localDate == null) {
			dos.writeLong(0);
		} else {
			dos.writeLong(localDate.toEpochDay());
		}
	}

	public static LocalTime readLocalTime(DataInputStream dis) throws IOException {
		int value = dis.readInt();
		return value == 0 ? null : LocalTime.ofSecondOfDay(value);
	}

	public static void writeLocalTime(DataOutputStream dos, LocalTime localTime) throws IOException {
		if (localTime == null) {
			dos.writeInt(0);
		} else {
			dos.writeInt(localTime.toSecondOfDay());
		}
	}

	public static Message readGenericMessage(DataInputStream dis, FileDataReader fileDataReader) throws IOException {
		int len = dis.readInt();
		if (len == 0) {
			return null;
		} else {
			byte[] bytes = new byte[len];
			dis.readFully(bytes);
			return new Message(bytes, fileDataReader);
		}
	}

	public static void writeGenericMessage(DataOutputStream dos, Message message, FileDataWriter fileDataWriter) throws IOException {
		if (message == null) {
			dos.writeInt(0);
		} else {
			byte[] bytes = message.toBytes(fileDataWriter);
			dos.writeInt(bytes.length);
			dos.write(bytes);
		}
	}

	public static void writeBoolean(ByteBuffer buffer, boolean value) {
		buffer.put((byte) (value ? 1 : 0));
	}

	public static boolean readBoolean(ByteBuffer buf) {
		return buf.get() == 1;
	}

	public static void writeShort(ByteBuffer buffer, int value) {
		buffer.putShort((short) value);
	}

	public static int readShort(ByteBuffer buffer) {
		return buffer.getShort();
	}

}
