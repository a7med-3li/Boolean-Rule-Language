package com.booleanrulelang.semantics;

import com.booleanrulelang.domain.ArithmeticOpNode;
import com.booleanrulelang.domain.AssignNode;
import com.booleanrulelang.domain.BoolNode;
import com.booleanrulelang.domain.ComparisonOpNode;
import com.booleanrulelang.domain.IdentifierNode;
import com.booleanrulelang.domain.LogicalOpNode;
import com.booleanrulelang.domain.NegationNode;
import com.booleanrulelang.domain.Node;
import com.booleanrulelang.domain.NotNode;
import com.booleanrulelang.domain.NumberNode;
import com.booleanrulelang.domain.PrintNode;
import com.booleanrulelang.domain.ProgramNode;
import com.booleanrulelang.exception.TypeCheckException;
import com.booleanrulelang.types.Type;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TypeChecker {

	public void check(ProgramNode program) {
		Map<String, Type> env = new HashMap<>();
		for (Node stmt : program.statements) {
			if (stmt instanceof AssignNode assign) {
				env.put(assign.name, inferExpr(assign.value, env));
			} else if (stmt instanceof PrintNode print) {
				inferExpr(print.expression, env);
			} else {
				throw new TypeCheckException(
						"Unexpected statement kind: " + stmt.getClass().getSimpleName());
			}
		}
	}

	private Type inferExpr(Node node, Map<String, Type> env) {
		if (node instanceof NumberNode) {
			return Type.NUMBER;
		}
		if (node instanceof BoolNode) {
			return Type.BOOLEAN;
		}
		if (node instanceof IdentifierNode id) {
			Type t = env.get(id.getName());
			if (t == null) {
				throw new TypeCheckException("Undefined variable '" + id.getName() + "'");
			}
			return t;
		}
		if (node instanceof NotNode n) {
			Type inner = inferExpr(n.operand, env);
			expectBoolean(inner, "operand of 'not'");
			return Type.BOOLEAN;
		}
		if (node instanceof NegationNode n) {
			Type inner = inferExpr(n.operand, env);
			expectNumber(inner, "operand of unary '-'");
			return Type.NUMBER;
		}
		if (node instanceof ArithmeticOpNode b) {
			Type l = inferExpr(b.left, env);
			Type r = inferExpr(b.right, env);
			expectNumber(l, "left operand of '" + b.op + "'");
			expectNumber(r, "right operand of '" + b.op + "'");
			return Type.NUMBER;
		}
		if (node instanceof ComparisonOpNode b) {
			Type l = inferExpr(b.left, env);
			Type r = inferExpr(b.right, env);
			if ("==".equals(b.op) || "!=".equals(b.op)) {
				if (l != r) {
					throw new TypeCheckException(
							"Operands of '"
									+ b.op
									+ "' must have the same type (got "
									+ l
									+ " and "
									+ r
									+ ")");
				}
				return Type.BOOLEAN;
			}
			expectNumber(l, "left operand of '" + b.op + "'");
			expectNumber(r, "right operand of '" + b.op + "'");
			return Type.BOOLEAN;
		}
		if (node instanceof LogicalOpNode b) {
			Type l = inferExpr(b.left, env);
			Type r = inferExpr(b.right, env);
			expectBoolean(l, "left operand of '" + b.op + "'");
			expectBoolean(r, "right operand of '" + b.op + "'");
			return Type.BOOLEAN;
		}
		throw new TypeCheckException(
				"Unsupported expression node: " + node.getClass().getSimpleName());
	}

	private static void expectNumber(Type t, String context) {
		if (t != Type.NUMBER) {
			throw new TypeCheckException(context + " must be NUMBER, was " + t);
		}
	}

	private static void expectBoolean(Type t, String context) {
		if (t != Type.BOOLEAN) {
			throw new TypeCheckException(context + " must be BOOLEAN, was " + t);
		}
	}
}
