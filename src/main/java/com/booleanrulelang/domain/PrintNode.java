package com.booleanrulelang.domain;

import com.booleanrulelang.visitor.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@AllArgsConstructor
public class PrintNode extends Node {
	public Node expression;
	
	@Override
	public <T> T accept(ASTVisitor<T> visitor) {
		return visitor.visitPrint(this);
	}
}
