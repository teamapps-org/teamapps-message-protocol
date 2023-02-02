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

import org.teamapps.message.protocol.message.AttributeType;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {


	public static Object readFromString(String s, AttributeType type) {
		if (s == null || s.isBlank()) {
			return null;
		}
		return switch (type) {
			case BOOLEAN -> s.equals("1") || s.equalsIgnoreCase("true");
			case BYTE -> Integer.parseInt(s);
			case INT -> Integer.parseInt(s);
			case LONG -> Long.parseLong(s);
			case FLOAT -> Float.parseFloat(s);
			case DOUBLE -> Double.parseDouble(s);
			case STRING -> s;
			case BITSET -> null;
			case BYTE_ARRAY -> Base64.getDecoder().decode(s);
			case INT_ARRAY -> readArray(s).stream().mapToInt(Integer::parseInt).toArray();
			case LONG_ARRAY -> readArray(s).stream().mapToLong(Long::parseLong).toArray();
			case FLOAT_ARRAY -> null;
			case DOUBLE_ARRAY -> readArray(s).stream().mapToDouble(Double::parseDouble).toArray();
			case STRING_ARRAY -> readArray(s).toArray(new String[0]);
			case ENUM -> null;
			case TIMESTAMP_32 -> null;
			case TIMESTAMP_64 -> null;
			case DATE_TIME -> null;
			case DATE -> null;
			case TIME -> null;
			case GENERIC_MESSAGE -> null;
			default -> null;
		};
	}

	private static List<String> readArray(String s) {
		return Arrays.stream(s.split(","))
				.map(String::trim)
				.collect(Collectors.toList());
	}

}
