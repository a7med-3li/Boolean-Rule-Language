package com.booleanrulelang.visitor;

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
import org.springframework.stereotype.Component;

/**
 * Prints a depth-first pre-order traversal of the AST — the visit sequence and node kinds,
 * not a serialized tree dump (no nested JSON AST).
 */
@Component
public class ASTTraversalPrinter {

	public String print(Node node) {
		StringBuilder sb = new StringBuilder();
		node.accept(new PreOrderWalker(sb));
		return sb.toString();
	}

	private static final class PreOrderWalker implements ASTVisitor<Void> {

		private final StringBuilder sb;
		private int depth;

		PreOrderWalker(StringBuilder sb) {
			this.sb = sb;
		}

		private void indent() {
			sb.append("  ".repeat(depth));
		}

		private void appendLine(String line) {
			indent();
			sb.append(line).append(System.lineSeparator());
		}

		@Override
		public Void visitProgram(ProgramNode node) {
			appendLine("program");
			depth++;
			for (Node stmt : node.statements) {
				stmt.accept(this);
			}
			depth--;
			return null;
		}

		@Override
		public Void visitAssign(AssignNode node) {
			appendLine("assign " + node.name);
			depth++;
			node.value.accept(this);
			depth--;
			return null;
		}

		@Override
		public Void visitPrint(PrintNode node) {
			appendLine("print");
			depth++;
			node.expression.accept(this);
			depth--;
			return null;
		}

		@Override
		public Void visitArithmeticOp(ArithmeticOpNode node) {
			appendLine("arithmetic " + node.op);
			depth++;
			node.left.accept(this);
			node.right.accept(this);
			depth--;
			return null;
		}

		@Override
		public Void visitComparisonOp(ComparisonOpNode node) {
			appendLine("comparison " + node.op);
			depth++;
			node.left.accept(this);
			node.right.accept(this);
			depth--;
			return null;
		}

		@Override
		public Void visitLogicalOp(LogicalOpNode node) {
			appendLine("logical " + node.op);
			depth++;
			node.left.accept(this);
			node.right.accept(this);
			depth--;
			return null;
		}

		@Override
		public Void visitNot(NotNode node) {
			appendLine("not");
			depth++;
			node.operand.accept(this);
			depth--;
			return null;
		}

		@Override
		public Void visitNegation(NegationNode node) {
			appendLine("negate");
			depth++;
			node.operand.accept(this);
			depth--;
			return null;
		}

		@Override
		public Void visitNumber(NumberNode node) {
			double v = node.getValue();
			if (v == Math.floor(v)) {
				appendLine("number " + (int) v);
			} else {
				appendLine("number " + v);
			}
			return null;
		}

		@Override
		public Void visitIdentifier(IdentifierNode node) {
			appendLine("identifier " + node.getName());
			return null;
		}

		@Override
		public Void visitBool(BoolNode node) {
			appendLine("bool " + node.isValue());
			return null;
		}
	}
}
