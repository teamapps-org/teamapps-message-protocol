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
package org.teamapps.protocol.maven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamapps.protocol.builder.MessagePojoBuilder;
import org.teamapps.protocol.model.ModelCollection;
import org.teamapps.protocol.model.ModelCollectionProvider;

import java.io.File;
import java.lang.invoke.MethodHandles;

public class ProtocolApiGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 2) {
			LOGGER.error("Error: missing argument(s). Mandatory arguments are: modelClassName targetPath");
			System.exit(1);
		}
		String schemaClassName = args[0];
		String targetPath = args[1];

		Class<?> schemaClass = Class.forName(schemaClassName);
		ModelCollectionProvider modelCollectionProvider = (ModelCollectionProvider) schemaClass.getConstructor().newInstance();
		ModelCollection modelCollection = modelCollectionProvider.getModelCollection();

		File basePath = new File(targetPath);
		if (!basePath.getParentFile().exists() && basePath.getParentFile().getParentFile().exists()) {
			basePath.getParentFile().mkdir();
		}
		basePath.mkdir();
		MessagePojoBuilder.createPojos(modelCollection, new File(targetPath));
	}
}
