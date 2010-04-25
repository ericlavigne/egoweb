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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class EditQuestionPanel extends Panel {
	
	/**
	 * private inner class extending DropDownChoice
	 * This is so it can implement IOnChangeListener and, 
	 * in effect, list to itself and hide the numericLimits Panel
	 * when its type is not Numeric
	 */
	
	private class DropDownChoicePlus extends DropDownChoice {
		
		public DropDownChoicePlus (String id, Model model, List<Answer.AnswerType> theList ) {
			super(id, model, theList);
		}
		
		protected void onSelectionChanged(Object newSelection) {
			boolean numericLimitsVisible = false;
			boolean multipleSelectionLimitsVisible = false;
			
			if ( newSelection==Answer.AnswerType.NUMERICAL) {
				numericLimitsVisible = true;
			} else if ( newSelection==Answer.AnswerType.MULTIPLE_SELECTION){
				multipleSelectionLimitsVisible = true;
			}
			numericLimitsPanel.setVisible(numericLimitsVisible);
			multipleSelectionLimitsPanel.setVisible(multipleSelectionLimitsVisible);		
		}
		
		protected boolean wantOnSelectionChangedNotifications() { return (true);}
	}
	
	/**
	 * end of private inner class DropDownChoicePlus
	 */
	
	private Question question;

	private FeedbackPanel feedbackPanel;
	private Form form;
	private TextField questionTitleField;
	private TextArea questionPromptField;
	private TextArea questionPrefaceField;
	private TextArea questionCitationField;
	private Model questionResponseTypeModel;
	private Model questionAnswerReasonModel;
	private Model askingStyleModel;
	private DropDownChoicePlus dropDownQuestionTypes;
	private NumericLimitsPanel numericLimitsPanel;
	private MultipleSelectionLimitsPanel multipleSelectionLimitsPanel;
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

		if(question.getType().equals(Question.QuestionType.ALTER)) {
			form.add(new Label("promptHelpText", "(Refer to the alter as $$)"));
		} else if(question.getType().equals(Question.QuestionType.ALTER_PAIR)) {
			form.add(new Label("promptHelpText", "(Refer to the alters as $$1 and $$2)"));
		} else {
			form.add(new Label("promptHelpText", ""));
		}
		
		numericLimitsPanel = new NumericLimitsPanel("numericLimitsPanel", question);
		form.add(numericLimitsPanel);
		numericLimitsPanel.setVisible(false);
		
		multipleSelectionLimitsPanel = new MultipleSelectionLimitsPanel ("multipleSelectionLimitsPanel");
		form.add(multipleSelectionLimitsPanel);
		multipleSelectionLimitsPanel.setVisible(false);
		
		questionPromptField = new TextArea("questionPromptField", new Model(""));
		questionPromptField.setRequired(true);
		form.add(questionPromptField);

		questionPrefaceField = new TextArea("questionPrefaceField", new Model(""));
		form.add(questionPrefaceField);

		questionCitationField = new TextArea("questionCitationField", new Model(""));
		form.add(questionCitationField);

		questionResponseTypeModel = new Model(Answer.AnswerType.TEXTUAL); // Could also leave this null.
		dropDownQuestionTypes = new DropDownChoicePlus(
				"questionResponseTypeField",
				questionResponseTypeModel,
				Arrays.asList(Answer.AnswerType.values()));
		
		form.add(dropDownQuestionTypes);

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
		
		Label askingStyleListLabel = new Label("askingStyleListLabel","Ask with list of alters:");
		askingStyleModel = new Model();
		askingStyleModel.setObject(Boolean.FALSE);
		CheckBox askingStyleListField = new CheckBox("askingStyleListField",askingStyleModel);
		form.add(askingStyleListLabel);
		form.add(askingStyleListField);
		if(question.getType().equals(Question.QuestionType.EGO) ||
				question.getType().equals(Question.QuestionType.EGO_ID))
		{
			askingStyleListLabel.setVisible(false);
			askingStyleListField.setVisible(false);
		}
		
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
		String msg = "Asking style in setFormFields: "+askingStyleModel.getObject();
		askingStyleModel.setObject(question.getAskingStyleList());
		msg += " -> "+askingStyleModel.getObject()+" (question had "+question.getAskingStyleList()+")";
		//throw new RuntimeException(msg);
		
		if ( question.getAnswerType()==Answer.AnswerType.NUMERICAL) {
			numericLimitsPanel.setVisible(true);
		} else if ( question.getAnswerType()==Answer.AnswerType.MULTIPLE_SELECTION) {
			multipleSelectionLimitsPanel.setVisible(true);
		}
		numericLimitsPanel.setMinLimitType( question.getMinLimitType());
		numericLimitsPanel.setMinLiteral  ( question.getMinLiteral());
		numericLimitsPanel.setMinPrevQues ( question.getMinPrevQues());
		numericLimitsPanel.setMaxLimitType( question.getMaxLimitType());
		numericLimitsPanel.setMaxLiteral  ( question.getMaxLiteral());
		numericLimitsPanel.setMaxPrevQues ( question.getMaxPrevQues());
		
		multipleSelectionLimitsPanel.setMinCheckableBoxes ( question.getMinCheckableBoxes());
		multipleSelectionLimitsPanel.setMaxCheckableBoxes ( question.getMaxCheckableBoxes());
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
		Boolean askingStyle = (Boolean) askingStyleModel.getObject();
		String msg = "Asking style in insertFormFields (model="+askingStyle+
			"): "+question.getAskingStyleList();
		question.setAskingStyleList(askingStyle); // TODO: need to trace what happens in this method
		msg += " -> "+question.getAskingStyleList();
		// throw new RuntimeException(msg);
		if ( question.getAnswerType()==Answer.AnswerType.NUMERICAL) {
			question.setMinLimitType( numericLimitsPanel.getMinLimitType());
			question.setMinLiteral  ( numericLimitsPanel.getMinLiteral());
			question.setMinPrevQues ( numericLimitsPanel.getMinPrevQues());
			question.setMaxLimitType( numericLimitsPanel.getMaxLimitType());
			question.setMaxLiteral  ( numericLimitsPanel.getMaxLiteral());
			question.setMaxPrevQues ( numericLimitsPanel.getMaxPrevQues());
		} else if ( question.getAnswerType()==Answer.AnswerType.MULTIPLE_SELECTION) {
			question.setMinCheckableBoxes(multipleSelectionLimitsPanel.getMinCheckableBoxes());
			question.setMaxCheckableBoxes(multipleSelectionLimitsPanel.getMaxCheckableBoxes());
		}
	}
	
}
