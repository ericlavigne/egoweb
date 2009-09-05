package net.sf.egonet.persistence;

import java.util.List;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;

import org.hibernate.Session;

public class Answers {

	@SuppressWarnings("unchecked")
	public static List<Answer> getAnswersForInterview(
			Session session, final Long interviewId, final QuestionType questionType) 
	{
		return
		session.createQuery("from Answer a where a.interviewId = :interviewId and a.questionTypeDB = :questionTypeDB")
			.setLong("interviewId", interviewId)
			.setString("questionTypeDB", Question.typeDB(questionType))
			.list();
	}


	@SuppressWarnings("unchecked")
	public static List<Answer> getAnswersForQuestion(
			Session session, final Long questionId) 
	{
		return
		session.createQuery("from Answer a where a.questionId = :questionId")
			.setLong("questionId", questionId)
			.list();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Answer> getAnswersForStudy(
			Session session, final Long studyId) 
	{
		return
		session.createQuery("from Answer a where a.studyId = :studyId")
			.setLong("studyId", studyId)
			.list();
	}
	@SuppressWarnings("unchecked")
	public static List<Answer> getAnswersForStudy(
			Session session, final Long studyId, final QuestionType questionType) 
	{
		return
		session.createQuery("from Answer a where a.studyId = :studyId and a.questionTypeDB = :questionTypeDB")
			.setLong("studyId", studyId)
			.setString("questionTypeDB", Question.typeDB(questionType))
			.list();
	}

	// TODO: Only works for ego questions. Needs extra parameter with list of alters. Check that question type
	// matches the number of alters provided.
	@SuppressWarnings("unchecked")
	public static Answer getAnswerForInterviewAndQuestion(Session session, Interview interview, Question question) 
	{
		List<Answer> answers = session.createQuery(
				"from Answer a where a.interviewId = :interviewId and a.questionId = :questionId")
			.setLong("interviewId", interview.getId())
			.setLong("questionId", question.getId())
			.list();
		if(answers.isEmpty()) {
			return null;
		}
		return answers.get(0);
	}
	
	public static void setAnswerForInterviewAndQuestion(
			Session session, Interview interview, Question question, String answerString) 
	{
		Answer answer = getAnswerForInterviewAndQuestion(session,interview,question);
		if(answer == null) {
			answer = new Answer(interview,question,answerString);
		} else {
			answer.setValue(answerString);
		}
		DB.save(answer);
	}
	
	public static void setAnswerForInterviewAndQuestion(
			final Long interviewId, final Question question, final String answerString) {
		DB.withTx(new DB.Action<Object>() {
			public Object get() {
				setAnswerForInterviewAndQuestion(
						session,
						Interviews.getInterview(session, interviewId),
						question,answerString);
				return null;
			}
		});
	}
}
