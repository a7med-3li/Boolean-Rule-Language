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
import com.booleanrulelang.exception.EvaluationException;
import com.booleanrulelang.runtime.BooleanValue;
import com.booleanrulelang.runtime.NumberValue;
import com.booleanrulelang.runtime.RuntimeValue;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class Interpreter {

	public void execute(ProgramNode program) {
		Map<String, RuntimeValue> env = new HashMap<>();
		for (Node stmt : program.statements) {
			if (stmt instanceof AssignNode assign) {
				RuntimeValue v = eval(assign.value, env);
				env.put(assign.name, v);
				System.out.println(assign.name + " := " + format(v));
			} else if (stmt instanceof PrintNode print) {
				System.out.println(format(eval(print.expression, env)));
			} else {
				throw new EvaluationException(
						"Unexpected statement kind: " + stmt.getClass().getSimpleName());
			}
		}
	}

	private static RuntimeValue eval(Node node, Map<String, RuntimeValue> env) {
		if (node instanceof NumberNode n) {
			return new NumberValue(n.getValue());
		}
		if (node instanceof BoolNode b) {
			return new BooleanValue(b.isValue());
		}
		if (node instanceof IdentifierNode id) {
			RuntimeValue v = env.get(id.getName());
			if (v == null) {
				throw new EvaluationException("Undefined variable '" + id.getName() + "'");
			}
			return v;
		}
		if (node instanceof UnaryOpNode u) {
			RuntimeValue inner = eval(u.operand, env);
			if ("not".equals(u.op)) {
				return new BooleanValue(!asBoolean(inner));
			}
			if ("-".equals(u.op)) {
				return new NumberValue(-asNumber(inner));
			}
			throw new EvaluationException("Unknown unary operator '" + u.op + "'");
		}
		if (node instanceof BinaryOpNode b) {
			return evalBinary(b.op, eval(b.left, env), eval(b.right, env));
		}
		throw new EvaluationException(
				"Unsupported expression node: " + node.getClass().getSimpleName());
	}

	private static RuntimeValue evalBinary(String op, RuntimeValue left, RuntimeValue right) {
		if ("and".equals(op)) {
			return new BooleanValue(asBoolean(left) && asBoolean(right));
		}
		if ("or".equals(op)) {
			return new BooleanValue(asBoolean(left) || asBoolean(right));
		}
		if ("+".equals(op)) {
			return new NumberValue(asNumber(left) + asNumber(right));
		}
		if ("-".equals(op)) {
			return new NumberValue(asNumber(left) - asNumber(right));
		}
		if ("*".equals(op)) {
			return new NumberValue(asNumber(left) * asNumber(right));
		}
		if ("/".equals(op)) {
			double denominator = asNumber(right);
			if (denominator == 0.0) {
				throw new EvaluationException("Division by zero");
			}
			return new NumberValue(asNumber(left) / denominator);
		}
		if ("<".equals(op)) {
			return new BooleanValue(asNumber(left) < asNumber(right));
		}
		if (">".equals(op)) {
			return new BooleanValue(asNumber(left) > asNumber(right));
		}
		if ("<=".equals(op)) {
			return new BooleanValue(asNumber(left) <= asNumber(right));
		}
		if (">=".equals(op)) {
			return new BooleanValue(asNumber(left) >= asNumber(right));
		}
		if ("==".equals(op)) {
			return new BooleanValue(equality(left, right));
		}
		if ("!=".equals(op)) {
			return new BooleanValue(!equality(left, right));
		}
		throw new EvaluationException("Unknown binary operator '" + op + "'");
	}

	private static boolean equality(RuntimeValue left, RuntimeValue right) {
		if (left instanceof NumberValue ln && right instanceof NumberValue rn) {
			return Double.compare(ln.value(), rn.value()) == 0;
		}
		if (left instanceof BooleanValue lb && right instanceof BooleanValue rb) {
			return lb.value() == rb.value();
		}
		throw new EvaluationException(
				"Incompatible operands for equality: "
						+ left.getClass().getSimpleName()
						+ " vs "
						+ right.getClass().getSimpleName());
	}

	private static double asNumber(RuntimeValue value) {
		if (value instanceof NumberValue nv) {
			return nv.value();
		}
		throw new EvaluationException(
				"Expected number, got " + value.getClass().getSimpleName());
	}

	private static boolean asBoolean(RuntimeValue value) {
		if (value instanceof BooleanValue bv) {
			return bv.value();
		}
		throw new EvaluationException(
				"Expected boolean, got " + value.getClass().getSimpleName());
	}

	private static String format(RuntimeValue value) {
		return switch (value) {
			case NumberValue n -> {
				double x = n.value();
				yield x == Math.floor(x) ? String.valueOf((int) x) : String.valueOf(x);
			}
			case BooleanValue b -> Boolean.toString(b.value());
		};
	}
}
