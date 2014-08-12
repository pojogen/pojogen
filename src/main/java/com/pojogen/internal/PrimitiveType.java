package com.pojogen.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.lang.model.type.TypeKind;

class PrimitiveType implements Type {

	private final Class<?> primitiveClass;

	PrimitiveType(Class<?> primitiveClass) {
		checkNotNull(primitiveClass);
		checkArgument(primitiveClass.isPrimitive());
		this.primitiveClass = primitiveClass;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.valueOf(toString().toUpperCase());
	}

	@Override
	public String toString() {
		return primitiveClass.getSimpleName();
	}
}
