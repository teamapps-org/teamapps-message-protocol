package org.teamapps.message.protocol.message;

import org.teamapps.message.protocol.model.EnumDefinition;
import org.teamapps.message.protocol.model.MessageModel;

import java.util.HashMap;
import java.util.Map;

public class DefinitionCache {

	private final Map<String, MessageModel> modelCache = new HashMap<>();
	private final Map<String, EnumDefinition> enumCache = new HashMap<>();

	public void addModel(MessageModel model) {
		modelCache.put(model.getObjectUuid(), model);
	}

	public boolean containsModel(MessageModel model) {
		return modelCache.containsKey(model.getObjectUuid());
	}

	public MessageModel getModel(String uuid) {
		return modelCache.get(uuid);
	}

	public void addEnum(EnumDefinition enumDefinition) {
		enumCache.put(enumDefinition.getName(), enumDefinition);
	}

	public EnumDefinition getEnum(String name) {
		return enumCache.get(name);
	}

	public boolean containsEnum(EnumDefinition enumDefinition) {
		return enumCache.containsKey(enumDefinition.getName());
	}
}
