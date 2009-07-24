package net.sf.egonet.web.page;

import net.sf.egonet.model.Study;

public class InterviewingEgoIDPage extends EgonetPage {
	public InterviewingEgoIDPage(Study study) {
		super(study.getName());
		
		// Need to learn how to create a dynamic list of text input fields.
		// Until then, first/last name are there as filler.
	}
}
