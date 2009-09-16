package net.sf.egonet.persistence;

import java.util.List;

import net.sf.egonet.model.Alter;

import org.hibernate.Session;

public class Alters {
	@SuppressWarnings("unchecked")
	static List<Alter> getForInterview(Session session, Long interviewId) {
		return session.createQuery("from Alter where interviewId = :interviewId and active = 1")
		.setParameter("interviewId", interviewId)
		.list();
	}
	
	public static List<Alter> getForInterview(final Long interviewId) {
		return new DB.Action<List<Alter>>() {
			public List<Alter> get() {
				return getForInterview(session,interviewId);
			}
		}.execute();
	}
}
