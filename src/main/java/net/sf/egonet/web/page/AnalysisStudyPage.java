package net.sf.egonet.web.page;

import net.sf.egonet.model.Study;

public class AnalysisStudyPage extends EgonetPage {
	public AnalysisStudyPage(Study study) {
		super("Analysis for "+study.getName());

	}
}

