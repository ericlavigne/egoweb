package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Expressions;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.web.component.FocusOnLoadBehavior;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
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
	private TextArea questionPrefaceField;
	private TextArea questionCitationField;
	private Model questionResponseTypeModel;
	private Model questionAnswerReasonModel;
	private static final String answerAlways = "Always";
	
	private Component parentThatNeedsUpdating;

	public EditQuestionPanel(String id, Component parentThatNeedsUpdating, Question question) {
		super(id);
		this.question = question;
		this.parentThatNeedsUpdating = parentThatNeedsUpdating;
		build();
	}
	
	public EditQuestionPanel(String id, Component parentThatNeedsUpdating, 
			Question question, QuestionType questionType, Long studyId) 
	{
		super(id);
		this.question = question;
		question.setType(questionType);
		question.setStudyId(studyId);
		this.parentThatNeedsUpdating = parentThatNeedsUpdating;
		build();
	}
	
	private void build() {

		feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);
		
		form = new Form("questionForm");
		form.setOutputMarkupId(true);
		
		questionTitleField = new TextField("questionTitleField", new Model(""));
		questionTitleField.setRequired(true);
		questionTitleField.add(new FocusOnLoadBehavior());
		form.add(questionTitleField);

		questionPromptField = new TextArea("questionPromptField", new Model(""));
		questionPromptField.setRequired(true);
		form.add(questionPromptField);

		questionPrefaceField = new TextArea("questionPrefaceField", new Model(""));
		form.add(questionPrefaceField);

		questionCitationField = new TextArea("questionCitationField", new Model(""));
		form.add(questionCitationField);

		questionResponseTypeModel = new Model(Answer.AnswerType.TEXTUAL); // Could also leave this null.
		form.add(new DropDownChoice(
				"questionResponseTypeField",
				questionResponseTypeModel,
				Arrays.asList(Answer.AnswerType.values())));

		questionAnswerReasonModel = new Model(answerAlways);
		List<Object> answerChoices = new ArrayList<Object>();
		answerChoices.add(answerAlways);
		for(Expression expression : Expressions.forStudy(question.getStudyId())) {
			answerChoices.add(expression);
		}
		form.add(new DropDownChoice(
				"questionAnswerReasonField",
				questionAnswerReasonModel,
				answerChoices));
		
		form.add(
			new AjaxFallbackButton("submitQuestion",form)
            {
				@Override
				public void onSubmit(AjaxRequestTarget target, Form form)
                {
					insertFormFieldsIntoQuestion(question);
					if(question.getId() == null) {
						List<Question> questions = 
							Questions.getQuestionsForStudy(question.getStudyId(), question.getType());
						questions.add(question);
						for(Integer i = 0; i < questions.size(); i++) {
							questions.get(i).setOrdering(i);
							DB.save(questions.get(i));
						}
					} else {
						DB.save(question);
					}
					form.setVisible(false);
					target.addComponent(parentThatNeedsUpdating);
					target.addComponent(form);
				}
			}
        );
		add(form);
		
		setFormFieldsFromQuestion(question);
	}
	
	private void setFormFieldsFromQuestion(Question question) {
		questionTitleField.setModelObject(question.getTitle());
		questionPromptField.setModelObject(question.getPrompt());
		questionPrefaceField.setModelObject(question.getPreface());
		questionCitationField.setModelObject(question.getCitation());
		questionResponseTypeModel.setObject(question.getAnswerType());
		Long answerReasonId = question.getAnswerReasonExpressionId();
		questionAnswerReasonModel.setObject(
				answerReasonId == null ? 
						answerAlways : Expressions.get(answerReasonId));
	}
	
	private void insertFormFieldsIntoQuestion(Question question) {
		question.setTitle((String) questionTitleField.getModelObject());
		question.setPrompt((String) questionPromptField.getModelObject());
		question.setPreface((String) questionPrefaceField.getModelObject());
		question.setCitation((String) questionCitationField.getModelObject());
		question.setAnswerType((Answer.AnswerType) questionResponseTypeModel.getObject());
		Object answerReason = questionAnswerReasonModel.getObject();
		question.setAnswerReasonExpressionId(
				answerReason == null || answerReason.equals(answerAlways) ?
						null : ((Expression) answerReason).getId());
	}
	
}
