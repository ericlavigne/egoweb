package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Question.QuestionType;

import org.hibernate.Session;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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
	
	public Boolean evaluate(
			final Expression expression, 
			final Interview interview, 
			final ArrayList<Alter> alters) 
	{
		return new DB.Action<Boolean>() {
			public Boolean get() {
				return evaluate(session,expression,interview,alters);
			}
		}.execute();
	}
	
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
			List<String> selectedStrings = Lists.newArrayList(answer.getValue().split(","));
			for(Long id : (List<Long>) expression.getValue()) {
				if(selectedStrings.contains(id.toString())) {
					trues++;
				} else {
					falses++;
				}
			}
			return aggregateResultsForCompoundOrSelectionExpression(
					operator,trues,falses,nulls);
		}
		// Textual
		if(eType.equals(Expression.Type.Text)) {
			if(answer.getValue() == null) {
				return null;
			}
			if(operator.equals(Expression.Operator.Equals)) {
				return answer.getValue().equals((String) expression.getValue());
			}
			if(operator.equals(Expression.Operator.Contains)) {
				return answer.getValue().contains((String) expression.getValue());
			}
		}
		// Numerical
		if(eType.equals(Expression.Type.Number)) {
			Long answerNumber = null;
			Long expressionNumber = (Long) expression.getValue();
			try {
				answerNumber = Long.parseLong(answer.getValue());
			} catch(Exception ex) {
				return null;
			}
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
		}
		throw new RuntimeException("Unable to evaluate expression "+expression);
	}
}
