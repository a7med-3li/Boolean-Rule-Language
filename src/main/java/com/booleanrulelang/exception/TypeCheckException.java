package com.booleanrulelang.exception;

public class TypeCheckException extends CompilerException {

	public TypeCheckException(String message) {
		super("Type error: " + message, 2, 0, 0);
	}

	@Override
	public String getFormattedMessage() {
		return getMessage();
	}
}
