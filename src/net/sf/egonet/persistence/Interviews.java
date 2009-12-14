package net.sf.egonet.persistence;

import java.util.List;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question.QuestionType;

import org.hibernate.Session;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Interviews {

	public static Interview getInterview(Session session, final Long id) {
		return (Interview) session.createQuery("from Interview where id = :id and active = 1")
			.setParameter("id", id).list().get(0);
	}
	
	public static Interview getInterview(final Long id) {
		return DB.withTx(new DB.Action<Interview>() {
			public Interview get() {
				return getInterview(session,id);
			}
		});
	}

	public static String getEgoNameForInterview(Session session, Long interviewId) {
		return Joiner.on(" ").join(
				Lists.transform(Answers.getAnswersForInterview(session,interviewId,QuestionType.EGO_ID),
						new Function<Answer,String>(){
							public String apply(Answer answer) {
								return answer.getValue();
							}
						}));
	}
	
	public static String getEgoNameForInterview(final Long interviewId) {
		return DB.withTx(new Function<Session,String>() {
			public String apply(Session session) {
				return getEgoNameForInterview(session,interviewId);
			}
		});
	}

	public static List<Interview> getInterviewsForStudy(final Long studyId) {
		return new DB.Action<List<Interview>>() {
			public List<Interview> get() {
				return getInterviewsForStudy(session,studyId);
			}
		}.execute();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Interview> getInterviewsForStudy(
			Session session, final Long studyId) 
	{
		return
		session.createQuery("from Interview i where i.studyId = :studyId and active = 1")
			.setLong("studyId", studyId)
			.list();
	}
}
