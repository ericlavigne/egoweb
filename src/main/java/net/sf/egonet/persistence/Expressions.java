package net.sf.egonet.persistence;

import java.util.Collection;
import java.util.List;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;

import org.hibernate.Session;

import com.google.common.base.Function;

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
	
	@SuppressWarnings("unchecked")
	public Boolean evaluate(Session session, Expression expression, Question question, Collection<Alter> alters) 
	{
		Integer nulls = 0, trues = 0, falses = 0;
		if(expression.getType().equals(Expression.Type.Compound)) {
			for(Long id : (List<Long>) expression.getValue()) {
				// TODO: If subexpression is simple, for alter question, and two alters, evaluate twice.
				Boolean result = evaluate(session, Expressions.get(session, id),question,alters);
				if(result == null) {
					nulls++;
				} else if(result) {
					trues++;
				} else {
					falses++;
				}
				if(expression.getOperator().equals(Expression.Operator.All)) {
					if(falses > 0) {
						return false;
					} else {
						return nulls > 0 ? null : true;
					}
				} else if(expression.getOperator().equals(Expression.Operator.Some)) {
					if(trues > 0) {
						return true;
					} else {
						return nulls > 0 ? null : false;
					}
				} else if(expression.getOperator().equals(Expression.Operator.None)) {
					if(trues > 0) {
						return false;
					} else {
						return nulls > 0 ? null : true;
					}
				}
			}
		}
		// TODO: Selection (lots of similarity to compound)
		// TODO: Textual
		// TODO: Numerical
		throw new RuntimeException("Unable to evaluate expression "+expression+
				" for question "+question);
	}
}
