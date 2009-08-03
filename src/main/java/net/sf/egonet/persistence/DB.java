package net.sf.egonet.persistence;

import java.util.Collection;
import java.util.List;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Entity;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.web.Main;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class DB {

	public static void save(final Entity e) {
		withTx(new Function<Session,Object>(){
			public Object apply(Session s) {
				s.saveOrUpdate(e);
				return null;
			}
		});
	}

	//----------------------------------------

	@SuppressWarnings("unchecked")
	public static List<Study> getStudies(Session session) {
		return
			session.createQuery("from Study s order by s.name")
				.list();
	}

	public static Study getStudy(Session session, final Long id) {
		// Yes, this is different from session.load(Study.class, id),
		// which triggers a lazy initialization exception when any 
		// field of Study is requested after the session is closed.
		return (Study) session.createQuery("from Study where id = :id")
		.setParameter("id", id).uniqueResult();
	}
	public static Study getStudy(final Long id) {
		return withTx(new Function<Session,Study>(){
			public Study apply(Session session) {
				return getStudy(session, id);
			}
		});
	}

	public static Interview getInterview(Session session, final Long id) {
		return (Interview) session.load(Interview.class, id);
	}
	
	public static List<Study> getStudies()
    {
		Session session = Main.getDBSessionFactory().openSession();
		Transaction tx = session.beginTransaction();

		List<Study> studies = getStudies(session);

		tx.commit();
		session.close();

		return studies;
	}

	@SuppressWarnings("unchecked")
	public static List<Question> getQuestionsForStudy(
			Session session, final Long studyId, final QuestionType type) 
	{
		return
		session.createQuery("from Question q where q.studyId = :studyId and q.typeDB = :type")
			.setLong("studyId", studyId)
			.setString("type", Question.typeDB(type))
			.list();
	}
	
	public static List<Question> getQuestionsForStudy(final Long studyId, final QuestionType type) {
		return withTx(new Function<Session,List<Question>>() {
			public List<Question> apply(Session session) {
				return getQuestionsForStudy(session,studyId,type);
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
	
	public static Interview findOrCreateMatchingInterviewForStudy(
			final Long studyId, final List<Answer> egoIdAnswers)
	{
		return withTx(new Function<Session,Interview>(){
			public Interview apply(Session session) {
				Multimap<Long,Answer> answersByInterview = ArrayListMultimap.create();
				for(Answer answer : getAnswersForStudy(session,studyId,QuestionType.EGO_ID)) {
					answersByInterview.put(answer.getInterviewId(), answer);
				}
				for(Long interviewId : answersByInterview.keySet()) {
					Collection<Answer> interviewAnswers = answersByInterview.get(interviewId);
					if(answersMatch(egoIdAnswers,interviewAnswers)) {
						return getInterview(session, interviewId);
					}
				}
				// If reach this point without finding a match, time to start a new interview.
				Interview interview = new Interview(getStudy(studyId));
				Long interviewId = (Long) session.save(interview);
				interview.setId(interviewId);
				List<Question> egoIdQuestions = 
					getQuestionsForStudy(session, studyId, QuestionType.EGO_ID);
				for(Question question : egoIdQuestions) {
					for(Answer answer : egoIdAnswers) {
						if(answer.getQuestionId().equals(question.getId())) {
							save(new Answer(interview,question,answer.getValue()));
						}
					}
				}
				return interview;
			}
			private Boolean answersMatch(Collection<Answer> ego1Answers, Collection<Answer> ego2Answers) {
				for(Answer ego1Answer : ego1Answers) {
					for(Answer ego2Answer : ego2Answers) {
						if(ego1Answer.getQuestionId().equals(ego2Answer.getQuestionId()) &&
								! ego1Answer.getValue().equals(ego2Answer.getValue())) {
							return false;
						}
					}
				}
				return true;
			}
		});
	}
	
	public static Study getStudyForInterview(Session session, Long interviewId) {
		return getStudy(session,getInterview(session,interviewId).getStudyId());
	}
	
	public static Study getStudyForInterview(final Long interviewId) {
		return withTx(new Function<Session,Study>() {
			public Study apply(Session session) {
				return getStudyForInterview(session,interviewId);
			}
		});
	}
	
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
	
	public static String getEgoNameForInterview(Session session, Long interviewId) {
		return Joiner.on(" ").join(
				Lists.transform(getAnswersForInterview(session,interviewId,QuestionType.EGO_ID),
						new Function<Answer,String>(){
							public String apply(Answer answer) {
								return answer.getValue();
							}
						}));
	}
	
	public static String getEgoNameForInterview(final Long interviewId) {
		return withTx(new Function<Session,String>() {
			public String apply(Session session) {
				return getEgoNameForInterview(session,interviewId);
			}
		});
	}
	
	//----------------------------------------

	private static <E> E withTx(Function<Session,E> f) {
		Session session = Main.getDBSessionFactory().openSession();
		Transaction tx = session.beginTransaction();

		E result = f.apply(session);

		tx.commit();
		session.close();

		return result;
	}
}
