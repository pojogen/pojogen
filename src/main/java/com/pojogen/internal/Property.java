package com.pojogen.internal;

import static com.google.common.base.Preconditions.checkNotNull;

final class Property {

	private final String name;
	private final Type type;

	Property(String name, Type type) {
		this.name = checkNotNull(name);
		this.type = checkNotNull(type);
	}

	String getName() {
		return name;
	}

	Type getType() {
		return type;
	}
}
