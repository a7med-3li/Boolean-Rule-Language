package com.booleanrulelang.exception;

public class UnexpectedTokenException extends CompilerException {
	
	public UnexpectedTokenException(int line, String lexeme) {
		super("Parse error at line " + line +
				": expected statement, got '" + lexeme + "'", 2, 0, 0);
	}
}
