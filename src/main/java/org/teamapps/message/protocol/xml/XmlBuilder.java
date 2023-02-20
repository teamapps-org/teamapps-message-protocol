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

import org.apache.commons.lang.StringEscapeUtils;

public class XmlBuilder {

	private final XmlNode rootNode;

	public XmlBuilder(XmlNode rootNode) {
		this.rootNode = rootNode;
	}

	public String getXml() {
		StringBuilder sb = new StringBuilder();
		printXml(0, rootNode, sb);
		return sb.toString();
	}

	private void printXml(int level, XmlNode node, StringBuilder sb) {
		printComment(level, node.getComment(), sb);
		if (node.getDefaultValue() != null && node.getValue() == null && node.getChildren().isEmpty()) {
			printDefaultValue(level, node, sb);
		} else if (node.isValue()) {
			printValue(level, node, sb);
		} else {
			printObject(level, node.getName(), false, sb);
			for (XmlNode child : node.getChildren()) {
				printXml(level + 1, child, sb);
			}
			printObject(level, node.getName(), true, sb);
		}
	}

	private void printTabs(int level, StringBuilder sb) {
		for (int i = 0; i < level; i++) {
			sb.append("\t");
		}
	}

	private void printComment(int level, String comment, StringBuilder sb) {
		if (comment != null) {
			printTabs(level, sb);
			sb.append("<!-- ").append(comment).append(" ->\n");
		}
	}

	private void printObject(int level, String name, boolean closeTag, StringBuilder sb) {
		printTabs(level, sb);
		printTag(name, closeTag, sb);
		sb.append("\n");
	}

	private void printDefaultValue(int level, XmlNode node, StringBuilder sb) {
		printTabs(level, sb);
		sb.append("<!-- ");
		printTag(node.getName(), false, sb);
		sb.append(StringEscapeUtils.escapeXml(node.getDefaultValue()));
		printTag(node.getName(), true, sb);
		sb.append(" -->");
		sb.append("\n");
	}

	private void printValue(int level, XmlNode node, StringBuilder sb) {
		printTabs(level, sb);
		printTag(node.getName(), false, sb);
		sb.append(StringEscapeUtils.escapeXml(node.getValue()));
		printTag(node.getName(), true, sb);
		sb.append("\n");
	}

	private void printTag(String name, boolean closeTag, StringBuilder sb) {
		sb.append("<");
		if (closeTag) {
			sb.append("/");
		}
		sb.append(name).append(">");
	}
}
