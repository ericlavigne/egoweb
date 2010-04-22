package net.sf.egonet.persistence;

import java.util.List;
import java.util.Set;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.functionalj.tuple.Triple;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class Questions {

	public static void delete(Session session, final Question question) {
		for(Question dbQuestion : matchingQuestionsFor(session,question)) {
			deleteSimpleExpressionsFor(session,dbQuestion);
			deleteAllAnswersForQuestion(session, question.getId());
			deleteOptionsFor(session,dbQuestion);
			removeReferencesInCountingExpressionsFor(session,dbQuestion);
			DB.delete(session, dbQuestion);
		}
	}
	public static void delete(final Question question) {
		DB.withTx(new Function<Session,Object>(){
			public Object apply(Session session) {
				delete(session,question);
				return null;
			}
		});
	}
	
	private static void removeReferencesInCountingExpressionsFor(
			Session session, Question dbQuestion) 
	{
		for(Expression expression : Expressions.forStudy(session,dbQuestion.getStudyId())) {
			if(expression.getType().equals(Expression.Type.Counting)) {
				Triple<Integer,List<Long>,List<Long>> numberExprsQuests =
					(Triple<Integer,List<Long>,List<Long>>) expression.getValue();
				if(numberExprsQuests.getSecond().contains(dbQuestion.getId())) {
					List<Long> newQuests = Lists.newArrayList();
					for(Long quest : numberExprsQuests.getThird()) {
						if(! quest.equals(dbQuestion.getId())) {
							newQuests.add(quest);
						}
					}
					expression.setValue(new Triple<Integer,List<Long>,List<Long>>(
							numberExprsQuests.getFirst(),
							numberExprsQuests.getSecond(),
							newQuests));
					DB.save(session, expression);
				}
			}
		}
	}
	
	private static void deleteAllAnswersForQuestion(Session session, final Long questionId) {
		session.createQuery("update Answer a set active = 0 where a.questionId = :questionId")
		.setParameter("questionId", questionId)
		.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Question> matchingQuestionsFor(Session session, final Question question) {
		return 
			session.createQuery("from Question where id = :id and title = :title and studyId = :studyId and active = 1")
			.setParameter("id", question.getId())
			.setParameter("title", question.getTitle())
			.setParameter("studyId", question.getStudyId())
			.list();
	}

	public static void deleteOptionsFor(final Question question) {
		new DB.Action<Object>() {
			public Object get() {
				deleteOptionsFor(session,question);
				return null;
			}
		}.execute();
	}
	
	public static void deleteOptionsFor(Session session, final Question question) {
		for(Question dbQuestion : matchingQuestionsFor(session,question)) {
			for(QuestionOption option : Options.getOptionsForQuestion(session, dbQuestion.getId())) {
				DB.delete(option);
			}
		}
	}
	public static void deleteSimpleExpressionsFor(Session session, final Question question) {
		for(Question dbQuestion : matchingQuestionsFor(session,question)) {
			for(Expression expression : Expressions.getSimpleExpressionsForQuestion(session, dbQuestion.getId())) {
				Expressions.delete(session, expression);
			}
		}
	}

	public static Question getQuestion(Session session, final Long id) {
		// Yes, this is different from session.load(Study.class, id),
		// which triggers a lazy initialization exception when any 
		// field of Study is requested after the session is closed.
		return (Question) session.createQuery("from Question where id = :id and active = 1")
		.setParameter("id", id).list().get(0);
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
		try {
			Query query = session.createQuery("from Question q where q.active = 1 and q.studyId = :studyId " +
					(type == null ? "" : "and q.typeDB = :type")+ " order by q.ordering")
					.setLong("studyId", studyId);
			if(type != null) {
				query.setString("type", Question.typeDB(type));
			}
			return query.list();
		} catch(Exception ex) {
			throw new RuntimeException(
					"Unable to getQuestionsForStudy("+studyId+","+type+")",
					ex);
		}
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
	
	public static void pull(Session session, Question target, Set<Question> selectedQuestions) {
		List<Question> questions = 
			getQuestionsForStudy(session,target.getStudyId(),target.getType());
		List<Question> newOrder = Lists.newArrayList();
		for(Question question : questions) {
			boolean afterTarget = question.getOrdering() > target.getOrdering();
			boolean selected = selectedQuestions.contains(question);
			if(! (afterTarget || selected)) {
				newOrder.add(question);
			}
		}
		for(Question question : questions) {
			boolean selected = selectedQuestions.contains(question);
			if(selected) {
				newOrder.add(question);
			}
		}
		for(Question question : questions) {
			boolean afterTarget = question.getOrdering() > target.getOrdering();
			boolean selected = selectedQuestions.contains(question);
			if(afterTarget && ! selected) {
				newOrder.add(question);
			}
		}
		for(Integer j = 0; j < newOrder.size(); j++) {
			newOrder.get(j).setOrdering(j);
			DB.save(newOrder.get(j));
		}
	}

	public static void pull(final Question target, final Set<Question> selectedQuestions) {
		DB.withTx(new Function<Session,Object>() {
			public Object apply(Session session) {
				pull(session,target,selectedQuestions);
				return null;
			}
		});
	}
	
	/**
	 * given the title of the question, retrieve the question
	 * @param session database session object
	 * @param title - String Uniquely identifying the question
	 * @param iType - the 'section' ( EGO_ID, EGO, ALTER, ALTER_PAIR ) we are looking at.
	 *  note that it is possible this will be different than the type of the 'original'
	 *  question.  That is, when used with variable substitution we may be looking for
	 *  a question with a given title in a different section.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Question getQuestionUsingTitleAndTypeAndStudy(Session session, 
			final String title, final Question.QuestionType iType, final Long studyId) {
		// Yes, this is different from session.load(Study.class, id),
		// which triggers a lazy initialization exception when any 
		// field of Study is requested after the session is closed.
		List<Question> questionList;
		
		questionList = session.createQuery("from Question where title = :title and typeDB = :typeDB and studyId = :studyId and active = 1")
		.setParameter("title", title)
		.setParameter("typeDB", iType.toString())
		.setParameter("studyId", studyId)
		.list();
		if ( questionList!=null && !questionList.isEmpty())
			return questionList.get(0);
		return(null);
	}
	
	/**
	 * given the title of the question, retrieve the question
	 * @param title - String Uniquely identifying the question
	 * @return
	 */
	public static Question getQuestionUsingTitleAndTypeAndStudy(final String title, 
			final Question.QuestionType iType, final Long studyId) {
		return DB.withTx(new Function<Session,Question>(){
			public Question apply(Session session) {
				return getQuestionUsingTitleAndTypeAndStudy(session, title, iType, studyId);
			}
		});
	}
}
