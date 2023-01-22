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

import org.teamapps.message.protocol.file.FileData;
import org.teamapps.message.protocol.file.FileDataWriter;
import org.teamapps.message.protocol.model.AttributeDefinition;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.BitSet;
import java.util.List;

public interface MessageAttribute {

	AttributeDefinition getAttributeDefinition();

	Message getReferencedObject();

	List<Message> getReferencedObjects();

	<TYPE extends Message> TYPE getReferencedObjectAsType();

	<TYPE extends Message> List<TYPE> getReferencedObjectsAsType();

	boolean getBooleanAttribute();

	byte getByteAttribute();

	int getIntAttribute();

	long getLongAttribute();

	float getFloatAttribute();

	double getDoubleAttribute();

	String getStringAttribute();

	FileData getFileData();

	String getFileDataFileName();

	long getFileDataFileLength();

	BitSet getBitSetAttribute();

	byte[] getByteArrayAttribute();

	int[] getIntArrayAttribute();

	long[] getLongArrayAttribute();

	float[] getFloatArrayAttribute();

	double[] getDoubleArrayAttribute();

	String[] getStringArrayAttribute();

	Instant getTimestampAttribute();

	LocalDateTime getDateTimeAttribute();

	LocalDate getDateAttribute();

	LocalTime getTimeAttribute();

	Message getGenericMessageAttribute();

	String getAsString();

	void write(DataOutputStream dos, FileDataWriter fileDataWriter) throws IOException;

	byte[] toBytes() throws IOException;

	byte[] toBytes(FileDataWriter fileDataWriter) throws IOException;

	String explain(int level);
}
