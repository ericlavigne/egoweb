package net.sf.egonet.persistence;

import java.util.List;

import net.sf.egonet.model.Study;
import net.sf.egonet.web.Main;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.common.base.Function;

public class Studies {

	@SuppressWarnings("unchecked")
	public static List<Study> getStudies(Session session) {
		return
			session.createQuery("from Study s where active = 1 order by s.name")
				.list();
	}

	public static Study getStudy(Session session, final Long id) {
		// Yes, this is different from session.load(Study.class, id),
		// which triggers a lazy initialization exception when any 
		// field of Study is requested after the session is closed.
		return (Study) session.createQuery("from Study where id = :id and active = 1")
		.setParameter("id", id).uniqueResult();
	}
	
	public static Study getStudy(final Long id) {
		return DB.withTx(new Function<Session,Study>(){
			public Study apply(Session session) {
				return getStudy(session, id);
			}
		});
	}


	public static Study getStudyForInterview(Session session, Long interviewId) {
		return getStudy(session, Interviews.getInterview(session,interviewId).getStudyId());
	}
	

	public static Study getStudyForInterview(final Long interviewId) {
		return DB.withTx(new Function<Session,Study>() {
			public Study apply(Session session) {
				return getStudyForInterview(session,interviewId);
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
}
