package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Expression extends Entity {
	
	public static enum Operator {All,Some,None,Equals,Contains,Greater,GreaterOrEqual,LessOrEqual,Less}
	
	public static enum Type {Compound,Selection,Text,Number}
	
	private String name;
	private Type type;
	private Operator operator;
	private String valueString;
	
	public Expression(Type type) {
		this.name = "";
		this.type = type;
		this.valueString = type.equals(Type.Number) ? "0" : "";
	}
	
	public Type getType() {
		return type;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public void setOperator(Operator operator) {
		if(allowedOperators().contains(operator)) {
			this.operator = operator;
		}
		throw new IllegalArgumentException("Operator "+operator+" not allowed for type "+type);
	}
	public Operator getOperator() {
		return operator;
	}
	
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if(value != null) {
			if(type.equals(Type.Compound) || type.equals(Type.Selection)) {
				this.valueString = Joiner.on(",").join((List<Long>) value);
			} else if(type.equals(Type.Number) || type.equals(Type.Text)) {
				this.valueString = value.toString();
			}
			throw new RuntimeException("Can't setValue("+value+") for Expression " +
					"because no case provided for type "+type);
		}
	}
	public Object getValue() {
		if(type.equals(Type.Compound) || type.equals(Type.Selection)) {
			if(valueString == null) {
				return new ArrayList<Long>();
			}
			return Lists.transform(Lists.newArrayList(valueString.split(",")), new Function<String,Long>() {
				public Long apply(String string) {
					return Long.parseLong(string);
				}
			});
		}
		if(type.equals(Type.Number)) {
			return valueString == null ? null : Integer.parseInt(valueString);
		}
		if(type.equals(Type.Text)) {
			return valueString;
		}
		throw new RuntimeException("Can't getValue() for Expression because no case provided for type "+type);
	}
	public List<Operator> allowedOperators() {
		return allowedOperators(this.type);
	}
	public static List<Operator> allowedOperators(Type type) {
		List<Operator> operators = Lists.newArrayList();
		if(type.equals(Type.Compound) || type.equals(Type.Selection)) {
			operators.add(Operator.All);
			operators.add(Operator.Some);
			operators.add(Operator.None);
		}
		if(type.equals(Type.Text)) {
			operators.add(Operator.Equals);
			operators.add(Operator.Contains);
		}
		if(type.equals(Type.Number)) {
			operators.add(Operator.Greater);
			operators.add(Operator.GreaterOrEqual);
			operators.add(Operator.Equals);
			operators.add(Operator.LessOrEqual);
			operators.add(Operator.Less);
		}
		return operators;
	}
	
	// For Hibernate only
	protected Expression() {
		
	}
	protected void setTypeDB(String typeString) {
		this.type = Type.valueOf(typeString);
	}
	protected String getTypeDB() {
		return type.name();
	}
	protected void setOperatorDB(String operatorString) {
		this.operator = Operator.valueOf(operatorString);
	}
	protected String getOperatorDB() {
		return operator.name();
	}
	protected void setValueDB(String valueString) {
		this.valueString = valueString;
	}
	protected String getValueDB() {
		return valueString;
	}
}
