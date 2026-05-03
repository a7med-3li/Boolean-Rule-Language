package com.booleanrulelang.scanner;

import static java.lang.Character.isDigit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.booleanrulelang.domain.Token;
import com.booleanrulelang.domain.TokenType;
import com.booleanrulelang.exception.SourceFileException;
import com.booleanrulelang.exception.UnrecognizedText;
import org.springframework.stereotype.Service;

@Service
public class Lexer {
	
	private static final Map<String, TokenType> keywords;
	
	static {
		keywords = new HashMap<>();
		keywords.put("and",   TokenType.AND);
		keywords.put("or",    TokenType.OR);
		keywords.put("not",   TokenType.NOT);
		keywords.put("true",  TokenType.TRUE);
		keywords.put("false", TokenType.FALSE);
		keywords.put("print", TokenType.PRINT);
	}
	
	public List<Token> scan(String filePath) {
		List<Token> tokens = new ArrayList<>();
		int line = 1;
		
		try (PushbackReader reader = new PushbackReader(new BufferedReader(new FileReader(filePath)))) {
			int c;
			while ((c = reader.read()) != -1) {
				char ch = (char) c;
				
				switch (ch) {
					// Delimiters
					case '(': tokens.add(new Token(TokenType.LPAREN, "(", line)); break;
					case ')': tokens.add(new Token(TokenType.RPAREN, ")", line)); break;
					case ';': tokens.add(new Token(TokenType.SEMICOLON, ";", line)); break;
					
					// Arithmetic
					case '+': tokens.add(new Token(TokenType.PLUS, "+", line)); break;
					case '-': tokens.add(new Token(TokenType.MINUS, "-", line)); break;
					case '*': tokens.add(new Token(TokenType.STAR, "*", line)); break;
					case '/': tokens.add(new Token(TokenType.SLASH, "/", line)); break;
					
					// Assignment & Comparison (Lookahead logic)
					case ':':
						if ((c = reader.read()) == '=') {
							tokens.add(new Token(TokenType.ASSIGN, ":=", line));
						}else{
							reader.unread(c);
						}
						break;
					case '!':
						if ((c = reader.read()) == '=') {
							tokens.add(new Token(TokenType.NEQ, "!=", line));
						} else{
							reader.unread(c);
						}
						break;
					case '>':
						if ((c = reader.read()) == '=') {
							tokens.add(new Token(TokenType.GEQ, ">=", line));
						} else {
							reader.unread(c);
							tokens.add(new Token(TokenType.GT, ">", line));
						}
						break;
					case '<':
						if ((c = reader.read()) == '=') {
							tokens.add(new Token(TokenType.LEQ, "<=", line));
						} else {
							reader.unread(c);
							tokens.add(new Token(TokenType.LT, "<", line));
						}
						break;
					case '=':
						if ((c = reader.read()) == '=') {
							tokens.add(new Token(TokenType.EQ, "==", line));
						} else {
							reader.unread(c);
							throw new UnrecognizedText(line, ch);
						}
						break;
						
					default:
						if (Character.isWhitespace(ch)) {
							if (ch == '\n') line++;
							continue;
						}
						if (Character.isDigit(ch)) {
							tokens.add(readNumber(ch, reader, line));
						} else if (Character.isLetter(ch)) {
							// Handle Identifiers and Keywords
							tokens.add(readIdentifier(ch, reader, line));
						} else {
							throw new UnrecognizedText(line, ch);
						}
						break;
				}
			}
			
			
			tokens.add(new Token(TokenType.EOF, "", line));
			return tokens;
		} catch (IOException e) {
			throw new SourceFileException(filePath);
		}
		
	}
	
	private Token readNumber(char firstChar, PushbackReader reader, int line) throws IOException {
		StringBuilder sb = new StringBuilder().append(firstChar);
		int c;
		while ((c = reader.read()) != -1 && isDigit((char)c)) {
			sb.append((char)c);
		}
		if (c != -1) reader.unread(c); // Put back the non-digit char
		return new Token(TokenType.NUMBER, sb.toString(), line);
	}
	
	private Token readIdentifier(char firstChar, PushbackReader reader, int line) throws IOException {
		StringBuilder sb = new StringBuilder().append(firstChar);
		int c;
		while ((c = reader.read()) != -1 && Character.isLetter((char)c)) {
			sb.append((char)c);
		}
		if (c != -1) reader.unread(c);
		String identifierStr = sb.toString();
		
		return new Token(keywords.getOrDefault(identifierStr.toLowerCase(), TokenType.ID), identifierStr, line);
	}
}
