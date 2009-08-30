package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.QuestionOption;

import org.hibernate.Session;

import com.google.common.base.Function;

public class Options {

	@SuppressWarnings("unchecked")
	public static List<QuestionOption> 
	getOptionsForQuestion(Session session, final Long questionId) 
	{
		new ArrayList<Expression>(session.createQuery("from Expression").list());
		return 
		session.createQuery("from QuestionOption o where o.questionId = :questionId order by o.ordering")
			.setLong("questionId", questionId)
			.list();
	}
	

	public static List<QuestionOption> getOptionsForQuestion(final Long questionId) {
		return DB.withTx(new DB.Action<List<QuestionOption>>() {
			public List<QuestionOption> get() {
				return getOptionsForQuestion(session,questionId);
			}
		});
	}


	@SuppressWarnings("unchecked")
	public static void delete(Session session, final QuestionOption option) {
		for(QuestionOption dbOption : matchingOptionsFor(session,option)) {
			// TODO: Delete single-selection answers to the same question that reference this option
			// TODO: Remove references to this option from multiple-selection answers
			List<Expression> expressions = 
				Expressions.getSimpleExpressionsForQuestion(session, dbOption.getQuestionId());
			for(Expression expression : expressions) {
				if(expression.getType().equals(Expression.Type.Selection)) {
					List<Long> optionIds = (List<Long>) expression.getValue();
					optionIds.remove(dbOption.getId());
					expression.setValue(optionIds);
					DB.save(expression);
				}
			}
			DB.delete(dbOption);
		}
	}
	public static void delete(final QuestionOption option) {
		DB.withTx(new Function<Session,Object>(){
			public Object apply(Session session) {
				delete(session,option);
				return null;
			}
		});
	}
	
	
	public static void moveEarlier(Session session, final QuestionOption option) {
		List<QuestionOption> options = getOptionsForQuestion(session,option.getQuestionId());
		Integer i = null;
		for(Integer j = 0; j < options.size(); j++) {
			if(options.get(j).getId().equals(option.getId())) {
				i = j;
			}
		}
		if(i != null && i > 0) {
			QuestionOption swap = options.get(i);
			options.set(i, options.get(i-1));
			options.set(i-1, swap);
		}
		for(Integer j = 0; j < options.size(); j++) {
			options.get(j).setOrdering(j);
			DB.save(options.get(j));
		}
	}

	public static void moveEarlier(final QuestionOption option) {
		DB.withTx(new Function<Session,Object>() {
			public Object apply(Session session) {
				moveEarlier(session,option);
				return null;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static List<QuestionOption> matchingOptionsFor(Session session, final QuestionOption option) {
		return 
			session.createQuery(
					"from QuestionOption where id = :id and name = :name and questionId = :questionId")
			.setParameter("id", option.getId())
			.setParameter("name", option.getName())
			.setParameter("questionId", option.getQuestionId())
			.list();
	}

}
