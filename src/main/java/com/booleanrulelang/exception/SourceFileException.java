package com.booleanrulelang.exception;

public class SourceFileException extends CompilerException {
	public SourceFileException(String path) {
		super("Could not read file: " + path, 2, 0, 0);
	}
}
