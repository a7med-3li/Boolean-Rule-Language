package com.booleanrulelang.domain;

import com.booleanrulelang.visitor.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class IdentifierNode extends Node {
	private String name;
	
	@Override
	public <T> T accept(ASTVisitor<T> visitor) {
		return visitor.visitIdentifier(this);
	}
}
