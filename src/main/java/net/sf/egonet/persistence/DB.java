package net.sf.egonet.persistence;

import java.util.ArrayList;
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
	
	public static Object withTx(Function<Session,Object> f) {

		Session session = Main.getDBSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		
		Object result = f.apply(session);
		
		tx.commit();
		session.close();
		
		return result;
	}
	
	public static void save(final Entity e) {
		withTx(new Function<Session,Object>(){
			public Object apply(Session s) {
				s.saveOrUpdate(e);
				return null;
			}
		});
	}
	
	public static List<Study> getStudies(Session session) {
		return new ArrayList<Study>(session.createQuery("from Study s order by s.name").list());
	}

	public static Entity get(Session session, String className, Long id) {
		return (Entity) session.createQuery("from "+className+" e where e.id = :id")
			.setLong("id", id)
			.uniqueResult();
	}
	
	public static Study getStudy(final Long studyId) {
		return (Study) withTx(new Function<Session,Object>(){
			public Object apply(Session session) {
				return get(session,"Study",studyId);
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
		return (List<Question>) withTx(new Function<Session,Object>() {
			public List<Question> apply(Session session) {
				return session.createQuery(
						"from Question q where q.studyId = :studyId and q.typeDB = :type")
				.setLong("studyId", studyId)
				.setString("type", Question.typeDB(type))
				.list();
			}
		});
	}
}
