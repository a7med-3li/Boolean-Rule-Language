package com.booleanrulelang.exception;

import lombok.Getter;

@Getter
public abstract class CompilerException extends RuntimeException {
	private final int exitCode;
	private final int line;
	private final int column;
	
	public CompilerException(String message, int exitCode, int line, int column) {
		super(message);
		this.exitCode = exitCode;
		this.line = line;
		this.column = column;
	}
	
	public String getFormattedMessage() {
		return String.format("Error at [%d:%d]: %s", line, column, getMessage());
	}
	
}
