package com.pojogen.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.ImmutableList;

class ImmutableGenerator {

	private final String packageName;
	private final String interfaceName;
	private final String className;
	private final ImmutableList<Property> properties;

	ImmutableGenerator(String packageName, String interfaceName, String className, Iterable<Property> properties) {
		this.packageName = checkNotNull(packageName);
		this.interfaceName = checkNotNull(interfaceName);
		this.className = checkNotNull(className);
		this.properties = ImmutableList.copyOf(properties);
	}

	@SuppressWarnings("unchecked")
	String generateClass() {
		JdtCodeGenerator codeGenerator = new JdtCodeGenerator();

		codeGenerator.setPackage(packageName);

		Type interfaceType = new DeclaredType(interfaceName);
		TypeDeclaration classDeclaration = codeGenerator.newClassDeclaration(codeGenerator.newModifiers(Modifier.FINAL), className,
				interfaceType);

		MethodDeclaration constructorDeclaration = codeGenerator.newConstructorDeclaration(className);
		Block constructorBlock = codeGenerator.newBlock();

		String objName = "obj";
		SingleVariableDeclaration equalsParameter = codeGenerator.newParameterDeclaration(new DeclaredType("Object"), objName);
		MethodDeclaration equalsDeclaration = codeGenerator.newMethodDeclaration(codeGenerator.newModifiers(Modifier.PUBLIC),
				new PrimitiveType(Boolean.TYPE), "equals", equalsParameter);

		Block equalsBlock = codeGenerator.newBlock();
		Expression nullLiteral = codeGenerator.newNullLiteral();
		Expression objEqualsNull = newVariableEquals(codeGenerator, objName, nullLiteral);
		Statement returnFalse = newReturnBoolean(codeGenerator, false);
		Statement ifObjEqualsNull = codeGenerator.newIfStatement(objEqualsNull, returnFalse);
		equalsBlock.statements().add(ifObjEqualsNull);

		Expression thisObj = codeGenerator.newThisExpression();
		Expression objEqualsThis = newVariableEquals(codeGenerator, objName, thisObj);
		Statement returnTrue = newReturnBoolean(codeGenerator, true);
		Statement ifObjEqualsThis = codeGenerator.newIfStatement(objEqualsThis, returnTrue);
		equalsBlock.statements().add(ifObjEqualsThis);

		Expression objGetClass = codeGenerator.newMethodInvocation(codeGenerator.newSimpleName(objName), "getClass");
		Expression thisGetClass = codeGenerator.newMethodInvocation(codeGenerator.newThisExpression(), "getClass");
		Expression classesNotEqual = codeGenerator.newInfixExpression(objGetClass, InfixExpression.Operator.NOT_EQUALS, thisGetClass);
		returnFalse = newReturnBoolean(codeGenerator, false);
		Statement ifClassesNotEqual = codeGenerator.newIfStatement(classesNotEqual, returnFalse);
		equalsBlock.statements().add(ifClassesNotEqual);

		String rhsName = "rhs";
		Expression rhsDeclaration = codeGenerator.newVariableDeclaration(new DeclaredType(className), rhsName);
		Expression castToThisClass = codeGenerator.newCast(className, codeGenerator.newSimpleName(objName));
		Statement castAssignment = codeGenerator.newAssignment(rhsDeclaration, castToThisClass);
		equalsBlock.statements().add(castAssignment);

		Expression newEqualsBuilder = codeGenerator.newInstanceCreation("org.apache.commons.lang3.builder.EqualsBuilder");
		Expression equalsBuilderChain = newEqualsBuilder;

		MethodDeclaration hashCodeDeclaration = codeGenerator.newMethodDeclaration(codeGenerator.newModifiers(Modifier.PUBLIC),
				new PrimitiveType(Integer.TYPE), "hashCode");

		Block hashCodeBlock = codeGenerator.newBlock();
		Expression newHashCodeBuilder = codeGenerator.newInstanceCreation("org.apache.commons.lang3.builder.HashCodeBuilder");
		Expression hashCodeBuilderChain = newHashCodeBuilder;

		for (Property property : properties) {
			String propertyName = property.getName();
			Type propertyType = property.getType();

			FieldDeclaration fieldDeclaration = codeGenerator.newFieldDeclaration(codeGenerator.newModifiers(Modifier.PRIVATE),
					propertyType, propertyName);
			classDeclaration.bodyDeclarations().add(fieldDeclaration);

			SingleVariableDeclaration constructorParameter = codeGenerator.newParameterDeclaration(propertyType, propertyName);
			constructorDeclaration.parameters().add(constructorParameter);

			Expression fieldAccess = newThisFieldAccess(codeGenerator, propertyName);
			Statement setterStatement = codeGenerator.newAssignment(fieldAccess, codeGenerator.newSimpleName(propertyName));
			constructorBlock.statements().add(setterStatement);

			Expression thisFieldAccess = newThisFieldAccess(codeGenerator, propertyName);
			Expression rhsFieldAccess = codeGenerator.newFieldAccess(codeGenerator.newSimpleName(rhsName), propertyName);
			equalsBuilderChain = codeGenerator.newMethodInvocation(equalsBuilderChain, "append", thisFieldAccess, rhsFieldAccess);

			Expression hashFieldAccess = newThisFieldAccess(codeGenerator, propertyName);
			hashCodeBuilderChain = codeGenerator.newMethodInvocation(hashCodeBuilderChain, "append", hashFieldAccess);

			MethodDeclaration methodDeclaration = codeGenerator.newMethodDeclaration(codeGenerator.newModifiers(Modifier.PUBLIC),
					propertyType, String.format("get%s", StringUtils.capitalize(propertyName)));

			Block methodBlock = codeGenerator.newBlock();
			fieldAccess = newThisFieldAccess(codeGenerator, propertyName);
			Statement methodReturn = codeGenerator.newReturnStatement(fieldAccess);
			methodBlock.statements().add(methodReturn);
			methodDeclaration.setBody(methodBlock);

			classDeclaration.bodyDeclarations().add(methodDeclaration);
		}

		constructorDeclaration.setBody(constructorBlock);
		classDeclaration.bodyDeclarations().add(constructorDeclaration);

		Expression equalsBuilderResult = codeGenerator.newMethodInvocation(equalsBuilderChain, "isEquals");
		Statement returnEquals = codeGenerator.newReturnStatement(equalsBuilderResult);
		equalsBlock.statements().add(returnEquals);

		equalsDeclaration.setBody(equalsBlock);
		classDeclaration.bodyDeclarations().add(equalsDeclaration);

		Expression hashCodeBuilderResult = codeGenerator.newMethodInvocation(hashCodeBuilderChain, "toHashCode");
		Statement returnHashCode = codeGenerator.newReturnStatement(hashCodeBuilderResult);
		hashCodeBlock.statements().add(returnHashCode);

		hashCodeDeclaration.setBody(hashCodeBlock);
		classDeclaration.bodyDeclarations().add(hashCodeDeclaration);

		return codeGenerator.generateCode();
	}

	private Expression newThisFieldAccess(JdtCodeGenerator codeGenerator, String fieldName) {
		return codeGenerator.newFieldAccess(codeGenerator.newThisExpression(), fieldName);
	}

	private Statement newReturnBoolean(JdtCodeGenerator codeGenerator, boolean value) {
		return codeGenerator.newReturnStatement(codeGenerator.newBooleanLiteral(value));
	}

	private Expression newVariableEquals(JdtCodeGenerator codeGenerator, String variableName, Expression rhsExpression) {
		return codeGenerator.newInfixExpression(codeGenerator.newSimpleName(variableName), InfixExpression.Operator.EQUALS, rhsExpression);
	}
}
