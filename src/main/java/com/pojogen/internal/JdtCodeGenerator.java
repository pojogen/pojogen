package com.pojogen.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import com.google.common.base.Throwables;

class JdtCodeGenerator {

	private final Document document;
	private final CompilationUnit cu;
	private final AST ast;

	JdtCodeGenerator() {
		document = new Document();
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(document.get().toCharArray());
		cu = (CompilationUnit) parser.createAST(null);
		cu.recordModifications();
		ast = cu.getAST();
	}

	String generateCode() {
		TextEdit edit = cu.rewrite(document, null);
		try {
			edit.apply(document);
			return document.get();
		} catch (BadLocationException e) {
			throw Throwables.propagate(e);
		}
	}

	void setPackage(String packageName) {
		cu.setPackage(newPackageDeclaration(packageName));
	}

	private PackageDeclaration newPackageDeclaration(String packageName) {
		PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
		packageDeclaration.setName(ast.newName(packageName));
		return packageDeclaration;
	}

	@SuppressWarnings("unchecked")
	TypeDeclaration newClassDeclaration(Collection<? extends IExtendedModifier> modifiers, String className, Type... superInterfaces) {
		TypeDeclaration classDeclaration = ast.newTypeDeclaration();
		classDeclaration.modifiers().addAll(modifiers);
		classDeclaration.setName(ast.newSimpleName(className));
		for (Type superInterface : superInterfaces) {
			classDeclaration.superInterfaceTypes().add(newJdtType(superInterface));
		}
		cu.types().add(classDeclaration);
		return classDeclaration;
	}

	MethodDeclaration newConstructorDeclaration(String className) {
		MethodDeclaration constructorDeclaration = ast.newMethodDeclaration();
		constructorDeclaration.setConstructor(true);
		constructorDeclaration.setName(ast.newSimpleName(className));
		return constructorDeclaration;
	}

	@SuppressWarnings("unchecked")
	MethodDeclaration newMethodDeclaration(Collection<? extends IExtendedModifier> modifiers, Type returnType, String methodName,
			SingleVariableDeclaration... parameters) {
		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.modifiers().addAll(modifiers);
		methodDeclaration.setReturnType2(newJdtType(returnType));
		methodDeclaration.setName(ast.newSimpleName(methodName));
		methodDeclaration.parameters().addAll(Arrays.asList(parameters));
		return methodDeclaration;
	}

	SingleVariableDeclaration newParameterDeclaration(Type variableType, String variableName) {
		SingleVariableDeclaration variableDeclaration = ast.newSingleVariableDeclaration();
		variableDeclaration.setType(newJdtType(variableType));
		variableDeclaration.setName(ast.newSimpleName(variableName));
		return variableDeclaration;
	}

	FieldDeclaration newFieldDeclaration(Collection<? extends IExtendedModifier> modifiers, Type fieldType, String fieldName) {
		VariableDeclarationFragment fieldVariableDeclaration = newVariableDeclarationFragment(fieldName);
		FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(fieldVariableDeclaration);
		fieldDeclaration.modifiers().addAll(modifiers);
		fieldDeclaration.setType(newJdtType(fieldType));
		return fieldDeclaration;
	}

	Expression newVariableDeclaration(Type variableType, String variableName) {
		VariableDeclarationFragment variableDeclarationFragment = newVariableDeclarationFragment(variableName);
		VariableDeclarationExpression variableDeclaration = ast.newVariableDeclarationExpression(variableDeclarationFragment);
		variableDeclaration.setType(newJdtType(variableType));
		return variableDeclaration;
	}

	private VariableDeclarationFragment newVariableDeclarationFragment(String variableName) {
		VariableDeclarationFragment declarationFragment = ast.newVariableDeclarationFragment();
		declarationFragment.setName(ast.newSimpleName(variableName));
		return declarationFragment;
	}

	Expression newInstanceCreation(String className) {
		ClassInstanceCreation newInstanceCreation = ast.newClassInstanceCreation();
		newInstanceCreation.setType(newSimpleType(className));
		return newInstanceCreation;
	}

	@SuppressWarnings("unchecked")
	Expression newMethodInvocation(Expression lhs, String methodName, Object... arguments) {
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(lhs);
		methodInvocation.setName(ast.newSimpleName(methodName));
		methodInvocation.arguments().addAll(Arrays.asList(arguments));
		return methodInvocation;
	}

	Expression newFieldAccess(Expression fieldOwner, String fieldName) {
		FieldAccess fieldAccess = ast.newFieldAccess();
		fieldAccess.setExpression(fieldOwner);
		fieldAccess.setName(ast.newSimpleName(fieldName));
		return fieldAccess;
	}

	Block newBlock() {
		return ast.newBlock();
	}

	@SuppressWarnings("unchecked")
	List<Modifier> newModifiers(int flags) {
		return ast.newModifiers(flags);
	}

	Expression newInfixExpression(Expression leftOperand, InfixExpression.Operator operator, Expression rightOperand) {
		InfixExpression infixExpression = ast.newInfixExpression();
		infixExpression.setLeftOperand(leftOperand);
		infixExpression.setOperator(operator);
		infixExpression.setRightOperand(rightOperand);
		return infixExpression;
	}

	Statement newIfStatement(Expression condition, Statement then) {
		IfStatement ifStatement = ast.newIfStatement();
		ifStatement.setExpression(condition);
		ifStatement.setThenStatement(then);
		return ifStatement;
	}

	Statement newReturnStatement(Expression returnedExpression) {
		ReturnStatement returnStatement = ast.newReturnStatement();
		returnStatement.setExpression(returnedExpression);
		return returnStatement;
	}

	Statement newAssignment(Expression lhs, Expression rhs) {
		Assignment assignment = ast.newAssignment();
		assignment.setLeftHandSide(lhs);
		assignment.setRightHandSide(rhs);
		return ast.newExpressionStatement(assignment);
	}

	Expression newCast(String typeName, Expression castedExpression) {
		CastExpression castExpression = ast.newCastExpression();
		castExpression.setType(newSimpleType(typeName));
		castExpression.setExpression(castedExpression);
		return castExpression;
	}

	SimpleName newSimpleName(String identifier) {
		return ast.newSimpleName(identifier);
	}

	Expression newNullLiteral() {
		return ast.newNullLiteral();
	}

	Expression newBooleanLiteral(boolean value) {
		return ast.newBooleanLiteral(value);
	}

	Expression newThisExpression() {
		return ast.newThisExpression();
	}

	private org.eclipse.jdt.core.dom.Type newJdtType(Type type) {
		switch (type.getKind()) {
		case BOOLEAN:
			return newPrimitiveType(PrimitiveType.BOOLEAN);
		case BYTE:
			return newPrimitiveType(PrimitiveType.BYTE);
		case CHAR:
			return newPrimitiveType(PrimitiveType.CHAR);
		case DOUBLE:
			return newPrimitiveType(PrimitiveType.DOUBLE);
		case FLOAT:
			return newPrimitiveType(PrimitiveType.FLOAT);
		case INT:
			return newPrimitiveType(PrimitiveType.INT);
		case LONG:
			return newPrimitiveType(PrimitiveType.LONG);
		case SHORT:
			return newPrimitiveType(PrimitiveType.LONG);
		case VOID:
			return newPrimitiveType(PrimitiveType.VOID);
		case DECLARED:
			return newSimpleType(type.toString());
		case ARRAY:
		case TYPEVAR:
		case WILDCARD:
			throw new UnsupportedOperationException();
		case ERROR:
		case EXECUTABLE:
		case NONE:
		case NULL:
		case OTHER:
		case PACKAGE:
		default:
			throw new IllegalArgumentException();
		}
	}

	private org.eclipse.jdt.core.dom.Type newSimpleType(String typeName) {
		return ast.newSimpleType(ast.newName(typeName));
	}

	private org.eclipse.jdt.core.dom.Type newPrimitiveType(PrimitiveType.Code typeCode) {
		return ast.newPrimitiveType(typeCode);
	}
}
