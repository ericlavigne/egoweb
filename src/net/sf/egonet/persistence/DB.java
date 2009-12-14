package net.sf.egonet.persistence;

import net.sf.egonet.model.Entity;
import net.sf.egonet.web.Main;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.common.base.Function;

public class DB {

	static void save(Session session, Entity e) {
		session.saveOrUpdate(e);
	}
	
	public static void save(final Entity e) {
		withTx(new Function<Session,Object>(){
			public Object apply(Session s) {
				save(s,e);
				return null;
			}
		});
	}
	
	static void delete(Session s, Entity e) {
		e.setActive(false);
		s.saveOrUpdate(e);
	}
	
	static void delete(final Entity e) {
		withTx(new Function<Session,Object>(){
			public Object apply(Session s) {
				delete(s,e);
				return null;
			}
		});
	}

	static <E> E withTx(Function<Session,E> f) {
		Session session = Main.getDBSessionFactory().openSession();
		Transaction tx = session.beginTransaction();

		E result = f.apply(session);

		tx.commit();
		session.close();

		return result;
	}
	
	static abstract class Action<R> implements Function<Session,R> {
		protected Session session;
		
		public R apply(Session session) {
			this.session = session;
			return get();
		}
		
		public abstract R get();
		
		public R execute() {
			return withTx(this);
		}
	}
	
	public static void migrate() {
		new Action<Object>() {
			public Object get() {
				// TODO: Need to store schema version so each migration can be applied exactly once.
				/*
				for(String entity : new String[]{
						"Alter","Answer","Expression","Interview","Question","QuestionOption","Study"}) 
				{
					this.session.createQuery(
							"update "+entity+" set active = 1 where active is null")
							.executeUpdate();
				}
				*/
				return null;
			}
		}.execute();
	}
}
