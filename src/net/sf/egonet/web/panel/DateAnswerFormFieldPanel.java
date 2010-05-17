package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Answer.SkipReason;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;

public class DateAnswerFormFieldPanel extends AnswerFormFieldPanel {

	public static enum DATE_FIELDS { DF_YEAR, DF_MONTH , DF_DAY, DF_HOUR, DF_MINUTE };
	private Integer[] theDate;
	private TextField inputYear;
	private TextField inputMonth;
	private TextField inputDay;
	private Calendar  calendarDay;
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
	 * YYYY MM DD
	 * This is a tremendous simplification over any of the string 
	 * representations intrinsic to the calendar class
	 * @param strDate a date or time span, in string format
	 * @return
	 */
	private boolean stringToCalendar ( String strDate ) {
		int ix = 0;
		int iSize = DATE_FIELDS.values().length;
		String[] dateStrings;
		
		calendarDay = Calendar.getInstance();
		theDate = new Integer[iSize];
		
		for ( ix=0 ; ix<iSize ; ++ix )
			theDate[ix] = new Integer(0);
		
		if ( strDate!=null && strDate.length()>0 ) {
			dateStrings = strDate.trim().split(" ");
			for ( ix=0 ; ix<iSize && ix<dateStrings.length ; ++ix ) {
				try {
					theDate[ix] = Integer.parseInt(dateStrings[ix].trim());
				} catch ( NumberFormatException nfe ) {
					theDate[ix] = 0;
				}
			}
		}
	
		calendarDay.set(theDate[0], theDate[1], theDate[2], theDate[3], theDate[4]);
		return(true);
	}
	
	/**
	 * 'collects' the year, month and day data and creates a 
	 * string
	 * @return string representation of the date
	 */
	
	private String calendarToString() {
		String strDate = new String();
		
		strDate = String.format("%d %d %d", theDate[0], theDate[1], theDate[2]);
		return(strDate);
	}
	
	
	private void build(String answer) {
		
		inputYear = new TextField ("Year", new PropertyModel(this, "theYear"), Integer.class);
		inputMonth = new TextField ("Month", new PropertyModel(this,"theMonth"), Integer.class);
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
		if ( theDate[0] < 0 )
			strNotification += "Year too low ";
		if ( theDate[0] > 3000 ) 
			strNotification += "Year too high";
		if ( theDate[1]<0 )
			strNotification += "Month too low ";
		if ( theDate[1]>12 )
			strNotification += "Month too high ";
		if ( theDate[2]<0 )
			strNotification += "Day too low ";
		if (theDate[2]>31 )
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
		if ( theDate[0] < 0 || theDate[0] > 3000 ) 
			return(false);
		if ( theDate[1]<0 || theDate[1]>12 )
			return(false);
		if ( theDate[2]<0 || theDate[2]>31 )
			return(false);
		return(true);
	}
	
	public String getAnswer() {
		return (calendarToString());
	}
	
	public void setTheYear ( Integer theYear ) {
		theDate[0] = (theYear==null)?new Integer(0) : theYear;
	}
	public Integer getTheYear() { return (theDate[0]);}
	
	public void setTheMonth ( Integer theMonth ) {
		theDate[1] = (theMonth==null)?new Integer(0) : theMonth;
	}
	public Integer getTheMonth() { return (theDate[1]);}
	
	public void setTheDay ( Integer theDay ) {
		theDate[2] = (theDay==null)?new Integer(0) : theDay;
	}
	public Integer getTheDay() { return (theDate[2]);}
	
	
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
