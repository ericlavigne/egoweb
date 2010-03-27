package net.sf.egonet.persistence;

import java.util.List;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.QuestionOption;

import org.hibernate.Session;

import com.google.common.base.Function;

public class Options {
	
	public static List<QuestionOption> getOptionsForStudy(final Long studyId) {
		return DB.withTx(new DB.Action<List<QuestionOption>>() {
			public List<QuestionOption> get() {
				return getOptionsForStudy(session,studyId);
			}
		});
	}
	
	public static List<QuestionOption>
	getOptionsForStudy(Session session, final Long studyId)
	{
		return session.createQuery("from QuestionOption where active = 1 and " +
				" studyId = :studyId")
				.setLong("studyId", studyId)
				.list();
	}
	
	@SuppressWarnings("unchecked")
	public static List<QuestionOption> 
	getOptionsForQuestion(Session session, final Long questionId) 
	{
		List<QuestionOption> options = 
			session.createQuery("from QuestionOption o where o.active = 1 and " +
				"o.questionId = :questionId order by o.ordering")
			.setLong("questionId", questionId)
			.list();
		// Add values to those that don't have them (null or "").
		for(QuestionOption option : options) {
			if(option.getValue() == null || option.getValue().isEmpty()) {
				for(Integer possibleValue = options.size()+1; possibleValue > 0; possibleValue--) {
					Boolean foundMatch = false;
					for(QuestionOption otherOption : options) {
						if(otherOption.getValue() != null && otherOption.getValue().equals(possibleValue.toString())) {
							foundMatch = true;
						}
					}
					if(! foundMatch) {
						option.setValue(possibleValue.toString());
					}
				}
				DB.save(session, option);
			}
		}
		return options;
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
	
	public static void addOption(final Long questionId, final String optionName, final String optionValue) {
		new DB.Action<Object>() {
			public Object get() {
				addOption(session, questionId, optionName, optionValue);
				return null;
			}
		}.execute();
	}
	
	public static void addOption(Session session, Long questionId, String optionName, String optionValue) {
		List<QuestionOption> options = getOptionsForQuestion(session,questionId);
		QuestionOption newOption = new QuestionOption(questionId,optionName);
		if(optionValue == null || optionValue.isEmpty()) {
			for(Integer possibleValue = options.size()+1; possibleValue > 0; possibleValue--) {
				Boolean foundMatch = false;
				for(QuestionOption option : options) {
					if(option.getValue() != null && option.getValue().equals(possibleValue.toString())) {
						foundMatch = true;
					}
				}
				if(! foundMatch) {
					newOption.setValue(possibleValue.toString());
				}
			}
		} else {
			newOption.setValue(optionValue);
		}
		options.add(newOption);
		for(Integer i = 0; i < options.size(); i++) {
			options.get(i).setOrdering(i);
			DB.save(session, options.get(i));
		}
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
					"from QuestionOption where id = :id and name = :name and questionId = :questionId and active = 1")
			.setParameter("id", option.getId())
			.setParameter("name", option.getName())
			.setParameter("questionId", option.getQuestionId())
			.list();
	}

}
