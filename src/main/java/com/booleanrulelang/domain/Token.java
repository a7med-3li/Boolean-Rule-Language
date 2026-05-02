package com.booleanrulelang.domain;

public record Token(
	TokenType type,
	String lexeme,
	int line
) {}
