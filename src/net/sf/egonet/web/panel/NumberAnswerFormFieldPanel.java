package net.sf.egonet.web.panel;

import java.util.ArrayList;

import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Answer.SkipReason;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;

public class NumberAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private TextField textField;
	private CheckboxesPanel<String> refDKCheck;
	
	public NumberAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters) { 
		super(id,question,Answer.SkipReason.NONE,alters); 
		build("");
	}
	
	public NumberAnswerFormFieldPanel(String id, 
			Question question, String answer, Answer.SkipReason skipReason, ArrayList<Alter> alters) 
	{ 
		super(id,question,skipReason,alters); 
		build(answer);
	}
	
	private void build(String previousAnswer) {
		textField = new TextField("answer", new Model(previousAnswer));
		textField.add(new PatternValidator("[0-9]*"));
		add(textField);
		
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
}
