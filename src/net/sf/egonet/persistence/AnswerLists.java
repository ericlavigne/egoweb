package net.sf.egonet.persistence;

import java.util.List;
import net.sf.egonet.model.AnswerList;
import org.hibernate.Session;
import com.google.common.base.Function;

/**
 * AnswerLists are groups of precreated arrays of strings
 * for use in the Selection and Multiple Selection questions.
 * AnswersLists are in between the PreSets class and the
 * QuestionOptions class
 * @author Kevin
 */
public class AnswerLists {

	/**
	 * returns a list of answers lists for this study
	 * @param session dataBase Session
	 * @param studyId identifies the study we are dealing with
	 * @return list of AnswerLists
	 */
	@SuppressWarnings("unchecked")
	public static List<AnswerList> getAnswerListsUsingStudy (Session session, final Long studyId ) {

		List<AnswerList> returnList;
		returnList = session.createQuery("from AnswerList where studyId = :studyId and active = 1")
		.setParameter("studyId", studyId)
		.list();
		return(returnList);
	}

	/**
	 * used by AnswerListMgr to get all the AnswerLists
	 * for use in this study
	 * @param studyId identifies the study we are interested in
	 * @return list of AnswerLists
	 */
	public static List<AnswerList> getAnswerListsUsingStudy (final Long studyId) {
		return DB.withTx(new Function<Session,List<AnswerList>>() {
			public List<AnswerList> apply(Session session) {
				return ( getAnswerListsUsingStudy(session, studyId));
			}
		});
	}

}
