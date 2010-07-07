package net.sf.egonet.web.page;

import org.apache.wicket.markup.html.link.Link;

public class InterviewNewOrContinuePage extends EgonetPage {

	public InterviewNewOrContinuePage() {
	add(
            new Link("interviewingLink")
            {
                public void onClick()
                {
                    setResponsePage(new InterviewingStudyListPage());
                }
            }
        );
	add(
            new Link("interviewContinueLink")
            {
                public void onClick()
                {
                    setResponsePage(new InterviewContinuePage());
                }
            }
        );		
	}
	
	public boolean isBookmarkable() {
		return false;
	}
}
