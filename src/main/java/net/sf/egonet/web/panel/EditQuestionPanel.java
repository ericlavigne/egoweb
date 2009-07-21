package net.sf.egonet.web.panel;

import java.util.Arrays;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.persistence.DB;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class EditQuestionPanel extends Panel {
	
	private Question question;

	private FeedbackPanel feedbackPanel;
	private Form form;
	private TextField questionTitleField;
	private TextArea questionPromptField;
	private TextArea questionCitationField;
	private Model questionResponseTypeModel;

	public EditQuestionPanel(String id, Question question) {
		super(id);
		this.question = question;
		build();
	}
	
	public EditQuestionPanel(String id, Question question, QuestionType questionType, Long studyId) {
		super(id);
		this.question = question;
		question.setType(questionType);
		question.setStudyId(studyId);
		build();
	}
	
	private void build() {

		feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);
		
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
					insertFormFieldsIntoQuestion(question);
					DB.save(question);
					form.setVisible(false);
				}
			}
        );
		add(form);
		
		setFormFieldsFromQuestion(question);
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
	}
	
}
