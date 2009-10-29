package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.persistence.Presets;
import net.sf.egonet.persistence.Questions;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;

public class EditQuestionOptionsPanel extends Panel {

	private Question question;
	
	private Form addOptionForm;
	private Form optionsForm;
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
	
	public List<Question> getOtherQuestionsWithOptions() {
		List<Question> questionsWithOptions = Lists.newArrayList();
		for(QuestionType type : QuestionType.values()) {
			List<Question> questions = 
				Questions.getQuestionsForStudy(question.getStudyId(), type);
			for(Question question : questions) {
				if(! (this.question.getId().equals(question.getId()) || 
						Options.getOptionsForQuestion(question.getId()).isEmpty())) 
				{
					questionsWithOptions.add(question);
				}
			}
		}
		return questionsWithOptions;
	}
	
	private void build() {
		add(new Label("questionTitle",question.getTitle()));

		optionsForm = new Form("optionsForm") {
			public void onSubmit() {
				// TODO: save changes to option text and value
			}
		};
		optionsForm.setOutputMarkupId(true);
		
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
		optionsForm.add(options);
		
		optionsForm.add(new Link("optionDeleteAll") {
			public void onClick() {
				Questions.deleteOptionsFor(question);
			}
		});

		add(optionsForm);
		
		addOptionForm = new Form("addOptionForm");

		optionTitleField = new TextField("optionTitleField", new Model(""));
		optionTitleField.setRequired(true);
		optionTitleField.setOutputMarkupId(true);
		addOptionForm.add(optionTitleField);
		
		addOptionForm.add(
			new AjaxFallbackButton("addOption",addOptionForm)
            {
				protected void onSubmit(AjaxRequestTarget target, Form f) {
					Options.addOption(question.getId(), optionTitleField.getModelObjectAsString());
					optionTitleField.setModelObject("");
					target.addComponent(optionsForm);
					target.addChildren(f, TextField.class);
				}
			}
        );
		
		add(addOptionForm);

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
		
		ListView otherQuestions = new ListView("otherQuestions", new PropertyModel(this,"otherQuestionsWithOptions"))
		{
			protected void populateItem(ListItem item) {
				final Question otherQuestion = (Question)  item.getModelObject();
				Link link = new Link("copyFromOtherQuestion") {
					public void onClick() {
						for(QuestionOption option : getOptions()) {
							Options.delete(option);
						}
						for(QuestionOption option : Options.getOptionsForQuestion(otherQuestion.getId())) {
							Options.addOption(question.getId(), option.getName());
						}
					}
				};
				link.add(new Label("otherQuestionName",otherQuestion.getTitle()));
				item.add(link);
			}
		};
		add(otherQuestions);
	}
}
