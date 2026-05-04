package com.booleanrulelang.exception;

public class StatementException extends CompilerException {
	
	public StatementException(int line, String lexeme) {
		super("Parse error at line " + line +
				": expected statement, got '" + lexeme + "'", 2, 0, 0);
	}
}
