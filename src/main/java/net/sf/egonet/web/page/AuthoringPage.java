package net.sf.egonet.web.page;

import com.google.common.collect.Lists;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
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
	private Model studyActiveModel;

	public AuthoringPage()
	{
		super("Authoring");

		add(new FeedbackPanel("feedback"));

		Form form = new Form("form");
		studyField = new TextField("studyNameField", new Model(""));
		studyField.setRequired(true);
		form.add(studyField);

		List<String> studyActiveOptions = Lists.newArrayList();
		studyActiveOptions.add("Active");
		studyActiveOptions.add("Inactive");
		studyActiveModel = new Model(studyActiveOptions.get(0)); // Could also leave this null.
		form.add(new DropDownChoice("studyActiveField",studyActiveModel,studyActiveOptions));

		form.add(
			new Button("createStudy")
            {
				@Override
				public void onSubmit()
                {
					String name = (String) studyField.getModelObject();
					String activity = (String) studyActiveModel.getObject();
                    Boolean active = activity.equalsIgnoreCase("active");

					studyField.setModelObject("");

                    Study newStudy = new Study(name);
                    newStudy.setActive(active);
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
