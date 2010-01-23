package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import static net.sf.egonet.model.Answer.AnswerType;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class AnswerFormFieldPanel extends Panel {
	
	protected Question question;
	protected ArrayList<Alter> alters;

	protected final Answer.SkipReason originalSkipReason;
	
	public final static String dontKnow = "Don't know";
	public final static String refuse = "Refuse";
	public final static String none = "None";

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
	
	public boolean consistent(Collection<String> pageLevelFlags) {
		return inconsistencyReason(pageLevelFlags) == null;
	}
	public String inconsistencyReason(Collection<String> pageLevelFlags) {
		if(pageLevelFlags.size() > 1) {
			return "Can't select more than one page-level flag";
		}
		boolean ref = refused() || pageLevelFlags.contains(refuse);
		boolean dk = dontKnow() || pageLevelFlags.contains(dontKnow);
		boolean pageNone = pageLevelFlags.contains(none);
		if(ref && dk) {
			return "Can't give two skip reasons";
		}
		if((ref || dk || pageNone) && answered()) {
			return "Can't provide a skip reason without skipping";
		}
		return null;
	}
	public boolean answered() {
		return ! (getAnswer() == null || getAnswer().isEmpty());
	}
	public boolean answeredOrRefused(Collection<String> pageLevelFlags) {
		return answered() || refused() || dontKnow() || ! pageLevelFlags.isEmpty();
	}
	public static boolean 
	allConsistent(Collection<AnswerFormFieldPanel> panels, Collection<String> pageLevelFlags)
	{
		for(AnswerFormFieldPanel panel : panels) {
			if(! panel.consistent(pageLevelFlags)) {
				return false;
			}
		}
		return true;
	}

	public static boolean 
	someAnswered(Collection<AnswerFormFieldPanel> panels)
	{
		for(AnswerFormFieldPanel panel : panels) {
			if(panel.answered()) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean 
	allAnsweredOrRefused(Collection<AnswerFormFieldPanel> panels, Collection<String> pageLevelFlags)
	{
		for(AnswerFormFieldPanel panel : panels) {
			if(! panel.answeredOrRefused(pageLevelFlags)) {
				return false;
			}
		}
		return true;
	}

	public static boolean 
	okayToContinue(Collection<AnswerFormFieldPanel> panels, Collection<String> pageLevelFlags)
	{
		return allConsistent(panels,pageLevelFlags) &&
			(allAnsweredOrRefused(panels,pageLevelFlags) ||
					(someAnswered(panels) && 
							panels.iterator().next().question.needsMultiSelectionResponse()));
	}
}
