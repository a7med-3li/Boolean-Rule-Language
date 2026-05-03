package com.booleanrulelang.domain;

import com.booleanrulelang.visitor.ASTVisitor;
import org.springframework.stereotype.Component;

@Component
public abstract class Node {
	public abstract <T> T accept(ASTVisitor<T> visitor);
}
