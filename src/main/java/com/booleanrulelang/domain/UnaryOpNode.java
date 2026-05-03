package com.booleanrulelang.domain;

import com.booleanrulelang.visitor.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@AllArgsConstructor
public class UnaryOpNode extends Node {
	public String op;
	public Node operand;
	
	@Override
	public <T> T accept(ASTVisitor<T> visitor) {
		return visitor.visitUnaryOp(this);
	}
}
