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
