package net.sf.egonet.web.page;

import net.sf.egonet.web.panel.StudyListPanel;

public class InterviewingPage extends EgonetPage {
	public InterviewingPage() {
		super("Interviewing - Select a study");
		
		add(new StudyListPanel("studyList"));
	}
}
