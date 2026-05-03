package com.booleanrulelang.domain;

import com.booleanrulelang.visitor.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@AllArgsConstructor
public class AssignNode extends Node{
	public String name;
	public Node value;
	
	@Override
	public <T> T accept(ASTVisitor<T> visitor) {
		return visitor.visitAssign(this);
	}
}
