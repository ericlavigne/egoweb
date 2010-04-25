package net.sf.egonet.web.panel;

import java.util.ArrayList;

import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.markup.html.basic.Label;
import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Answer.SkipReason;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;
import net.sf.egonet.web.panel.NumericLimitsPanel.NumericLimitType;

public class NumberAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private TextField textField;
	private CheckboxesPanel<String> refDKCheck;
	private Label lblNumberPrompt;
	private NumberValidator.RangeValidator rangeValidator;
	
	public NumberAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters, Long interviewId) { 
		super(id,question,Answer.SkipReason.NONE,alters,interviewId); 
		build("");
	}
	
	public NumberAnswerFormFieldPanel(String id, 
			Question question, String answer, Answer.SkipReason skipReason, ArrayList<Alter> alters, Long interviewId) 
	{ 
		super(id,question,skipReason,alters,interviewId); 
		build(answer);
	}
	
	private void build(String previousAnswer) {
		textField = new TextField("answer", new Model(previousAnswer), Integer.class);
		add(textField);
		
		lblNumberPrompt = new Label ("numberPrompt", getNumericRangePrompt(alters));
		add(lblNumberPrompt);
		
		ArrayList<String> refAndDK = Lists.newArrayList(refuse,dontKnow);
		if(question.getType().equals(Question.QuestionType.EGO_ID)) {
			refAndDK = Lists.newArrayList();
		}
		ArrayList<String> selectedRefAndDK = Lists.newArrayList();
		if(originalSkipReason.equals(SkipReason.DONT_KNOW)) {
			selectedRefAndDK.add(dontKnow);
		}
		if(originalSkipReason.equals(SkipReason.REFUSE)) {
			selectedRefAndDK.add(refuse);
		}
		refDKCheck = new CheckboxesPanel<String>("refDKCheck",
				refAndDK,selectedRefAndDK);
		add(refDKCheck);
		setBounds(alters);
	}

	public String getAnswer() {
		return textField.getText();
	}
	
	public void setAutoFocus() {
		textField.add(new FocusOnLoadBehavior());
	}

	public boolean dontKnow() {
		return refDKCheck.getSelected().contains(dontKnow);
	}

	public boolean refused() {
		return refDKCheck.getSelected().contains(refuse);
	}
	
	// the next two functions are virtual duplicates, differing only in 
	// that one deals with integers and the other with strings.
	// they could be merged, but are separate now 
	/**
	 * IF the question requires a numeric answer and a range has been set
	 * this displays the allowed range.
	 * @param alterId1 - ID of alter number 1 (may be null)
	 * @param alterId2 - ID of alter number 2 (may be null)
	 * @return a string that shows the allowed numeric input range
	 */
	
	public String getNumericRangePrompt(ArrayList<Alter> listOfAlters) {
		String strAnswer;
		String strReturn;
		NumericLimitType minLimitType;
		NumericLimitType maxLimitType;
		boolean hasLowBound; 
        boolean hasHighBound; 
        
        minLimitType = question.getMinLimitType();
        maxLimitType = question.getMaxLimitType();
        
        hasLowBound  = (minLimitType==NumericLimitType.NLT_NONE)?false:true;
        hasHighBound = (maxLimitType==NumericLimitType.NLT_NONE)?false:true;
        
        if ( !hasLowBound && !hasHighBound )
        	return("(number)");
		
		strReturn = "(number [";
		switch ( minLimitType ) {
			case NLT_NONE:     strReturn += " up"; break;
			case NLT_LITERAL:  strReturn += question.getMinLiteral(); break;
			case NLT_PREVQUES: 
				 strAnswer = question.answerToRangeLimitingQuestion(interviewId, alters, false);
				 strReturn += strAnswer; 
				 break;
		}
		
		switch ( maxLimitType ) {
			case NLT_NONE:     strReturn += " and up"; break;
			case NLT_LITERAL:  strReturn += " to " + question.getMaxLiteral(); break;
			case NLT_PREVQUES: 
				 strAnswer = question.answerToRangeLimitingQuestion(interviewId, alters, true);
				 strReturn += " to " + strAnswer; 
				 break;
		}
		strReturn += "])";
		return(strReturn);
	}

	/**
	 * grabs values from the question to construct the range validator
	 * if there are no bounds or the lowbound>highbound the validator
	 * is not set and false is returned
	 */
	
	private boolean setBounds(ArrayList<Alter> listOfAlters) {
		String strAnswer;
		NumericLimitType minLimitType;
		NumericLimitType maxLimitType;
		boolean hasLowBound; 
        boolean hasHighBound; 
        int lowBound = 0;
        int highBound = 10000;
        
        minLimitType = question.getMinLimitType();
        maxLimitType = question.getMaxLimitType();
        
        hasLowBound  = (minLimitType==NumericLimitType.NLT_NONE)?false:true;
        hasHighBound = (maxLimitType==NumericLimitType.NLT_NONE)?false:true;
        
        if ( !hasLowBound && !hasHighBound )
        	return(false);
        
        switch (minLimitType ) {
        	case NLT_NONE: break;
        	case NLT_LITERAL: lowBound = question.getMinLiteral(); break;
        	case NLT_PREVQUES: 
        		 strAnswer = question.answerToRangeLimitingQuestion(interviewId, alters, false);
        		 try { 
        			 lowBound = Integer.parseInt(strAnswer);
        		 } catch ( NumberFormatException nfe) {
        			 lowBound = Integer.MIN_VALUE;
        		 }
        		 break;
        	}
        
        switch (maxLimitType ) {
    		case NLT_NONE:    break;
    		case NLT_LITERAL: highBound = question.getMaxLiteral(); break;
    		case NLT_PREVQUES: 
    			 strAnswer = question.answerToRangeLimitingQuestion(interviewId, alters, true);
    			 try { 
    				 highBound = Integer.parseInt(strAnswer);
    			 } catch ( NumberFormatException nfe) {
    				 highBound = Integer.MAX_VALUE;
    			 }
    			 break;
        	}
        
        // if we somehow got to a state where the lowBound > highBound
        // play it safe and don't set any bounds;
        if ( lowBound>highBound )
        	return(false);
		rangeValidator = new 
		NumberValidator.RangeValidator(lowBound,highBound);
		textField.add(rangeValidator);
		return(true);
	}
}

