package net.sf.egonet.persistence;

import java.util.List;

import net.sf.egonet.model.Answer;
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
	
}
