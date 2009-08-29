package net.sf.egonet.web.panel;

import java.util.List;

import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Studies;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class StudyListPanel extends Panel {
	public StudyListPanel(String id) {
		super(id);

        ListView studyView = new ListView("studies", new PropertyModel(this, "studies"))
        {
			protected void populateItem(ListItem item)
            {
				final Study s = (Study) item.getModelObject();
				Link studyLink = new Link("studyLink")
                {
					public void onClick()
                    {
						Page responsePage = onStudyClick(s);
						if(responsePage != null) {
							setResponsePage(responsePage);
						}
					}
				};
				studyLink.add(new Label("name", s.getName()));
				item.add(studyLink);
			}
		};
		add(studyView);
	}
	
	protected Page onStudyClick(Study study) {
		return null;
	}

	public static List<Study> getStudies()
    {
		return Studies.getStudies();
	}
}
