package net.sf.egonet.persistence;

import java.util.List;

import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.common.base.Function;

public class Questions {

	@SuppressWarnings("unchecked")
	public static List<Question> getQuestionsForStudy(
			Session session, final Long studyId, final QuestionType type) 
	{
		Query query = session.createQuery("from Question q where q.studyId = :studyId " +
				(type == null ? "" : "and q.typeDB = :type"))
				.setLong("studyId", studyId);
		if(type != null) {
			query.setString("type", Question.typeDB(type));
		}
		return query.list();
	}

	public static List<Question> getQuestionsForStudy(final Long studyId, final QuestionType type) {
		return DB.withTx(new Function<Session,List<Question>>() {
			public List<Question> apply(Session session) {
				return getQuestionsForStudy(session,studyId,type);
			}
		});
	}

}
