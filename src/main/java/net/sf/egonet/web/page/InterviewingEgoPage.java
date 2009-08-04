package net.sf.egonet.web.page;

import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;

public class InterviewingEgoPage extends EgonetPage {
	
	private Long interviewId;
	
	public InterviewingEgoPage(Long interviewId) {
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		this.interviewId = interviewId;
	}

}
