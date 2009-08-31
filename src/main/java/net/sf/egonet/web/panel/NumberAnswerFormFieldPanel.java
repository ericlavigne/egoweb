package net.sf.egonet.web.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

import net.sf.egonet.model.Question;

public class NumberAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private Model answer;
	
	public NumberAnswerFormFieldPanel(String id, Question question) { 
		super(id,question); 
		this.answer = new Model("");
		build();
	}
	
	public NumberAnswerFormFieldPanel(String id, Question question, String answer) { 
		super(id,question); 
		this.answer = new Model(answer);
		build();
	}
	
	private void build() {
		add(new Label("prompt",question.getPrompt()));
		TextField answerField = new TextField("answer",answer);
		answerField.add(new PatternValidator("[0-9]*"));
		add(answerField);
	}

	public String getAnswer() {
		return (String) answer.getObject();
	}
}
