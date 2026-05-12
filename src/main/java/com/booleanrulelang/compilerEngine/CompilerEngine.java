package com.booleanrulelang.compilerEngine;

import java.util.List;
import com.booleanrulelang.domain.ProgramNode;
import com.booleanrulelang.domain.Token;
import com.booleanrulelang.parser.Parser;
import com.booleanrulelang.scanner.Lexer;
import com.booleanrulelang.scanner.TokenPrinter;
import com.booleanrulelang.semantics.Interpreter;
import com.booleanrulelang.semantics.TypeChecker;
import com.booleanrulelang.visitor.ASTTraversalPrinter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompilerEngine {

	private final Lexer lexer;
	private final TokenPrinter tokenPrinter;
	private final Parser parser;
	private final ASTTraversalPrinter astTraversalPrinter;
	private final TypeChecker typeChecker;
	private final Interpreter interpreter;

	public void compile(String filePath) {
		List<Token> tokens = lexer.scan(filePath);
		System.out.println("== Tokens (scanner output) ==");
		System.out.print(tokenPrinter.print(tokens));
		System.out.println();

		ProgramNode ast = parser.parseProgram(tokens);
		System.out.println("== AST (pre-order traversal) ==");
		System.out.print(astTraversalPrinter.print(ast));
		System.out.println();

		typeChecker.check(ast);
		System.out.println("== Type check: OK ==");
		System.out.println();

		System.out.println("== Execution ==");
		interpreter.execute(ast);
	}
}
