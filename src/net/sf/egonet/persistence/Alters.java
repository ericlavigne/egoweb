package net.sf.egonet.persistence;

import java.util.Collections;
import java.util.List;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;

import org.hibernate.Session;

import com.google.common.collect.Lists;

public class Alters {
	static List<Alter> getForInterview(Session session, Long interviewId) {
		List<Alter> alters = 
			session.createQuery("from Alter where interviewId = :interviewId and active = 1")
			.setParameter("interviewId", interviewId)
			.list();
		Collections.sort(alters);
		for(Integer i = 0; i < alters.size(); i++) {
			Alter alter = alters.get(i);
			if(alter.getOrdering() == null || ! alter.getOrdering().equals(i)) {
				alter.setOrdering(i);
				DB.save(session, alter);
			}
		}
		return alters;
	}
	
	public static List<Alter> getForInterview(final Long interviewId) {
		return new DB.Action<List<Alter>>() {
			public List<Alter> get() {
				return getForInterview(session,interviewId);
			}
		}.execute();
	}

	public static void delete(final Alter alter) {
		new DB.Action<Object>() {
			public Object get() {
				delete(session,alter);
				return null;
			}
		}.execute();
	}
	public static void delete(Session session, Alter alter) {
		List<Answer> answers = Lists.newArrayList();
		answers.addAll(
				Answers.getAnswersForInterview(session, alter.getInterviewId(), Question.QuestionType.ALTER));
		answers.addAll(
				Answers.getAnswersForInterview(session, alter.getInterviewId(), Question.QuestionType.ALTER_PAIR));
		for(Answer answer : answers) {
			if((answer.getAlterId1() != null && answer.getAlterId1().equals(alter.getId())) ||
					(answer.getAlterId2() != null && answer.getAlterId2().equals(alter.getId()))) 
			{
				DB.delete(session, answer);
			}
		}
		DB.delete(session, alter);
	}
}
