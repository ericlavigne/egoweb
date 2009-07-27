package net.sf.egonet.web.page;


import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.web.panel.StudyListPanel;

public class AuthoringPage extends EgonetPage
{
	private TextField studyField;

	public AuthoringPage()
	{
		super("Authoring");

		add(new FeedbackPanel("feedback"));

		Form form = new Form("form");
		studyField = new TextField("studyNameField", new Model(""));
		studyField.setRequired(true);
		form.add(studyField);

		form.add(
			new Button("createStudy")
            {
				@Override
				public void onSubmit()
                {
					String name = (String) studyField.getModelObject();

					studyField.setModelObject("");

                    Study newStudy = new Study(name);
                    DB.save(newStudy);
                }
            }
        );
		add(form);
		
		add(new StudyListPanel("studyList") {
			protected Page onStudyClick(Study s) {
				return new EditStudyPage(s);
			}
		});	
    }
}
