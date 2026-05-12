package com.booleanrulelang.compilerEngine;

import java.util.List;
import com.booleanrulelang.domain.ProgramNode;
import com.booleanrulelang.domain.Token;
import com.booleanrulelang.parser.Parser;
import com.booleanrulelang.scanner.Lexer;
import com.booleanrulelang.semantics.Interpreter;
import com.booleanrulelang.semantics.TypeChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompilerEngine {

	private final Lexer lexer;
	private final Parser parser;
	private final TypeChecker typeChecker;
	private final Interpreter interpreter;

	public void compile(String filePath) {
		List<Token> tokens = lexer.scan(filePath);
		ProgramNode ast = parser.parseProgram(tokens);
		System.out.println("Compilation successful (lex + parse).");
		typeChecker.check(ast);
		System.out.println("Type check passed.");
		System.out.println("Execution:");
		interpreter.execute(ast);
	}
}
