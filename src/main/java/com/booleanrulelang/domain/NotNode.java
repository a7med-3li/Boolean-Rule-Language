package com.booleanrulelang.domain;

import com.booleanrulelang.visitor.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@AllArgsConstructor
public class NotNode extends Node {
	public Node operand;

	@Override
	public <T> T accept(ASTVisitor<T> visitor) {
		return visitor.visitNot(this);
	}
}
