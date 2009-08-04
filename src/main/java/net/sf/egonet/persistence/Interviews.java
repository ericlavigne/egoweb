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
		return (Interview) session.load(Interview.class, id);
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

	@SuppressWarnings("unchecked")
	public static List<Interview> getInterviewsForStudy(
			Session session, final Long studyId) 
	{
		return
		session.createQuery("from Interview i where i.studyId = :studyId")
			.setLong("studyId", studyId)
			.list();
	}
}
