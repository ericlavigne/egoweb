package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Answer.AnswerType;
import net.sf.functionalj.tuple.Pair;
import net.sf.functionalj.tuple.Triple;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Expression extends Entity {
	
	public static enum Operator 
		{All,Some,None,Equals,Contains,Greater,GreaterOrEqual,LessOrEqual,Less,Count,Sum}
	
	public static enum Type 
		{Compound,Selection,Text,Number,Counting,Comparison}
	
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
	public static Expression comparisonAbout(Expression expression) {
		Expression result = new Expression();
		result.setName("");
		result.type = Type.Comparison;
		result.setOperator(Operator.Equals);
		result.studyId = expression.studyId;
		result.setValue(new Pair<Integer,Long>(1,expression.getId()));
		return result;
	}
	public static Expression countingForStudy(Study study) {
		Expression result = new Expression(study);
		result.type = Type.Counting;
		result.setOperator(Operator.Sum);
		List<Long> noneSelected = Lists.newArrayList();
		result.setValue(new Triple<Integer,List<Long>,List<Long>>(1,noneSelected,noneSelected));
		return result;
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
	
	public void setValue(Object value) {
		if(value != null) {
			if(type.equals(Type.Comparison)) {
				Pair<Integer,Long> numberExpr = (Pair<Integer,Long>) value;
				setValueDB(numberExpr.getFirst()+":"+numberExpr.getSecond());
			} else if(type.equals(Type.Counting)) {
				Triple<Integer,List<Long>,List<Long>> numberExprsQuests =
					(Triple<Integer,List<Long>,List<Long>>) value;
				setValueDB(numberExprsQuests.getFirst()+":"+
						Joiner.on(",").join(numberExprsQuests.getSecond())+":"+
						Joiner.on(",").join(numberExprsQuests.getThird()));
			} else if(type.equals(Type.Compound) || type.equals(Type.Selection)) {
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
		String valDB = getValueDB();
		if(type.equals(Type.Comparison)) {
			try {
				String[] numberExpr = valDB.split(":");
				if(numberExpr.length == 2) {
					return new Pair<Integer,Long>(
							Integer.parseInt(numberExpr[0]),
							Long.parseLong(numberExpr[1]));
				} else {
					throw new RuntimeException("Wrong number of : separated parts in value.");
				}
			} catch(Exception ex) {
				throw new RuntimeException(
						"Invalid value in comparison expression: name="+
							getName()+", value="+getValueDB(),
						ex);
			}
		}
		if(type.equals(Type.Counting)) {
			try {
				List<String> numberExprsQuests = colonSep(valDB);
				if(numberExprsQuests.size() == 3) {
					Integer number = Integer.parseInt(numberExprsQuests.get(0));
					List<Long> exprs = commaSepToLongs(numberExprsQuests.get(1));
					List<Long> quests = commaSepToLongs(numberExprsQuests.get(2));
					return new Triple<Integer,List<Long>,List<Long>>(number,exprs,quests);
				} else {
					throw new RuntimeException("Wrong number of : separated parts in value: valDB");
				}
			} catch(Exception ex) {
				throw new RuntimeException(
						"Invalid value in counting expression: name="+
							getName()+", value="+getValueDB(),
						ex);
			}
		}
		if(type.equals(Type.Compound) || type.equals(Type.Selection)) {
			return commaSepToLongs(valDB);
		}
		if(type.equals(Type.Number)) {
			return getValueDB() == null ? null : Integer.parseInt(getValueDB());
		}
		if(type.equals(Type.Text)) {
			return getValueDB();
		}
		throw new RuntimeException("Can't getValue() for Expression because no case provided for type "+type);
	}
	public static List<String> colonSep(String target) {
		int startSearch = 0, nextColon = 0;
		List<String> results = Lists.newArrayList();
		while(nextColon > -1) {
			nextColon = target.indexOf(":", startSearch);
			if(nextColon < 0) {
				results.add(target.substring(startSearch));
			} else {
				results.add(target.substring(startSearch, nextColon));
				startSearch = nextColon + 1;
			}
		}
		return results;
	}
	private static List<Long> commaSepToLongs(String commaSep) {
		return commaSep == null || commaSep.isEmpty() ?
				new ArrayList<Long>() : 
					Lists.transform(Lists.newArrayList(commaSep.split(",")), parseLong);
	}
	private static Function<String,Long> parseLong = new Function<String,Long>() {
		public Long apply(String string) {
			return Long.parseLong(string);
		}
	};
	public List<Operator> allowedOperators() {
		return allowedOperators(this.type);
	}
	public static List<Operator> allowedOperators(Type type) {
		if(type.equals(Type.Comparison)) {
			return allowedOperators(Type.Number);
		}
		if(type.equals(Type.Counting)) {
			return Lists.newArrayList(Operator.Count,Operator.Sum);
		}
		if(type.equals(Type.Compound)) {
			return Lists.newArrayList(Operator.All,Operator.Some,Operator.None);
		}
		if(type.equals(Type.Selection)) {
			return Lists.newArrayList(Operator.All,Operator.Some,Operator.None);
		}
		if(type.equals(Type.Text)) {
			return Lists.newArrayList(Operator.Equals,Operator.Contains);
		}
		if(type.equals(Type.Number)) {
			return Lists.newArrayList(
					Operator.Greater,
					Operator.GreaterOrEqual,
					Operator.Equals,
					Operator.LessOrEqual,
					Operator.Less);
		}
		throw new RuntimeException("Unrecognized expression type: "+type);
	}
	
	// For Hibernate only
	public Expression() {
		
	}
	public void setTypeDB(String typeString) {
		if(typeString != null && ! typeString.isEmpty()) {
			this.type = Type.valueOf(typeString);
		}
	}
	public String getTypeDB() {
		return type == null ? null : type.name();
	}
	public void setOperatorDB(String operatorString) {
		if(operatorString != null && ! operatorString.isEmpty()) {
			this.operator = Operator.valueOf(operatorString);
		}
	}
	public String getOperatorDB() {
		return operator == null ? null : operator.name();
	}
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}
	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}
	
	// -----------------------------------------

	public void setValueDB(String valueString) {
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
