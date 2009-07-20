package net.sf.egonet.web.panel;

import java.util.Arrays;
import java.util.List;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.web.model.EntityModel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class EditStudyQuestionsPanel extends Panel {

	private Long studyId;
	private Question.QuestionType questionType;
	private IModel editedQuestion;
	
	private Form form;
	private TextField questionTitleField;
	private TextArea questionPromptField;
	private TextArea questionCitationField;
	private Model questionResponseTypeModel;
	
	public EditStudyQuestionsPanel(String id, Study study, Question.QuestionType questionType) {
		super(id);
		this.studyId = study.getId();
		this.questionType = questionType;
		build();
	}
	
	public Study getStudy() {
		return DB.getStudy(studyId);
	}

	public List<Question> getQuestions() {
		return DB.getQuestionsForStudy(studyId,questionType);
	}
	
	private void build()
    {
		add(new Label("caption",questionType+" Questions"));
		
		add(new FeedbackPanel("feedback"));

		ListView questions = new ListView("questions", new PropertyModel(this,"questions"))
        {
			protected void populateItem(ListItem item) {
				final Question question = (Question) item.getModelObject();

				Link questionLink = new Link("questionLink")
                {
					public void onClick() {
						editedQuestion = new EntityModel(question);
						setFormFieldsFromQuestion(question);
						form.setVisible(true);
					}
				};

				questionLink.add(new Label("questionTitle", question.getTitle()));
				item.add(questionLink);
				item.add(new Label("questionPrompt", question.getPrompt()));
				item.add(new Label("questionResponseType", question.getAnswerType().toString()));
			}
		};
		add(questions);

		add(new Link("newQuestion") {
			public void onClick() {
				editedQuestion = new Model(new Question());
				setFormFieldsFromQuestion(new Question());
				form.setVisible(true);
			}
		});
		
		form = new Form("questionForm");
		
		questionTitleField = new TextField("questionTitleField", new Model(""));
		questionTitleField.setRequired(true);
		form.add(questionTitleField);

		questionPromptField = new TextArea("questionPromptField", new Model(""));
		questionPromptField.setRequired(true);
		form.add(questionPromptField);

		questionCitationField = new TextArea("questionCitationField", new Model(""));
		form.add(questionCitationField);

		questionResponseTypeModel = new Model(Answer.AnswerType.TEXTUAL); // Could also leave this null.
		form.add(new DropDownChoice(
				"questionResponseTypeField",
				questionResponseTypeModel,
				Arrays.asList(Answer.AnswerType.values())));

		form.add(
			new Button("submitQuestion")
            {
				@Override
				public void onSubmit()
                {
					Question question = 
						editedQuestion == null ? new Question() : (Question) editedQuestion.getObject();
					insertFormFieldsIntoQuestion(question);
					DB.save(question);
					form.setVisible(false);
				}
			}
        );
		add(form);
		form.setVisible(false);
	}
	
	private void setFormFieldsFromQuestion(Question question) {
		questionTitleField.setModelObject(question.getTitle());
		questionPromptField.setModelObject(question.getPrompt());
		questionCitationField.setModelObject(question.getCitation());
		questionResponseTypeModel.setObject(question.getAnswerType());
	}
	
	private void insertFormFieldsIntoQuestion(Question question) {
		question.setTitle((String) questionTitleField.getModelObject());
		question.setPrompt((String) questionPromptField.getModelObject());
		question.setCitation((String) questionCitationField.getModelObject());
		question.setAnswerType((Answer.AnswerType) questionResponseTypeModel.getObject());
		question.setType(questionType);
		question.setStudyId(studyId);
	}

}
