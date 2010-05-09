package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;

import net.sf.egonet.model.AnswerListMgr;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.NameAndValue;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.persistence.Questions;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;

public class EditQuestionOptionsPanel extends Panel {

	private Question question;
	
	private Form addOptionForm;
	private Form optionsForm;
	private TextField optionTitleField;
	private TextField optionValueField;
	private Component parentThatNeedsUpdating;
	private WebMarkupContainer editOptionContainer;
	private Form editOptionForm;
	private QuestionOption currentlyEditing;
	private Label editOptionFormLegend;
	private TextField editOptionTitleField;
	private TextField editOptionValueField;
	private Model selectedPreset, selectedQuestion;
	
	public EditQuestionOptionsPanel(String id, Component parentThatNeedsUpdating, Question question) {
		super(id);
		this.question = question;
		this.parentThatNeedsUpdating = parentThatNeedsUpdating;
		AnswerListMgr.loadAnswerListsForStudy(question.getStudyId());
		build();
	}
	
	private void updateOptionsAndParent(AjaxRequestTarget target) {
		target.addComponent(parentThatNeedsUpdating);
		target.addComponent(optionsForm);
		target.addComponent(editOptionContainer);
	}

	public List<QuestionOption> getOptions() {
		return Options.getOptionsForQuestion(question.getId());
	}
	
	public List<String> getPresetKeys() {
		return new ArrayList<String>(AnswerListMgr.get().keySet());
	}
	
	private ArrayList<Question> selectionQuestions;
	private DateTime selectionQuestionsRefresh;
	
	public List<Question> getOtherQuestionsWithOptions() {
		DateTime now = new DateTime();
		if(selectionQuestionsRefresh == null || 
				selectionQuestionsRefresh.isBefore(now.minusSeconds(1)))
		{
			List<Question> questions = Questions.getQuestionsForStudy(question.getStudyId(), null);
			ArrayList<Question> selectionQuestions = Lists.newArrayList();
			for(Question question : questions)
			{
				if(question.getAnswerType().equals(Answer.AnswerType.SELECTION) ||
						question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION))
				{
					selectionQuestions.add(question);
				}
			}
			this.selectionQuestions = selectionQuestions;
			selectionQuestionsRefresh = now;
		}
		return selectionQuestions;
	}
	
	private void build() {
		add(new Label("questionTitle",question.getTitle()));

		optionsForm = new Form("optionsForm");
		optionsForm.setOutputMarkupId(true);
		
		ListView options = new ListView("options", new PropertyModel(this,"options"))
        {
			protected void populateItem(ListItem item) {
				final QuestionOption option = (QuestionOption) item.getModelObject();

				item.add(new Label("optionTitle", option.getName()+
						(option.getValue() == null || option.getValue().isEmpty() ? 
								"" : " ("+option.getValue()+")")));

				Link deleteLink = new AjaxFallbackLink("optionDelete")
                {
					public void onClick(AjaxRequestTarget target) {
						Options.delete(option);
						editOptionForm.setVisible(false);
						updateOptionsAndParent(target);
					}
				};
				item.add(deleteLink);
				
				Link moveLink = new AjaxFallbackLink("optionMoveUp")
                {
					public void onClick(AjaxRequestTarget target) {
						Options.moveEarlier(option);
						editOptionForm.setVisible(false);
						updateOptionsAndParent(target);
					}
				};
				item.add(moveLink);
				
				Link editLink = new AjaxFallbackLink("optionEdit")
                {
					public void onClick(AjaxRequestTarget target) {
						editOption(target,option);
					}
				};
				item.add(editLink);
			}
		};
		optionsForm.add(options);
		
		optionsForm.add(new AjaxFallbackLink("optionDeleteAll") {
			public void onClick(AjaxRequestTarget target) {
				Questions.deleteOptionsFor(question);
				editOptionForm.setVisible(false);
				updateOptionsAndParent(target);
			}
		});

		add(optionsForm);
		
		addOptionForm = new Form("addOptionForm");

		optionTitleField = new TextField("optionTitleField", new Model(""));
		optionTitleField.setRequired(true);
		optionTitleField.setOutputMarkupId(true);
		optionTitleField.add(new FocusOnLoadBehavior());
		addOptionForm.add(optionTitleField);
		
		optionValueField = new TextField("optionValueField", new Model(""));
		optionValueField.setOutputMarkupId(true);
		addOptionForm.add(optionValueField);
		
		addOptionForm.add(
			new AjaxFallbackButton("addOption",addOptionForm)
            {
				protected void onSubmit(AjaxRequestTarget target, Form f) {
					Options.addOption(question, 
							optionTitleField.getText(), 
							optionValueField.getText());
					optionTitleField.setModelObject("");
					optionValueField.setModelObject("");
					editOptionForm.setVisible(false);
					updateOptionsAndParent(target);
					target.addChildren(f, TextField.class);
				}
			}
        );
		
		add(addOptionForm);

		editOptionContainer = new WebMarkupContainer("editOptionContainer");
		editOptionContainer.setOutputMarkupId(true);
		
		editOptionForm = new Form("editOptionForm");
		editOptionForm.setOutputMarkupId(true);
		
		editOptionFormLegend = new Label("editOptionLegend", new Model(""));
		editOptionForm.add(editOptionFormLegend);
		
		editOptionTitleField = new TextField("editOptionTitleField", new Model(""));
		editOptionTitleField.setRequired(true);
		editOptionTitleField.add(new FocusOnLoadBehavior());
		editOptionForm.add(editOptionTitleField);
		
		editOptionValueField = new TextField("editOptionValueField", new Model(""));
		editOptionValueField.setRequired(true);
		editOptionForm.add(editOptionValueField);

		editOptionForm.add(
			new AjaxFallbackButton("editOption",editOptionForm)
            {
				protected void onSubmit(AjaxRequestTarget target, Form f) {
					QuestionOption option = currentlyEditing;
					if(option != null) {
						option.setName(editOptionTitleField.getText());
						String value = editOptionValueField.getText();
						if(value != null && ! value.isEmpty()) {
							option.setValue(value);
						}
						DB.save(option);
					}
					currentlyEditing = null;
					editOptionForm.setVisible(false);
					updateOptionsAndParent(target);
				}
			}
        );
		
		editOptionContainer.add(editOptionForm);
		add(editOptionContainer);
		editOptionForm.setVisible(false);
		
		Form presetForm = new Form("presetForm");
		selectedPreset = new Model();
		presetForm.add(
				new DropDownChoice("selectPreset",selectedPreset,
						new ArrayList<String>(AnswerListMgr.get().keySet())));
		presetForm.add(
				new AjaxFallbackButton("applyPreset",presetForm) {
					protected void onSubmit(AjaxRequestTarget target, Form form) {
						String presetName = (String) selectedPreset.getObject();
						if(presetName != null) {
							for(QuestionOption option : getOptions()) {
								Options.delete(option);
							}
							for(NameAndValue preset : (AnswerListMgr.get().get(presetName))  ) {
								QuestionOption option = new QuestionOption(question.getId(),preset.getName());
								if(preset.equals("Yes")) {
									option.setValue("1");
								} else if(preset.equals("No")) {
									option.setValue("0");
								} else {
									option.setValue(preset.getValue().toString());
								}
								option.setStudyId(question.getStudyId());
								DB.save(option);
							}
							editOptionForm.setVisible(false);
							updateOptionsAndParent(target);
						}
					}
				});
		add(presetForm);
		
		Form copyQuestionForm = new Form("copyQuestionForm");
		selectedQuestion = new Model();
		copyQuestionForm.add(
				new DropDownChoice("selectQuestion",selectedQuestion,
						getOtherQuestionsWithOptions()));
		copyQuestionForm.add(
				new AjaxFallbackButton("applyQuestion",copyQuestionForm) {
					protected void onSubmit(AjaxRequestTarget target, Form form) {
						Question otherQuestion = (Question) selectedQuestion.getObject();
						if(otherQuestion != null) {
							for(QuestionOption option : getOptions()) {
								Options.delete(option);
							}
							for(QuestionOption option : Options.getOptionsForQuestion(otherQuestion.getId())) {
								Options.addOption(question, option.getName(), option.getValue());
							}
							editOptionForm.setVisible(false);
							updateOptionsAndParent(target);
						}
					}
				});
		add(copyQuestionForm);
		
		/*
		ListView presets = new ListView("presets", new PropertyModel(this,"presetKeys"))
		{
			protected void populateItem(ListItem item) {
				final String presetName = item.getModelObjectAsString();
				Link presetLink = new AjaxFallbackLink("presetLink") {
					public void onClick(AjaxRequestTarget target) {
						for(QuestionOption option : getOptions()) {
							Options.delete(option);
						}
						for(String preset : Presets.get().get(presetName)) {
							QuestionOption option = new QuestionOption(question.getId(),preset);
							if(preset.equals("Yes")) {
								option.setValue("1");
							} else if(preset.equals("No")) {
								option.setValue("0");
							}
							DB.save(option);
						}
						editOptionForm.setVisible(false);
						updateOptionsAndParent(target);
					}
				};
				presetLink.add(new Label("presetName",presetName));
				item.add(presetLink);
			}
		};
		add(presets);
		*/
		/*
		ListView otherQuestions = new ListView("otherQuestions", new PropertyModel(this,"otherQuestionsWithOptions"))
		{
			protected void populateItem(ListItem item) {
				final Question otherQuestion = (Question)  item.getModelObject();
				Link link = new AjaxFallbackLink("copyFromOtherQuestion") {
					public void onClick(AjaxRequestTarget target) {
						for(QuestionOption option : getOptions()) {
							Options.delete(option);
						}
						for(QuestionOption option : Options.getOptionsForQuestion(otherQuestion.getId())) {
							Options.addOption(question.getId(), option.getName(), option.getValue());
						}
						editOptionForm.setVisible(false);
						updateOptionsAndParent(target);
					}
				};
				link.add(new Label("otherQuestionName",otherQuestion.getTitle()));
				item.add(link);
			}
		};
		add(otherQuestions);
		*/
	}
	
	private void editOption(AjaxRequestTarget target, QuestionOption option) {
		this.currentlyEditing = option;
		this.editOptionTitleField.setModelObject(option.getName());
		this.editOptionValueField.setModelObject(option.getValue() == null ? "" : option.getValue());
		this.editOptionFormLegend.setModelObject(
				"Editing option: "+option.getName()+" ("+option.getValue()+")");
		this.editOptionForm.setVisible(true);
		updateOptionsAndParent(target);
	}
}
