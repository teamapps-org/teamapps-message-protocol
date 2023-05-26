package org.teamapps.message.protocol.model;

import org.teamapps.message.protocol.message.Message;

public interface ExtendedAttributesUpdater {

	ExtendedAttributesUpdater setDefaultValue(String defaultValue);
	ExtendedAttributesUpdater setComment(String comment);
	ExtendedAttributesUpdater setSpecificType(Message specificType);
}
