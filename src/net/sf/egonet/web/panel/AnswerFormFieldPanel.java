package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.SimpleLogicMgr;
import static net.sf.egonet.model.Answer.AnswerType;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class AnswerFormFieldPanel extends Panel {
	
	protected Long interviewId;
	protected Question question;
	protected ArrayList<Alter> alters;

	protected final Answer.SkipReason originalSkipReason;
	
	private Label notification;
    private boolean firstTimeOnQuestion; // ad-hoc to list-of-alters 'none' option
    
	public final static String dontKnow = "DON'T KNOW";
	public final static String refuse = "REFUSE";
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
		firstTimeOnQuestion = false;
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
		if(type.equals(AnswerType.TEXTUAL_PP)) {
			return new TextAreaAnswerFormFieldPanel(id,question,alters,interviewId);
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
		if ( type.equals(AnswerType.DATE)) {
			return new DateAnswerFormFieldPanel(id,question,alters, interviewId);
		}
		if ( type.equals(AnswerType.TIME_SPAN)) {
			return new TimeSpanAnswerFormFieldPanel(id,question,alters,interviewId);
		}
		throw new RuntimeException("Unable to create AnswerFormFieldPanel for AnswerType="+type);
	}
	
	public static AnswerFormFieldPanel getInstance(String id, Question question, String answer, 
			String otherSpecAnswer, Answer.SkipReason skipReason, Long interviewId) {
		return getInstance(id,question,answer,otherSpecAnswer,skipReason,new ArrayList<Alter>(), interviewId);
	}
	
	public static AnswerFormFieldPanel getInstance(String id, 
			Question question, String answer, String otherSpecAnswer, Answer.SkipReason skipReason, ArrayList<Alter> alters, Long interviewId) 
	{
		AnswerType type = question.getAnswerType();
		
		if(type.equals(AnswerType.TEXTUAL)) {
			return new TextAnswerFormFieldPanel(id,question,answer,skipReason,alters,interviewId);
		}
		if(type.equals(AnswerType.TEXTUAL_PP)) { 
			return new TextAreaAnswerFormFieldPanel(id,question,answer,skipReason,alters,interviewId);
		}
		if(type.equals(AnswerType.NUMERICAL)) {
			return new NumberAnswerFormFieldPanel(id,question,answer,skipReason,alters,interviewId);
		}
		if(type.equals(AnswerType.SELECTION)) {
			return new SelectionAnswerFormFieldPanel(id,question,answer,otherSpecAnswer,skipReason,alters,interviewId);
		}
		if(type.equals(AnswerType.MULTIPLE_SELECTION)) {
			return new MultipleSelectionAnswerFormFieldPanel(id,question,answer,otherSpecAnswer,skipReason,alters,interviewId);
		}
		if ( type.equals(AnswerType.DATE)) {
			return new DateAnswerFormFieldPanel(id,question,answer,skipReason,alters, interviewId);
		}
		if ( type.equals(AnswerType.TIME_SPAN)) {
			return new TimeSpanAnswerFormFieldPanel(id,question,answer,skipReason,alters,interviewId);
		}
		throw new RuntimeException("Unable to create AnswerFormFieldPanel for AnswerType="+type);
	}
	
	public Question getQuestion() {
		return question;
	}
	
	public abstract String getAnswer();
	/**
	 * the 'otherText' is for Other/Specify types of questions
	 * which will only affect Selection and Multiple Selection questions
	 * with the otherSpecifyStyle flag set
	 * @return string of ad-hoc 'other' text, which will be processed
	 * separately from the regular answers
	 */
	public String getOtherText() {
		return("");
	}
	public ArrayList<Alter> getAlters() {
		return alters;
	}
	
	public String getPrompt() {
		String strPrompt;

		// System.out.println ( "alters=" + alters);
		strPrompt = question.individualizePrompt(alters);
		strPrompt = question.answerCountInsertion(strPrompt, interviewId);
		strPrompt = question.questionContainsAnswerInsertion(strPrompt, interviewId, alters);
		strPrompt = question.dateDataInsertion(strPrompt, interviewId, alters);
		strPrompt = question.calculationInsertion(strPrompt, interviewId, alters);
		strPrompt = question.variableInsertion(strPrompt, interviewId, alters);
		strPrompt = question.conditionalTextInsertion(strPrompt, interviewId, alters);
		
		if ( SimpleLogicMgr.hasError()) {
			System.out.println ("Var Insertion error in " + question.getTitle());
		}
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
			if ( ref )
				return "Can't select page-level Refuse and other answer choices at the same time.  Please correct.";
			if ( dk )
				return "Can't select page-level Don't know and other answer choices at the same time.  Please correct.";
			if ( pageNone )
				return "Can't select None and other answer choices at the same time.  Please correct.";
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
				allRangeChecksOkay(panels);
	}
	
	/**
	 * some answerforms - specifically MultipleSelection - can have a low bound and
	 * a high bound set on the number of checkBoxes to select.
	 * In a similar vein, numeric answer text boxes can have a low and a high limit
	 * on the number entered.  A range check will have to be performed on the value
	 * entered
	 * @return 0 if the number of checkboxes is within the predetermined range OR
	 * if a text answer IS within the allowed range OR
	 * the AnswerForm is not of the MultipleSelection OR Numeric kind
	 */
	public boolean rangeCheckOkay() {
		return(true);
	}
	
	/**
	 * like the above function rangeChecksOkay, 
	 * this function is ad hoc to multiple Selection questions
	 * and numeric questions.
	 * @return a string with an error message specific to 
	 * multiple selection question with an incorrect number of 
	 * checkboxes selected or numeric answers that are out of 
	 * a specified range
	 */
	
	public String getRangeCheckNotification() {
		return("");
	}
	
	/**
	 * checks all the MultipleSelection question panels, 
	 * returns false if any one of them has an incorrect number
	 * of checkboxes selected.
	 * Also checks numeric entry panels, returns false if they
	 * contain a number out of range 
	 * @param panels a collection of AnswerFormFieldPanels
	 * @return false if any one of the panels is a MultipleSelection
	 * panel with an incorrect number of boxes checked OR
	 * any panel is a numeric entry with a value out of range
	 */
	
	public static boolean 
	allRangeChecksOkay(Collection<AnswerFormFieldPanel> panels ) 
	{
		for ( AnswerFormFieldPanel panel : panels ) {
			if( !panel.rangeCheckOkay())
		 		return(false);
		}
		return (true);
	}
	
	/**
	 * this is specific to list-of-alters pages using Multiple Selection
	 * or Single Selection answer panels.  In some cases a question is apt
	 * to be 'indicate your most recent four sex partners' and we'll check
	 * that 4 and only 4 'yes' answers are selected.
	 * @param panels collection of AnswerFormFieldPanels in the 'main' panel
	 * @param strAnswer - answer to count
	 * @return count of how many times strAnswer is selected
	 */
	private static int
	countSelectionItem(Collection<AnswerFormFieldPanel> panels, String strAnswer ) {
		int iCount = 0;
		
		for ( AnswerFormFieldPanel panel : panels ) {
			if ( panel instanceof MultipleSelectionAnswerFormFieldPanel ) {
				if ( ((MultipleSelectionAnswerFormFieldPanel)panel).isSelected(strAnswer))
					++iCount;
			}
			if ( panel instanceof SelectionAnswerFormFieldPanel ) {
				if ( ((SelectionAnswerFormFieldPanel)panel).isSelected(strAnswer))
					++iCount;
			}		
		}
		return(iCount);
	}
	
	/**
	 * IF this is an alter question, it is in list-of-alters format, AND the 
	 * 'useListRange' option is on, this will count how many times the specified
	 * answer is use and return TRUE if that count is within the set range
	 * @param question - question being asked
	 * @param panels - collection of answer panels
	 * @return true if everything is within bounds
	 */
	public static boolean checkCountOfListItem(Question question, Collection<AnswerFormFieldPanel> panels) {
		String strAnswer;
		int iMin;
		int iMax;
		int iTemp;
		int iAnswerCount;
		
		// this check only needs to take place if this is a
		// list-of-alters question.
		if ( !question.isAboutAlter()     || !question.getAskingStyleList() || 
			 !question.getWithListRange() || panels.isEmpty() )
			return(true);
		
		strAnswer = question.getListRangeString();
		if ( strAnswer==null || strAnswer.length()==0)
			return(true);
		
		iMin = question.getMinListRange();
		iMax = question.getMaxListRange();
		// in case the survey author got confused,
		// force min<max
		if ( iMin>iMax ) {
			iTemp = iMin;
			iMin = iMax;
			iMax = iTemp;
		}
		// check for the case of the survey author asking for a minimum
		// of more options than are actually available
		if ( iMin >= panels.size())
			iMin = panels.size();
		
		iAnswerCount = countSelectionItem(panels, strAnswer);
		if ( iAnswerCount<iMin ) 
			return(false);
		
		if ( iAnswerCount>iMax ) {
			return(false);
		}
		return(true);
	}
	
	/**
	 * IF the question is a list-of-alters style AND we want a specific response
	 * limited to a range AND that specific response was answered outside that range
	 * this provides feedback for the user
	 * @param question - the question asked
	 * @param panels - the panels that make up the list of answers
	 * @return a string to display on the screen
	 */
	public static String getStatusCountOfListItem(Question question, Collection<AnswerFormFieldPanel> panels) {
		int iMin;
		int iMax;
		int iTemp;
		String strAnswer;
		String strStatus = "";
		int iAnswerCount;
		
		// this check only needs to take place if this is a
		// list-of-alters question.
		if ( !question.isAboutAlter()     || !question.getAskingStyleList() || 
			 !question.getWithListRange() || panels.isEmpty() )
			return(strStatus);

		strAnswer = question.getListRangeString();
		if ( strAnswer==null || strAnswer.length()==0)
			return(strStatus);
		iMin = question.getMinListRange();
		iMax = question.getMaxListRange();
		// in case the survey author got confused,
		// force min<max
		if ( iMin>iMax ) {
			iTemp = iMin;
			iMin = iMax;
			iMax = iTemp;
		}
		// check for the case of the survey author asking for a minimum
		// of more options than are actually available
		if ( iMin >= panels.size())
			iMin = panels.size();
		iAnswerCount = countSelectionItem(panels, strAnswer);
		if ( iAnswerCount<iMin ) {
			strStatus = strAnswer + " not selected enough, need " + 
					(iMin-iAnswerCount) + " more.";
		}
		if ( iAnswerCount>iMax ) {
			iTemp = iAnswerCount-iMax;
			strStatus = strAnswer + " selected too many times, remove " +
					iTemp + " selection" + ((iTemp==1) ? "." : "s.");
		}
		return(strStatus);	
	}
	
	/**
	 * used in list-of-alters pages by the 'global' Don't know and Refuse
	 * buttons.  If one of these is clicked, all UNANSWERED panels will
	 * change to that answer
	 * @param panels list of answer panels
	 * @param strAnswer - string to change to, 'Don't know' or 'Refuse'
	 * @return a count of the altered panels
	 */
	protected static int
	forceSelectionIfNone(Collection<AnswerFormFieldPanel> panels, String strAnswer, int iMaxSelection ) {
		int iCount = 0;
		
		for ( AnswerFormFieldPanel panel : panels ) {
			if ( panel instanceof MultipleSelectionAnswerFormFieldPanel ) {
				if ( ((MultipleSelectionAnswerFormFieldPanel)panel).forceSelectionIfNone(strAnswer, iMaxSelection))
					++iCount;
			}		
		}
		return(iCount);
	}

	/**
	 * this will be used in the list-of-alters page to (optionally) select
	 * one of the 'global' checkboxes at the bottom of the panel
	 * @param panels - collection of answer panels, one for each alter of interest
	 * @return if ALL of the answers have a certain skip-reason, a string indicating
	 * which skip-reason is unanymouse among the answers
	 */
	public static String getSkipReasonForListOfAlters( Collection<AnswerFormFieldPanel> panels) {
		int[] histoGram = new int[6];
		int ix;
	    boolean firstTime = true;
	    
		for ( ix=0 ; ix<histoGram.length ; ++ix ) {
			histoGram[0] =0;
		}
		
		for ( AnswerFormFieldPanel panel : panels ) {
			if ( !panel.firstTimeOnQuestion)
				firstTime = false;
			if ( panel.originalSkipReason == null ) {
				++histoGram[0];
			} else {
				switch ( panel.originalSkipReason ) {
				    case NONE:
				    	 if ( panel.getAnswer()==null || panel.getAnswer().trim().length()==0)
				    	     ++histoGram[1];
				    	 else
				    		 ++histoGram[4];
				    	 break;
				    case REFUSE:    ++histoGram[2]; break;
				    case DONT_KNOW: ++histoGram[3]; break;
				    default:        ++histoGram[5]; break;
				}
			}
		}
		// System.out.println ("     NULL skips = " + histoGram[0]);
		// System.out.println ("     NONE skips = " + histoGram[1]);
		// System.out.println ("   REFUSE skips = " + histoGram[2]);
		// System.out.println ("DONT_KNOW skips = " + histoGram[3]);
		// System.out.println ("       Answered = " + histoGram[4]);
		// System.out.println ("          Other = " + histoGram[5]);
		if ( histoGram[1] == panels.size()  &&  !firstTime )
		    return(none);
		if ( histoGram[2] == panels.size())
			return(refuse);
		if ( histoGram[3] == panels.size())
			return(dontKnow);
		return("");
	}
	
	/**
	 * the firstTimeOnQuestion variable is a flag that helps with the 
	 * page level None checkbox.  If all the questions have a skip reason of
	 * None we want the none checkbox checked, but *NOT* on the first time through.
	 * The first time the question is asked we want surveyers to explicitly choose it
	 * if its appropriate.  After that it can be checked by default if thats appropriate.
	 * firstTimeOnQuestion is false by default, it is explicitly set to true by constructors
	 * that have no prior answer data.
	 * @param firstTimeOnQuestion
	 */
	protected void setFirstTimeOnQuestion ( boolean firstTimeOnQuestion ) {
		this.firstTimeOnQuestion = firstTimeOnQuestion;
	}
}
