package com.booleanrulelang.domain;

import java.util.List;
import com.booleanrulelang.visitor.ASTVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgramNode extends Node{
	public final List<Node> statements;
	
	@Override
	public <T> T accept(ASTVisitor<T> visitor) {
		return visitor.visitProgram(this);
	}
}
