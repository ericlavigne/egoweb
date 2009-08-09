package net.sf.egonet.web.panel;

import java.util.ArrayList;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class AnswerFormFieldPanel extends Panel {
	
	protected Question question;
	//protected ArrayList<Alter> alters;

	protected AnswerFormFieldPanel(String id, Question question) { //, ArrayList<Alter> alters) {
		super(id);
		this.question = question;
		//this.alters = alters;
	}

	public static AnswerFormFieldPanel getInstance(String id, Question question) { //, ArrayList<Alter> alters) {
		// TODO: Needs to return different subclass depending on question.answerType
		return new TextAnswerFormFieldPanel(id,question); //,alters);
	}
	
	public Question getQuestion() {
		return question;
	}
	
	public abstract String getAnswer();
	
	//protected ArrayList<Alter> getAlters() {
	//	return alters;
	//}
}
