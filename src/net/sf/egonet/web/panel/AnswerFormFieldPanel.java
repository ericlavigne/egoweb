package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import static net.sf.egonet.model.Answer.AnswerType;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class AnswerFormFieldPanel extends Panel {
	
	protected Long interviewId;
	protected Question question;
	protected ArrayList<Alter> alters;

	protected final Answer.SkipReason originalSkipReason;
	
	private Label notification;
	
	public final static String dontKnow = "Don't know";
	public final static String refuse = "Refuse";
	public final static String none = "None";

	protected AnswerFormFieldPanel(String id, Question question, 
			                    Answer.SkipReason originalSkipReason, Long interviewId) { 
		this(id,question, originalSkipReason, new ArrayList<Alter>(), interviewId);
	}
	
	protected AnswerFormFieldPanel(String id, 
			Question question, Answer.SkipReason originalSkipReason, ArrayList<Alter> alters, Long interviewId) 
	{
		super(id);
		this.question = question;
		this.alters = alters;
		this.originalSkipReason = originalSkipReason;
		this.interviewId = interviewId;
		notification = new Label("notification","");
		add(notification);
	}
	
	public void setNotification(String text) {
		notification.setModelObject(text);
	}

	public static AnswerFormFieldPanel getInstance(String id, Question question, Long interviewId ) 
	{
		return getInstance(id,question, new ArrayList<Alter>(), interviewId);
	}
	
	public static AnswerFormFieldPanel getInstance(String id, Question question, 
			ArrayList<Alter> alters, Long interviewId ) {
		
		AnswerType type = question.getAnswerType();
		
		if(type.equals(AnswerType.TEXTUAL)) {
			return new TextAnswerFormFieldPanel(id,question,alters, interviewId);
		}
		if(type.equals(AnswerType.NUMERICAL)) {
			return new NumberAnswerFormFieldPanel(id,question,alters, interviewId);
		}
		if(type.equals(AnswerType.SELECTION)) {
			return new SelectionAnswerFormFieldPanel(id,question,alters, interviewId);
		}
		if(type.equals(AnswerType.MULTIPLE_SELECTION)) {
			return new MultipleSelectionAnswerFormFieldPanel(id,question,alters, interviewId);
		}
		throw new RuntimeException("Unable to create AnswerFormFieldPanel for AnswerType="+type);
	}
	
	public static AnswerFormFieldPanel getInstance(String id, Question question, String answer, 
			                       Answer.SkipReason skipReason, Long interviewId) {
		return getInstance(id,question,answer,skipReason,new ArrayList<Alter>(), interviewId);
	}
	
	public static AnswerFormFieldPanel getInstance(String id, 
			Question question, String answer, Answer.SkipReason skipReason, ArrayList<Alter> alters, Long interviewId) 
	{
		AnswerType type = question.getAnswerType();
		
		if(type.equals(AnswerType.TEXTUAL)) {
			return new TextAnswerFormFieldPanel(id,question,answer,skipReason,alters,interviewId);
		}
		if(type.equals(AnswerType.NUMERICAL)) {
			return new NumberAnswerFormFieldPanel(id,question,answer,skipReason,alters,interviewId);
		}
		if(type.equals(AnswerType.SELECTION)) {
			return new SelectionAnswerFormFieldPanel(id,question,answer,skipReason,alters,interviewId);
		}
		if(type.equals(AnswerType.MULTIPLE_SELECTION)) {
			return new MultipleSelectionAnswerFormFieldPanel(id,question,answer,skipReason,alters,interviewId);
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
		String strPrompt;

		System.out.println ( "alters=" + alters);
		strPrompt = question.individualizePrompt(alters);
		strPrompt = question.answerCountInsertion(strPrompt, interviewId);
		strPrompt = question.questionContainsAnswerInsertion(strPrompt, interviewId, alters);
		strPrompt = question.calculationInsertion(strPrompt, interviewId, alters);
		strPrompt = question.variableInsertion(strPrompt, interviewId, alters);
		strPrompt = question.conditionalTextInsertion(strPrompt, interviewId, alters);		
		return (strPrompt);
	}

	public abstract void setAutoFocus();
	public abstract boolean dontKnow();
	public abstract boolean refused();
	
	public Answer.SkipReason getSkipReason(Collection<String> pageLevelFlags) {
		if(refused() || pageLevelFlags.contains(refuse)) {
			return Answer.SkipReason.REFUSE;
		} else if(dontKnow() || pageLevelFlags.contains(dontKnow)) {
			return Answer.SkipReason.DONT_KNOW;
		} else {
			return Answer.SkipReason.NONE;
		}
	}
	
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
							panels.iterator().next().question.needsMultiSelectionResponse())) &&
				allMultipleSelectionOkay(panels);
	}
	
	/**
	 * some answerforms - specifically MultipleSelection - can have a low bound and
	 * a high bound set on the number of checkBoxes to select.
	 * @return 0 if the number of checkboxes is within the predetermined range OR
	 * the AnswerForm is not of the MultipleSelection kind
	 */
	public boolean multipleSelectionOkay() {
		return(true);
	}
	
	/**
	 * like the above function multipleSelectionOkay, 
	 * this function is ad hoc to multiple Selection questions
	 * @return a string with an error message specific to 
	 * multiple selection question with an incorrect number of 
	 * checkboxes selected
	 */
	
	public String getMultipleSelectionNotification() {
		return("");
	}
	
	/**
	 * checks all the MultipleSelection question panels, 
	 * returns false if any one of them has an incorrect number
	 * of checkboxes selected
	 * @param panels a collection of AnswerFormFieldPanels
	 * @return false if any one of the panels is a MultipleSelection
	 * panel with an incorrect number of boxes checked
	 */
	
	public static boolean 
	allMultipleSelectionOkay(Collection<AnswerFormFieldPanel> panels ) 
	{
		for ( AnswerFormFieldPanel panel : panels ) {
			if( !panel.multipleSelectionOkay())
		 		return(false);
		}
		return (true);
	}
}
