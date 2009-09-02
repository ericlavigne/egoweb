package net.sf.egonet.web.panel;

import net.sf.egonet.model.Question;
import static net.sf.egonet.model.Answer.AnswerType;

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
		
		AnswerType type = question.getAnswerType();
		
		if(type.equals(AnswerType.TEXTUAL)) {
			return new TextAnswerFormFieldPanel(id,question);
		}
		if(type.equals(AnswerType.NUMERICAL)) {
			return new NumberAnswerFormFieldPanel(id,question);
		}
		if(type.equals(AnswerType.SELECTION)) {
			return new SelectionAnswerFormFieldPanel(id,question);
		}
		if(type.equals(AnswerType.MULTIPLE_SELECTION)) {
			return new MultipleSelectionAnswerFormFieldPanel(id,question);
		}
		throw new RuntimeException("Unable to create AnswerFormFieldPanel for AnswerType="+type);
	}
	
	public Question getQuestion() {
		return question;
	}
	
	public abstract String getAnswer();
	
	//protected ArrayList<Alter> getAlters() {
	//	return alters;
	//}
}
