package com.booleanrulelang.scanner;

import com.booleanrulelang.domain.Token;
import com.booleanrulelang.domain.TokenType;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Formats the lexer output as a readable, independent listing.
 * Each row shows the token <em>category</em> (TokenType), its <em>lexeme</em>
 * (raw text from the source), and the line number where it was scanned.
 */
@Component
public class TokenPrinter {

	public String print(List<Token> tokens) {
		StringBuilder sb = new StringBuilder();
		int indexWidth = Math.max(2, String.valueOf(tokens.size()).length());
		int typeWidth = widestTypeName(tokens);

		int i = 0;
		for (Token t : tokens) {
			i++;
			sb.append(String.format(
					"%" + indexWidth + "d  %-" + typeWidth + "s  %-14s  line %d%n",
					i,
					t.type().name(),
					formatLexeme(t),
					t.line()));
		}
		return sb.toString();
	}

	private static int widestTypeName(List<Token> tokens) {
		int w = 0;
		for (Token t : tokens) {
			int len = t.type().name().length();
			if (len > w) {
				w = len;
			}
		}
		return Math.max(w, 4);
	}

	private static String formatLexeme(Token t) {
		if (t.type() == TokenType.EOF) {
			return "<eof>";
		}
		return "'" + t.lexeme() + "'";
	}
}
