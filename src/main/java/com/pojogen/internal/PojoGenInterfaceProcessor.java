package com.pojogen.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.Introspector;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Throwables;
import com.pojogen.api.annotation.PojoGen;

public class PojoGenInterfaceProcessor {

	private final ProcessingEnvironment processingEnv;
	private final TypeElement pojogenInterface;

	public PojoGenInterfaceProcessor(ProcessingEnvironment processingEnv, TypeElement pojogenInterface) {
		this.processingEnv = checkNotNull(processingEnv);
		this.pojogenInterface = checkNotNull(pojogenInterface);
		checkArgument(pojogenInterface.getKind() == ElementKind.INTERFACE);
	}

	public void process() {
		String packageName = processingEnv.getElementUtils().getPackageOf(pojogenInterface).getQualifiedName().toString();
		String interfaceName = pojogenInterface.getSimpleName().toString();
		String immutableClassName = String.format("Immutable%s", interfaceName);
		ImmutableGenerator immutableGenerator = new ImmutableGenerator(packageName, interfaceName, immutableClassName,
				getInterfaceProperties());
		String immutableClass = immutableGenerator.generateClass();

		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, immutableClass);

		try {
			JavaFileObject javaFile = processingEnv.getFiler().createSourceFile(String.format("%s.%s", packageName, immutableClassName));
			Writer writer = javaFile.openWriter();
			try {
				writer.write(immutableClass);
			} finally {
				writer.close();
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	private Collection<Property> getInterfaceProperties() {
		List<Property> properties = new ArrayList<>();
		for (ExecutableElement method : getMethods()) {
			if (isGetter(method)) {
				properties.add(getPropertyFromGetter(method));
			} else {
				reportErrorNotGetter(method);
			}
		}
		return properties;
	}

	private List<ExecutableElement> getMethods() {
		return ElementFilter.methodsIn(pojogenInterface.getEnclosedElements());
	}

	private boolean isGetter(ExecutableElement method) {
		if (!method.getParameters().isEmpty()) {
			return false;
		}
		if (!isNonEmptyType(method.getReturnType())) {
			return false;
		}
		if (!isGetterName(method.getSimpleName()))
			return false;
		return true;
	}

	private Property getPropertyFromGetter(ExecutableElement method) {
		return new Property(getPropertyName(method.getSimpleName()), new MirrorType(method.getReturnType()));
	}

	private boolean isNonEmptyType(TypeMirror type) {
		return type.getKind() != TypeKind.NONE && type.getKind() != TypeKind.NULL && type.getKind() != TypeKind.VOID;
	}

	private boolean isGetterName(Name methodName) {
		String methodNameString = methodName.toString();
		return methodNameString.startsWith("get") || methodNameString.startsWith("is");
	}

	private String getPropertyName(Name getterName) {
		String propertyName = StringUtils.removeStart(getterName.toString(), "get");
		propertyName = StringUtils.removeStart(propertyName, "is");
		return Introspector.decapitalize(propertyName);
	}

	private void reportErrorNotGetter(ExecutableElement method) {
		processingEnv.getMessager().printMessage(
				Diagnostic.Kind.ERROR,
				"Method " + method.getSimpleName() + " is not a getter. " + PojoGen.class.getName()
						+ " can only be applied to interfaces composed of getters", method);
	}
}
