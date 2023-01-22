package org.teamapps.message.protocol.xml;

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
		if (node.isValue()) {
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

	private void printObject(int level, String name, boolean closeTag, StringBuilder sb) {
		printTabs(level, sb);
		printTag(name, closeTag, sb);
		sb.append("\n");
	}

	private void printValue(int level, XmlNode node, StringBuilder sb) {
		printTabs(level, sb);
		printTag(node.getName(), false, sb);
		sb.append(node.getValue()); //todo escape for xml!
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
