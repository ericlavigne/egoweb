package net.sf.egonet.web.panel;

import java.util.List;

import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.web.page.EgonetPage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class InterviewNavigationPanel extends Panel {
	
	private Long interviewId;
	
	public InterviewNavigationPanel(String id, Long interviewId) {
		super(id);
		this.interviewId = interviewId;
		build();
	}
	
	public List<EgonetPage> getPages() {
		return Interviewing.getAnsweredPagesForInterview(interviewId);
	}
	
	private void build() {
		add(new ListView("pages", new PropertyModel(this,"pages")) {
			protected void populateItem(ListItem item) {
				final EgonetPage page = (EgonetPage) item.getModelObject();
				Link link = new Link("pageLink") {
					public void onClick() {
						setResponsePage(page);
					}
				};
				link.add(new Label("pageName",page.toString()));
				item.add(link);
			}
		});
	}
}
