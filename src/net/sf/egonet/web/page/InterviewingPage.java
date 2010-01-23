package net.sf.egonet.web.page;

import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;
import net.sf.egonet.web.panel.InterviewNavigationPanel;

public class InterviewingPage extends EgonetPage {
	public InterviewingPage(Long interviewId) {
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");

		add(new InterviewNavigationPanel("navigation",interviewId));
	}
	
	public final static String 
		dontKnow = AnswerFormFieldPanel.dontKnow, 
		refuse = AnswerFormFieldPanel.refuse,
		none = AnswerFormFieldPanel.none;
}
