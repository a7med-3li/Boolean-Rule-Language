package com.booleanrulelang.parser;

import java.util.ArrayList;
import java.util.List;
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
import com.booleanrulelang.domain.Token;
import com.booleanrulelang.domain.TokenType;
import com.booleanrulelang.exception.StatementException;
import com.booleanrulelang.exception.SyntaxException;
import com.booleanrulelang.exception.UnexpectedTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Parser {

	private List<Token> tokens;
	private int currentPosition = 0;

	public ProgramNode parseProgram(List<Token> tokens) {
		this.tokens = tokens;
		this.currentPosition = 0;
		List<Node> statements = new ArrayList<>();
		while (!check(TokenType.EOF)) {
			statements.add(parseStatement());
		}
		return new ProgramNode(statements);
	}

	private Node parseStatement() {
		if (check(TokenType.PRINT)) {
			return parsePrint();
		} else if (check(TokenType.ID)) {
			return parseAssignment();
		}
		throw new StatementException(peek().line(), peek().lexeme());
	}

	private AssignNode parseAssignment() {
		Token id = expect(TokenType.ID, "identifier");
		expect(TokenType.ASSIGN, ":=");
		Node value = parseExpression();
		expect(TokenType.SEMICOLON, ";");
		return new AssignNode(id.lexeme(), value);
	}

	private PrintNode parsePrint() {
		expect(TokenType.PRINT, "print");
		Node expr = parseExpression();
		expect(TokenType.SEMICOLON, ";");
		return new PrintNode(expr);
	}

	private Node parseExpression() {
		return parseLogicOr();
	}

	private Node parseLogicOr() {
		Node left = parseLogicAnd();

		while (check(TokenType.OR)) {
			advance();
			Node right = parseLogicAnd();
			left = new LogicalOpNode("or", left, right);
		}
		return left;
	}

	private Node parseLogicAnd() {
		Node left = parseLogicNot();

		while (check(TokenType.AND)) {
			advance();
			Node right = parseLogicNot();
			left = new LogicalOpNode("and", left, right);
		}
		return left;
	}

	private Node parseLogicNot() {
		if (check(TokenType.NOT)) {
			advance();
			Node operand = parseLogicNot();
			return new NotNode(operand);
		}
		return parseComparison();
	}

	private Node parseComparison() {
		Node left = parseArithmetic();

		if (check(TokenType.EQ)  || check(TokenType.NEQ) ||
				check(TokenType.LT)  || check(TokenType.GT)  ||
				check(TokenType.LEQ) || check(TokenType.GEQ)) {

			Token op = advance();
			Node right = parseArithmetic();
			return new ComparisonOpNode(op.lexeme(), left, right);
		}
		return left;
	}

	private Node parseArithmetic() {
		Node left = parseTerm();

		while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
			Token op = advance();
			Node right = parseTerm();
			left = new ArithmeticOpNode(op.lexeme(), left, right);
		}
		return left;
	}

	private Node parseTerm() {
		Node left = parseFactor();

		while (check(TokenType.STAR) || check(TokenType.SLASH)) {
			Token op = advance();
			Node right = parseFactor();
			left = new ArithmeticOpNode(op.lexeme(), left, right);
		}
		return left;
	}

	private Node parseFactor() {
		if (check(TokenType.MINUS)) {
			advance();
			Node operand = parsePrimary();
			return new NegationNode(operand);
		}
		return parsePrimary();
	}

	private Node parsePrimary() {
		Token t = peek();

		if (t.type() == TokenType.NUMBER) {
			advance();
			return new NumberNode(Double.parseDouble(t.lexeme()));
		}

		if (t.type() == TokenType.ID) {
			advance();
			return new IdentifierNode(t.lexeme());
		}

		if (t.type() == TokenType.TRUE) {
			advance();
			return new BoolNode(true);
		}

		if (t.type() == TokenType.FALSE) {
			advance();
			return new BoolNode(false);
		}

		if (t.type() == TokenType.LPAREN) {
			advance();
			Node inner = parseExpression();
			expect(TokenType.RPAREN, ")");
			return inner;
		}

		throw new UnexpectedTokenException(t.line(), t.lexeme());
	}

	private Token peek() {
		return tokens.get(currentPosition);
	}

	private Token advance() {
		Token t = tokens.get(currentPosition);
		currentPosition++;
		return t;
	}

	private boolean check(TokenType type) {
		return peek().type() == type;
	}

	private Token expect(TokenType type, String message) {
		if (!check(type)) {
			Token t = peek();
			throw new SyntaxException(t.line(), message, t.lexeme());
		}
		return advance();
	}
}
