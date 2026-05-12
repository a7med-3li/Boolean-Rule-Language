package com.booleanrulelang.visitor;

import com.booleanrulelang.domain.ArithmeticOpNode;
import com.booleanrulelang.domain.AssignNode;
import com.booleanrulelang.domain.BoolNode;
import com.booleanrulelang.domain.ComparisonOpNode;
import com.booleanrulelang.domain.IdentifierNode;
import com.booleanrulelang.domain.LogicalOpNode;
import com.booleanrulelang.domain.NegationNode;
import com.booleanrulelang.domain.NotNode;
import com.booleanrulelang.domain.NumberNode;
import com.booleanrulelang.domain.PrintNode;
import com.booleanrulelang.domain.ProgramNode;

public interface ASTVisitor<T> {
	T visitProgram(ProgramNode node);
	T visitAssign(AssignNode node);
	T visitPrint(PrintNode node);

	T visitArithmeticOp(ArithmeticOpNode node);
	T visitComparisonOp(ComparisonOpNode node);
	T visitLogicalOp(LogicalOpNode node);

	T visitNot(NotNode node);
	T visitNegation(NegationNode node);

	T visitNumber(NumberNode node);
	T visitIdentifier(IdentifierNode node);
	T visitBool(BoolNode node);
}
