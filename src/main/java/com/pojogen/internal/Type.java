package com.pojogen.internal;

import javax.lang.model.type.TypeKind;

interface Type {
	@Override
	String toString();

	TypeKind getKind();
}
