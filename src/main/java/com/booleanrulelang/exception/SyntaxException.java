package com.booleanrulelang.exception;

public class SyntaxException extends CompilerException {
	
	public SyntaxException(int line, String message, String lexeme) {
		super("Parse error at line " + line +
				": expected " + message +
				" but got '" + lexeme+ "'", 2, 0, 0);
	}
}
