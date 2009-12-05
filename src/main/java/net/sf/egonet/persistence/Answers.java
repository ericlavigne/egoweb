package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;

import org.hibernate.Query;
import org.hibernate.Session;

public class Answers {

	@SuppressWarnings("unchecked")
	public static List<Answer> getAnswersForInterview(
			Session session, final Long interviewId, final QuestionType questionType) 
	{
		return
		session.createQuery("from Answer a where a.active = 1 and a.interviewId = :interviewId and " +
				" a.questionTypeDB = :questionTypeDB")
			.setLong("interviewId", interviewId)
			.setString("questionTypeDB", Question.typeDB(questionType))
			.list();
	}

	@SuppressWarnings("unchecked")
	public static List<Answer> getAnswersForInterview(
			Session session, final Long interviewId) 
	{
		return
		session.createQuery("from Answer a where a.active = 1 and a.interviewId = :interviewId")
			.setLong("interviewId", interviewId)
			.list();
	}


	@SuppressWarnings("unchecked")
	public static List<Answer> getAnswersForQuestion(
			Session session, final Long questionId) 
	{
		return
		session.createQuery("from Answer a where a.questionId = :questionId and a.active = 1")
			.setLong("questionId", questionId)
			.list();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Answer> getAnswersForStudy(
			Session session, final Long studyId) 
	{
		return
		session.createQuery("from Answer a where a.studyId = :studyId and a.active = 1")
			.setLong("studyId", studyId)
			.list();
	}
	@SuppressWarnings("unchecked")
	public static List<Answer> getAnswersForStudy(
			Session session, final Long studyId, final QuestionType questionType) 
	{
		return
		session.createQuery("from Answer a where a.active = 1 and a.studyId = :studyId and " +
				" a.questionTypeDB = :questionTypeDB")
			.setLong("studyId", studyId)
			.setString("questionTypeDB", Question.typeDB(questionType))
			.list();
	}

	public static Answer getAnswerForInterviewAndQuestion(
			final Long interviewId, final Question question) 
	{
		return new DB.Action<Answer>() {
			public Answer get() {
				Interview interview = Interviews.getInterview(session, interviewId);
				return getAnswerForInterviewAndQuestion(session,interview,question);
			}
		}.execute();
	}
	
	public static Answer getAnswerForInterviewAndQuestion(Session session, Interview interview, Question question) {
		return getAnswerForInterviewQuestionAlters(session,interview,question,new ArrayList<Alter>());
	}
	
	public static Answer getAnswerForInterviewQuestionAlters(
			final Interview interview, final Question question, final ArrayList<Alter> alters) {
		return new DB.Action<Answer>() {
			public Answer get() {
				return getAnswerForInterviewQuestionAlters(session,interview,question,alters);
			}
		}.execute();
	}
	
	@SuppressWarnings("unchecked")
	public static Answer getAnswerForInterviewQuestionAlters(
			Session session, Interview interview, Question question, ArrayList<Alter> alters) 
	{
		Integer numAlters = alters.size();
		if(question.getType().equals(QuestionType.EGO) || 
				question.getType().equals(QuestionType.EGO_ID)) 
		{
			if(numAlters > 0) {
				throw new IllegalArgumentException(question.getType()+" questions have zero alters, not "+numAlters+".");
			}
		}
		if(question.getType().equals(QuestionType.ALTER)) {
			if(! numAlters.equals(1)) {
				throw new IllegalArgumentException("Alter questions have one alter, not "+numAlters+".");
			}
		}
		if(question.getType().equals(QuestionType.ALTER_PAIR)) {
			if(! numAlters.equals(2)) {
				throw new IllegalArgumentException("Alter pair questions have two alters, not "+numAlters+".");
			}
		}
		String queryString = "from Answer a where a.active = 1 and a.interviewId = :interviewId" +
				" and a.questionId = :questionId";
		if(numAlters > 0) {
			queryString += " and a.alterId1 = :a1";
		}
		if(numAlters > 1) {
			queryString += " and a.alterId2 = :a2";
		}
		Query query = session.createQuery(queryString)
			.setLong("interviewId", interview.getId())
			.setLong("questionId", question.getId());

		if(numAlters.equals(1)) {
			query.setLong("a1", alters.get(0).getId());
		}
		if(numAlters.equals(2)) {
			Boolean smallestFirst = alters.get(0).getId() < alters.get(1).getId();
			query.setLong("a1", alters.get(smallestFirst ? 0 : 1).getId());
			query.setLong("a2", alters.get(smallestFirst ? 1 : 0).getId());
		}
		List<Answer> answers = query.list();
		if(answers.isEmpty()) {
			return null;
		}
		return answers.get(0);
	}
	
	public static void setAnswerForInterviewAndQuestion(
			Session session, Interview interview, Question question, ArrayList<Alter> alters, String answerString) 
	{
		Answer answer = getAnswerForInterviewQuestionAlters(session,interview,question,alters);
		if(answer == null) {
			answer = new Answer(interview,question,answerString);
			if(new Integer(1).equals(alters.size())) {
				answer.setAlterId1(alters.get(0).getId());
			}
			if(new Integer(2).equals(alters.size())) {
				Boolean smallestFirst = alters.get(0).getId() < alters.get(1).getId();
				answer.setAlterId1(alters.get(smallestFirst ? 0 : 1).getId());
				answer.setAlterId2(alters.get(smallestFirst ? 1 : 0).getId());
			}
		} else {
			answer.setValue(answerString);
		}
		DB.save(answer);
	}
	
	public static void setAnswerForInterviewAndQuestion(
			final Long interviewId, final Question question, final String answerString) {
		setAnswerForInterviewQuestionAlters(interviewId,question,new ArrayList<Alter>(),answerString);
	}
	
	public static void setAnswerForInterviewQuestionAlters(
			final Long interviewId, final Question question, 
			final ArrayList<Alter> alters, final String answerString) {
		DB.withTx(new DB.Action<Object>() {
			public Object get() {
				setAnswerForInterviewAndQuestion(
						session,
						Interviews.getInterview(session, interviewId),
						question,alters,answerString);
				return null;
			}
		});
	}
}
