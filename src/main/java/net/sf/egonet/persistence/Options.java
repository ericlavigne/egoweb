package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.QuestionOption;

import org.hibernate.Session;

public class Options {

	@SuppressWarnings("unchecked")
	public static List<QuestionOption> 
	getOptionsForQuestion(Session session, final Long questionId) 
	{
		new ArrayList<Expression>(session.createQuery("from Expression").list());
		return 
		session.createQuery("from QuestionOption o where o.questionId = :questionId")
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
}
