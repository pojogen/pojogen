package com.pojogen.internal;

import javax.lang.model.type.TypeKind;

class DeclaredType implements Type {

	private final String typeName;

	DeclaredType(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public String toString() {
		return typeName;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.DECLARED;
	}

}
