package com.booleanrulelang.domain;

public enum TokenType {
	// Keywords
	AND, OR, NOT, TRUE, FALSE, PRINT,
	
	// Identifiers
	ID,
	
	// Literals
	NUMBER,
	
	// Arithmetic Operators
	PLUS, MINUS, STAR, SLASH,
	
	// Comparison Operators
	GT, LT, EQ, NEQ, GEQ, LEQ,
	
	// Assignment
	ASSIGN,
	
	// Delimiters
	LPAREN, RPAREN, SEMICOLON,
	
	// Special
	EOF
}
