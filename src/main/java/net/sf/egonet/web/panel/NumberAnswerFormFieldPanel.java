package net.sf.egonet.web.panel;

import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;

public class NumberAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private Model answer;
	
	public NumberAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters) { 
		super(id,question,alters); 
		this.answer = new Model("");
		build();
	}
	
	public NumberAnswerFormFieldPanel(String id, Question question, String answer, ArrayList<Alter> alters) { 
		super(id,question,alters); 
		this.answer = new Model(answer);
		build();
	}
	
	private void build() {
		add(new Label("prompt",getPrompt()));
		TextField answerField = new TextField("answer",answer);
		answerField.add(new PatternValidator("[0-9]*"));
		add(answerField);
	}

	public String getAnswer() {
		return (String) answer.getObject();
	}
}
