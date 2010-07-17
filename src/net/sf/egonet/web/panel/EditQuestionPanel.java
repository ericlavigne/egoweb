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

	/*
	 * For NETWORK questions, the question-editor panel will have several
	 * DropDownChoice controls listing ALTER and ALTER_PAIR questions.
	 * The interview author can select these questions to set parameters
	 * used in network graph rendering, varying node/edge parameters
	 * based on an interviewee's response. 
	 * 
	 * The NetworkQuestionTId class contains the title and ID# for a question
	 * listed in one of these DropDownChoice controls, and the ID# will be used
	 * to configure the NETWORK question.
	 */
	private class NetworkQuestionTId
	{
		public String title;
		public Long id;

		public NetworkQuestionTId()
		{
		}

		public NetworkQuestionTId(String _title, Long _id)
		{
			title = _title;
			id = _id;
		}

		@Override
		public String toString()
		{
			return title;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null)
				return false;
			if (getClass() != o.getClass())
				return false;
			final NetworkQuestionTId toCompare = (NetworkQuestionTId)o;
			return (title.equals(toCompare.title) && (id.compareTo(toCompare.id) == 0));

		}

		@Override
		public int hashCode()
		{
			int hash = 1;
			if (title != null)
				hash = hash * 31 + title.hashCode();
			if (id != null)
				hash = hash * 31 + id.hashCode();
			return hash;
		}
	}

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
	private Model symmetricModel;
	private Model dontKnowModel;
	private Model refuseModel;
	private Model keepOnSamePageModel;
	private Label otherSpecifyLabel; 
	private CheckBox otherSpecifyCheckBox;
	private Label symmetricLabel;
	private CheckBox symmetricCheckBox;
	private Label keepOnSamePageLabel;
	private CheckBox keepOnSamePageCheckBox;
	private DropDownChoice dropDownQuestionTypes;  
	private CheckBox cbDontKnow;
	private CheckBox cbRefuse;
	
	private NumericLimitsPanel numericLimitsPanel;
	private MultipleSelectionLimitsPanel multipleSelectionLimitsPanel;
	private TimeUnitsPanel timeUnitsPanel;
	private ListLimitsPanel listLimitsPanel;
	
	private static final String answerAlways = "Always";

	private Model networkRelationshipExpr;
	private NetworkQuestionTId nodeShapeId;
	private NetworkQuestionTId nodeColorId;
	private NetworkQuestionTId nodeSizeId;
	private NetworkQuestionTId edgeColorId;
	private NetworkQuestionTId edgeSizeId;

	private DropDownChoice networkRelationshipExprChoice;
	private DropDownChoice nodeShapeIdChoice;
	private DropDownChoice nodeColorIdChoice;
	private DropDownChoice nodeSizeIdChoice;
	private DropDownChoice edgeColorIdChoice;
	private DropDownChoice edgeSizeIdChoice;
	
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

		// For NETWORK questions, default to a large text box for answer-entry
		boolean networkQuestion = question.getType().equals(Question.QuestionType.NETWORK);
		if (networkQuestion)
			questionResponseTypeModel = new Model(Answer.AnswerType.TEXTUAL_PP);
		else
			questionResponseTypeModel = new Model(Answer.AnswerType.TEXTUAL); 

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
				question.getType().equals(Question.QuestionType.EGO_ID) ||
				question.getType().equals(Question.QuestionType.NETWORK))
		{
			askingStyleListLabel.setVisible(false);
			askingStyleListField.setVisible(false);
		}

		dontKnowModel = new Model();
		dontKnowModel.setObject(Boolean.TRUE);
		refuseModel = new Model();
		refuseModel.setObject(Boolean.TRUE);
		lblOptionalButtons = new Label("optionalButtons", "Optional Buttons: ");
		lblDontKnowBtn = new Label("dontKnowBtn", "DON'T KNOW: ");
		lblRefuseBtn = new Label("refuseBtn", "REFUSE: ");
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
		
		symmetricLabel = new Label("symmetricLabel", "Symmetric Question?: ");
		symmetricModel = new Model();
		symmetricModel.setObject(Boolean.TRUE);
		symmetricCheckBox = new CheckBox("symmetricField", symmetricModel);
		form.add(symmetricLabel);
		form.add(symmetricCheckBox);
		symmetricLabel.setOutputMarkupId(true);
		symmetricCheckBox.setOutputMarkupId(true);
		symmetricLabel.setOutputMarkupPlaceholderTag(true);
		symmetricCheckBox.setOutputMarkupPlaceholderTag(true);
		if( !question.getType().equals(Question.QuestionType.ALTER_PAIR )) {
			symmetricLabel.setVisible(false);
			symmetricCheckBox.setVisible(false);
		}
		keepOnSamePageLabel = new Label("keepOnSamePageLabel", "Keep on same page as prev question?");
		keepOnSamePageModel = new Model();
		keepOnSamePageModel.setObject(Boolean.FALSE);
		keepOnSamePageCheckBox = new CheckBox("keepOnSamePageField", keepOnSamePageModel);
		form.add(keepOnSamePageLabel);
		form.add(keepOnSamePageCheckBox);
		keepOnSamePageLabel.setOutputMarkupId(true);
		keepOnSamePageCheckBox.setOutputMarkupId(true);
		keepOnSamePageLabel.setOutputMarkupPlaceholderTag(true);
		keepOnSamePageCheckBox.setOutputMarkupPlaceholderTag(true);
		if ( !question.ALLOW_MULTIPLE_QUESTIONS_PER_PAGE  ||  
			  question.getType().equals(Question.QuestionType.EGO_ID)) {
			keepOnSamePageLabel.setVisible(false);
			keepOnSamePageCheckBox.setVisible(false);
		}
		// questionUseIfField = new TextField("questionUseIfField", new Model(""));
		// form.add(questionUseIfField);
		
		
		// Configuration options for network questions (hidden/not populated for other question types)
		if (networkQuestion)
		{
			List<NetworkQuestionTId> alterQs = new ArrayList<NetworkQuestionTId>();
			for (Question q : Questions.getQuestionsForStudy(question.getStudyId(), Question.QuestionType.ALTER))
			{
				alterQs.add(new NetworkQuestionTId(q.getTitle(), q.getId()));
			}

			List<NetworkQuestionTId> alterPairQs = new ArrayList<NetworkQuestionTId>();
			for (Question q : Questions.getQuestionsForStudy(question.getStudyId(), Question.QuestionType.ALTER_PAIR))
			{
				alterPairQs.add(new NetworkQuestionTId(q.getTitle(), q.getId()));
			}

			List<NetworkQuestionTId> alterQsOptional = new ArrayList<NetworkQuestionTId>();
			alterQsOptional.add(null);
			alterQsOptional.addAll(alterQs);

			List<NetworkQuestionTId> alterPairQsOptional = new ArrayList<NetworkQuestionTId>();
			alterPairQsOptional.add(null);
			alterPairQsOptional.addAll(alterPairQs);

			/*
			 * The Expression used to determine whether an edge exists between each pair 
			 * of alters. Without this, the network graph cannot be created.
			 */
			if (networkRelationshipExpr == null)
				networkRelationshipExpr = new Model();
			networkRelationshipExprChoice = new DropDownChoice(
					"networkRelationshipField",
					networkRelationshipExpr,
					Expressions.forStudy(question.getStudyId()));

			nodeShapeId = new NetworkQuestionTId();
			nodeShapeIdChoice = new DropDownChoice(
					"networkNodeShapeField",
					new PropertyModel(this, "nodeShapeId"),
					alterQsOptional);

			nodeColorId = new NetworkQuestionTId();
			nodeColorIdChoice = new DropDownChoice(
					"networkNodeColorField",
					new PropertyModel(this, "nodeColorId"),
					alterQsOptional);

			nodeSizeId = new NetworkQuestionTId();
			nodeSizeIdChoice = new DropDownChoice(
					"networkNodeSizeField",
					new PropertyModel(this, "nodeSizeId"),
					alterQsOptional);

			edgeColorId = new NetworkQuestionTId();
			edgeColorIdChoice = new DropDownChoice(
					"networkEdgeColorField",
					new PropertyModel(this, "edgeColorId"),
					alterPairQsOptional);

			edgeSizeId = new NetworkQuestionTId();
			edgeSizeIdChoice = new DropDownChoice(
					"networkEdgeSizeField",
					new PropertyModel(this, "edgeSizeId"),
					alterPairQsOptional);
		}
		else
		{
			networkRelationshipExprChoice = new DropDownChoice("networkRelationshipField");
			nodeShapeIdChoice = new DropDownChoice("networkNodeShapeField");
			nodeColorIdChoice = new DropDownChoice("networkNodeColorField");
			nodeSizeIdChoice = new DropDownChoice("networkNodeSizeField");
			edgeColorIdChoice = new DropDownChoice("networkEdgeColorField");
			edgeSizeIdChoice = new DropDownChoice("networkEdgeSizeField");
		}
		Label networkRelationshipLabel = new Label("networkRelationshipLabel","Alters are adjacent when:");
		Label networkNodeShapeLabel = new Label("networkNodeShapeLabel", "Alter attribute for network node shape (optional):");
		Label networkNodeColorLabel = new Label("networkNodeColorLabel", "Alter attribute for network node color (optional):");
		Label networkNodeSizeLabel = new Label("networkNodeSizeLabel", "Alter attribute for network node size (optional):");
		Label networkEdgeColorLabel = new Label("networkEdgeColorLabel", "Alter attribute for network edge color (optional):");
		Label networkEdgeSizeLabel = new Label("networkEdgeSizeLabel", "Alter attribute for network edge size (optional):");

		form.add(networkRelationshipExprChoice);
		form.add(nodeShapeIdChoice);
		form.add(nodeColorIdChoice);
		form.add(nodeSizeIdChoice);
		form.add(edgeColorIdChoice);
		form.add(edgeSizeIdChoice);

		form.add(networkRelationshipLabel);
		form.add(networkNodeShapeLabel);
		form.add(networkNodeColorLabel);
		form.add(networkNodeSizeLabel);
		form.add(networkEdgeColorLabel);
		form.add(networkEdgeSizeLabel);

		networkRelationshipExprChoice.setVisible(networkQuestion);
		nodeShapeIdChoice.setVisible(networkQuestion);
		nodeColorIdChoice.setVisible(networkQuestion);
		nodeSizeIdChoice.setVisible(networkQuestion);
		edgeColorIdChoice.setVisible(networkQuestion);
		edgeSizeIdChoice.setVisible(networkQuestion);

		networkRelationshipLabel.setVisible(networkQuestion);
		networkNodeShapeLabel.setVisible(networkQuestion);
		networkNodeColorLabel.setVisible(networkQuestion);
		networkNodeSizeLabel.setVisible(networkQuestion);
		networkEdgeColorLabel.setVisible(networkQuestion);
		networkEdgeSizeLabel.setVisible(networkQuestion);

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
		symmetricCheckBox.setModelObject(question.getSymmetric());
		keepOnSamePageCheckBox.setModelObject(question.getKeepOnSamePage());
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
	
		if (question.getType() == QuestionType.NETWORK)
		{
			nodeShapeId = null;
			nodeColorId = null;
			nodeSizeId = null;
			edgeColorId = null;
			edgeSizeId = null;
			
			Long questionRelationshipExprId = question.getNetworkRelationshipExprId();
			Long questionNShapeQId = question.getNetworkNShapeQId();
			Long questionNColorQId = question.getNetworkNColorQId();
			Long questionNSizeQId = question.getNetworkNSizeQId();
			Long questionEColorQId = question.getNetworkEColorQId();
			Long questionESizeQId = question.getNetworkESizeQId();

			List<Question> alterQs = Questions.getQuestionsForStudy(question.getStudyId(), Question.QuestionType.ALTER);
			List<Question> alterPairQs = Questions.getQuestionsForStudy(question.getStudyId(), Question.QuestionType.ALTER_PAIR);
			
			if (questionRelationshipExprId != null)
			{

				networkRelationshipExpr.setObject(Expressions.get(questionRelationshipExprId));
			}

			if (questionNShapeQId != null)
			{
				for(Question q : alterQs)
				{
					if (questionNShapeQId.compareTo(q.getId()) == 0)
					{
						nodeShapeId = new NetworkQuestionTId(q.getTitle(), q.getId());
						break;
					}
				}
			}
			if (questionNColorQId != null)
			{
				for(Question q : alterQs)
				{
					if (questionNColorQId.compareTo(q.getId()) == 0)
					{
						nodeColorId = new NetworkQuestionTId(q.getTitle(), q.getId());
						break;
					}
				}
			}
			if (questionNSizeQId != null)
			{
				for(Question q : alterQs)
				{
					if (questionNSizeQId.compareTo(q.getId()) == 0)
					{
						nodeSizeId = new NetworkQuestionTId(q.getTitle(), q.getId());
						break;
					}
				}
			}
			if (questionEColorQId != null)
			{
				for(Question q : alterPairQs)
				{
					if (questionEColorQId.compareTo(q.getId()) == 0)
					{
						edgeColorId = new NetworkQuestionTId(q.getTitle(), q.getId());
						break;
					}
				}
			}
			if (questionESizeQId != null)
			{
				for(Question q : alterPairQs)
				{
					if (questionESizeQId.compareTo(q.getId()) == 0)
					{
						edgeSizeId = new NetworkQuestionTId(q.getTitle(), q.getId());
						break;
					}
				}
			}
		}
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
		question.setSymmetric((Boolean) symmetricCheckBox.getModelObject()); 
		question.setKeepOnSamePage((Boolean) keepOnSamePageCheckBox.getModelObject());
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
		}
		if (question.getType() == QuestionType.NETWORK)
		{
			if (networkRelationshipExpr != null)
			{
				Expression adjacencyExpr = (Expression) networkRelationshipExpr.getObject();
				question.setNetworkRelationshipExprId(
					adjacencyExpr == null? null : adjacencyExpr.getId());
			}
			else
			{
			}
			question.setNetworkNShapeQId(
				nodeShapeId == null ? null : ((NetworkQuestionTId)nodeShapeId).id);
			question.setNetworkNColorQId(
				nodeColorId == null ? null : ((NetworkQuestionTId)nodeColorId).id);
			question.setNetworkNSizeQId(
				nodeSizeId == null ? null : ((NetworkQuestionTId)nodeSizeId).id);
			question.setNetworkEColorQId(
				edgeColorId == null ? null : ((NetworkQuestionTId)edgeColorId).id);
			question.setNetworkESizeQId(
				edgeSizeId == null ? null : ((NetworkQuestionTId)edgeSizeId).id);
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
