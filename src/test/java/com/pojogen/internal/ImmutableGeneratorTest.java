package com.pojogen.internal;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.tools.JavaFileObject;

import org.junit.Test;

import com.google.common.base.Throwables;
import com.google.testing.compile.JavaFileObjects;
import com.pojogen.api.apt.PojoGenAnnotationProcessor;

public class ImmutableGeneratorTest {

	@Test
	public void testOhlcGeneratesImmutableOhlc() {
		ASSERT.about(javaSource()).that(source("Ohlc.java")).processedWith(new PojoGenAnnotationProcessor()).compilesWithoutError().and()
				.generatesSources(source("ImmutableOhlc.java"));
	}

	private JavaFileObject source(String filename) {
		// Support for expected sources with file: URLs is clumsy in
		// compile-testing 0.5:
		// https://github.com/google/compile-testing/issues/39
		//
		// Expected sources are packaged in JAR to use jar: URLs instead.
		String jarPath = System.getProperty("test.jar");
		try {
			String jarURL = "jar:" + new File(jarPath).toURI().toURL();
			String packageDirectory = getClass().getPackage().getName().replace(".", "/");
			String resourceName = packageDirectory + "/" + filename;
			URL resourceURL = new URL(jarURL + "!/" + resourceName);
			return JavaFileObjects.forResource(resourceURL);
		} catch (MalformedURLException e) {
			throw Throwables.propagate(e);
		}
	}
}
