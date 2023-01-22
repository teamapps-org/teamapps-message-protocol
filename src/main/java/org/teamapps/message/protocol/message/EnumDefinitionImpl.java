package org.teamapps.message.protocol.message;

import org.teamapps.message.protocol.model.EnumDefinition;

import java.util.List;

public class EnumDefinitionImpl implements EnumDefinition {

	private final String name;
	private final List<String> values;

	public EnumDefinitionImpl(String name, List<String> values) {
		this.name = name;
		this.values = values;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getEnumValues() {
		return values;
	}
}
