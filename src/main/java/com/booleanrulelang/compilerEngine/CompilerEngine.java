package com.booleanrulelang.compilerEngine;

import java.util.List;
import com.booleanrulelang.domain.ProgramNode;
import com.booleanrulelang.domain.Token;
import com.booleanrulelang.parser.Parser;
import com.booleanrulelang.scanner.Lexer;
import com.booleanrulelang.visitor.ASTTraversalPrinter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompilerEngine {
	
	private final Lexer lexer;
	private final Parser parser;
	private final ASTTraversalPrinter astTraversalPrinter;
	
	public void compile(String filePath) {
		List<Token> tokens = lexer.scan(filePath);
		ProgramNode ast = parser.parseProgram(tokens);
		System.out.println("Compilation successful! AST traversal (pre-order depth-first):");
		System.out.print(astTraversalPrinter.print(ast));
	}
}
