package com.booleanrulelang.parser;

import java.util.ArrayList;
import java.util.List;
import com.booleanrulelang.domain.AssignNode;
import com.booleanrulelang.domain.BinaryOpNode;
import com.booleanrulelang.domain.BoolNode;
import com.booleanrulelang.domain.IdentifierNode;
import com.booleanrulelang.domain.Node;
import com.booleanrulelang.domain.NumberNode;
import com.booleanrulelang.domain.PrintNode;
import com.booleanrulelang.domain.ProgramNode;
import com.booleanrulelang.domain.Token;
import com.booleanrulelang.domain.TokenType;
import com.booleanrulelang.domain.UnaryOpNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Parser {
	
	//todo: handle exceptions
	
	private List<Token> tokens;
	private int currentPosition = 0;
	
	// program = { statement } ;
	public ProgramNode parseProgram(List<Token> tokens) {
		this.tokens = tokens;
		List<Node> statements = new ArrayList<>();
		while (!check(TokenType.EOF)) {
			statements.add(parseStatement());
		}
		return new ProgramNode(statements);
	}
	
	// statement = assignment | print_stmt ;
	private Node parseStatement() {
		if (check(TokenType.PRINT)) {
			return parsePrint();
		} else if (check(TokenType.ID)) {
			return parseAssignment();
		}
		throw new RuntimeException(
				"Parse error at line " + peek().line() +
						": expected statement, got '" + peek().lexeme() + "'"
		);
	}
	
	// assignment = identifier ":=" expression ";" ;
	private AssignNode parseAssignment() {
		Token id = expect(TokenType.ID, "identifier");
		expect(TokenType.ASSIGN, ":=");
		Node value = parseExpression();
		expect(TokenType.SEMICOLON, ";");
		return new AssignNode(id.lexeme(), value);
	}
	
	// print_stmt = "print" expression ";" ;
	private PrintNode parsePrint() {
		expect(TokenType.PRINT, "print");
		Node expr = parseExpression();
		expect(TokenType.SEMICOLON, ";");
		return new PrintNode(expr);
	}
	
	// expression = logic_or ;
	private Node parseExpression() {
		return parseLogicOr();
	}
	
	// logic_or = logic_and { "or" logic_and } ;
	private Node parseLogicOr() {
		Node left = parseLogicAnd();
		
		while (check(TokenType.OR)) {
			advance();  // consume "or"
			Node right = parseLogicAnd();
			left = new BinaryOpNode("or", left, right);
		}
		return left;
	}
	
	// logic_and = logic_not { "and" logic_not } ;
	private Node parseLogicAnd() {
		Node left = parseLogicNot();
		
		while (check(TokenType.AND)) {
			advance();  // consume "and"
			Node right = parseLogicNot();
			left = new BinaryOpNode("and", left, right);
		}
		return left;
	}
	
	// logic_not = [ "not" ] comparison ;
	private Node parseLogicNot() {
		if (check(TokenType.NOT)) {
			advance();  // consume "not"
			Node operand = parseLogicNot(); // right-recursive so "not not x" works
			return new UnaryOpNode("not", operand);
		}
		return parseComparison();
	}
	
	// comparison = arithmetic [ comp_op arithmetic ] ;
	private Node parseComparison() {
		Node left = parseArithmetic();
		
		if (check(TokenType.EQ)  || check(TokenType.NEQ) ||
				check(TokenType.LT)  || check(TokenType.GT)  ||
				check(TokenType.LEQ) || check(TokenType.GEQ)) {
			
			Token op = advance();  // consume the operator
			Node right = parseArithmetic();
			return new BinaryOpNode(op.lexeme(), left, right);
		}
		return left;
	}
	
	// arithmetic = term { ( "+" | "-" ) term } ;
	private Node parseArithmetic() {
		Node left = parseTerm();
		
		while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
			Token op = advance();
			Node right = parseTerm();
			left = new BinaryOpNode(op.lexeme(), left, right);
		}
		return left;
	}
	
	// term = factor { ( "*" | "/" ) factor } ;
	private Node parseTerm() {
		Node left = parseFactor();
		
		while (check(TokenType.STAR) || check(TokenType.SLASH)) {
			Token op = advance();
			Node right = parseFactor();
			left = new BinaryOpNode(op.lexeme(), left, right);
		}
		return left;
	}
	
	// factor = [ "-" ] primary ;
	private Node parseFactor() {
		if (check(TokenType.MINUS)) {
			advance();  // consume "-"
			Node operand = parsePrimary();
			return new UnaryOpNode("-", operand);
		}
		return parsePrimary();
	}
	
	// primary = NUMBER | IDENTIFIER | "true" | "false" | "(" expression ")" ;
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
			advance();                          // consume "("
			Node inner = parseExpression();     // recurse fully
			expect(TokenType.RPAREN, ")");      // consume ")"
			return inner;
		}
		
		throw new RuntimeException(
				"Parse error at line " + t.line() +
						": unexpected token '" + t.lexeme() + "'"
		);
	}
	
	// Look at current token without consuming it
	private Token peek() {
		return tokens.get(currentPosition);
	}
	
	// Consume and return the current token
	private Token advance() {
		Token t = tokens.get(currentPosition);
		currentPosition++;
		return t;
	}
	
	// Check if current token matches type (without consuming)
	private boolean check(TokenType type) {
		return peek().type() == type;
	}
	
	// Consume if matches, return true. Otherwise, return false.
	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}
		return false;
	}
	
	// Consume and assert — throws if wrong token
	private Token expect(TokenType type, String message) {
		if (!check(type)) {
			Token t = peek();
			throw new RuntimeException(
					"Parse error at line " + t.line() +
							": expected " + message +
							" but got '" + t.lexeme()+ "'"
			);
		}
		return advance();
	}
}
