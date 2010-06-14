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
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

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
	private Model askingStyleModel;
	private Model otherSpecifyModel;
	private Model dontKnowModel;
	private Model refuseModel;
	private TextField questionUseIfField;
	private Label otherSpecifyLabel; 
	private CheckBox otherSpecifyCheckBox;
	private DropDownChoice dropDownQuestionTypes;  
	private CheckBox cbDontKnow;
	private CheckBox cbRefuse;
	
	private NumericLimitsPanel numericLimitsPanel;
	private MultipleSelectionLimitsPanel multipleSelectionLimitsPanel;
	private TimeUnitsPanel timeUnitsPanel;
	private ListLimitsPanel listLimitsPanel;
	
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
		Label lblOptionalButtons;
		Label lblDontKnowBtn;
		Label lblRefuseBtn;
		
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
	
		timeUnitsPanel = new TimeUnitsPanel ("timeUnitsPanel", question);
		form.add(timeUnitsPanel);
		timeUnitsPanel.setVisible(false);
		
		listLimitsPanel = new ListLimitsPanel ("listLimitsPanel", question );
		form.add(listLimitsPanel);
		listLimitsPanel.setVisible(question.getAskingStyleList());
		
		questionPromptField = new TextArea("questionPromptField", new Model(""));
		questionPromptField.setRequired(true);
		form.add(questionPromptField);

		questionPrefaceField = new TextArea("questionPrefaceField", new Model(""));
		form.add(questionPrefaceField);

		questionCitationField = new TextArea("questionCitationField", new Model(""));
		form.add(questionCitationField);

		questionResponseTypeModel = new Model(Answer.AnswerType.TEXTUAL); // Could also leave this null.
		dropDownQuestionTypes = new DropDownChoice(
				"questionResponseTypeField",
				questionResponseTypeModel,
				Arrays.asList(Answer.AnswerType.values()));
				
		dropDownQuestionTypes.add(new AjaxFormComponentUpdatingBehavior("onchange") {
		    protected void onUpdate(AjaxRequestTarget target)
		    	{
		        onSelectionChanged( Integer.parseInt(dropDownQuestionTypes.getModelValue()));
		        // target.addComponent(form);
		        target.addComponent(numericLimitsPanel);
		        target.addComponent(multipleSelectionLimitsPanel);
		        target.addComponent(otherSpecifyLabel); 
		        target.addComponent(otherSpecifyCheckBox);
		        target.addComponent(timeUnitsPanel);
		        target.addComponent(listLimitsPanel);
		        }
		   	});
		
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
		AjaxCheckBox askingStyleListField = new AjaxCheckBox("askingStyleListField",askingStyleModel) {
			protected void onUpdate (AjaxRequestTarget target ) {
				Boolean listLimitsVisible = false;
				
//				if ( questionResponseTypeModel.getObject().equals(Answer.AnswerType.MULTIPLE_SELECTION) ||
//					 questionResponseTypeModel.getObject().equals(Answer.AnswerType.SELECTION ))
					listLimitsVisible = (Boolean) askingStyleModel.getObject();
					
			    listLimitsPanel.setVisible(listLimitsVisible);	
				target.addComponent(form); 
			}
		};
		askingStyleListLabel.setOutputMarkupId(true);
		askingStyleListLabel.setOutputMarkupPlaceholderTag(true);
		askingStyleListField.setOutputMarkupId(true);
		askingStyleListField.setOutputMarkupPlaceholderTag(true);
		form.add(askingStyleListLabel);
		form.add(askingStyleListField);
		if(question.getType().equals(Question.QuestionType.EGO) ||
				question.getType().equals(Question.QuestionType.EGO_ID))
		{
			askingStyleListLabel.setVisible(false);
			askingStyleListField.setVisible(false);
		}

		dontKnowModel = new Model();
		dontKnowModel.setObject(Boolean.TRUE);
		refuseModel = new Model();
		refuseModel.setObject(Boolean.TRUE);
		lblOptionalButtons = new Label("optionalButtons", "Optional Buttons: ");
		lblDontKnowBtn = new Label("dontKnowBtn", "Don't know: ");
		lblRefuseBtn = new Label("refuseBtn", "Refuse: ");
    	form.add(lblOptionalButtons);
    	form.add(lblDontKnowBtn);
    	form.add(lblRefuseBtn);
    	
    	cbDontKnow = new CheckBox("dontknow", dontKnowModel);
    	cbRefuse   = new CheckBox("refuse", refuseModel);
    	form.add(cbDontKnow);
    	form.add(cbRefuse);
		if(question.getType().equals(Question.QuestionType.EGO_ID )) {
			lblOptionalButtons.setVisible(false);
			lblDontKnowBtn.setVisible(false);
			lblRefuseBtn.setVisible(false);
			cbDontKnow.setVisible(false);
			cbRefuse.setVisible(false);
		}
		otherSpecifyLabel = new Label("otherSpecifyLabel", "Other/Specify Type Question?: ");
		otherSpecifyModel = new Model();
		otherSpecifyModel.setObject(Boolean.FALSE);
		otherSpecifyCheckBox = new CheckBox("otherSpecifyField", otherSpecifyModel);
		form.add(otherSpecifyLabel);
		form.add(otherSpecifyCheckBox);
		otherSpecifyLabel.setOutputMarkupId(true);
		otherSpecifyCheckBox.setOutputMarkupId(true);
		otherSpecifyLabel.setOutputMarkupPlaceholderTag(true);
		otherSpecifyCheckBox.setOutputMarkupPlaceholderTag(true);
		
		// questionUseIfField = new TextField("questionUseIfField", new Model(""));
		// form.add(questionUseIfField);
		
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
		Answer.AnswerType aType = question.getAnswerType();
		
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
		// questionUseIfField.setModelObject(question.getUseIfExpression());
		otherSpecifyCheckBox.setModelObject(question.getOtherSpecify());
		cbDontKnow.setModelObject(question.getDontKnowButton());
		cbRefuse.setModelObject(question.getRefuseButton());
		if ( aType==Answer.AnswerType.NUMERICAL) {
			numericLimitsPanel.setVisible(true);
		} else if ( aType==Answer.AnswerType.MULTIPLE_SELECTION) {
			multipleSelectionLimitsPanel.setVisible(true);
		}
		if ( aType==Answer.AnswerType.SELECTION  ||  aType==Answer.AnswerType.MULTIPLE_SELECTION) {
			otherSpecifyLabel.setVisible(true); 
			otherSpecifyCheckBox.setVisible(true);
		} else {
			otherSpecifyLabel.setVisible(false); 
			otherSpecifyCheckBox.setVisible(false);	
		}
		if ( aType==Answer.AnswerType.DATE  || aType==Answer.AnswerType.TIME_SPAN ) {
			timeUnitsPanel.setVisible(true);
		} else {
			timeUnitsPanel.setVisible(false);
		}
		numericLimitsPanel.setMinLimitType( question.getMinLimitType());
		numericLimitsPanel.setMinLiteral  ( question.getMinLiteral());
		numericLimitsPanel.setMinPrevQues ( question.getMinPrevQues());
		numericLimitsPanel.setMaxLimitType( question.getMaxLimitType());
		numericLimitsPanel.setMaxLiteral  ( question.getMaxLiteral());
		numericLimitsPanel.setMaxPrevQues ( question.getMaxPrevQues());
		
		multipleSelectionLimitsPanel.setMinCheckableBoxes ( question.getMinCheckableBoxes());
		multipleSelectionLimitsPanel.setMaxCheckableBoxes ( question.getMaxCheckableBoxes());
		
		listLimitsPanel.setQuestion(question);
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
		/// question.setUseIfExpression((String) questionUseIfField.getModelObject());
		question.setOtherSpecify((Boolean)otherSpecifyCheckBox.getModelObject());
		question.setTimeUnits((Integer) timeUnitsPanel.getTimeUnits());
		question.setDontKnowButton((Boolean) cbDontKnow.getModelObject());
		question.setRefuseButton((Boolean) cbRefuse.getModelObject());
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
		if ( askingStyle ) {
	        question.setWithListRange(listLimitsPanel.getWithListRange());
	        question.setListRangeString(listLimitsPanel.getListRangeString());
	        question.setMinListRange(listLimitsPanel.getMinListRange());
	        question.setMaxListRange(listLimitsPanel.getMaxListRange());
	       
	        question.setPageLevelRefuseButton(listLimitsPanel.getPageLevelRefuseButton());
	        question.setPageLevelDontKnowButton(listLimitsPanel.getPageLevelDontKnowButton());
	        question.setAllButton(listLimitsPanel.getAllButton());
	        question.setNoneButton(listLimitsPanel.getNoneButton());
	        question.setAllOptionString(listLimitsPanel.getAllOptionString());
		}
	}
	
	/**
	 * called by the dropdownchoice when its selection changes to 
	 * hide/make visible panels that deal with range limits and the
	 * 'Other/Specify' check box
	 * the value we get from the ajax onUpdate getModelValue()
	 * is a string indicating the position of the selected choice in 
	 * the drop down
	 * @param iValue
	 */
	protected void onSelectionChanged(int iValue) {
		boolean numericLimitsVisible = false;
		boolean multipleSelectionLimitsVisible = false;
		boolean timeSpanUnitsVisible = false;
		boolean weeksVisible = false;
		boolean otherSpecifyVisible = false;
		
		switch ( iValue ) {
			case 0: // TEXT
				 listLimitsPanel.setAnswerType(Answer.AnswerType.TEXTUAL);
			     break;
			case 1:  // NUMERIC
			     numericLimitsVisible = true;
			     listLimitsPanel.setAnswerType(Answer.AnswerType.NUMERICAL);
			     break;
			case 2: // DROP_DOWN - single SELECTION
				 otherSpecifyVisible = true;
				 listLimitsPanel.setAnswerType(Answer.AnswerType.SELECTION);
				 break;
			case 3: // MULTIPLE_SELECTION
			     multipleSelectionLimitsVisible = true;
			     otherSpecifyVisible = true;
			     listLimitsPanel.setAnswerType(Answer.AnswerType.MULTIPLE_SELECTION);
			     break;
			case 4: // DATE
				 listLimitsPanel.setAnswerType(Answer.AnswerType.DATE);
				 timeSpanUnitsVisible = true;
				 weeksVisible = false;
				 break;
			case 5: // TIME_SPAN
				 listLimitsPanel.setAnswerType(Answer.AnswerType.TIME_SPAN);
				 timeSpanUnitsVisible = true;
				 weeksVisible = true;
				 break;
			}
				
		numericLimitsPanel.setVisible(numericLimitsVisible);
		multipleSelectionLimitsPanel.setVisible(multipleSelectionLimitsVisible);
		otherSpecifyLabel.setVisible(otherSpecifyVisible); 
		otherSpecifyCheckBox.setVisible(otherSpecifyVisible);
		timeUnitsPanel.setVisible(timeSpanUnitsVisible);
		timeUnitsPanel.setWeeksVisible(weeksVisible);
	}
}
