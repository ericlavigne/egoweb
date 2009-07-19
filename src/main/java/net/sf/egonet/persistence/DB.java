package net.sf.egonet.persistence;

import java.util.ArrayList;

import net.sf.egonet.model.Entity;
import net.sf.egonet.model.Study;
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
	
	public static ArrayList<Study> getStudies(Session session) {
		return new ArrayList<Study>(session.createQuery("from Study s order by s.name").list());
	}
	
	public static ArrayList<Study> getStudies()
    {
		Session session = Main.getDBSessionFactory().openSession();
		Transaction tx = session.beginTransaction();

		ArrayList<Study> studies = getStudies(session);

		tx.commit();
		session.close();

		return studies;
	}
}
