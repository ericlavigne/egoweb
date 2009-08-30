package net.sf.egonet.persistence;

import java.util.List;

import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.common.base.Function;

public class Questions {


	public static Question getQuestion(Session session, final Long id) {
		// Yes, this is different from session.load(Study.class, id),
		// which triggers a lazy initialization exception when any 
		// field of Study is requested after the session is closed.
		return (Question) session.createQuery("from Question where id = :id")
		.setParameter("id", id).uniqueResult();
	}
	
	public static Question getQuestion(final Long id) {
		return DB.withTx(new Function<Session,Question>(){
			public Question apply(Session session) {
				return getQuestion(session, id);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public static List<Question> getQuestionsForStudy(
			Session session, final Long studyId, final QuestionType type) 
	{
		Query query = session.createQuery("from Question q where q.studyId = :studyId " +
				(type == null ? "" : "and q.typeDB = :type")+ " order by q.ordering")
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

	public static void moveEarlier(Session session, final Question question) {
		List<Question> questions = getQuestionsForStudy(session,question.getStudyId(),question.getType());
		Integer i = null;
		for(Integer j = 0; j < questions.size(); j++) {
			if(questions.get(j).getId().equals(question.getId())) {
				i = j;
			}
		}
		if(i != null && i > 0) {
			Question swap = questions.get(i);
			questions.set(i, questions.get(i-1));
			questions.set(i-1, swap);
		}
		for(Integer j = 0; j < questions.size(); j++) {
			questions.get(j).setOrdering(j);
			DB.save(questions.get(j));
		}
	}

	public static void moveEarlier(final Question question) {
		DB.withTx(new Function<Session,Object>() {
			public Object apply(Session session) {
				moveEarlier(session,question);
				return null;
			}
		});
	}
}
