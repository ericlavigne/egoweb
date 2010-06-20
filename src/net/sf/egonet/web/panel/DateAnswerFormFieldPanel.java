package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.RadioChoice;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Answer.SkipReason;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;
import net.sf.egonet.web.panel.TimeSpanAnswerFormFieldPanel;

public class DateAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private static final List<String> MONTHS = Arrays.asList(new String[] 
	   {"January", "February", "March", "April", "May", "June", 
		"July", "August", "September", "October", "November", "December" });
	private static final List<String> AM_PM = Arrays.asList(new String[] 
	   { "AM", "PM" });
	
	private boolean useYears;
	private boolean useMonths;
	private boolean useDays;
	private boolean useHours;
	private boolean useMinutes;
	
	
	private String   theYear;
	private String   theMonth;
	private String   theDay;
	private String   theHour;
	private String   theMinute;
	private String   amHour;
	
	private int yearValue; // int values of corresponding strings
	private int dayValue;
	private int hourValue;
	private int minuteValue;
	
	private TextField inputYear;
	private DropDownChoice inputMonth;
	private TextField inputDay;
	private TextField inputHour;
	private TextField inputMinute;
	private RadioChoice radioAmPm;
	
	private Label lblYear;
	private Label lblHour;
	private String lblYearPrompt;
	private String lblHourPrompt;
	
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
	 * MMM DD YYYY HH:MM AM/PM
	 * where MMM is month in a string form (see MONTHS above)
	 * This is a tremendous simplification over any of the string 
	 * representations intrinsic to the calendar class
	 * @param strDate a date or time span, in string format
	 * @return
	 */
	private boolean stringToCalendar ( String strDate ) {
		int iTokens;
		StringTokenizer strk;
		String strAmPm;
		
		theYear = "";
		theMonth = "Jan";
		theDay = "";
		theHour = "";
		theMinute = "";
		amHour = "AM";
		
		if ( strDate!=null && strDate.length()>0 ) {
			strk = new StringTokenizer(strDate.trim(), " :");
			iTokens =  strk.countTokens();
			if ( iTokens>0 )  // Month
				theMonth = strk.nextToken();
			if ( iTokens>1 )  // day in the month
				theDay = strk.nextToken();
			if ( iTokens>2 )  //year
				theYear = strk.nextToken();
			if ( iTokens>3 )  // hour
				theHour = strk.nextToken();
			if ( iTokens>4 )  // minute
				theMinute = strk.nextToken();
			if ( iTokens>5 ) { // AM / PM
				strAmPm = strk.nextToken();
				if ( strAmPm.trim().equalsIgnoreCase("PM") ||
					 strAmPm.trim().equalsIgnoreCase("P.M.")) {
					amHour = "PM";
				} else {
					amHour = "AM";
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
		
		if ( theDay.isEmpty() && theYear.isEmpty() && 
			 theHour.isEmpty() && theMinute.isEmpty() )
			return(strDate);
		
		strDate = String.format ("%s %s %s %s:%s %s", theMonth, theDay, theYear,
				theHour, theMinute, amHour);
	
		return(strDate);
	}
	
	
	private void build(String answer) {
		int iTimeUnits = question.getTimeUnits();
		
		useYears   = ((iTimeUnits & TimeSpanAnswerFormFieldPanel.BIT_YEAR)>0 )   ? true : false;
		useMonths  = ((iTimeUnits & TimeSpanAnswerFormFieldPanel.BIT_MONTH)>0 )  ? true : false;
		useDays    = ((iTimeUnits & TimeSpanAnswerFormFieldPanel.BIT_DAY)>0 )    ? true : false;
		useHours   = ((iTimeUnits & TimeSpanAnswerFormFieldPanel.BIT_HOUR)>0 )   ? true : false;
		useMinutes = ((iTimeUnits & TimeSpanAnswerFormFieldPanel.BIT_MINUTE)>0 ) ? true : false;
		

		lblYearPrompt = " Date (";
		if ( useMonths )
			lblYearPrompt += "Month,";
		if ( useDays )
			lblYearPrompt += "Day,";
		if ( useYears )
			lblYearPrompt += "Year";
		lblYearPrompt += ")";
		
		lblHourPrompt = "Time ";
		if ( useHours && useMinutes )
			lblHourPrompt += "(HH:MM)";
		else if ( useHours)
			lblHourPrompt += "(Hours)";
		else if ( useMinutes )
			lblHourPrompt += "(Minutes)";
		
		inputYear  = new TextField ("Year", new PropertyModel(this, "theYear"), String.class);
		inputMonth = new DropDownChoice("Month", new PropertyModel(this,"theMonth"), MONTHS);
		add(inputMonth);
		inputDay    = new TextField("Day", new PropertyModel(this,"theDay"), String.class);
	    inputHour   = new TextField("Hour", new PropertyModel(this,"theHour"), String.class);
	    inputMinute = new TextField("Minute", new PropertyModel(this,"theMinute"), String.class);
	    radioAmPm = new RadioChoice ("ampm", new PropertyModel(this, "amHour"), AM_PM);
	    radioAmPm.setPrefix("<span style=\"white-space:nowrap;\">");
	    radioAmPm.setSuffix(
				"</span><span style=\"white-space:pre-wrap; color:#ffffff\"> . . </span>");
		
		add(inputMinute);
		add(inputHour);
		add(radioAmPm);
		add(inputDay);
		add(inputMonth);
	    add(inputYear);
	    
		lblYear   = new Label("lblYear", lblYearPrompt);
		lblHour   = new Label("lblHour", lblHourPrompt);
		add(lblYear);

		add(lblHour);
		
		inputYear.setVisible(useYears);
		lblYear.setVisible(useYears);
		inputMonth.setVisible(useMonths);	
		inputDay.setVisible(useDays);	
		inputHour.setVisible(useHours);
		lblHour.setVisible(useHours);	
		radioAmPm.setVisible(useHours);
		inputMinute.setVisible(useMinutes);
		
	    ArrayList<String> refAndDK = Lists.newArrayList();
	    if ( !question.getType().equals(Question.QuestionType.EGO_ID)) {
	    	if ( question.getDontKnowButton())
	    		refAndDK.add(dontKnow);
	    	if ( question.getRefuseButton())
	    		refAndDK.add(refuse);
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
		if ( useYears ) {
			theYear = theYear.trim();
			try { yearValue = Integer.parseInt(theYear);
			} catch ( NumberFormatException nfeYear) {
				yearValue = 1000;
				strNotification += " Non-digits in year field";
			}
			if ( yearValue < 1000 )
				strNotification += "Year too low ";
			if ( yearValue > 9999 ) 
				strNotification += "Year too high ";
		}
		if ( useDays ) {
			theDay = theDay.trim();
			try { dayValue = Integer.parseInt(theDay);
			} catch ( NumberFormatException nfeDay) {
				dayValue = 1;
				strNotification += " Non-digits in day field";
			}
			if ( dayValue<1 )
				strNotification += "Day too low ";
			if (dayValue>31 )
				strNotification += "Day too high ";
		}
		if ( useHours ) {
			theHour = theHour.trim();
			try { hourValue = Integer.parseInt(theHour);
			} catch ( NumberFormatException nfeHour) {
				hourValue = 1;
				strNotification += " Non-digits in hour field";
			}
			if ( hourValue<1 )
				strNotification += "Hour too low ";
			if ( hourValue>12 )
				strNotification += "Hour too high ";
		}
		if ( useMinutes ) {
			theMinute = theMinute.trim();
			try { minuteValue = Integer.parseInt(theMinute);
			} catch ( NumberFormatException nfeMinute ) {
				minuteValue = 1;
				strNotification += " Non-digits in minute field";
			}
			if ( minuteValue<0 )
				strNotification += "Minute too low ";
			if ( minuteValue>59 )
				strNotification += "Minute too high ";
		}
		return(strNotification);
	}
	
	/** 
	 * if the user selected dontKnow or refused to answer a question
	 * don't bother checking the responses.
	 */
	public boolean rangeCheckOkay() {
		
		if ( dontKnow() || refused())
			return (true);
		if ( useYears ) {
			theYear = theYear.trim();
			try { yearValue = Integer.parseInt(theYear);
			} catch ( NumberFormatException nfeYear) {
				yearValue = 1000;
				return(false);
			}
			if ( yearValue < 1000 || yearValue > 9999 ) 
				return(false);
		}
		if ( useDays ) {
			theDay = theDay.trim();
			try { dayValue = Integer.parseInt(theDay);
			} catch ( NumberFormatException nfeDay) {
				dayValue = 1;
				return(false);
			}
			if ( dayValue<1 || dayValue>31 )
				return(false);
		}
		if ( useHours ) {
			theHour = theHour.trim();
			try { hourValue = Integer.parseInt(theHour);
			} catch ( NumberFormatException nfeHour) {
				hourValue = 1;
				return(false);
			}
			if ( hourValue<1 || hourValue>12 )
				return(false);
		}
		if ( useMinutes ) {
			theMinute = theMinute.trim();
			try { minuteValue = Integer.parseInt(theMinute);
			} catch ( NumberFormatException nfeMinute ) {
				minuteValue = 1;
				return(false);
			}
			if ( minuteValue<0 || minuteValue>59 )
				return(false);
		}
		return(true);
	}
	
	public String getAnswer() {
		return (calendarToString());
	}
	
	public void setTheYear ( String theYear ) {
		this.theYear = (theYear==null) ? "" : theYear;
	}
	public String getTheYear() { return (theYear);}
	
	public void setTheMonth ( String theMonth ) {
		this.theMonth = (theMonth==null) ? "Jan" : theMonth;
	}
	public String getTheMonth() { return (theMonth);}
	
	public void setTheDay ( String theDay ) {
		this.theDay = (theDay==null) ? "" : theDay;
	}
	public String getTheDay() { return (theDay);}
	
	public void setTheHour ( String theHour ) {
		this.theHour = (theHour==null) ? "" : theHour;
	}
	public String getTheHour() { return (theHour);}
	
	public void setTheMinute ( String theMinute ) {
		this.theMinute = (theMinute==null)? "" : theMinute;
	}
	public String getTheMinute() { return (theMinute);}
	
	public void setAmHour ( String amHour ) {
		this.amHour = (amHour==null) ? "AM" : amHour;
	}
	public String getAmHour() { return (amHour);}
	
	
	public void setAutoFocus() {
		if (inputHour.isVisible()) {
			inputHour.add(new FocusOnLoadBehavior());
			return;
		}	
		if (inputMinute.isVisible()) {
			inputMinute.add(new FocusOnLoadBehavior());
			return;
		}
		if (inputDay.isVisible()) {
			inputDay.add(new FocusOnLoadBehavior());
			return;
		}
		if (inputMonth.isVisible()) {
			inputMonth.add(new FocusOnLoadBehavior());
			return;
		}
		if (inputYear.isVisible()) {
			inputYear.add(new FocusOnLoadBehavior());
			return;
		}		
	}

	public boolean dontKnow() {
		return refDKCheck.getSelected().contains(dontKnow);
	}

	public boolean refused() {
		return refDKCheck.getSelected().contains(refuse);
	}	
}
