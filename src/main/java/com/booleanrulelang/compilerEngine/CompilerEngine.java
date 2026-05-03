package com.booleanrulelang.compilerEngine;

import java.util.List;
import com.booleanrulelang.domain.ProgramNode;
import com.booleanrulelang.domain.Token;
import com.booleanrulelang.parser.Parser;
import com.booleanrulelang.scanner.Lexer;
import com.booleanrulelang.visitor.ASTJsonPrinter;
import com.booleanrulelang.visitor.ASTVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompilerEngine {
	
	private final Lexer lexer;
	private final Parser parser;
	private final ASTJsonPrinter astJsonPrinter;
	
	public void compile(String filePath) {
		List<Token> tokens = lexer.scan(filePath);
		ProgramNode ast = parser.parseProgram(tokens);
		System.out.println("Compilation successful! Abstract Syntax Tree:");
		System.out.println( astJsonPrinter.print(ast).toString(2));
	}
}
