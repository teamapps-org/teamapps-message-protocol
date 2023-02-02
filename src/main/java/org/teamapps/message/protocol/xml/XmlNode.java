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

import java.util.ArrayList;
import java.util.List;

public class XmlNode {

	private String name;
	private String value;
	private List<XmlNode> children = new ArrayList<>();


	public XmlNode(String name) {
		this.name = name;
	}

	public XmlNode(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public boolean isValue() {
		return value != null;
	}

	public void addChild(XmlNode node) {
		children.add(node);
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public List<XmlNode> getChildren() {
		return children;
	}
}
