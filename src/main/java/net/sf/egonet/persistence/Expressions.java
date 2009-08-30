package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.List;

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
		return session.createQuery("from Expression e where e.studyId = :studyId")
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
		return (Expression) session.createQuery("from Expression e where e.id = :id")
				.setLong("id", expressionId).list().get(0);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Expression> getSimpleExpressionsForQuestion(Session session, Long questionId) {
		return session.createQuery("from Expression e where e.questionId = :questionId")
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
}
