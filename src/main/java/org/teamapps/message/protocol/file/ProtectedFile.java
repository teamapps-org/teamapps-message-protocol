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
