package com.booleanrulelang.compilerEngine;

import java.util.List;
import com.booleanrulelang.domain.Token;
import com.booleanrulelang.scanner.Lexer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompilerEngine {
	
	private final Lexer lexer;
	public void compile(String filePath) {
		List<Token> tokens = lexer.scan(filePath);
		
		for (Token token : tokens) {
			System.out.println(token.toString());
		}
	}
}
