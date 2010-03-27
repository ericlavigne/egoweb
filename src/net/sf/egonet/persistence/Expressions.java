package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Expression.Operator;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.functionalj.tuple.PairUni;
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

	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
	public static void delete(Session session, Expression expression) {
		for(Expression otherExpression : forStudy(session,expression.getStudyId())) {
			if(otherExpression.getType().equals(Expression.Type.Compound)) {
				List<Long> expressionIds = (List<Long>) otherExpression.getValue();
				expressionIds.remove(expression.getId());
				otherExpression.setValue(expressionIds);
				DB.save(otherExpression);
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
		DB.delete(expression);
	}
	
	private static Boolean aggregateResultsForCompoundOrSelectionExpression(
			Expression.Operator operator, Integer trues, Integer falses, Integer nulls) 
	{
		if(operator.equals(Expression.Operator.All)) {
			if(falses > 0) {
				return false;
			} else {
				return nulls > 0 ? null : true;
			}
		} else if(operator.equals(Expression.Operator.Some)) {
			if(trues > 0) {
				return true;
			} else {
				return nulls > 0 ? null : false;
			}
		} else if(operator.equals(Expression.Operator.None)) {
			if(trues > 0) {
				return false;
			} else {
				return nulls > 0 ? null : true;
			}
		}
		throw new RuntimeException("Unable to aggregate parts of compound or selection expression" +
				" with operator "+operator+" and trues= "+trues+", falses="+falses+", nulls="+nulls);
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public static Boolean evaluate(Session session, 
			Expression expression, Interview interview, ArrayList<Alter> alters) 
	{
		Expression.Operator operator = expression.getOperator();
		Expression.Type eType = expression.getType();
		Integer nulls = 0, trues = 0, falses = 0;
		if(eType.equals(Expression.Type.Compound)) {
			for(Long expressionId : (List<Long>) expression.getValue()) {
				Expression subExpression = Expressions.get(session, expressionId);
				ArrayList<ArrayList<Alter>> alterGroups = Lists.newArrayList();
				Boolean simple = ! subExpression.getType().equals(Expression.Type.Compound);
				if(simple && Questions.getQuestion(session, subExpression.getQuestionId()).isAboutAlter()) {
					for(Alter alter : alters) { // If alter question, needs one alter at a time.
						alterGroups.add(Lists.newArrayList(alter));
					}
				} else {
					alterGroups.add(alters);
				}
				for(ArrayList<Alter> alterGroup : alterGroups) {
					Boolean result = evaluate(session, subExpression,interview,alterGroup);
					if(result == null) {
						nulls++;
					} else if(result) {
						trues++;
					} else {
						falses++;
					}
				}
			}
			return aggregateResultsForCompoundOrSelectionExpression(
					operator,trues,falses,nulls);
		}
		Question question = Questions.getQuestion(session, expression.getQuestionId());
		QuestionType qType = question.getType();
		// Handle case where you have wrong number of alters
		if((qType.equals(QuestionType.EGO) || qType.equals(QuestionType.EGO_ID)) && ! alters.isEmpty()) {
			return evaluate(session, expression,interview, new ArrayList<Alter>());
		}
		if((qType.equals(QuestionType.ALTER_PAIR) && alters.size() < 2) ||
				(qType.equals(QuestionType.ALTER) && alters.isEmpty())) {
			return null;
		}
		if(qType.equals(QuestionType.ALTER) && alters.size() > 1) {
			Boolean result = null;
			for(Alter alter : alters) {
				Boolean next = evaluate(session, expression,interview, Lists.newArrayList(alter));
				if(next == null || (result != null && ! next.equals(result))) {
					return null;
				}
				result = next;
			}
			return result;
		}
		
		Answer answer = Answers.getAnswerForInterviewQuestionAlters(session,interview,question,alters);
		if(answer == null) {
			return null;
		}
		// Selection (lots of similarity to compound)
		if(eType.equals(Expression.Type.Selection)) {
			return evaluateSelectionExpression(expression,answer);
		}
		// Textual
		if(eType.equals(Expression.Type.Text)) {
			return evaluateTextualExpression(expression,answer);
		}
		// Numerical
		if(eType.equals(Expression.Type.Number)) {
			return evaluateNumericalExpression(expression,answer);
		}
		throw new RuntimeException("Unable to evaluate expression "+expression);
	}
	
	public static class EvaluationContext {
		public Map<Long,Question> qidToQuestion = Maps.newHashMap(); 
		public Map<Long,Expression> eidToExpression = Maps.newHashMap();
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
	
	public static Boolean evaluate(Expression expression, ArrayList<Alter> alters, 
			EvaluationContext context) 
	{
		return evaluate(expression,alters,
				context.qidToQuestion,context.eidToExpression,
				context.qidToEgoAnswer,context.qidAidToAlterAnswer,
				context.qidA1idA2idToAlterPairAnswer);
	}
	
	public static Boolean evaluate(Expression expression, ArrayList<Alter> alters,
			Map<Long,Question> qidToQuestion, Map<Long,Expression> eidToExpression,
			Map<Long,Answer> qidToEgoAnswer, Map<PairUni<Long>,Answer> qidAidToAlterAnswer,
			Map<TripleUni<Long>,Answer> qidA1idA2idToAlterPairAnswer) 
	{
		if(expression.getType().equals(Expression.Type.Compound)) {
			return evaluateCompoundExpression(expression, alters,
					qidToQuestion, eidToExpression,
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
	
	@SuppressWarnings("unchecked")
	private static Boolean evaluateCompoundExpression(Expression expression, ArrayList<Alter> alters,
			Map<Long,Question> qidToQuestion, Map<Long,Expression> eidToExpression,
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
			Boolean simple = ! subExpression.getType().equals(Expression.Type.Compound);
			if(simple && qidToQuestion.get(subExpression.getQuestionId()).isAboutAlter()) {
				for(Alter alter : alters) { // If alter question, needs one alter at a time.
					alterGroups.add(Lists.newArrayList(alter));
				}
			} else {
				alterGroups.add(alters);
			}
			for(ArrayList<Alter> alterGroup : alterGroups) {
				Boolean result = 
					evaluate(subExpression,alterGroup,
							qidToQuestion,eidToExpression,
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
	
	@SuppressWarnings("unchecked")
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
