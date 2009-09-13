package net.sf.egonet.web.page;

import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.Interviewing;

public class InterviewingAlterPage extends EgonetPage {
	public InterviewingAlterPage(Long interviewId, Question question) {
		super("alter questions not yet implemented");
	}

	public static EgonetPage askNextUnanswered(Long interviewId) {
		Question nextAlterQuestion = Interviewing.nextUnansweredAlterQuestionForInterview(interviewId);
		if(nextAlterQuestion != null) {
			return new InterviewingAlterPage(interviewId, nextAlterQuestion);
		}
		return new InterviewingConclusionPage(interviewId);
	}
}
