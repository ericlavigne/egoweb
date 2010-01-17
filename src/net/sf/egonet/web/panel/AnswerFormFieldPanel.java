package net.sf.egonet.web.panel;

import java.util.ArrayList;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import static net.sf.egonet.model.Answer.AnswerType;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class AnswerFormFieldPanel extends Panel {
	
	protected Question question;
	protected ArrayList<Alter> alters;

	protected final Answer.SkipReason originalSkipReason;
	
	protected final String dontKnow = "Don't know", refuse = "Refuse";

	protected AnswerFormFieldPanel(String id, Question question, Answer.SkipReason originalSkipReason) { 
		this(id,question, originalSkipReason, new ArrayList<Alter>());
	}
	
	protected AnswerFormFieldPanel(String id, 
			Question question, Answer.SkipReason originalSkipReason, ArrayList<Alter> alters) 
	{
		super(id);
		this.question = question;
		this.alters = alters;
		this.originalSkipReason = originalSkipReason;
	}

	public static AnswerFormFieldPanel getInstance(String id, Question question) 
	{
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
	
	public static AnswerFormFieldPanel getInstance(String id, Question question, String answer, Answer.SkipReason skipReason) {
		return getInstance(id,question,answer,skipReason,new ArrayList<Alter>());
	}
	
	public static AnswerFormFieldPanel getInstance(String id, 
			Question question, String answer, Answer.SkipReason skipReason, ArrayList<Alter> alters) 
	{
		AnswerType type = question.getAnswerType();
		
		if(type.equals(AnswerType.TEXTUAL)) {
			return new TextAnswerFormFieldPanel(id,question,answer,skipReason,alters);
		}
		if(type.equals(AnswerType.NUMERICAL)) {
			return new NumberAnswerFormFieldPanel(id,question,answer,skipReason,alters);
		}
		if(type.equals(AnswerType.SELECTION)) {
			return new SelectionAnswerFormFieldPanel(id,question,answer,skipReason,alters);
		}
		if(type.equals(AnswerType.MULTIPLE_SELECTION)) {
			return new MultipleSelectionAnswerFormFieldPanel(id,question,answer,skipReason,alters);
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
	
	public String getPrompt() {
		return question.individualizePrompt(alters);
	}

	public abstract void setAutoFocus();
	public abstract boolean dontKnow();
	public abstract boolean refused();
}
