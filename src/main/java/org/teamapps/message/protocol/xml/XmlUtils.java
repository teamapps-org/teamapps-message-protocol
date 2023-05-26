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
package org.teamapps.message.protocol.xml;

import org.teamapps.message.protocol.file.FileData;
import org.teamapps.message.protocol.file.FileDataReader;
import org.teamapps.message.protocol.file.FileDataWriter;
import org.teamapps.message.protocol.message.Message;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class XmlUtils {

	public static final String VALUE_NODE_NAME = "value";

	public static Element readChildElement(Element element, String childName) {
		NodeList elements = element.getElementsByTagName(childName);
		for (int i = 0; i < elements.getLength(); i++) {
			Node node = elements.item(i);
			if (node.getParentNode().isSameNode(element)) {
				return (Element) node;
			}
		}
		return null;
	}

	public static List<Element> readChildrenElements(Element element, String name) {
		NodeList elements = element.getElementsByTagName(name);
		if (elements.getLength() > 0) {
			List<Element> list = new ArrayList<>();
			for (int i = 0; i < elements.getLength(); i++) {
				Element node = (Element) elements.item(i);
				if (node.getParentNode().isSameNode(element)) {
					list.add(node);
				}
			}
			return list;
		} else {
			return Collections.emptyList();
		}
	}

	public static short readShort(String s) {
		if (s == null || s.isBlank()) {
			return 0;
		} else {
			return Short.parseShort(s);
		}
	}

	public static String readString(Element element) {
		return element.getTextContent();
	}

	public static boolean readBoolean(Element element) {
		String s = readString(element);
		if (s == null || s.isBlank()) {
			return false;
		} else {
			return s.equals("1") || s.equalsIgnoreCase("true");
		}
	}

	public static int readInt(Element element) {
		String s = readString(element);
		if (s == null || s.isBlank()) {
			return 0;
		} else {
			return Integer.parseInt(s);
		}
	}

	public static long readLong(Element element) {
		String s = readString(element);
		if (s == null || s.isBlank()) {
			return 0;
		} else {
			return Long.parseLong(s);
		}
	}

	public static byte readByte(Element element) {
		return (byte) readInt(element);
	}

	public static float readFloat(Element element) {
		String s = readString(element);
		if (s == null || s.isBlank()) {
			return 0;
		} else {
			return Float.parseFloat(s);
		}
	}

	public static double readDouble(Element element) {
		String s = readString(element);
		if (s == null || s.isBlank()) {
			return 0;
		} else {
			return Double.parseDouble(s);
		}
	}

	public static BitSet readBitSet(Element element) {
		return null; //todo
	}

	public static byte[] readByteArray(Element element) {
		String base64 = readString(element);
		return base64 == null ? null : Base64.getDecoder().decode(base64);
	}

	private static List<String> readValues(Element element) {
		List<Element> elements = readChildrenElements(element, VALUE_NODE_NAME);
		return elements.stream().map(XmlUtils::readString).collect(Collectors.toList());
	}

	public static int[] readIntArray(Element element) {
		List<String> values = readValues(element);
		return values.isEmpty() ? null : values.stream().mapToInt(Integer::parseInt).toArray();
	}

	public static long[] readLongArray(Element element) {
		List<String> values = readValues(element);
		return values.isEmpty() ? null : values.stream().mapToLong(Long::parseLong).toArray();
	}

	public static float[] readFloatArray(Element element) {
		List<String> values = readValues(element);
		if (values.isEmpty()) {
			return null;
		} else {
			float[] floats = new float[values.size()];
			for (int i = 0; i < floats.length; i++) {
				floats[i] = Float.parseFloat(values.get(i));
			}
			return floats;
		}
	}

	public static double[] readDoubleArray(Element element) {
		List<String> values = readValues(element);
		return values.isEmpty() ? null : values.stream().mapToDouble(Double::parseDouble).toArray();
	}

	public static String[] readStringArray(Element element) {
		List<String> values = readValues(element);
		return values.isEmpty() ? null : values.toArray(String[]::new);
	}

	public static FileData readFile(Element element, FileDataReader fileDataReader) {
		return null; //todo
	}

	public static Instant readInstant32(Element element) {
		int value = readInt(element);
		return value == 0 ? null : Instant.ofEpochSecond(value);
	}

	public static Instant readInstant64(Element element) {
		long value = readLong(element);
		return value == 0 ? null : Instant.ofEpochMilli(value);
	}

	public static LocalDateTime readLocalDateTime(Element element) {
		long value = readLong(element);
		return value == 0 ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC);
	}

	public static LocalDate readLocalDate(Element element) {
		long value = readLong(element);
		return value == 0 ? null : LocalDate.ofEpochDay(value);
	}

	public static LocalTime readLocalTime(Element element) {
		int value = readInt(element);
		return value == 0 ? null : LocalTime.ofSecondOfDay(value);
	}

	public static Message readGenericMessage(Element element, FileDataReader fileDataReader) {
		String base64 = readString(element);
		if (base64 == null) {
			return null;
		} else {
			byte[] bytes = Base64.getDecoder().decode(base64);
			try {
				return new Message(bytes, fileDataReader);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void writeGenericMessage(XmlNode xmlNode, Message message, FileDataWriter fileDataWriter) throws IOException {
		if (message != null) {
			String base64 = Base64.getEncoder().encodeToString(message.toBytes(fileDataWriter));
			xmlNode.setValue(base64);
		}
	}

	public static void writeBoolean(XmlNode xmlNode, boolean value) {
		xmlNode.setValue(value ? "true" : "false");
	}

	public static void writeByte(XmlNode xmlNode, byte value) {
		xmlNode.setValue(Byte.toString(value)); //todo
	}

	public static void writeBitSet(XmlNode xmlNode, BitSet bitSetAttribute) {
		//todo
	}

	public static void writeByteArray(XmlNode xmlNode, byte[] bytes) {
		xmlNode.setValue(Base64.getEncoder().encodeToString(bytes));
	}

	public static void writeIntArray(XmlNode xmlNode, int[] ints) {
		for (int value : ints) {
			xmlNode.addChild(new XmlNode(VALUE_NODE_NAME, Integer.toString(value)));
		}
	}

	public static void writeLongArray(XmlNode xmlNode, long[] longs) {
		for (long value : longs) {
			xmlNode.addChild(new XmlNode(VALUE_NODE_NAME, Long.toString(value)));
		}
	}

	public static void writeFloatArray(XmlNode xmlNode, float[] floats) {
		for (float value : floats) {
			xmlNode.addChild(new XmlNode(VALUE_NODE_NAME, Float.toString(value)));
		}
	}

	public static void writeDoubleArray(XmlNode xmlNode, double[] doubles) {
		for (double value : doubles) {
			xmlNode.addChild(new XmlNode(VALUE_NODE_NAME, Double.toString(value)));
		}
	}

	public static void writeStringArray(XmlNode xmlNode, String[] strings) {
		for (String value : strings) {
			xmlNode.addChild(new XmlNode(VALUE_NODE_NAME, value));
		}
	}

	public static void writeFile(XmlNode xmlNode, FileData fileData, FileDataWriter fileDataWriter) {
		//todo
	}

	public static void writeInstant32(XmlNode xmlNode, Instant timestampAttribute) {
		xmlNode.setValue(Long.toString(timestampAttribute.getEpochSecond()));
	}

	public static void writeInstant64(XmlNode xmlNode, Instant timestampAttribute) {
		xmlNode.setValue(Long.toString(timestampAttribute.toEpochMilli()));
	}

	public static void writeLocalDateTime(XmlNode xmlNode, LocalDateTime dateTimeAttribute) {
		xmlNode.setValue(Long.toString(dateTimeAttribute.toInstant(ZoneOffset.UTC).toEpochMilli()));
	}

	public static void writeLocalDate(XmlNode xmlNode, LocalDate dateAttribute) {
		xmlNode.setValue(Long.toString(dateAttribute.toEpochDay()));
	}

	public static void writeLocalTime(XmlNode xmlNode, LocalTime timeAttribute) {
		xmlNode.setValue(Integer.toString(timeAttribute.toSecondOfDay()));
	}
}
