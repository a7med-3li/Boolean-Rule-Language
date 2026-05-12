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
		if (node instanceof NotNode n) {
			return new BooleanValue(!asBoolean(eval(n.operand, env)));
		}
		if (node instanceof NegationNode n) {
			return new NumberValue(-asNumber(eval(n.operand, env)));
		}
		if (node instanceof ArithmeticOpNode b) {
			double l = asNumber(eval(b.left, env));
			double r = asNumber(eval(b.right, env));
			return switch (b.op) {
				case "+" -> new NumberValue(l + r);
				case "-" -> new NumberValue(l - r);
				case "*" -> new NumberValue(l * r);
				case "/" -> {
					if (r == 0.0) {
						throw new EvaluationException("Division by zero");
					}
					yield new NumberValue(l / r);
				}
				default -> throw new EvaluationException("Unknown arithmetic operator '" + b.op + "'");
			};
		}
		if (node instanceof ComparisonOpNode b) {
			RuntimeValue left = eval(b.left, env);
			RuntimeValue right = eval(b.right, env);
			return switch (b.op) {
				case "==" -> new BooleanValue(equality(left, right));
				case "!=" -> new BooleanValue(!equality(left, right));
				case "<"  -> new BooleanValue(asNumber(left) <  asNumber(right));
				case ">"  -> new BooleanValue(asNumber(left) >  asNumber(right));
				case "<=" -> new BooleanValue(asNumber(left) <= asNumber(right));
				case ">=" -> new BooleanValue(asNumber(left) >= asNumber(right));
				default -> throw new EvaluationException("Unknown comparison operator '" + b.op + "'");
			};
		}
		if (node instanceof LogicalOpNode b) {
			return switch (b.op) {
				case "and" -> new BooleanValue(asBoolean(eval(b.left, env)) && asBoolean(eval(b.right, env)));
				case "or"  -> new BooleanValue(asBoolean(eval(b.left, env)) || asBoolean(eval(b.right, env)));
				default -> throw new EvaluationException("Unknown logical operator '" + b.op + "'");
			};
		}
		throw new EvaluationException(
				"Unsupported expression node: " + node.getClass().getSimpleName());
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
