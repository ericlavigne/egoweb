package net.sf.egonet.persistence;

import com.google.common.collect.Lists;

import java.util.List;

import net.sf.egonet.model.Entity;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.web.Main;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.common.base.Function;

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

	public static Study getStudy(final Long id) {
		return withTx(new Function<Session,Study>(){
			public Study apply(Session session) {
				return (Study) session.load(Study.class, id);
			}
		});
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

	public static List<Question> getQuestionsForStudy(final Long studyId, final QuestionType type) {
		return withTx(new Function<Session,List<Question>>() {
			@SuppressWarnings("unchecked")
			public List<Question> apply(Session session) {
				return
					session.createQuery("from Question q where q.studyId = :studyId and q.typeDB = :type")
						.setLong("studyId", studyId)
						.setString("type", Question.typeDB(type))
						.list();
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
