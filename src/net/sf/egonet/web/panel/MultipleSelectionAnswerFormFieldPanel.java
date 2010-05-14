package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.persistence.Options;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;

public class MultipleSelectionAnswerFormFieldPanel extends AnswerFormFieldPanel 
	implements ActionListener {

	private CheckboxesPanel<Object> answerField;
	private ArrayList<QuestionOption> originallySelectedOptions;
	// variables dealing with lower & upper limits on the
	// number of checkboxes we can select:
	private Label checkBoxPrompt;
	private Integer maxCheckable;
	private Integer minCheckable;
	// these variables deal with the textbox
	// that appears if the 'Other' checkbox is selected;
	private Label otherSpecifyLabel;
	private TextField otherSpecifyTextField;
	private String otherText;
	private boolean otherSpecifyStyle;
	
	public MultipleSelectionAnswerFormFieldPanel(String id, Question question, 
			ArrayList<Alter> alters, Long interviewId) {
		super(id,question,Answer.SkipReason.NONE,alters,interviewId);
		originallySelectedOptions = Lists.newArrayList();
		setOtherText("");
		build();
	}
	
	public MultipleSelectionAnswerFormFieldPanel(String id, 
			Question question, String answer, String otherSpecText, Answer.SkipReason skipReason, ArrayList<Alter> alters, Long interviewId) 
	{
		super(id,question,skipReason,alters,interviewId);
		originallySelectedOptions = Lists.newArrayList();
		setOtherText(otherSpecText);
		try {
			for(String answerIdString : answer.split(",")) {
				Long answerId = Long.parseLong(answerIdString);
				for(QuestionOption option : getOptions()) {
					if(answerId.equals(option.getId())) {
						originallySelectedOptions.add(option);
					}
				}
			}
		} catch(Exception ex) {
			// Most likely failed to parse answer. Fall back to no existing answer.
		}
		build();
	}
	
	private void build() {
		this.setOutputMarkupId(true);
		checkBoxPrompt = new Label ("checkBoxPrompt",getCheckRangePrompt());
		add(checkBoxPrompt);

		List<Object> allItems = Lists.newArrayList();
		allItems.addAll(getOptions());
		if(! question.getType().equals(Question.QuestionType.EGO_ID)) { // Can't refuse EgoID question
			allItems.add(dontKnow);
			allItems.add(refuse);
		}
		List<Object> selectedItems = Lists.newArrayList();
		if(originalSkipReason.equals(Answer.SkipReason.NONE)) {
			selectedItems.addAll(originallySelectedOptions);
		} else if(originalSkipReason.equals(Answer.SkipReason.DONT_KNOW)) {
			selectedItems.add(dontKnow);
		} else if(originalSkipReason.equals(Answer.SkipReason.REFUSE)) {
			selectedItems.add(refuse);
		}
		answerField = new CheckboxesPanel<Object>("answer",allItems,selectedItems) 
		{
			protected String showItem(Object option) {
				return option instanceof QuestionOption ? 
						((QuestionOption) option).getName() : 
							option.toString();
			}
		};
		add(answerField); 
		// features that will be visible only for
		// 'other/specify' questions
		otherSpecifyStyle = question.getOtherSpecify();
		otherSpecifyLabel = new Label("otherSpecifyLabel", "Specify Other: ");
		otherSpecifyTextField = new TextField("otherSpecifyTextField", new PropertyModel(this, "otherText"));
		add(otherSpecifyLabel);
		add(otherSpecifyTextField);
		otherSpecifyLabel.setOutputMarkupId(true);
		otherSpecifyTextField.setOutputMarkupId(true);
		if ( otherSpecifyStyle ) {
			answerField.addActionListener(this);
			answerField.addComponentToUpdate(this);		
			answerField.setOtherSpecifyStyle(true);
		} else {
			answerField.setOtherSpecifyStyle(false);
		}	
		if ( otherSpecifyStyle && answerField.getOtherSelected(true)) {
			otherSpecifyLabel.setVisible(true);
			otherSpecifyTextField.setVisible(true);	
		} else {
			otherSpecifyLabel.setVisible(false);
			otherSpecifyTextField.setVisible(false);
		}
	}

	public String getAnswer() {
		List<String> optionIdStrings = Lists.newArrayList();
		for(Object option : answerField.getSelected()) {
			if(option instanceof QuestionOption) {
				optionIdStrings.add(((QuestionOption) option).getId().toString());
			}
		}
		return Joiner.on(",").join(optionIdStrings);
	}
	
	public boolean dontKnow() {
		return answerField.getSelected().contains(dontKnow);
	}
	
	public boolean refused() {
		return answerField.getSelected().contains(refuse);
	}
	
	public List<QuestionOption> getOptions() {
		return Options.getOptionsForQuestion(getQuestion().getId());
	}
	
	public void setAutoFocus() {
		answerField.setAutoFocus();
	}
	
	/**
	 * creates the string that will tell the surveyer how many checkboxes
	 * to select.
	 * Sets the variables minCheckable and maxCheckable as a side effect
	 * @return prompt to display above checkboxes
	 */
	private String getCheckRangePrompt() {
		Integer checkBoxCount = getOptions().size();
		maxCheckable = question.getMaxCheckableBoxes();
		minCheckable = question.getMinCheckableBoxes();
		
		if ( checkBoxCount<maxCheckable )
			maxCheckable = checkBoxCount;
		if ( maxCheckable.equals(1)  &&  minCheckable.equals(1))
			return("Select just one response please.");
		return("Select " + minCheckable + " to " + maxCheckable + " responses");
	}
	
	/** 
	 * performs simple verification that the number of selected checkboxes
	 * was within the limits.  Returns an integer so we have the option of
	 * rather detailed error messages such as "Please uncheck 2 boxes.", but
	 * for the most part we're concerned whether or not this returns 0
	 * @return 0 if we are in the range , >0 if we have not selected
	 * enough checkboxes, <0 if too many are selected 
	 */
	
	private int multipleSelectionCountStatus() {
		int iSelectedCount;
		
		// if we somehow got in a state where min/max are
		// incompatible, play it safe and do NO error checking
		if ( minCheckable>maxCheckable )
			return(0); 
		
		iSelectedCount = answerField.getSelected().size();
		if ( iSelectedCount < minCheckable ) {
			return(minCheckable-iSelectedCount);
		}
		if (iSelectedCount > maxCheckable ) {
			return(maxCheckable - iSelectedCount);
		}
		return(0);
	}
	
	/**
	 * returns the string used if an incorrect number of checkboxes
	 * are selected to prompt the user to check more or fewer
	 */
	
	public String getRangeCheckNotification() {
		int iCheckBoxStatus;
		String strNotification = "";
		
		if ( dontKnow() || refused())
			return(strNotification);
		iCheckBoxStatus = multipleSelectionCountStatus();
		if ( iCheckBoxStatus<0 ) {
			strNotification = "Too many responses selected"; 
		} else if ( iCheckBoxStatus>0 ) {
			strNotification = "Not enough responses selected";
		} 
		if ( otherSpecifyStyle && otherSpecifyTextField.isVisible() &&
			( otherText==null || otherText.length()==0 ))
			strNotification += " Specify Other blank";
		return(strNotification);
	}
	
	/** 
	 * if the user selected dontKnow or refused to answer a question
	 * don't bother counting the responses.
	 */
	public boolean rangeCheckOkay() {
		boolean bOkay = true;
		
		if ( dontKnow() || refused())
			return (bOkay);
		if (multipleSelectionCountStatus()!=0)
			bOkay = false;
		if ( otherSpecifyStyle && otherSpecifyTextField.isVisible()
			&& ( otherText==null || otherText.length()==0 ))
			bOkay = false;
		return(bOkay);
	}
	
	/**
	 * responds to ActionEvents.
	 * The only object that is apt to send any events is the
	 * answerField.  We will use this interaction to avoid any
	 * hard-coded references to MultipleSelectionAnswerFormat
	 * and maintain loose-binding.
	 */
	public void actionPerformed (ActionEvent event) {
		Object src = event.getSource();
		boolean on = (event.getID()==0) ? false: true;
		
		if ( src==answerField ) {
			otherSpecifyLabel.setVisible(on);
			otherSpecifyTextField.setVisible(on);
			if ( !on )
				setNotification("");
		}
	}
	
	public void setOtherText ( String otherText ) {
		this.otherText = (otherText==null) ? "" : otherText;
		this.otherText = this.otherText.trim();
	}
	public String getOtherText() {
		return (( otherText==null) ? "" : otherText ) ;
	}	
}
