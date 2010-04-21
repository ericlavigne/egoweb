package net.sf.egonet.web.panel;

import java.util.List;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.web.component.TextField;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;

/**
 * this panel will be visible when users are editing NUMERICAL questions
 * it will offer them options to set a minimum and maximum range and
 * each of these in turn can be a literal value or a result of a
 * previous question ( or none )
 * @author Kevin
 *
 */

public class NumericLimitsPanel extends Panel {
	
	/**
	 * private inner class extending RadioGroup
	 * This is so it can implement IOnChangeListener and, 
	 * in effect, list to itself and set class variables.
	 * A RadioGroup takes the model of the selected Radio item
	 * within it.  Unfortunately, if you have two RadioGroups on a page
	 * they can interfere with each other if two individual radiobuttons
	 * have the same model.  Hence the one-model-per-radiobutton approach
	 */
	
	private class RadioGroupPlus extends RadioGroup {
		
		public RadioGroupPlus (String id, Model model ) {
			super(id, model );
		}
		
		protected void onSelectionChanged(Object newSelection) {
			 
			if ( newSelection.equals("minLiteral") ) {
				minLimitType = NumericLimitType.NLT_LITERAL;
			} else if ( newSelection.equals("minPrev") ) {
				minLimitType = NumericLimitType.NLT_PREVQUES;
			} else if ( newSelection.equals("minNone") ) {
				minLimitType = NumericLimitType.NLT_NONE;
			} else if ( newSelection.equals("maxLiteral") ) {
				maxLimitType = NumericLimitType.NLT_LITERAL;
			} else if ( newSelection.equals("maxPrev") ) {
				maxLimitType = NumericLimitType.NLT_PREVQUES;
			} else if ( newSelection.equals("maxNone") ) {
				maxLimitType = NumericLimitType.NLT_NONE;
			}
		}
		
		protected boolean wantOnSelectionChangedNotifications() { return (true);}
	}
	
	/**
	 * end of private inner class RadioGroupPlus
	 */
	
	public static enum NumericLimitType { NLT_LITERAL, NLT_PREVQUES, NLT_NONE };
	
	private Question question;
	private Form numericLimitsForm;
	private RadioGroup radioMinimum;
	private RadioGroup radioMaximum;
	private TextField textMinLiteral;
	private TextField textMaxLiteral;
	private DropDownChoice choiceMinPrevQ;
	private DropDownChoice choiceMaxPrevQ;
	private Radio radioMinLiteral;
	private Radio radioMinPrevQues;
	private Radio radioMinNone;
	private Radio radioMaxLiteral;
	private Radio radioMaxPrevQues;
	private Radio radioMaxNone;	
	private NumericLimitType minLimitType;
	private NumericLimitType maxLimitType;
	private Integer minLiteral;
	private Integer maxLiteral;
	private String strMinPrevQues;
	private String strMaxPrevQues;
	private List<String> numericQuestions;
	
	/**
	 * 
	 * @param id needed to put the panel in place using wicket tool kit
	 * @param question the question we're dealing with
	 */
	
	public NumericLimitsPanel(String id, Question question) {
		super(id);
		this.question = question;
		minLimitType = NumericLimitType.NLT_NONE;
		maxLimitType = NumericLimitType.NLT_NONE;
		minLiteral = 0; // Integer.MIN_VALUE;
		maxLiteral = 10000; // Integer.MAX_VALUE;
		strMinPrevQues = "";
		strMaxPrevQues = "";
		build();
	}
	
	/**
	 * builds the panel
	 * creates the form an all input widgets
	 */
	
	private void build() {		
		numericLimitsForm = new Form("numericLimitsForm");
		radioMinimum = new RadioGroupPlus("radioMin", new Model());
		radioMaximum = new RadioGroupPlus("radioMax", new Model());
		radioMinLiteral =  new Radio("minLiteral", new Model("minLiteral"));
		radioMinPrevQues=  new Radio("minPrev", new Model("minPrev"));
		radioMinNone	=  new Radio("minNone", new Model("minNone"));
		radioMaxLiteral	=  new Radio("maxLiteral", new Model("maxLiteral"));
		radioMaxPrevQues=  new Radio("maxPrev", new Model("maxPrev"));
		radioMaxNone	=  new Radio("maxNone", new Model("maxNone"));
		
		radioMinimum.add( radioMinLiteral);
		radioMinimum.add( radioMinPrevQues);
		radioMinimum.add( radioMinNone);
		numericLimitsForm.add(radioMinimum);
		
		radioMaximum.add( radioMaxLiteral);
		radioMaximum.add( radioMaxPrevQues);
		radioMaximum.add( radioMaxNone); 
		numericLimitsForm.add(radioMaximum);
		
		textMinLiteral = new TextField("minLiteralEntry", new PropertyModel(this, "minLiteral"), Integer.class);
		radioMinimum.add(textMinLiteral); 
		
		textMaxLiteral = new TextField("maxLiteralEntry", new PropertyModel(this, "maxLiteral"), Integer.class);
		radioMaximum.add(textMaxLiteral);
		
		add(numericLimitsForm);
		setMinLimitType( NumericLimitType.NLT_NONE);
		setMaxLimitType( NumericLimitType.NLT_NONE);
		
		// now populate the drop-down lists of previous NUMERICAL questions
		numericQuestions = getPrecedingNumericQuestions();
		
		choiceMinPrevQ = new DropDownChoice("minChoicePQ", new PropertyModel(this, "strMinPrevQues"), numericQuestions);
		radioMinimum.add(choiceMinPrevQ);
		
		choiceMaxPrevQ = new DropDownChoice("maxChoicePQ", new PropertyModel(this, "strMaxPrevQues"), numericQuestions);
		radioMaximum.add(choiceMaxPrevQ);
	}
	
	/***
	 * this generates a list of numeric questions that precede the current
	 * question.  The answers to these previous questions can be used as 
	 * the source of a lower or upper bound
	 * @return list of question titles
	 */
	
	public List<String> getPrecedingNumericQuestions() {
		List<String> previousNumericQuestions = Lists.newArrayList();
		
		List<Question> questions = 
			Questions.getQuestionsForStudy(question.getStudyId(), question.getType());
		for(Question q : questions) {
			if ( q.getId().equals(question.getId())) 
				break;
			if ( q.getAnswerType()==Answer.AnswerType.NUMERICAL &&  q.getActive()) {
				previousNumericQuestions.add(q.getTitle());
			}
		}
		return previousNumericQuestions;
	}
	
	/**
	 * getters and setters
	 */
	
	/**
	 * these seem like a lot of work to determine if a radiobutton
	 * is toggled or not...
	 */
	
	public void setMinLimitType (NumericLimitType minLimitType ) {
		this.minLimitType = minLimitType;

		switch ( this.minLimitType ) {
			case NLT_LITERAL:  radioMinimum.setModel(new Model("minLiteral")); break;
			case NLT_PREVQUES: radioMinimum.setModel(new Model("minPrev")); break;
			case NLT_NONE: 	   radioMinimum.setModel(new Model("minNone")); break;
		}
	}
	public NumericLimitType getMinLimitType() { 
		return(minLimitType);
	}

	public void setMaxLimitType (NumericLimitType maxLimitType ) {
		this.maxLimitType = maxLimitType;
		
		switch ( this.maxLimitType ) {
			case NLT_LITERAL:  radioMaximum.setModel(new Model("maxLiteral")); break;
			case NLT_PREVQUES: radioMaximum.setModel(new Model("maxPrev")); break;
			case NLT_NONE: 	   radioMaximum.setModel(new Model("maxNone")); break;
		}		
	}
	public NumericLimitType getMaxLimitType() { 
		return(maxLimitType);
	}
	
	public void setMinLiteral (Integer minLiteral) {
		this.minLiteral = minLiteral;
	}
	public Integer getMinLiteral() { 
		return( minLiteral );
	}
	
	public void setMaxLiteral (Integer maxLiteral) {
		this.maxLiteral = maxLiteral;
	}
	public Integer getMaxLiteral() { 
		return(maxLiteral);
	}
	
	public void setMinPrevQues ( String strPrevQues) {
		strMinPrevQues = strPrevQues;
	}
	public String getMinPrevQues() {
		return(strMinPrevQues);
	}
	
	public void setMaxPrevQues ( String strPrevQues) {
		strMaxPrevQues = strPrevQues;
	}
	public String getMaxPrevQues() { 
		return(strMaxPrevQues);
	}
	
}
