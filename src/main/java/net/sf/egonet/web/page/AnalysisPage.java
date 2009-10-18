package net.sf.egonet.web.page;

import org.apache.wicket.Page;

import net.sf.egonet.model.Study;
import net.sf.egonet.web.panel.StudyListPanel;

public class AnalysisPage extends EgonetPage {
	public AnalysisPage() {
		super("Analysis - Select a study");

		add(new StudyListPanel("studyList") {
			protected Page onStudyClick(Study s) {
				return new AnalysisStudyPage(s);
			}
		});	
	}
}

