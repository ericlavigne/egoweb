package net.sf.egonet.web.page;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Alters;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;

public class InterviewingAlterPromptPage extends EgonetPage {

	private Interview interview;
	private TextField addAlterField;
	
	public InterviewingAlterPromptPage(Long interviewId) {
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		this.interview = Interviews.getInterview(interviewId);
		build();
	}
	
	public List<Alter> getAlters() {
		return Alters.getForInterview(interview.getId());
	}
	
	public Study getStudy() {
		return Studies.getStudy(interview.getStudyId());
	}
	
	private void build() {
		add(new Label("alterPrompt",getStudy().getAlterPrompt())); 
		Form form = new Form("form") {
			public void onSubmit() {
				// TODO: add alter based on addAlterField
			}
		};

		addAlterField = new TextField("addAlterField", new Model(""));
		addAlterField.setRequired(true);
		form.add(addAlterField);
		
		add(form);
		
		ListView alters = new ListView("alters", new PropertyModel(this,"alters"))
        {
			protected void populateItem(ListItem item) {
				final Alter alter = (Alter) item.getModelObject();

				item.add(new Label("alterName", alter.getName()));

				Link deleteLink = new Link("alterDelete")
                {
					public void onClick() {
						// DB.delete(alter); // TODO
					}
				};
				item.add(deleteLink);
			}
		};
		add(alters);
		
	}
}
