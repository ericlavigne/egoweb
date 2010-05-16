package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Answer.SkipReason;
import net.sf.egonet.model.Expression.Operator;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.functionalj.tuple.Pair;
import net.sf.functionalj.tuple.PairUni;
import net.sf.functionalj.tuple.Triple;
import net.sf.functionalj.tuple.TripleUni;

import org.hibernate.Session;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Expressions {

	public static List<Expression> forStudy(final Long studyId) {
		return DB.withTx(new Function<Session,List<Expression>>() {
			public List<Expression> apply(Session session) {
				return forStudy(session,studyId);
			}
		});
	}

	public static List<Expression> forStudy(Session session, Long studyId) {
		return session.createQuery("from Expression e where e.studyId = :studyId and e.active = 1")
				.setLong("studyId", studyId).list();
	}
	
	public static Expression get(final Long expressionId) {
		return DB.withTx(new Function<Session,Expression>() {
			public Expression apply(Session session) {
				return get(session,expressionId);
			}
		});
	}

	public static Expression get(Session session, Long expressionId) {
		return (Expression) session.createQuery("from Expression e where e.id = :id and e.active = 1")
				.setLong("id", expressionId).list().get(0);
	}
	
	public static List<Expression> getSimpleExpressionsForQuestion(Session session, Long questionId) {
		return session.createQuery("from Expression e where e.questionId = :questionId and e.active = 1")
				.setLong("questionId", questionId).list();
	}
	
	public static void delete(final Expression expression) {
		DB.withTx(new Function<Session,Object>(){
			public Object apply(Session session) {
				delete(session,expression);
				return null;
			}
		});
	}
	
	public static void delete(Session session, Expression expression) {
		for(Expression otherExpression : forStudy(session,expression.getStudyId())) {
			if(otherExpression.getType().equals(Expression.Type.Compound)) {
				List<Long> expressionIds = (List<Long>) otherExpression.getValue();
				expressionIds.remove(expression.getId());
				otherExpression.setValue(expressionIds);
				DB.save(session,otherExpression);
			} else if(otherExpression.getType().equals(Expression.Type.Comparison)) {
				Pair<Integer,Long> numberExpr = (Pair<Integer,Long>) otherExpression.getValue();
				if(numberExpr.getSecond().equals(expression.getId())) {
					delete(session,otherExpression);
				}
			} else if(otherExpression.getType().equals(Expression.Type.Counting)) {
				Triple<Integer,List<Long>,List<Long>> numberExprsQuests =
					(Triple<Integer,List<Long>,List<Long>>) otherExpression.getValue();
				if(numberExprsQuests.getSecond().contains(expression.getId())) {
					List<Long> newExprs = Lists.newArrayList();
					for(Long expr : numberExprsQuests.getSecond()) {
						if(! expr.equals(expression.getId())) {
							newExprs.add(expr);
						}
					}
					otherExpression.setValue(new Triple<Integer,List<Long>,List<Long>>(
							numberExprsQuests.getFirst(),
							newExprs,
							numberExprsQuests.getThird()));
					DB.save(session, otherExpression);
				}
			}
		}
		Long studyId = expression.getStudyId();
		Study study = Studies.getStudy(session, studyId);
		if(study.getAdjacencyExpressionId() != null && 
				study.getAdjacencyExpressionId().equals(expression.getId()))
		{
			study.setAdjacencyExpressionId(null);
		}
		for(Question question : Questions.getQuestionsForStudy(session, studyId, null)) {
			if(question.getAnswerReasonExpressionId() != null &&
					question.getAnswerReasonExpressionId().equals(expression.getId()))
			{
				question.setAnswerReasonExpressionId(null);
			}
		}
		DB.delete(session,expression);
	}
	
	public static class EvaluationContext {
		public Map<Long,Question> qidToQuestion = Maps.newHashMap();
		public Map<Long,Expression> eidToExpression = Maps.newHashMap();
		public Map<Long,QuestionOption> idToOption = Maps.newHashMap();
		public Map<Long,Answer> qidToEgoAnswer = Maps.newHashMap();
		public Map<PairUni<Long>,Answer> qidAidToAlterAnswer = Maps.newHashMap();
		public Map<TripleUni<Long>,Answer> qidA1idA2idToAlterPairAnswer = Maps.newHashMap();
	}
	
	public static EvaluationContext getContext(final Interview interview) {
		return new DB.Action<EvaluationContext>() {
			public EvaluationContext get() {
				return getContext(session,interview);
			}
		}.execute();
	}
	
	public static EvaluationContext getContext(Session session, Interview interview) {
		EvaluationContext context = new EvaluationContext();
		Long studyId = interview.getStudyId();
		Long interviewId = interview.getId();
		for(Question question : Questions.getQuestionsForStudy(session, studyId, null)) {
			context.qidToQuestion.put(question.getId(), question);
		}
		for(Expression expression : Expressions.forStudy(session, studyId)) {
			context.eidToExpression.put(expression.getId(), expression);
		}
		for(QuestionOption option : Options.getOptionsForStudy(session, studyId)) {
			context.idToOption.put(option.getId(), option);
		}
		for(Answer answer : Answers.getAnswersForInterview(session, interviewId, Question.QuestionType.EGO)) {
			context.qidToEgoAnswer.put(answer.getQuestionId(), answer);
		}
		for(Answer answer : Answers.getAnswersForInterview(session, interviewId, Question.QuestionType.EGO_ID)) {
			context.qidToEgoAnswer.put(answer.getQuestionId(), answer);
		}
		for(Answer answer : Answers.getAnswersForInterview(session, interviewId, Question.QuestionType.ALTER)) {
			context.qidAidToAlterAnswer.put(
					new PairUni<Long>(
							answer.getQuestionId(),
							answer.getAlterId1()), 
					answer);
		}
		for(Answer answer : Answers.getAnswersForInterview(session, interviewId, Question.QuestionType.ALTER_PAIR)) {
			context.qidA1idA2idToAlterPairAnswer.put(
					new TripleUni<Long>(
							answer.getQuestionId(),
							answer.getAlterId1(),
							answer.getAlterId2()), 
					answer);
			context.qidA1idA2idToAlterPairAnswer.put(
					new TripleUni<Long>(
							answer.getQuestionId(),
							answer.getAlterId2(),
							answer.getAlterId1()), 
					answer);
		}
		return context;
	}
	
	public static Object evaluate(Expression expression, ArrayList<Alter> alters, 
			EvaluationContext context) 
	{
		return evaluate(expression,alters,
				context.qidToQuestion,context.eidToExpression,context.idToOption,
				context.qidToEgoAnswer,context.qidAidToAlterAnswer,
				context.qidA1idA2idToAlterPairAnswer);
	}

	public static Boolean evaluateAsBool(Expression expression, ArrayList<Alter> alters, 
			EvaluationContext context) 
	{
		return evaluateAsBool(expression,alters,
				context.qidToQuestion,context.eidToExpression,context.idToOption,
				context.qidToEgoAnswer,context.qidAidToAlterAnswer,
				context.qidA1idA2idToAlterPairAnswer);
	}
	public static Integer evaluateAsInt(Expression expression, ArrayList<Alter> alters, 
			EvaluationContext context) 
	{
		return evaluateAsInt(expression,alters,
				context.qidToQuestion,context.eidToExpression,context.idToOption,
				context.qidToEgoAnswer,context.qidAidToAlterAnswer,
				context.qidA1idA2idToAlterPairAnswer);
	}

	private static Boolean evaluateAsBool(Expression expression, ArrayList<Alter> alters,
			Map<Long,Question> qidToQuestion, Map<Long,Expression> eidToExpression,
			Map<Long,QuestionOption> idToOption,
			Map<Long,Answer> qidToEgoAnswer, Map<PairUni<Long>,Answer> qidAidToAlterAnswer,
			Map<TripleUni<Long>,Answer> qidA1idA2idToAlterPairAnswer) 
	{
		Object result = evaluate(expression, alters,
				qidToQuestion, eidToExpression, idToOption,
				qidToEgoAnswer, qidAidToAlterAnswer,
				qidA1idA2idToAlterPairAnswer);
		if(result instanceof Boolean) {
			return (Boolean) result;
		}
		if(result instanceof Integer) {
			return ! result.equals(new Integer(0));
		}
		throw new RuntimeException("Unable to coerce "+result.getClass()+" to Boolean: "+result);
	}
	
	private static Integer evaluateAsInt(Expression expression, ArrayList<Alter> alters,
			Map<Long,Question> qidToQuestion, Map<Long,Expression> eidToExpression,
			Map<Long,QuestionOption> idToOption,
			Map<Long,Answer> qidToEgoAnswer, Map<PairUni<Long>,Answer> qidAidToAlterAnswer,
			Map<TripleUni<Long>,Answer> qidA1idA2idToAlterPairAnswer) 
	{
		Object result = evaluate(expression, alters,
				qidToQuestion, eidToExpression, idToOption,
				qidToEgoAnswer, qidAidToAlterAnswer,
				qidA1idA2idToAlterPairAnswer);
		if(result instanceof Boolean) {
			return ((Boolean) result) ? 1 : 0;
		}
		if(result instanceof Integer) {
			return (Integer) result;
		}
		throw new RuntimeException("Unable to coerce "+result.getClass()+" to Integer: "+result);
	}
	
	private static Object evaluate(Expression expression, ArrayList<Alter> alters,
			Map<Long,Question> qidToQuestion, Map<Long,Expression> eidToExpression,
			Map<Long,QuestionOption> idToOption,
			Map<Long,Answer> qidToEgoAnswer, Map<PairUni<Long>,Answer> qidAidToAlterAnswer,
			Map<TripleUni<Long>,Answer> qidA1idA2idToAlterPairAnswer) 
	{
		if(expression.getType().equals(Expression.Type.Compound)) {
			return evaluateCompoundExpression(expression, alters,
					qidToQuestion, eidToExpression, idToOption,
					qidToEgoAnswer, qidAidToAlterAnswer,
					qidA1idA2idToAlterPairAnswer);
		}
		if(expression.getType().equals(Expression.Type.Counting)) {
			return evaluateCountingExpression(expression, alters,
					qidToQuestion, eidToExpression, idToOption,
					qidToEgoAnswer, qidAidToAlterAnswer,
					qidA1idA2idToAlterPairAnswer);
		}
		if(expression.getType().equals(Expression.Type.Comparison)) {
			return evaluateComparisonExpression(expression, alters,
					qidToQuestion, eidToExpression, idToOption,
					qidToEgoAnswer, qidAidToAlterAnswer,
					qidA1idA2idToAlterPairAnswer);
		}
		return evaluateSimpleExpression(expression, 
				qidToQuestion.get(expression.getQuestionId()),
				alters, qidToEgoAnswer, qidAidToAlterAnswer,
				qidA1idA2idToAlterPairAnswer);
	}
	
	private static Boolean evaluateSimpleExpression(Expression expression, Question question,
			ArrayList<Alter> alters,
			Map<Long,Answer> qidToEgoAnswer, Map<PairUni<Long>,Answer> qidAidToAlterAnswer,
			Map<TripleUni<Long>,Answer> qidA1idA2idToAlterPairAnswer) 
	{
		QuestionType qType = question.getType();
		Answer answer = null;
		if(qType.equals(QuestionType.EGO) || qType.equals(QuestionType.EGO_ID)) {
			answer = qidToEgoAnswer.get(question.getId());
		}
		if(qType.equals(QuestionType.ALTER)) {
			if(alters.isEmpty()) {
				return expression.getResultForUnanswered();
			} else if(alters.size() > 1) {
				for(Alter alter : alters) {
					if( ! evaluateSimpleExpression(expression,question,Lists.newArrayList(alter),
							null,qidAidToAlterAnswer,null)) 
					{
						return false;
					}
					return true;
				}
			}
			answer = qidAidToAlterAnswer.get(new PairUni<Long>(question.getId(),alters.get(0).getId()));
		}
		if(qType.equals(QuestionType.ALTER_PAIR)) {
			if(alters.size() < 2) {
				return expression.getResultForUnanswered();
			}
			answer = qidA1idA2idToAlterPairAnswer.get(
					new TripleUni<Long>(
							question.getId(),
							alters.get(0).getId(),
							alters.get(1).getId()));
		}
		Expression.Type eType = expression.getType();
		if(eType.equals(Expression.Type.Number)) {
			return evaluateNumericalExpression(expression, answer);
		}
		if(eType.equals(Expression.Type.Selection)) {
			return evaluateSelectionExpression(expression, answer);
		}
		if(eType.equals(Expression.Type.Text)) {
			return evaluateTextualExpression(expression, answer);
		}
		throw new RuntimeException(
				"evaluateSimpleExpression doesn't know how to evaluate expression of type "+eType);
	}

	private static Boolean evaluateComparisonExpression(Expression expression, ArrayList<Alter> alters,
			Map<Long,Question> qidToQuestion, Map<Long,Expression> eidToExpression,
			Map<Long,QuestionOption> idToOption,
			Map<Long,Answer> qidToEgoAnswer, Map<PairUni<Long>,Answer> qidAidToAlterAnswer,
			Map<TripleUni<Long>,Answer> qidA1idA2idToAlterPairAnswer) 
	{
		if(! expression.getType().equals(Expression.Type.Comparison)) {
			throw new RuntimeException("using evaluateComparisonExpression for " +
					"expression of type "+expression.getType());
		}
		Pair<Integer,Long> numberExpr = (Pair<Integer,Long>) expression.getValue();
		Expression subExpression = eidToExpression.get(numberExpr.getSecond());
		if(! subExpression.getType().equals(Expression.Type.Counting)) {
			throw new RuntimeException("Unable to evaluate counting expression " +
					expression.getName() + " because its subexpression  " +
					subExpression.getName() + " has type "+expression.getType());
		}
		Integer subResult = evaluateCountingExpression(subExpression,alters,
				qidToQuestion, eidToExpression, idToOption,
				qidToEgoAnswer, qidAidToAlterAnswer, qidA1idA2idToAlterPairAnswer);
		Integer number = numberExpr.getFirst();
		Expression.Operator operator = expression.getOperator();

		if(subResult > number) {
			return 
				operator.equals(Expression.Operator.Greater) || 
				operator.equals(Expression.Operator.GreaterOrEqual);
		}
		if(subResult < number) {
			return 
				operator.equals(Expression.Operator.Less) || 
				operator.equals(Expression.Operator.LessOrEqual);
		}
		return
			operator.equals(Expression.Operator.Equals) ||
			operator.equals(Expression.Operator.GreaterOrEqual) ||
			operator.equals(Expression.Operator.LessOrEqual);
	}
	
	private static Integer evaluateCountingExpression(Expression expression, ArrayList<Alter> alters,
			Map<Long,Question> qidToQuestion, Map<Long,Expression> eidToExpression,
			Map<Long,QuestionOption> idToOption,
			Map<Long,Answer> qidToEgoAnswer, Map<PairUni<Long>,Answer> qidAidToAlterAnswer,
			Map<TripleUni<Long>,Answer> qidA1idA2idToAlterPairAnswer) 
	{
		if(! expression.getType().equals(Expression.Type.Counting)) {
			throw new RuntimeException("using evaluateCountingExpression for " +
					"expression of type "+expression.getType());
		}
		Expression.Operator operator = expression.getOperator();
		Triple<Integer,List<Long>,List<Long>> numberExprsQuests =
			(Triple<Integer,List<Long>,List<Long>>) expression.getValue();
		
		Integer total = 0;

		for(Long expressionId : numberExprsQuests.getSecond()) {
			Expression subExpression = eidToExpression.get(expressionId);
			if(subExpression == null) {
				throw new RuntimeException("Unable to find expression#"+expressionId+
						" referenced in expression#"+expression.getId()+": "+
						expression.getName()+"("+expression.getType()+")");
			}
			ArrayList<ArrayList<Alter>> alterGroups = Lists.newArrayList();
			Boolean simple = subExpression.isSimpleExpression();
			if(simple && qidToQuestion.get(subExpression.getQuestionId()).isAboutAlter()) {
				for(Alter alter : alters) { // If alter question, needs one alter at a time.
					alterGroups.add(Lists.newArrayList(alter));
				}
			} else {
				alterGroups.add(alters);
			}
			for(ArrayList<Alter> alterGroup : alterGroups) {
				if(operator.equals(Operator.Count)) {
					if(evaluateAsBool(subExpression,alterGroup,
							qidToQuestion,eidToExpression,idToOption,
							qidToEgoAnswer,qidAidToAlterAnswer,
							qidA1idA2idToAlterPairAnswer))
					{
						total++;
					}
				} else if(operator.equals(Operator.Sum)) {
					total += evaluateAsInt(subExpression,alterGroup,
							qidToQuestion,eidToExpression,idToOption,
							qidToEgoAnswer,qidAidToAlterAnswer,
							qidA1idA2idToAlterPairAnswer);
				} else {
					throw new RuntimeException(
							"Unrecognized operator for counting expression: "+operator);
				}
			}
		}

		for(Long questionId : numberExprsQuests.getThird()) {
			Question question = qidToQuestion.get(questionId);
			if(question == null) {
				throw new RuntimeException("Unable to find expression#"+questionId+
						" referenced in expression#"+expression.getId()+": "+
						expression.getName()+"("+expression.getType()+")");
			}
			List<Answer> answers = Lists.newArrayList();
			if(question.isAboutAlter()) {
				for(Alter alter : alters) { // If alter question, needs one alter at a time.
					answers.add(qidAidToAlterAnswer.get(
							new PairUni<Long>(questionId,alter.getId())));
				}
			} else if(question.isAboutRelationship() && alters.size() > 1) {
				answers.add(
						qidA1idA2idToAlterPairAnswer.get(
								new TripleUni<Long>(
										questionId,
										alters.get(0).getId(),
										alters.get(1).getId())));
			} else if(question.isAboutEgo()){
				answers.add(qidToEgoAnswer.get(questionId));
			}
			for(Answer answer : answers) {
				if(answer != null && answer.getSkipReason().equals(SkipReason.NONE)) {
					List<Integer> results = Lists.newArrayList();
					String answerString = answer.getValue();
					
					// set result, depending on question type and answer
					if(question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION) ||
							question.getAnswerType().equals(Answer.AnswerType.SELECTION))
					{
						if(! answerString.isEmpty()) {
							for(String optionIdString : answerString.split(",")) {
								QuestionOption option = 
									idToOption.get(Long.parseLong(optionIdString));
								try {
									results.add(Integer.parseInt(option.getValue().trim()));
								} catch(Exception ex) {
									
								}
							}
						}
					} else if(question.getAnswerType().equals(Answer.AnswerType.NUMERICAL)) {
						results.add(Integer.parseInt(answerString));
					} else if(question.getAnswerType().equals(Answer.AnswerType.TEXTUAL)) {
						results.add(answerString == null || answerString.isEmpty() ? 0 : 1);
					} else {
						throw new RuntimeException(
								"Unrecognized answer type: "+question.getAnswerType());
					}
					
					// increment total, depending on count vs sum
					for(Integer result : results) {
						if(operator.equals(Operator.Count)) {
							if(! result.equals(new Integer(0))) {
								total++;
							}
						} else if(operator.equals(Operator.Sum)) {
							total += result;
						} else {
							throw new RuntimeException(
									"Unrecognized operator for counting expression: "+operator);
						}
					}
				}
			}
		}
		
		return total * numberExprsQuests.getFirst();
	}

	private static Boolean evaluateCompoundExpression(Expression expression, ArrayList<Alter> alters,
			Map<Long,Question> qidToQuestion, Map<Long,Expression> eidToExpression,
			Map<Long,QuestionOption> idToOption,
			Map<Long,Answer> qidToEgoAnswer, Map<PairUni<Long>,Answer> qidAidToAlterAnswer,
			Map<TripleUni<Long>,Answer> qidA1idA2idToAlterPairAnswer) 
	{
		Expression.Operator operator = expression.getOperator();
		for(Long expressionId : (List<Long>) expression.getValue()) {
			Expression subExpression = eidToExpression.get(expressionId);
			if(subExpression == null) {
				throw new RuntimeException("Unable to find expression#"+expressionId+
						" referenced in expression#"+expression.getId()+": "+
						expression.getName()+"("+expression.getType()+")");
			}
			ArrayList<ArrayList<Alter>> alterGroups = Lists.newArrayList();
			Boolean simple = subExpression.isSimpleExpression();
			if(simple && qidToQuestion.get(subExpression.getQuestionId()).isAboutAlter()) {
				for(Alter alter : alters) { // If alter question, needs one alter at a time.
					alterGroups.add(Lists.newArrayList(alter));
				}
			} else {
				alterGroups.add(alters);
			}
			for(ArrayList<Alter> alterGroup : alterGroups) {
				Boolean result = 
					evaluateAsBool(subExpression,alterGroup,
							qidToQuestion,eidToExpression,idToOption,
							qidToEgoAnswer,qidAidToAlterAnswer,
							qidA1idA2idToAlterPairAnswer);
				if(result) {
					if(operator.equals(Operator.Some)) {
						return true;
					}
					if(operator.equals(Operator.None)) {
						return false;
					}
				} else {
					if(operator.equals(Operator.All)) {
						return false;
					}
				}
			}
		}
		return operator.equals(Operator.All) || operator.equals(Operator.None);
	}
	
	private static Boolean evaluateSelectionExpression(Expression expression, Answer answer) {
		try {
			List<String> selectedStrings = Lists.newArrayList(answer.getValue().split(","));
			Operator operator = expression.getOperator();
			for(Long id : (List<Long>) expression.getValue()) {
				if(selectedStrings.contains(id.toString())) {
					if(operator.equals(Operator.Some)) {
						return true;
					}
					if(operator.equals(Operator.None)) {
						return false;
					}
				} else {
					if(operator.equals(Operator.All)) {
						return false;
					}
				}
			}
			return operator.equals(Operator.All) || operator.equals(Operator.None);
		} catch(Exception ex) {
			return expression.getResultForUnanswered();
		}
	}
	
	private static Boolean evaluateTextualExpression(Expression expression, Answer answer) {
		try {
			String answerString = answer.getValue();
			if(answerString == null) {
				answerString = "";
			}
			String expressionString = (String) expression.getValue();
			if(expressionString == null) {
				expressionString = "";
			}
			Operator operator = expression.getOperator();
			if(operator.equals(Expression.Operator.Equals)) {
				return answerString.equals((String) expression.getValue());
			}
			if(operator.equals(Expression.Operator.Contains)) {
				return answerString.contains((String) expression.getValue());
			}
			return false;
		} catch(Exception ex) {
			return expression.getResultForUnanswered();
		}
	}
	
	private static Boolean evaluateNumericalExpression(Expression expression, Answer answer) {
		try {
			Long answerNumber = Long.parseLong(answer.getValue());
			Long expressionNumber = ((Number) expression.getValue()).longValue();
			Operator operator = expression.getOperator();

			if(answerNumber > expressionNumber) {
				return 
					operator.equals(Expression.Operator.Greater) || 
					operator.equals(Expression.Operator.GreaterOrEqual);
			}
			if(answerNumber < expressionNumber) {
				return 
					operator.equals(Expression.Operator.Less) || 
					operator.equals(Expression.Operator.LessOrEqual);
			}
			return
				operator.equals(Expression.Operator.Equals) ||
				operator.equals(Expression.Operator.GreaterOrEqual) ||
				operator.equals(Expression.Operator.LessOrEqual);
		} catch(Exception ex) {
			return expression.getResultForUnanswered();
		}
	}
}
