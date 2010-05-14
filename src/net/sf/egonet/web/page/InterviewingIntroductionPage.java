package net.sf.egonet.web.page;

import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Studies;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class InterviewingIntroductionPage extends EgonetPage {
	
	private Study study;
	private PageParameters parameters;
	
	public InterviewingIntroductionPage(PageParameters parameters) {
		this(Studies.getStudy(Long.parseLong(parameters.getString("studyId"))),parameters);
	}

	private InterviewingIntroductionPage(Study study, PageParameters parameters) {
		super(study.getName());
		this.study = study;
		this.parameters = parameters;
		build();
	}

	private void build() {

        add(new MultiLineLabel("introduction", study.getIntroduction() == null ? "" : 
        			study.getIntroduction()).setEscapeModelStrings(false));
        
        add(new BookmarkablePageLink("egoIDPageLink", InterviewingEgoIDPage.class, parameters));
	}
}
