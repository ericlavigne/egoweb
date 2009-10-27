package net.sf.egonet.web.page;

import org.apache.wicket.PageParameters;

import net.sf.egonet.model.Study;
import net.sf.egonet.web.panel.StudyListPanel;
import net.sf.functionalj.tuple.Pair;

public class InterviewingPage extends EgonetPage {
	public InterviewingPage() {
		super("Interviewing - Select a study");

		add(new StudyListPanel("studyList") {
			protected Pair<Class<?>,PageParameters> getStudyBookmark(Study s) {
				PageParameters pars = new PageParameters();
				pars.add("studyId", s.getId().toString());
				return new Pair<Class<?>,PageParameters>(InterviewingEgoIDPage.class,pars);
			}
		});	
	}
}
