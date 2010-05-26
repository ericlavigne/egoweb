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
	
	private Integer   theYear;
	private String    theMonth;
	private Integer   theDay;
	private Integer   theHour;
	private Integer   theMinute;
	private String    amHour;
	
	private TextField inputYear;
	private DropDownChoice inputMonth;
	private TextField inputDay;
	private TextField inputHour;
	private TextField inputMinute;
	private RadioChoice radioAmPm;
	
	private Label lblYear;
	private Label lblMonth;
	private Label lblWeek;
	private Label lblDay;
	private Label lblHour;
	private Label lblMinute;
	
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
		
		theYear = new Integer(0);
		theMonth = "Jan";
		theDay = new Integer(0);
		theHour = new Integer(0);
		theMinute = new Integer(0);
		amHour = "AM";
		
		if ( strDate!=null && strDate.length()>0 ) {
			strk = new StringTokenizer(strDate.trim(), " :");
			iTokens =  strk.countTokens();
			if ( iTokens>0 )  // Month
				theMonth = strk.nextToken();
			if ( iTokens>1 ) { // day in the month
				try {
					theDay = new Integer(strk.nextToken());
				} catch ( NumberFormatException nfe1 ) {
					theDay = new Integer(0);
				}
			}
			if ( iTokens>2 ) { //year
				try { 
					theYear = new Integer(strk.nextToken());
				} catch ( NumberFormatException nfe2 ) {
					theYear = new Integer(0);
				}
			}
			if ( iTokens>3 ) { // hour
				try {
					theHour = new Integer(strk.nextToken());
				} catch ( NumberFormatException nfe3 ) {
					theHour = new Integer(0);
				}
			}
			if ( iTokens>4 ) { // minute
				try {
					theMinute = new Integer(strk.nextToken());
				} catch ( NumberFormatException nfe4 ) {
					theMinute = new Integer(0);
				}
			}
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
		
//		if ( useHours ) 
//			strDate += theHour;
//		if ( useHours && useMinutes )
//			strDate += ":";
//		if ( useMinutes )
//			strDate += theMinute;
//		if  (useHours)
//			strDate += " " + amHour;
//		if ( useMonths )
//			strDate += " " + theMonth;
//		if ( useDays )
//			strDate += " " + theDay;
//		if ( useYears )
//			strDate += " " + theYear;
		strDate = String.format ("%s %d %d %d:%d %s", theMonth, theDay, theYear,
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
		
		inputYear  = new TextField ("Year", new PropertyModel(this, "theYear"), Integer.class);
		inputMonth = new DropDownChoice("Month", new PropertyModel(this,"theMonth"), MONTHS);
		add(inputMonth);
		inputDay    = new TextField("Day", new PropertyModel(this,"theDay"), Integer.class);
	    inputHour   = new TextField("Hour", new PropertyModel(this,"theHour"), Integer.class);
	    inputMinute = new TextField("Minute", new PropertyModel(this,"theMinute"), Integer.class);
	    radioAmPm = new RadioChoice ("ampm", new PropertyModel(this, "amHour"), AM_PM);
	    
		add(inputMinute);
		add(inputHour);
		add(radioAmPm);
		add(inputDay);
		add(inputMonth);
	    add(inputYear);
	    
		lblYear   = new Label("lblYear", "Year");
		lblMonth  = new Label("lblMonth", "Month");
		lblDay    = new Label("lblDay", "Day");
		lblHour   = new Label("lblHour", "Hour");
		lblMinute = new Label("lblMinute", "Minute");
		add(lblYear);
		add(lblMonth);
		add(lblDay);
		add(lblHour);
		add(lblMinute);
		
		inputYear.setVisible(useYears);
		lblYear.setVisible(useYears);
		inputMonth.setVisible(useMonths);
		lblMonth.setVisible(useMonths);	
		inputDay.setVisible(useDays);
		lblDay.setVisible(useDays);		
		inputHour.setVisible(useHours);
		lblHour.setVisible(useHours);	
		radioAmPm.setVisible(useHours);
		inputMinute.setVisible(useMinutes);
		lblMinute.setVisible(useMinutes);
		
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
		if ( useYears ) {
			if ( theYear < 1000 )
				strNotification += "Year too low ";
			if ( theYear > 9999 ) 
				strNotification += "Year too high ";
		}
		if ( useDays ) {
			if ( theDay<1 )
				strNotification += "Day too low ";
			if (theDay>31 )
				strNotification += "Day too high ";
		}
		if ( useHours ) {
			if ( theHour>12 )
				strNotification += "Hour too high ";
			if ( theHour<1 )
				strNotification += "Hour too low ";
		}
		if ( useMinutes ) {
			if ( theMinute>59 )
				strNotification += "Minute too high ";
			if ( theMinute<0 )
				strNotification += "Minute too low ";
		}
		return(strNotification);
	}
	
	/** 
	 * if the user selected dontKnow or refused to answer a question
	 * don't bother counting the responses.
	 */
	public boolean rangeCheckOkay() {
		
		if ( dontKnow() || refused())
			return (true);
		if ( useYears && ( theYear < 1000 || theYear > 9999 )) 
			return(false);
		if ( useDays && (theDay<1 || theDay>31 ))
			return(false);
		if ( useHours && (theHour<1 || theHour>12 ))
			return(false);
		if ( useMinutes && (theMinute<0 || theMinute>59 ))
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
	
	public void setTheHour ( Integer theHour ) {
		this.theHour = (theHour==null)?new Integer(0) : theHour;
	}
	public Integer getTheHour() { return (theHour);}
	
	public void setTheMinute ( Integer theMinute ) {
		this.theMinute = (theMinute==null)?new Integer(0) : theMinute;
	}
	public Integer getTheMinute() { return (theMinute);}
	
	public void setAmHour ( String amHour ) {
		this.amHour = (amHour==null) ? "AM" : amHour;
	}
	public String getAmHour() { return (amHour);}
	
	
	public void setAutoFocus() {
		if (inputMinute.isVisible()) {
			inputMinute.add(new FocusOnLoadBehavior());
			return;
		}
		if (inputHour.isVisible()) {
			inputHour.add(new FocusOnLoadBehavior());
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
