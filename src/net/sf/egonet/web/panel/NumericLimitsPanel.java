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
	
	public static enum NumericLimitType { NLT_LITERAL, NLT_PREVQUES, NLT_NONE };
	
	private Question question;
	private Form numericLimitsForm;
	private RadioGroup radioMinimum;
	private RadioGroup radioMaximum;
	private TextField textMinLiteral;
	private TextField textMaxLiteral;
	private DropDownChoice choiceMinPrevQ;
	private DropDownChoice choiceMaxPrevQ;
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
		setOutputMarkupId(true);
		numericLimitsForm = new Form("numericLimitsForm");
		radioMinimum = new RadioGroup("radioMin", new PropertyModel(this,"minLimitType"));
		radioMaximum = new RadioGroup("radioMax", new PropertyModel(this,"maxLimitType"));
		
		radioMinimum.add(new Radio("minLiteral", new Model(NumericLimitType.NLT_LITERAL)));
		radioMinimum.add(new Radio("minPrev", new Model(NumericLimitType.NLT_PREVQUES)));
		radioMinimum.add(new Radio("minNone", new Model(NumericLimitType.NLT_NONE)));
		numericLimitsForm.add(radioMinimum);
		
		radioMaximum.add(new Radio("maxLiteral", new Model(NumericLimitType.NLT_LITERAL)));
		radioMaximum.add(new Radio("maxPrev", new Model(NumericLimitType.NLT_PREVQUES)));
		radioMaximum.add(new Radio("maxNone", new Model(NumericLimitType.NLT_NONE))); 
		numericLimitsForm.add(radioMaximum);
		
		textMinLiteral = new TextField("minLiteralEntry", new PropertyModel(this, "minLiteral"), Integer.class);
		radioMinimum.add(textMinLiteral); 
		
		textMaxLiteral = new TextField("maxLiteralEntry", new PropertyModel(this, "maxLiteral"), Integer.class);
		radioMaximum.add(textMaxLiteral);
		
		add(numericLimitsForm);
		
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
	
	public void setMinLimitType (NumericLimitType minLimitType ) {
		this.minLimitType = minLimitType;
	}
	public NumericLimitType getMinLimitType() { 
		return(minLimitType);
	}

	public void setMaxLimitType (NumericLimitType maxLimitType ) {
		this.maxLimitType = maxLimitType;
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
