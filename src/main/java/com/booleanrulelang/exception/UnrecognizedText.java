package com.booleanrulelang.exception;

public class UnrecognizedText extends CompilerException{
	
	public UnrecognizedText(int line, char ch) {
		super("Unrecognized character: '" + ch + "' at line " + line, 2, 0, 0);
	}
}
