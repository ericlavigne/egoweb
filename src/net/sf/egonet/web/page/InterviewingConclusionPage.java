package net.sf.egonet.web.page;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.basic.MultiLineLabel;

import net.sf.egonet.model.Interview;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.Main;

public class InterviewingConclusionPage extends EgonetPage {
	public InterviewingConclusionPage(Long interviewId) {
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Finished interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		
        add(new MultiLineLabel("conclusion", Studies.getStudyForInterview(interviewId).getConclusion())
        		.setEscapeModelStrings(false));
        
        // getting to this page indicates that this interview is complete,
        // so set the complete flag
        Interview interview = Interviews.getInterview(interviewId);
        interview.setCompleted(new Boolean(true));
    	DB.save(interview);
    	
        Application application = getApplication();
        if (application instanceof Main)
        {
            ((Main)application).interviewHasEnded();
        }

	}
}
