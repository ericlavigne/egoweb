package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Answer;

import org.hibernate.Session;

public class Options {

	@SuppressWarnings("unchecked")
	public static List<Answer> 
	getOptionsForQuestion(Session session, final Long questionId) 
	{
		return //new ArrayList<Answer>();
		session.createQuery("from QuestionOption o where o.questionId = :questionId")
			.setLong("questionId", questionId)
			.list();
	}
	

	public static List<Answer> getOptionsForQuestion(final Long questionId) {
		return DB.withTx(new DB.Action<List<Answer>>() {
			public List<Answer> get() {
				return getOptionsForQuestion(session,questionId);
			}
		});
	}
}
