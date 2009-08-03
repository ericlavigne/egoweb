package net.sf.egonet.web.page;

import net.sf.egonet.persistence.DB;

public class InterviewingEgoPage extends EgonetPage {
	
	private Long interviewId;
	
	public InterviewingEgoPage(Long interviewId) {
		super(DB.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+DB.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		this.interviewId = interviewId;
	}

}
