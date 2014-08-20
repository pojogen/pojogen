# pojogen: POJO Generator

Much ceremony is required to [write correct and effective Java](https://github.com/mgp/book-notes/blob/master/effective-java-2nd-edition.markdown).
pojogen is a [Java annotation processor](http://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/Processor.html) that generates [POJO](http://en.wikipedia.org/wiki/Plain_Old_Java_Object) source code and lets Java developers focus on creative work, not boilerplate.

[![Build Status](https://travis-ci.org/pojogen/pojogen.svg?branch=master)](https://travis-ci.org/pojogen/pojogen)

## Building
To build pojogen, run the following at the root of the checkout:

    gradle assemble
    
## Installing
To use pojogen in your Java project, configure your Java compiler settings with the following:
* Add com.pojogen.api.apt.PojoGenAnnotationProcessor as a Java annotation processor
* Add the built pojogen.jar and its [dependencies](build.gradle) to the compiler classpath

The following example illustrates an installation of pojogen:
* [Example: Gradle-based project using development version of pojogen](https://github.com/pojogen/pojogen-example-gradle-dev)

## Supported Platforms

pojogen has been tested on the following operating systems:
* Ubuntu 12.04 LTS 64-bit (continuous integration)

pojogen has been tested on the following JDKs:
* OpenJDK 7 (continuous integration)
* Oracle JDK 7 (continuous integration)

Please let us know if you are using pojogen in other environments. You are welcome to submit pull requests to add support for further environments.

## Usage
pojogen processes interfaces with a [@Pojogen annotation](src/main/java/com/pojogen/api/annotation/PojoGen.java). Such interfaces must be composed only of getter methods (no arguments, non-void return value). Each getter is interpreted as corresponding to a property of implementing objects. For these interfaces, pojogen generates classes with the following code generator.

### ImmutableGenerator
This generates an immutable implementation of the interface with:
* A constructor with one argument per property
* One getter method per property (as specified by the interface)
* equals() and hashCode() methods based on all properties 
For example, this generates class [ImmutableOhlc](src/test/resources/com/pojogen/internal/ImmutableOhlc.java) from interface [Ohlc](src/test/resources/com/pojogen/internal/Ohlc.java).

## License

See the [LICENSE](LICENSE) file for licensing information.


## Support

Please log tickets and issues at https://github.com/pojogen/pojogen/issues
