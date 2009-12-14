package net.sf.egonet.web.page;

import org.apache.wicket.markup.html.basic.MultiLineLabel;

import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;

public class InterviewingConclusionPage extends EgonetPage {
	public InterviewingConclusionPage(Long interviewId) {
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Finished interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		
        add(new MultiLineLabel("conclusion", Studies.getStudyForInterview(interviewId).getConclusion()));
	}
}
