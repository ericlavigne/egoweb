package net.sf.egonet.web.model;

import net.sf.egonet.model.Entity;
import net.sf.egonet.web.Main;

import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class EntityModel extends LoadableDetachableModel {

	private Long id;
	private String className;
	
	private transient Entity entity;
	private transient Session session;
	private transient Transaction tx;
	
	public EntityModel(Entity entity) {
		this.entity = entity;
		this.id = entity.getId();
		this.className = entity.getClass().getCanonicalName();
	}

	public void save() {
		if(entity != null) {
			ensureSessionAvailable();
			session.saveOrUpdate(entity);
		}
	}
	
	private void ensureSessionAvailable() {
		if(session == null || tx == null) {
			session = Main.getDBSessionFactory().openSession();
			tx = session.beginTransaction();
		}
	}
	
	@Override
	protected Entity load() {
		if(entity == null) {
			ensureSessionAvailable();
			entity = (Entity) 
			session.createQuery("from "+className+" e where e.id = :id")
			.setLong("id", id)
			.uniqueResult();
		}
		
		return entity;
	}
	
	protected void onDetach() {
		if(tx != null) {
			tx.commit();
		}
		if(session != null) {
			session.close();
		}
		tx = null;
		session = null;
	}
}
