package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.markup.html.form.DropDownChoice;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Answer.SkipReason;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;

public class DateAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private static final List MONTHS = Arrays.asList(new String[] 
	   {"Jan", "Feb", "Mar", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec" });
	private Integer   theYear;
	private String    theMonth;
	private Integer   theDay;
	private TextField inputYear;
	private DropDownChoice inputMonth;
	private TextField inputDay;
	private CheckboxesPanel<String> refDKCheck;
	
	public DateAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters, Long interviewId) {
		super(id,question,Answer.SkipReason.NONE,alters, interviewId);
		stringToCalendar("");
		build("");
	}
	
	public DateAnswerFormFieldPanel(String id, 
			Question question, String answer, Answer.SkipReason skipReason, ArrayList<Alter> alters, Long interviewId) 
	{
		super(id,question,skipReason,alters,interviewId);
		stringToCalendar(answer);
		build(answer);
	}

	/**
	 * 'our' string representation of a date will be
	 * MMM DD YYYY
	 * where MMM is month in a string form (see MONTHS above)
	 * This is a tremendous simplification over any of the string 
	 * representations intrinsic to the calendar class
	 * @param strDate a date or time span, in string format
	 * @return
	 */
	private boolean stringToCalendar ( String strDate ) {
		int iTokens;
		String[] dateStrings;
		StringTokenizer strk;
		
		theYear = new Integer(0);
		theMonth = "Jan";
		theDay = new Integer(0);
			
		if ( strDate!=null && strDate.length()>0 ) {
			strk = new StringTokenizer(strDate.trim());
			iTokens =  strk.countTokens();
			if ( iTokens>0 )
				theMonth = strk.nextToken();
			if ( iTokens>1 ) {
				try {
					theDay = new Integer(strk.nextToken());
				} catch ( NumberFormatException nfe1 ) {
					theDay = new Integer(0);
				}
			}
			if ( iTokens>2 ) {
				try { 
					theYear = new Integer(strk.nextToken());
				} catch ( NumberFormatException nfe2 ) {
					theYear = new Integer(0);
				}
			}
		}
		return(true);
	}
	
	/**
	 * 'collects' the year, month and day data and creates a 
	 * string
	 * @return string representation of the date
	 */
	
	private String calendarToString() {
		String strDate = new String();
		
		strDate = String.format("%s %d %d", theMonth, theDay, theYear);
		return(strDate);
	}
	
	
	private void build(String answer) {
		
		inputYear = new TextField ("Year", new PropertyModel(this, "theYear"), Integer.class);
		inputMonth = new DropDownChoice("Month", new PropertyModel(this,"theMonth"), MONTHS);
		add(inputMonth);
		inputDay = new TextField("Day", new PropertyModel(this,"theDay"), Integer.class);
	
		add(inputYear);
		add(inputMonth);
		add(inputDay);
		
		ArrayList<String> refAndDK = Lists.newArrayList(refuse,dontKnow);
		if(question.getType().equals(Question.QuestionType.EGO_ID)) {
			refAndDK = Lists.newArrayList();
		}
		ArrayList<String> selectedRefAndDK = Lists.newArrayList();
		if(originalSkipReason.equals(SkipReason.DONT_KNOW)) {
			selectedRefAndDK.add(dontKnow);
		}
		if(originalSkipReason.equals(SkipReason.REFUSE)) {
			selectedRefAndDK.add(refuse);
		}
		refDKCheck = new CheckboxesPanel<String>("refDKCheck",
				refAndDK,selectedRefAndDK);
		add(refDKCheck);
	}
	
	/**
	 * returns the string used if an incorrect number of checkboxes
	 * are selected to prompt the user to check more or fewer
	 */
	
	public String getRangeCheckNotification() {
		String strNotification = "";
		
		if ( dontKnow() || refused())
			return(strNotification);
		if ( theYear < 0 )
			strNotification += "Year too low ";
		if ( theYear > 3000 ) 
			strNotification += "Year too high ";
		if ( theDay<1 )
			strNotification += "Day too low ";
		if (theDay>31 )
			strNotification += "Day too high ";
		return(strNotification);
	}
	
	/** 
	 * if the user selected dontKnow or refused to answer a question
	 * don't bother counting the responses.
	 */
	public boolean rangeCheckOkay() {
		
		if ( dontKnow() || refused())
			return (true);
		if ( theYear < 0 || theYear > 3000 ) 
			return(false);
		if ( theDay<1 || theDay>31 )
			return(false);
		return(true);
	}
	
	public String getAnswer() {
		return (calendarToString());
	}
	
	public void setTheYear ( Integer theYear ) {
		this.theYear = (theYear==null)?new Integer(0) : theYear;
	}
	public Integer getTheYear() { return (theYear);}
	
	public void setTheMonth ( String theMonth ) {
		this.theMonth = (theMonth==null) ? "Jan" : theMonth;
	}
	public String getTheMonth() { return (theMonth);}
	
	public void setTheDay ( Integer theDay ) {
		this.theDay = (theDay==null)?new Integer(0) : theDay;
	}
	public Integer getTheDay() { return (theDay);}
	
	
	public void setAutoFocus() {
		inputYear.add(new FocusOnLoadBehavior());
	}

	public boolean dontKnow() {
		return refDKCheck.getSelected().contains(dontKnow);
	}

	public boolean refused() {
		return refDKCheck.getSelected().contains(refuse);
	}	
}
