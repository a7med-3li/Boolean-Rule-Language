package com.booleanrulelang.semantics;

import com.booleanrulelang.domain.AssignNode;
import com.booleanrulelang.domain.BinaryOpNode;
import com.booleanrulelang.domain.BoolNode;
import com.booleanrulelang.domain.IdentifierNode;
import com.booleanrulelang.domain.Node;
import com.booleanrulelang.domain.NumberNode;
import com.booleanrulelang.domain.PrintNode;
import com.booleanrulelang.domain.ProgramNode;
import com.booleanrulelang.domain.UnaryOpNode;
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
		if (node instanceof UnaryOpNode u) {
			Type inner = inferExpr(u.operand, env);
			if ("not".equals(u.op)) {
				expectBoolean(inner, "operand of 'not'");
				return Type.BOOLEAN;
			}
			if ("-".equals(u.op)) {
				expectNumber(inner, "operand of unary '-'");
				return Type.NUMBER;
			}
			throw new TypeCheckException("Unknown unary operator '" + u.op + "'");
		}
		if (node instanceof BinaryOpNode b) {
			Type left = inferExpr(b.left, env);
			Type right = inferExpr(b.right, env);
			return inferBinary(b.op, left, right);
		}
		throw new TypeCheckException(
				"Unsupported expression node: " + node.getClass().getSimpleName());
	}

	private Type inferBinary(String op, Type left, Type right) {
		if ("and".equals(op) || "or".equals(op)) {
			expectBoolean(left, "left operand of '" + op + "'");
			expectBoolean(right, "right operand of '" + op + "'");
			return Type.BOOLEAN;
		}
		if ("+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op)) {
			expectNumber(left, "left operand of '" + op + "'");
			expectNumber(right, "right operand of '" + op + "'");
			return Type.NUMBER;
		}
		if ("<".equals(op) || ">".equals(op) || "<=".equals(op) || ">=".equals(op)) {
			expectNumber(left, "left operand of '" + op + "'");
			expectNumber(right, "right operand of '" + op + "'");
			return Type.BOOLEAN;
		}
		if ("==".equals(op) || "!=".equals(op)) {
			if (left != right) {
				throw new TypeCheckException(
						"Operands of '"
								+ op
								+ "' must have the same type (got "
								+ left
								+ " and "
								+ right
								+ ")");
			}
			return Type.BOOLEAN;
		}
		throw new TypeCheckException("Unknown binary operator '" + op + "'");
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
