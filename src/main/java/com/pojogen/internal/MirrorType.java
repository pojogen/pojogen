package com.pojogen.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class MirrorType implements Type {

	private final TypeMirror delegate;

	MirrorType(TypeMirror delegate) {
		this.delegate = checkNotNull(delegate);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public TypeKind getKind() {
		return delegate.getKind();
	}

}
