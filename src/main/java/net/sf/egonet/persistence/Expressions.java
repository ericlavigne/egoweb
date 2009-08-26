package net.sf.egonet.persistence;

import java.util.List;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Question;

import org.hibernate.Session;

import com.google.common.base.Function;

public class Expressions {

	public static List<Expression> forStudy(final Long studyId) {
		return DB.withTx(new Function<Session,List<Expression>>() {
			public List<Expression> apply(Session session) {
				return forStudy(session,studyId);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static List<Expression> forStudy(Session session, Long studyId) {
		return session.createQuery("from Expression e where e.studyId = :studyId")
				.setLong("studyId", studyId).list();
	}
}
