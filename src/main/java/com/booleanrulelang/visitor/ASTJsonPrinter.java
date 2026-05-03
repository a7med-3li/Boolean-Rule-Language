package com.booleanrulelang.visitor;

import com.booleanrulelang.domain.AssignNode;
import com.booleanrulelang.domain.BinaryOpNode;
import com.booleanrulelang.domain.BoolNode;
import com.booleanrulelang.domain.IdentifierNode;
import com.booleanrulelang.domain.Node;
import com.booleanrulelang.domain.NumberNode;
import com.booleanrulelang.domain.PrintNode;
import com.booleanrulelang.domain.ProgramNode;
import com.booleanrulelang.domain.UnaryOpNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ASTJsonPrinter implements ASTVisitor<JSONObject> {
	
	public JSONObject print(Node node) {
		return node.accept(this);
	}
	
	@Override
	public JSONObject visitProgram(ProgramNode node) {
		JSONObject obj = new JSONObject();
		JSONArray stmts = new JSONArray();
		
		obj.put("type", "Program");
		for (Node stmt : node.statements) {
			stmts.put(stmt.accept(this));
		}
		obj.put("body", stmts);
		return obj;
	}
	
	@Override
	public JSONObject visitAssign(AssignNode node) {
		JSONObject obj = new JSONObject();
		obj.put("type", "Assign");
		obj.put("name", node.name);
		obj.put("value", node.value.accept(this));
		return obj;
	}
	
	@Override
	public JSONObject visitPrint(PrintNode node) {
		JSONObject obj = new JSONObject();
		obj.put("type", "Print");
		obj.put("expression", node.expression.accept(this));
		return obj;
	}
	
	@Override
	public JSONObject visitBinaryOp(BinaryOpNode node) {
		JSONObject obj = new JSONObject();
		obj.put("type", "BinaryOp");
		obj.put("op", node.op);
		obj.put("left", node.left.accept(this));
		obj.put("right", node.right.accept(this));
		return obj;
	}
	
	@Override
	public JSONObject visitUnaryOp(UnaryOpNode node) {
		JSONObject obj = new JSONObject();
		obj.put("type", "UnaryOp");
		obj.put("op", node.op);
		obj.put("operand", node.operand.accept(this));
		return obj;
	}
	
	@Override
	public JSONObject visitNumber(NumberNode node) {
		JSONObject obj = new JSONObject();
		obj.put("type", "Number");
		obj.put("value", node.getValue() == Math.floor(node.getValue())
				? (int) node.getValue()
				: node.getValue());
		return obj;
	}
	
	@Override
	public JSONObject visitIdentifier(IdentifierNode node) {
		JSONObject obj = new JSONObject();
		obj.put("type", "Identifier");
		obj.put("name", node.getName());
		return obj;
	}
	
	@Override
	public JSONObject visitBool(BoolNode node) {
		JSONObject obj = new JSONObject();
		obj.put("type", "Bool");
		obj.put("value", node.isValue());
		return obj;
	}
}
