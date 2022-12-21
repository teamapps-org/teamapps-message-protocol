package {package};

import org.teamapps.protocol.message.EnumData;

public enum {type} implements EnumData {

{enums}

	;
	private final int id;

	{type}(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}