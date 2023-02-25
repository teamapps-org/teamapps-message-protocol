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
package org.teamapps.message.protocol.builder;

import org.apache.commons.io.IOUtils;
import org.teamapps.message.protocol.message.Message;
import org.teamapps.message.protocol.model.MessageModel;
import org.teamapps.message.protocol.model.ModelCollection;
import org.teamapps.message.protocol.message.AttributeType;
import org.teamapps.message.protocol.message.MessageDefinition;
import org.teamapps.message.protocol.model.AttributeDefinition;
import org.teamapps.message.protocol.model.EnumDefinition;
import org.teamapps.message.protocol.service.ProtocolServiceBroadcastMethod;
import org.teamapps.message.protocol.service.ProtocolServiceMethod;
import org.teamapps.message.protocol.service.ServiceProtocol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class MessagePojoBuilder {


	public static void createPojos(ModelCollection modelCollection, File directory) throws IOException {
		File dir = directory;
		String namespace = modelCollection.getNamespace();
		for (String name : namespace.split("\\.")) {
			dir = new File(dir, name);
			dir.mkdir();
		}
		System.out.println("Create source in path: " + dir.getPath());
		createServiceClasses(modelCollection, dir);
		createSchemaPojo(modelCollection, dir);
		for (MessageModel model : modelCollection.getModels()) {
			createMessagePojoSave(modelCollection, model, dir);
		}

		for (EnumDefinition enumDefinition : modelCollection.getEnums()) {
			createEnum(modelCollection, enumDefinition, dir);
		}

	}

	private static void createServiceClasses(ModelCollection modelCollection, File directory) throws IOException {
		for (ServiceProtocol serviceProtocol : modelCollection.getProtocolServiceSchemas()) {
			String tpl = readTemplate("protocolService.tpl");
			tpl = setValue(tpl, "package", modelCollection.getNamespace());
			String type = "Abstract" + firstUpperCase(serviceProtocol.getServiceName());
			tpl = setValue(tpl, "type", type);
			tpl = setValue(tpl, "serviceName", serviceProtocol.getServiceName());

			StringBuilder data = new StringBuilder();
			StringBuilder cases = new StringBuilder();
			StringBuilder broadcastCases = new StringBuilder();
			for (ProtocolServiceMethod method : serviceProtocol.getServiceMethods()) {
				String inputMessageName = method.getInputMessage().getName();
				data.append(getTabs(1)).append("public abstract ")
						.append(firstUpperCase(method.getOutputMessage().getName()))
						.append(" ").append(method.getMethodName()).append("(").append(firstUpperCase(inputMessageName)).append(" value);\n\n");
				cases.append(getTabs(3)).append("case \"").append(method.getMethodName()).append("\" -> ").append(method.getMethodName()).append("(").append(firstUpperCase(inputMessageName)).append(".remap(request));\n");
			}

			for (ProtocolServiceBroadcastMethod method : serviceProtocol.getBroadcastMethods()) {
				String inputMessageName = method.getMessage().getName();
				data.append(getTabs(1)).append("public abstract void ")
						.append(method.getMethodName()).append("(").append(firstUpperCase(inputMessageName)).append(" value);\n\n");
				broadcastCases.append(getTabs(3)).append("case \"").append(method.getMethodName()).append("\" -> ").append(method.getMethodName()).append("(").append(firstUpperCase(inputMessageName)).append(".remap(request));\n");
			}


			tpl = setValue(tpl, "methods", data.toString());
			tpl = setValue(tpl, "cases", cases.toString());
			tpl = setValue(tpl, "broadcastCases", broadcastCases.toString());

			File file = new File(directory, type + ".java");
			Files.writeString(file.toPath(), tpl);

			type = firstUpperCase(serviceProtocol.getServiceName()) + "Client";
			tpl = readTemplate("protocolServiceClient.tpl");
			tpl = setValue(tpl, "package", modelCollection.getNamespace());
			tpl = setValue(tpl, "type", type);
			tpl = setValue(tpl, "serviceName", serviceProtocol.getServiceName());

			data = new StringBuilder();
			for (ProtocolServiceMethod method : serviceProtocol.getServiceMethods()) {
				String inputMessageName = method.getInputMessage().getName();
				String outputMessageName = method.getOutputMessage().getName();

				data.append(getTabs(1)).append("public ").append(firstUpperCase(outputMessageName))
						.append(" ").append(method.getMethodName()).append("(").append(firstUpperCase(inputMessageName)).append(" value) {\n");
				data.append(getTabs(2)).append("return executeClusterServiceMethod(\"").append(method.getMethodName()).append("\", value, ").append(firstUpperCase(outputMessageName)).append(".getMessageDecoder());\n");
				data.append(getTabs(1)).append("}\n\n");

				data.append(getTabs(1)).append("public ").append(firstUpperCase(outputMessageName))
						.append(" ").append(method.getMethodName()).append("(").append(firstUpperCase(inputMessageName)).append(" value, String clusterNodeId) {\n");
				data.append(getTabs(2)).append("return executeClusterServiceMethod(clusterNodeId, \"").append(method.getMethodName()).append("\", value, ").append(firstUpperCase(outputMessageName)).append(".getMessageDecoder());\n");
				data.append(getTabs(1)).append("}\n\n");
			}

			for (ProtocolServiceBroadcastMethod method : serviceProtocol.getBroadcastMethods()) {
				String inputMessageName = method.getMessage().getName();
				data.append(getTabs(1)).append("public void ")
						.append(method.getMethodName()).append("(").append(firstUpperCase(inputMessageName)).append(" value) {\n");
				data.append(getTabs(2)).append("executeServiceBroadcast(\"").append(method.getMethodName()).append("\", value);\n");
				data.append(getTabs(1)).append("}\n\n");
			}


			tpl = setValue(tpl, "methods", data.toString());

			file = new File(directory, type + ".java");
			Files.writeString(file.toPath(), tpl);
		}

	}

	private static void createSchemaPojo(ModelCollection modelCollection, File directory) throws IOException {
		String tpl = readTemplate("messageCollection.tpl");
		tpl = setValue(tpl, "package", modelCollection.getNamespace());
		tpl = setValue(tpl, "type", firstUpperCase(modelCollection.getName()));
		tpl = setValue(tpl, "version", "" + modelCollection.getVersion());
		tpl = setValue(tpl, "name", modelCollection.getName());

		StringBuilder data = new StringBuilder();
		StringBuilder registry = new StringBuilder();

		for (MessageModel model : modelCollection.getModels()) {
			String objName = model.getName();
			data.append(getTabs(2))
					.append("MessageDefinition ")
					.append(objName)
					.append(" = MODEL_COLLECTION.createModel(")
					.append(withQuotes(objName)).append(", ")
					.append(withQuotes(model.getObjectUuid())).append(", ")
					.append(model.getModelVersion()).append(", ")
					.append(base64EncodedReader(model.getSpecificType())).append(", ")
					.append("" + model.isMessageRecord())
					.append(");\n");
		}
		data.append("\n");

		for (EnumDefinition enumDefinition : modelCollection.getEnums()) {
			String objName = enumDefinition.getName();
			data.append(getTabs(2))
					.append("EnumDefinition ")
					.append(objName)
					.append(" = MODEL_COLLECTION.createEnum(")
					.append(withQuotes(objName)).append(", ");

			for (int i = 0; i < enumDefinition.getEnumValues().size(); i++) {
				String value = enumDefinition.getEnumValues().get(i);
				if (i > 0) {
					data.append(", ");
				}
				data.append(withQuotes(createConstantName(value)));
			}
			data.append(");\n");
		}
		data.append("\n");

		for (MessageModel model : modelCollection.getModels()) {
			String objName = model.getName();
			for (AttributeDefinition propDef : model.getAttributeDefinitions()) {
				if (model.isMessageRecord() && MessageDefinition.RESERVED_NAMES_LOWER_CASE.contains(propDef.getName().toLowerCase())) {
					continue;
				}
				if (propDef.isReferenceProperty()) {
					String method = propDef.getType() == AttributeType.OBJECT_SINGLE_REFERENCE ? "addSingleReference" : "addMultiReference";
					data.append(getTabs(2))
							.append(objName)
							.append(".").append(method).append("(")
							.append(withQuotes(propDef.getName())).append(", ")
							.append(propDef.getKey()).append(", ")
							.append(base64EncodedReader(propDef.getSpecificType())).append(", ")
							.append(propDef.getReferencedObject().getName())
							.append(");\n");

				} else if (propDef.isEnumProperty()){
					data.append(getTabs(2))
							.append(objName)
							.append(".addEnum(")
							.append(withQuotes(propDef.getName())).append(", ")
							.append(propDef.getEnumDefinition().getName()).append(", ")
							.append(propDef.getKey()).append(", ")
							.append(base64EncodedReader(propDef.getSpecificType()))
							.append(");\n");
				} else {
					data.append(getTabs(2))
							.append(objName)
							.append(".addAttribute(")
							.append(withQuotes(propDef.getName())).append(", ")
							.append(propDef.getKey()).append(", ")
							.append("AttributeType.").append(propDef.getType()).append(", ")
							.append(base64EncodedReader(propDef.getSpecificType())).append(", ")
							.append(propDef.getDefaultValue() != null ? withQuotes(propDef.getDefaultValue()) : null).append(", ")
							.append(propDef.getComment() != null ? withQuotes(propDef.getComment()) : null)
							.append(");\n");
				}

			}
			data.append("\n");
			registry.append(getTabs(2))
					.append("MODEL_COLLECTION.addMessageDecoder(")
					.append(objName).append(".getObjectUuid(), ")
					.append(firstUpperCase(objName))
					.append(".getMessageDecoder());\n");
		}


		tpl = setValue(tpl, "data", data.toString());
		tpl = setValue(tpl, "registry", registry.toString());

		File file = new File(directory, firstUpperCase(modelCollection.getName()) + ".java");
		Files.writeString(file.toPath(), tpl);
	}

	private static void createMessagePojoSave(ModelCollection modelCollection, MessageModel field, File directory) {
		try {
			createMessagePojo(modelCollection, field, directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createEnum(ModelCollection modelCollection, EnumDefinition enumDefinition, File directory) throws IOException {
		String tpl = readTemplate("enum.tpl");
		tpl = setValue(tpl, "package", modelCollection.getNamespace());
		tpl = setValue(tpl, "type", firstUpperCase(enumDefinition.getName()));
		StringBuilder data = new StringBuilder();

		for (int i = 0; i < enumDefinition.getEnumValues().size(); i++) {
			String value = enumDefinition.getEnumValues().get(i);
			data.append(getTabs(1)).append(createConstantName(value)).append("(").append(i + 1).append("),\n");
		}

		tpl = setValue(tpl, "enums", data.toString());
		File file = new File(directory, firstUpperCase(enumDefinition.getName()) + ".java");
		Files.writeString(file.toPath(), tpl);
		System.out.println("Write enum:" + file.getPath());
	}

	private static void createMessagePojo(ModelCollection modelCollection, MessageModel model, File directory) throws IOException {
		String tpl = readTemplate("messagePojo.tpl");
		tpl = setValue(tpl, "package", modelCollection.getNamespace());
		tpl = setValue(tpl, "type", firstUpperCase(model.getName()));
		tpl = setValue(tpl, "schema", firstUpperCase(modelCollection.getName()));
		tpl = setValue(tpl, "version", "" + model.getModelVersion());
		tpl = setValue(tpl, "uuid", model.getObjectUuid());
		StringBuilder data = new StringBuilder();

		for (AttributeDefinition propDef : model.getAttributeDefinitions()) {
			if (model.isMessageRecord() && MessageDefinition.RESERVED_NAMES_LOWER_CASE.contains(propDef.getName().toLowerCase())) {
				continue;
			} else if (propDef.getType() == AttributeType.ENUM) {
				data.append(getTabs(1))
						.append("public ")
						.append(getReturnType(propDef))
						.append(" ").append("get")
						.append(firstUpperCase(propDef.getName())).append("() {\n")
						.append(getTabs(2))
						.append("int id = get").append(getGetterSetterMethodName(propDef)).append("(").append(withQuotes(propDef.getName())).append(");\n")
						.append(getTabs(2))
						.append("return id > 0 ? ").append(firstUpperCase(propDef.getEnumDefinition().getName())).append(".values()[id - 1] : null;\n")
						.append(getTabs(1))
						.append("}\n\n");

				data.append(getTabs(1))
						.append("public ")
						.append(firstUpperCase(model.getName())).append(" ")
						.append("set")
						.append(firstUpperCase(propDef.getName())).append("(")
						.append(getReturnType(propDef)).append(" value) {\n")
						.append(getTabs(2))
						.append("set").append(getGetterSetterMethodName(propDef)).append("(")
						.append(withQuotes(propDef.getName())).append(", value != null ? value.getId() : null);\n")
						.append(getTabs(2))
						.append("return this;\n")
						.append(getTabs(1))
						.append("}\n\n");
				continue;
			}
			String objectReferenceWithType = propDef.isReferenceProperty() ? "AsType" : "";
			data.append(getTabs(1))
					.append("public ")
					.append(getReturnType(propDef))
					.append(" ").append(propDef.getType() == AttributeType.BOOLEAN ? "is" : "get")
					.append(firstUpperCase(propDef.getName())).append("() {\n")
					.append(getTabs(2))
					.append("return get").append(getGetterSetterMethodName(propDef)).append(objectReferenceWithType).append("(")
					.append(withQuotes(propDef.getName())).append(");\n")
					.append(getTabs(1))
					.append("}\n\n");

			data.append(getTabs(1))
					.append("public ")
					.append(firstUpperCase(model.getName())).append(" ")
					.append("set")
					.append(firstUpperCase(propDef.getName())).append("(")
					.append(getReturnType(propDef)).append(" value) {\n")
					.append(getTabs(2))
					.append("set").append(getGetterSetterMethodName(propDef)).append(objectReferenceWithType).append("(")
					.append(withQuotes(propDef.getName())).append(", value);\n")
					.append(getTabs(2))
					.append("return this;\n")
					.append(getTabs(1))
					.append("}\n\n");

			if (propDef.getType() == AttributeType.FILE) {
				data.append(getTabs(1))
						.append("public File get")
						.append(firstUpperCase(propDef.getName())).append("AsFile").append("() {\n")
						.append(getTabs(2))
						.append("return get").append("File").append("(")
						.append(withQuotes(propDef.getName())).append(");\n")
						.append(getTabs(1))
						.append("}\n\n");

				data.append(getTabs(1))
						.append("public String get")
						.append(firstUpperCase(propDef.getName())).append("AsFileName").append("() {\n")
						.append(getTabs(2))
						.append("return get").append("FileDataFileName").append("(")
						.append(withQuotes(propDef.getName())).append(");\n")
						.append(getTabs(1))
						.append("}\n\n");

				data.append(getTabs(1))
						.append("public long get")
						.append(firstUpperCase(propDef.getName())).append("AsFileLength").append("() {\n")
						.append(getTabs(2))
						.append("return get").append("FileDataFileLength").append("(")
						.append(withQuotes(propDef.getName())).append(");\n")
						.append(getTabs(1))
						.append("}\n\n");


				data.append(getTabs(1))
						.append("public ")
						.append(firstUpperCase(model.getName())).append(" ")
						.append("set")
						.append(firstUpperCase(propDef.getName())).append("(")
						.append("File file) {\n")
						.append(getTabs(2))
						.append("set").append(getGetterSetterMethodName(propDef)).append("(")
						.append(withQuotes(propDef.getName())).append(", file);\n")
						.append(getTabs(2))
						.append("return this;\n")
						.append(getTabs(1))
						.append("}\n\n");

				data.append(getTabs(1))
						.append("public ")
						.append(firstUpperCase(model.getName())).append(" ")
						.append("set")
						.append(firstUpperCase(propDef.getName())).append("(")
						.append("File file, String fileName) {\n")
						.append(getTabs(2))
						.append("set").append(getGetterSetterMethodName(propDef)).append("(")
						.append(withQuotes(propDef.getName())).append(", file, fileName);\n")
						.append(getTabs(2))
						.append("return this;\n")
						.append(getTabs(1))
						.append("}\n\n");
			}

			if (propDef.getType() == AttributeType.OBJECT_MULTI_REFERENCE) {
				data.append(getTabs(1))
						.append("public ")
						.append(firstUpperCase(model.getName())).append(" ")
						.append("add")
						.append(firstUpperCase(propDef.getName())).append("(")
						.append(firstUpperCase(propDef.getReferencedObject().getName())).append(" value) {\n")
						.append(getTabs(2))
						.append("addReference").append("(")
						.append(withQuotes(propDef.getName())).append(", value);\n")
						.append(getTabs(2))
						.append("return this;\n")
						.append(getTabs(1))
						.append("}\n\n");
			}

			if (propDef.getType() == AttributeType.STRING_ARRAY) {
				//todo getter and setter with List<String> --> getXxxAsList
			}
		}

		tpl = setValue(tpl, "methods", data.toString());
		File file = new File(directory, firstUpperCase(model.getName()) + ".java");
		Files.writeString(file.toPath(), tpl);
		System.out.println("Write pojo:" + file.getPath());
	}

	private static String readTemplate(String name) throws IOException {
		InputStream inputStream = MessagePojoBuilder.class.getResourceAsStream("/org/teamapps/message/templates/" + name);
		return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
	}

	private static String setValue(String template, String name, String value) {
		return template.replace("{" + name + "}", value);
	}

	private static String firstUpperCase(String value) {
		return value.substring(0, 1).toUpperCase() + value.substring(1);
	}

	private static String createConstantName(String s) {
		if (isConstant(s)) {
			return s;
		} else {
			return s.replaceAll("(.)(\\p{Upper})", "$1_$2").toUpperCase();
		}
	}

	private static boolean isConstant(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c != '_' && !Character.isUpperCase(c) && !Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	private static String getTabs(int count) {
		return "\t".repeat(count);
	}

	private static String withQuotes(String value) {
		return value != null ? "\"" + value + "\"" : "null";
	}

	private static String base64EncodedReader(Message message) {
		try {
			return message == null ? "null" : "readBase64Message(\"" + Base64.getEncoder().encodeToString(message.toBytes()) + "\")";
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getReturnType(AttributeDefinition propDef) {
		return switch (propDef.getType()) {
			case OBJECT -> firstUpperCase(propDef.getName());
			case OBJECT_SINGLE_REFERENCE -> firstUpperCase(propDef.getReferencedObject().getName());
			case OBJECT_MULTI_REFERENCE -> "List<" + firstUpperCase(propDef.getReferencedObject().getName()) + ">";
			case BOOLEAN -> "boolean";
			case BYTE -> "byte";
			case INT -> "int";
			case LONG -> "long";
			case FLOAT -> "float";
			case DOUBLE -> "double";
			case STRING -> "String";
			case BITSET -> "BitSet";
			case BYTE_ARRAY -> "byte[]";
			case INT_ARRAY -> "int[]";
			case LONG_ARRAY -> "long[]";
			case FLOAT_ARRAY -> "float[]";
			case DOUBLE_ARRAY -> "double[]";
			case STRING_ARRAY -> "String[]";
			case FILE -> "FileData";
			case ENUM -> firstUpperCase(propDef.getEnumDefinition().getName());
			case TIMESTAMP_32 -> "Instant";
			case TIMESTAMP_64 -> "Instant";
			case DATE_TIME -> "LocalDateTime";
			case DATE -> "LocalDate";
			case TIME -> "LocalTime";
			case GENERIC_MESSAGE -> "Message";
		};
	}

	private static String getGetterSetterMethodName(AttributeDefinition propDef) {
		return switch (propDef.getType()) {
			case OBJECT -> "Message";
			case OBJECT_SINGLE_REFERENCE -> "ReferencedObject";
			case OBJECT_MULTI_REFERENCE -> "ReferencedObjects";
			case BOOLEAN -> "BooleanAttribute";
			case BYTE -> "ByteAttribute";
			case INT -> "IntAttribute";
			case LONG -> "LongAttribute";
			case FLOAT -> "FloatAttribute";
			case DOUBLE -> "DoubleAttribute";
			case STRING -> "StringAttribute";
			case BITSET -> "BitSetAttribute";
			case BYTE_ARRAY -> "ByteArrayAttribute";
			case INT_ARRAY -> "IntArrayAttribute";
			case LONG_ARRAY -> "LongArrayAttribute";
			case FLOAT_ARRAY -> "FloatArrayAttribute";
			case DOUBLE_ARRAY -> "DoubleArrayAttribute";
			case STRING_ARRAY -> "StringArrayAttribute";
			case FILE -> "FileData";
			case ENUM -> "IntAttribute";
			case TIMESTAMP_32 -> "TimestampAttribute";
			case TIMESTAMP_64 -> "TimestampAttribute";
			case DATE_TIME -> "DateTimeAttribute";
			case DATE -> "DateAttribute";
			case TIME -> "TimeAttribute";
			case GENERIC_MESSAGE -> "GenericMessageAttribute";
		};
	}


}
