package net.sf.egonet.web.panel;

import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;

public class NumberAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private TextField textField;
	
	public NumberAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters) { 
		super(id,question,alters); 
		build("");
	}
	
	public NumberAnswerFormFieldPanel(String id, Question question, String answer, ArrayList<Alter> alters) { 
		super(id,question,alters); 
		build(answer);
	}
	
	private void build(String previousAnswer) {
		add(new Label("prompt",getPrompt()));
		textField = new TextField("answer", new Model(previousAnswer));
		textField.add(new PatternValidator("[0-9]*"));
		add(textField);
	}

	public String getAnswer() {
		return textField.getText();
	}
	
	public void setAutoFocus() {
		textField.add(new FocusOnLoadBehavior());
	}
}
