package net.sf.egonet.web.panel;

import java.util.ArrayList;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;
import static net.sf.egonet.model.Answer.AnswerType;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class AnswerFormFieldPanel extends Panel {
	
	protected Question question;
	protected ArrayList<Alter> alters;

	protected AnswerFormFieldPanel(String id, Question question) { 
		this(id,question, new ArrayList<Alter>());
	}
	
	protected AnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters) {
		super(id);
		this.question = question;
		this.alters = alters;
	}

	public static AnswerFormFieldPanel getInstance(String id, Question question) {
		return getInstance(id,question, new ArrayList<Alter>());
	}
	
	public static AnswerFormFieldPanel getInstance(String id, Question question, ArrayList<Alter> alters) {
		
		AnswerType type = question.getAnswerType();
		
		if(type.equals(AnswerType.TEXTUAL)) {
			return new TextAnswerFormFieldPanel(id,question,alters);
		}
		if(type.equals(AnswerType.NUMERICAL)) {
			return new NumberAnswerFormFieldPanel(id,question,alters);
		}
		if(type.equals(AnswerType.SELECTION)) {
			return new SelectionAnswerFormFieldPanel(id,question,alters);
		}
		if(type.equals(AnswerType.MULTIPLE_SELECTION)) {
			return new MultipleSelectionAnswerFormFieldPanel(id,question,alters);
		}
		throw new RuntimeException("Unable to create AnswerFormFieldPanel for AnswerType="+type);
	}
	
	public static AnswerFormFieldPanel getInstance(String id, Question question, String answer) {
		return getInstance(id,question,answer, new ArrayList<Alter>());
	}
	
	public static AnswerFormFieldPanel getInstance(String id, Question question, String answer, ArrayList<Alter> alters) {
		
		AnswerType type = question.getAnswerType();
		
		if(type.equals(AnswerType.TEXTUAL)) {
			return new TextAnswerFormFieldPanel(id,question,answer,alters);
		}
		if(type.equals(AnswerType.NUMERICAL)) {
			return new NumberAnswerFormFieldPanel(id,question,answer,alters);
		}
		if(type.equals(AnswerType.SELECTION)) {
			return new SelectionAnswerFormFieldPanel(id,question,answer,alters);
		}
		if(type.equals(AnswerType.MULTIPLE_SELECTION)) {
			return new MultipleSelectionAnswerFormFieldPanel(id,question,answer,alters);
		}
		throw new RuntimeException("Unable to create AnswerFormFieldPanel for AnswerType="+type);
	}
	
	public Question getQuestion() {
		return question;
	}
	
	public abstract String getAnswer();
	
	public ArrayList<Alter> getAlters() {
		return alters;
	}
	
	public static String individualizeQuestionPrompt(Question question, ArrayList<Alter> alters) {
		String questionText = question.getPrompt();
		String dd = "\\$\\$";
		String dd1 = dd+"1";
		String dd2 = dd+"2";
		if(question.getType().equals(QuestionType.ALTER)) {
			questionText = questionText.replaceAll(dd1, alters.get(0).getName());
			questionText = questionText.replaceAll(dd, alters.get(0).getName());
		}
		if(question.getType().equals(QuestionType.ALTER_PAIR)) {
			questionText = questionText.replaceAll(dd1, alters.get(0).getName());
			questionText = questionText.replaceAll(dd2, alters.get(1).getName());
			questionText = questionText.replaceFirst(dd, alters.get(0).getName());
			questionText = questionText.replaceFirst(dd, alters.get(1).getName());
		}
		return questionText;
	}
	
	public String getPrompt() {
		return individualizeQuestionPrompt(question,alters);
	}

	public void setAutoFocus() {
		
	}
}
