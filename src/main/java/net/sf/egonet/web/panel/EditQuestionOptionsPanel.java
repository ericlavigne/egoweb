package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.persistence.Presets;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class EditQuestionOptionsPanel extends Panel {

	private Question question;
	
	private Form form;
	private TextField optionTitleField;
	
	public EditQuestionOptionsPanel(String id, Question question) {
		super(id);
		this.question = question;
		build();
	}

	public List<QuestionOption> getOptions() {
		return Options.getOptionsForQuestion(question.getId());
	}
	
	public List<String> getPresetKeys() {
		return new ArrayList<String>(Presets.get().keySet());
	}
	
	private void build() {
		add(new Label("questionTitle",question.getTitle()));

		ListView options = new ListView("options", new PropertyModel(this,"options"))
        {
			protected void populateItem(ListItem item) {
				final QuestionOption option = (QuestionOption) item.getModelObject();

				item.add(new Label("optionTitle", option.getName()));

				Link deleteLink = new Link("optionDelete")
                {
					public void onClick() {
						Options.delete(option);
					}
				};
				item.add(deleteLink);
				
				Link moveLink = new Link("optionMoveUp")
                {
					public void onClick() {
						Options.moveEarlier(option);
					}
				};
				item.add(moveLink);
			}
		};
		add(options);
		
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
		
		ListView presets = new ListView("presets", new PropertyModel(this,"presetKeys"))
		{
			protected void populateItem(ListItem item) {
				final String presetName = item.getModelObjectAsString();
				Link presetLink = new Link("presetLink") {
					public void onClick() {
						for(QuestionOption option : getOptions()) {
							Options.delete(option);
						}
						for(String preset : Presets.get().get(presetName)) {
							DB.save(new QuestionOption(question.getId(),preset));
						}
					}
				};
				presetLink.add(new Label("presetName",presetName));
				item.add(presetLink);
			}
		};
		add(presets);
	}
}
