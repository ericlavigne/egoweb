package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Answer.AnswerType;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Expression extends Entity {
	
	public static enum Operator {All,Some,None,Equals,Contains,Greater,GreaterOrEqual,LessOrEqual,Less}
	
	public static enum Type {Compound,Selection,Text,Number}
	
	private String name;
	private Type type;
	private Operator operator;
	protected String valueDB;
	protected String valueDBOld;
	private Long studyId;
	private Long questionId;
	private Boolean resultForUnanswered;
	
	public Expression(Study study) {
		type = Type.Compound;
		operator = Operator.Some;
		studyId = study.getId();
		setDefaultValues();
	}
	public Expression(Question question) {
		type = typeOfQuestion(question);
		studyId = question.getStudyId();
		questionId = question.getId();
		setDefaultValues();
	}
	public String toString() {
		return name == null || name.isEmpty() ? "Untitled expression" : name;
	}
	
	public Boolean getResultForUnanswered() {
		return resultForUnanswered == null ? false : resultForUnanswered;
	}
	
	public void setResultForUnanswered(Boolean resultForUnanswered) {
		this.resultForUnanswered = resultForUnanswered;
	}
	
	public static Type typeOfQuestion(AnswerType answerType) {
		if(answerType.equals(AnswerType.NUMERICAL)) {
			return Type.Number;
		}
		if(answerType.equals(AnswerType.TEXTUAL)) {
			return Type.Text;
		}
		return Type.Selection;
	}
	public static Type typeOfQuestion(Question question) {
		return typeOfQuestion(question.getAnswerType());
	}
	
	private void setDefaultValues() {
		this.name = "";
		setValueDB(type.equals(Type.Number) ? "0" : "");
	}
	
	public Long getStudyId() {
		return studyId;
	}
	
	public Long getQuestionId() {
		return questionId;
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
		} else {
			throw new IllegalArgumentException("Operator "+operator+" not allowed for type "+type);
		}
	}
	public Operator getOperator() {
		return operator;
	}
	
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if(value != null) {
			if(type.equals(Type.Compound) || type.equals(Type.Selection)) {
				setValueDB(Joiner.on(",").join((List<Long>) value));
			} else if(type.equals(Type.Number) || type.equals(Type.Text)) {
				setValueDB(value.toString());
			} else {
				throw new RuntimeException("Can't setValue("+value+") for Expression " +
						"because no case provided for type "+type);
			}
		}
	}
	public Object getValue() {
		if(type.equals(Type.Compound) || type.equals(Type.Selection)) {
			if(getValueDB() == null || getValueDB().isEmpty()) {
				return new ArrayList<Long>();
			}
			return Lists.transform(Lists.newArrayList(getValueDB().split(",")), new Function<String,Long>() {
				public Long apply(String string) {
					return Long.parseLong(string);
				}
			});
		}
		if(type.equals(Type.Number)) {
			return getValueDB() == null ? null : Integer.parseInt(getValueDB());
		}
		if(type.equals(Type.Text)) {
			return getValueDB();
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
	public Expression() {
		
	}
	protected void setTypeDB(String typeString) {
		this.type = Type.valueOf(typeString);
	}
	public String getTypeDB() {
		return type.name();
	}
	protected void setOperatorDB(String operatorString) {
		this.operator = Operator.valueOf(operatorString);
	}
	public String getOperatorDB() {
		return operator.name();
	}
	protected void setStudyId(Long studyId) {
		this.studyId = studyId;
	}
	protected void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}
	
	// -----------------------------------------

	protected void setValueDB(String valueString) {
		this.valueDB = valueString;
	}
	protected void setValueDBOld(String valueString) {
		this.valueDBOld = valueString;
	}
	public String getValueDB() {
		return migrateToText(this,"valueDB");
	}
	protected String getValueDBOld() {
		return null;
	}
}
