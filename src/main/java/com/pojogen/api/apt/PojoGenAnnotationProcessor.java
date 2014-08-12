package com.pojogen.api.apt;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

import com.pojogen.api.annotation.PojoGen;
import com.pojogen.internal.PojoGenInterfaceProcessor;

@SupportedAnnotationTypes("com.pojogen.api.annotation.PojoGen")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class PojoGenAnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!shouldProcess(annotations))
			return false;
		if (!shouldProcess(roundEnv))
			return false;

		for (TypeElement pojogenType : getPojogenTypes(roundEnv)) {
			processPojoGenType(pojogenType);
		}
		return false;
	}

	private void processPojoGenType(TypeElement pojogenType) {
		if (pojogenType.getKind() == ElementKind.INTERFACE) {
			processPojoGenInterface(pojogenType);
		} else {
			reportErrorNotInterface(pojogenType);
		}
	}

	private void processPojoGenInterface(TypeElement pojogenInterface) {
		new PojoGenInterfaceProcessor(processingEnv, pojogenInterface).process();
	}

	private boolean shouldProcess(RoundEnvironment roundEnv) {
		return !roundEnv.processingOver();
	}

	private boolean shouldProcess(Set<? extends TypeElement> annotations) {
		for (TypeElement annotation : annotations) {
			if (typeElementMatches(annotation, PojoGen.class)) {
				return true;
			}
		}
		return false;
	}

	private boolean typeElementMatches(TypeElement typeElement, Class<?> expectedClass) {
		return typeElement.getQualifiedName().contentEquals(expectedClass.getName());
	}

	private Set<? extends TypeElement> getPojogenTypes(RoundEnvironment roundEnv) {
		Set<? extends TypeElement> pojogenTypes = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(PojoGen.class));
		checkArgument(!pojogenTypes.isEmpty());
		return pojogenTypes;
	}

	private void reportErrorNotInterface(TypeElement pojogenType) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
				"Annotation " + PojoGen.class.getName() + " can only be applied to interfaces", pojogenType);
	}
}
