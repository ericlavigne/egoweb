package net.sf.egonet.web.panel;

import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;

public class TextAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private TextField textField;
	
	public TextAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters) {
		super(id,question,alters);
		this.textField = new TextField("answer", new Model(""));
		build();
	}
	
	public TextAnswerFormFieldPanel(String id, Question question, String answer, ArrayList<Alter> alters) {
		super(id,question,alters);
		this.textField = new TextField("answer", new Model(answer));
		build();
	}
	
	private void build() {
		add(new Label("prompt",getPrompt()));
		add(textField);
	}

	public String getAnswer() {
		return textField.getText();
	}
	
	public void setAutoFocus() {
		textField.add(new FocusOnLoadBehavior());
	}
}
