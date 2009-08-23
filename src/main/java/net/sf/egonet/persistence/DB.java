package net.sf.egonet.persistence;

import net.sf.egonet.model.Entity;
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
	
	public static void delete(final Entity e) {
		withTx(new Function<Session,Object>(){
			public Object apply(Session s) {
				s.delete(e);
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
	}
}
