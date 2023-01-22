package org.teamapps.message.protocol.file;

public enum FileDataType {

	LOCAL_FILE(1),
	CLUSTER_STORE(2),

	;
	private final int id;

	FileDataType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static FileDataType getById(int id) {
		return switch (id) {
			case 1 -> LOCAL_FILE;
			case 2 -> CLUSTER_STORE;
			default -> null;
		};
	}
}
