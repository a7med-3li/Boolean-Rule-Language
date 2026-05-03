package com.booleanrulelang.visitor;

import com.booleanrulelang.domain.AssignNode;
import com.booleanrulelang.domain.BinaryOpNode;
import com.booleanrulelang.domain.BoolNode;
import com.booleanrulelang.domain.IdentifierNode;
import com.booleanrulelang.domain.NumberNode;
import com.booleanrulelang.domain.PrintNode;
import com.booleanrulelang.domain.ProgramNode;
import com.booleanrulelang.domain.UnaryOpNode;

public interface ASTVisitor<T> {
	T visitProgram(ProgramNode node);
	T visitAssign(AssignNode node);
	T visitPrint(PrintNode node);
	T visitBinaryOp(BinaryOpNode node);
	T visitUnaryOp(UnaryOpNode node);
	T visitNumber(NumberNode node);
	T visitIdentifier(IdentifierNode node);
	T visitBool(BoolNode node);
}
