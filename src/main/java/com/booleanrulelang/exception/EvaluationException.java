package com.booleanrulelang.exception;

public class EvaluationException extends CompilerException {

	public EvaluationException(String message) {
		super("Runtime error: " + message, 2, 0, 0);
	}

	@Override
	public String getFormattedMessage() {
		return getMessage();
	}
}
