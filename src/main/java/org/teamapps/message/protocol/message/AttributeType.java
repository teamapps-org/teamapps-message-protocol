/*-
 * ========================LICENSE_START=================================
 * TeamApps Protocol Schema
 * ---
 * Copyright (C) 2022 TeamApps.org
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
package org.teamapps.message.protocol.message;

public enum AttributeType {

	OBJECT(1),
	OBJECT_SINGLE_REFERENCE(2),
	OBJECT_MULTI_REFERENCE(3),
	BOOLEAN(4),
	BYTE(5),
	INT(6),
	LONG(7),
	FLOAT(8),
	DOUBLE(9),
	STRING(10),
	BITSET(11),
	BYTE_ARRAY(12),
	INT_ARRAY(13),
	LONG_ARRAY(14),
	FLOAT_ARRAY(15),
	DOUBLE_ARRAY(16),
	STRING_ARRAY(17),
	FILE(18),
	ENUM(19),

	TIMESTAMP_32(20),
	TIMESTAMP_64(21),
	DATE_TIME(22),
	DATE(23),
	TIME(24),
	GENERIC_MESSAGE(25),
	;

	private final int id;

	AttributeType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public boolean isReference() {
		return this == OBJECT_SINGLE_REFERENCE || this == OBJECT_MULTI_REFERENCE;
	}

	public static AttributeType getById(int id) {
		return switch (id) {
			case 1 -> OBJECT;
			case 2 -> OBJECT_SINGLE_REFERENCE;
			case 3 -> OBJECT_MULTI_REFERENCE;
			case 4 -> BOOLEAN;
			case 5 -> BYTE;
			case 6 -> INT;
			case 7 -> LONG;
			case 8 -> FLOAT;
			case 9 -> DOUBLE;
			case 10 -> STRING;
			case 11 -> BITSET;
			case 12 -> BYTE_ARRAY;
			case 13 -> INT_ARRAY;
			case 14 -> LONG_ARRAY;
			case 15 -> FLOAT_ARRAY;
			case 16 -> DOUBLE_ARRAY;
			case 17 -> STRING_ARRAY;
			case 18 -> FILE;
			case 19 -> ENUM;

			case 20 -> TIMESTAMP_32;
			case 21 -> TIMESTAMP_64;
			case 22 -> DATE_TIME;
			case 23 -> DATE;
			case 24 -> TIME;
			case 25 -> GENERIC_MESSAGE;
			default -> null;
		};
	}
}
