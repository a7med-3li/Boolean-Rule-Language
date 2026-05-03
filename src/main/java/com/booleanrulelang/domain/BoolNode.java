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
public class BoolNode extends Node{
	private boolean value;
	
	@Override
	public <T> T accept(ASTVisitor<T> visitor) {
		return visitor.visitBool(this);
	}
}
