package net.sf.egonet.web.page;

import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;

public class InterviewingEgoPage extends EgonetPage {
	
	private Long interviewId;
	private Question question;
	
	public InterviewingEgoPage(Long interviewId, Question question) {
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+") "
				+" Question: "+question.getPrompt()+" ("+question.getAnswerTypeDB()+")");
		this.interviewId = interviewId;
		this.question = question;
	}

	public static EgonetPage askNextUnanswered(Long interviewId) {
		return new InterviewingEgoPage(
				interviewId,
				Interviewing.nextUnansweredEgoQuestionForInterview(interviewId));
	}
}
