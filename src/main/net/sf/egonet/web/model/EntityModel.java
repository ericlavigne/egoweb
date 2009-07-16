package net.sf.egonet.web.model;

import java.util.ArrayList;

import net.sf.egonet.model.Entity;
import net.sf.egonet.web.Main;

import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class EntityModel<E extends Entity> extends LoadableDetachableModel {

	private Long id;
	private String className;
	
	private transient E entity;
	private transient Session session;
	private transient Transaction tx;
	
	public EntityModel(E entity) {
		this.entity = entity;
		this.id = entity.getId();
		this.className = entity.getClass().getCanonicalName();
	}

	public void save() {
		if(entity != null) {
			ensureSessionAvailable();
			if(entity.getId() == null) {
				this.id = (Long) session.save(entity);
			} else {
				session.saveOrUpdate(entity);
			}
		}
	}
	
	private void ensureSessionAvailable() {
		if(session == null || tx == null) {
			session = Main.getDBSessionFactory().openSession();
			tx = session.beginTransaction();
		}
	}
	
	@Override
	protected E load() {
		if(entity == null) {
			if(this.id == null) {
				return null;
			}
			ensureSessionAvailable();
			ArrayList<E> entityList = 
				new ArrayList<E>(
					session.createQuery("from "+className+" e where e.id = :id")
						.setLong("id", id)
						.list());
			entity = entityList.isEmpty() ? null : entityList.get(0);
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
