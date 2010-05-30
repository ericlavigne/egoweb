package net.sf.egonet.persistence;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import net.sf.egonet.model.Entity;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Study;
import net.sf.egonet.web.Main;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

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
		Session session = null;
		Transaction tx = null;
		E result = null;
		try {
			session = Main.getDBSessionFactory().openSession();
			tx = session.beginTransaction();
			result = f.apply(session);
			tx.commit();
		} catch(Exception ex) {
			if(tx != null) {
				tx.rollback();
			}
			throw new RuntimeException("Egoweb transaction failed.",ex);
		} finally {
			session.close();
		}
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
				session.createSQLQuery(
						"create index idx_questionoption_qid_ord on " +
						"question_option(question_id,ordering)")
				.executeUpdate();
				/*
				for(String entity : new String[]{
						"Alter","Answer","Expression","Interview","Question","QuestionOption","Study"}) 
				{
					this.session.createQuery(
							"update "+entity+" set active = 1 where active is null")
							.executeUpdate();
				}
				*/
				TreeMap<Long,Long> qIdToStudyId = Maps.newTreeMap();
				for(Object questionObj : session.createQuery("from Question").list()) {
					Question question = (Question) questionObj;
					qIdToStudyId.put(question.getId(), question.getStudyId());
				}
				for(Object optionObj : session.createQuery("from QuestionOption").list()) {
					QuestionOption option = (QuestionOption) optionObj;
					if(option.getQuestionId() != null && option.getStudyId() == null) {
						option.setStudyId(qIdToStudyId.get(option.getQuestionId()));
						DB.save(session, option);
					}
				}
				for(Study study : Studies.getStudies(session)) {
					List<Expression> expressions = Expressions.forStudy(session, study.getId());
					if((! expressions.isEmpty()) && expressions.get(0).getOrdering() == null) {
						Collections.sort(expressions, 
								new Comparator<Expression>() {
									public int compare(Expression expr1, Expression expr2) {
										return expr1.getId().compareTo(expr2.getId());
									}
						});
						Integer ordering = 0;
						for(Expression expression : expressions) {
							expression.setOrdering(ordering++);
							save(session, expression);
						}
					}
				}
				return null;
			}
		}.execute();
	}
}
