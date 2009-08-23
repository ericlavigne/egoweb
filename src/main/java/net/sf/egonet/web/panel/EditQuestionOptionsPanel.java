package net.sf.egonet.web.panel;

import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.DB;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class EditQuestionOptionsPanel extends Panel {

	private Question question;
	
	private Form form;
	private TextField optionTitleField;
	
	public EditQuestionOptionsPanel(String id, Question question) {
		super(id);
		this.question = question;
		build();
	}
	
	private void build() {
		add(new Label("questionTitle",question.getTitle()));
		form = new Form("optionForm");

		optionTitleField = new TextField("optionTitleField", new Model(""));
		optionTitleField.setRequired(true);
		form.add(optionTitleField);
		
		form.add(
			new Button("addOption")
            {
				@Override
				public void onSubmit()
                {
					DB.save(new QuestionOption(question.getId(),optionTitleField.getModelObjectAsString()));
				}
			}
        );
		
		add(form);
	}
}
