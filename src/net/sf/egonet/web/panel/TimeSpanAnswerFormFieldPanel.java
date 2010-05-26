package net.sf.egonet.web.panel;

import java.util.ArrayList;

import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.markup.html.basic.Label;

import com.google.common.collect.Lists;
import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Answer.SkipReason;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;

public class TimeSpanAnswerFormFieldPanel extends AnswerFormFieldPanel {

	public static final int BIT_YEAR 	= 0x01;
	public static final int BIT_MONTH 	= 0x02;
	public static final int BIT_WEEK 	= 0x04;
	public static final int BIT_DAY 	= 0x08;
	public static final int BIT_HOUR 	= 0x10;
	public static final int BIT_MINUTE  = 0x20;
	
	private boolean useYears;
	private boolean useMonths;
	private boolean useWeeks;
	private boolean useDays;
	private boolean useHours;
	private boolean useMinutes;
	private Integer years;
	private Integer months;
	private Integer weeks;
	private Integer days;
	private Integer hours;
	private Integer minutes;
	private TextField inputYears;
	private TextField inputMonths;
	private TextField inputWeeks;
	private TextField inputDays;
	private TextField inputHours;
	private TextField inputMinutes;
	private Label lblYears;
	private Label lblMonths;
	private Label lblWeeks;
	private Label lblDays;
	private Label lblHours;
	private Label lblMinutes;
	private CheckboxesPanel<String> refDKCheck;
	
	public TimeSpanAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters, Long interviewId) {
		super(id,question,Answer.SkipReason.NONE,alters, interviewId);
		stringToTimeSpan("");
		build("");
	}
	
	public TimeSpanAnswerFormFieldPanel(String id, 
			Question question, String answer, Answer.SkipReason skipReason, ArrayList<Alter> alters, Long interviewId) 
	{
		super(id,question,skipReason,alters,interviewId);
		stringToTimeSpan(answer);
		build(answer);
	}

	/**
	 * Time Spans will be kept in a string of the format
	 * "(YY) years (MM) months (WW) weeks (dd) days (hh) hours (mm) minutes
	 * where the (YY)... are integer values
	 * @param strTimeSpan - a string that contains the time span
	 * @return false if any errors are encountered, true otherwise
	 */
	private boolean stringToTimeSpan( String strTimeSpan ) {
		boolean okay = true;
		String[] strWords;
		int iValue = 0;

		years = new Integer(0);
		months = new Integer(0);
		weeks = new Integer(0);
		days = new Integer(0);
		hours = new Integer(0);
		minutes = new Integer(0);
		
		strTimeSpan = strTimeSpan.trim();
		if ( strTimeSpan==null || strTimeSpan.length()==0 )
			return(okay);
		
		strWords = strTimeSpan.split(" ");
		for ( String str : strWords ) {
			if ( str.equalsIgnoreCase("years")) {
				years = new Integer(iValue);
				iValue = 0;
			} else if ( str.equalsIgnoreCase("months")) {
				months = new Integer(iValue);
				iValue = 0;
			} else if ( str.equalsIgnoreCase("weeks")) {
				weeks = new Integer(iValue);
				iValue = 0;
			} else if ( str.equalsIgnoreCase("days")) {
				days = new Integer(iValue);
				iValue = 0;
			} else if ( str.equalsIgnoreCase("hours")) {
				hours = new Integer(iValue);
				iValue = 0;
			} else if ( str.equalsIgnoreCase("minutes")) {
				minutes = new Integer(iValue);
				iValue = 0;
			} else {
				try {
					iValue = Integer.parseInt(str);
				} catch ( NumberFormatException nfe ) {
					okay = false;
					iValue = 0;
				}
			}
		}
	return(okay);
	}
	
	/**
	 * creates a human-readable string with the results of the 
	 * various text entry fields.
	 * @return human-readable string
	 */
	private String timeSpanToString() {
		String strResult = "";
		
		if ( useYears && years!=null && years.intValue()>0 )
			strResult += " " + years + " years";
		if ( useMonths && months!=null && months.intValue()>0 )
			strResult += " " + months + " months";
		if ( useWeeks && weeks!=null && weeks.intValue()>0 )
			strResult += " " + weeks + " weeks";
		if ( useDays && days!=null && days.intValue()>0 )
			strResult += " " + days + " days";	
		if ( useHours && hours!=null && hours.intValue()>0 )
			strResult += " " + hours + " hours";
		if ( useMinutes && minutes!=null && minutes.intValue()>0 )
			strResult += " " + minutes + " minutes";
		strResult += " ";
		return(strResult);
	}
	
	private void build(String answer) {
		int iTimeUnits = question.getTimeUnits();
		
		useYears   = ((iTimeUnits & BIT_YEAR)>0 )   ? true : false;
		useMonths  = ((iTimeUnits & BIT_MONTH)>0 )  ? true : false;
		useWeeks   = ((iTimeUnits & BIT_WEEK)>0 )   ? true : false;
		useDays    = ((iTimeUnits & BIT_DAY)>0 )    ? true : false;
		useHours   = ((iTimeUnits & BIT_HOUR)>0 )   ? true : false;
		useMinutes = ((iTimeUnits & BIT_MINUTE)>0 ) ? true : false;
		
		inputYears   = new TextField("Years",   new PropertyModel(this,"years"), Integer.class);
		inputMonths  = new TextField("Months",  new PropertyModel(this,"months"), Integer.class);	
		inputWeeks   = new TextField("Weeks",   new PropertyModel(this,"weeks"), Integer.class);		
		inputDays    = new TextField("Days",    new PropertyModel(this,"days"), Integer.class);
	    inputHours   = new TextField("Hours",   new PropertyModel(this,"hours"), Integer.class);
	    inputMinutes = new TextField("Minutes", new PropertyModel(this,"minutes"), Integer.class);
		add(inputYears);
		add(inputMonths);
		add(inputWeeks);
		add(inputDays);
		add(inputHours);
		add(inputMinutes);
		
		lblYears   = new Label("lblYears", "Years");
		lblMonths  = new Label("lblMonths", "Months");
		lblWeeks   = new Label("lblWeeks", "Weeks");
		lblDays    = new Label("lblDays", "Days");
		lblHours   = new Label("lblHours", "Hours");
		lblMinutes = new Label("lblMinutes", "Minutes");
		add(lblYears);
		add(lblMonths);
		add(lblWeeks);
		add(lblDays);
		add(lblHours);
		add(lblMinutes);		
		
		// check against everything being disallowed
		if ( !useYears && !useMonths && !useDays && !useHours && !useMinutes )
			useWeeks = true;
		
		inputYears.setVisible(useYears);
		lblYears.setVisible(useYears);
		inputMonths.setVisible(useMonths);
		lblMonths.setVisible(useMonths);
		inputWeeks.setVisible(useWeeks);
		lblWeeks.setVisible(useWeeks);		
		inputDays.setVisible(useDays);
		lblDays.setVisible(useDays);		
		inputHours.setVisible(useHours);
		lblHours.setVisible(useHours);		
		inputMinutes.setVisible(useMinutes);
		lblMinutes.setVisible(useMinutes);
		
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
	
	public String getAnswer() {
		return (timeSpanToString());
	}
	
	/**
	 * returns the string used if an incorrect number of checkboxes
	 * are selected to prompt the user to check more or fewer
	 */
	
	public String getRangeCheckNotification() {
		String strNotification = "";
		int nonZeroCount = 0;
		
		if ( dontKnow() || refused())
			return(strNotification);
		if ( useYears && years>0)
			++nonZeroCount;
		if ( useMonths && months>0)
			++nonZeroCount;
		if ( useWeeks && weeks>0)
			++nonZeroCount;
		if ( useDays && days>0)
			++nonZeroCount;
		if ( useHours && hours>0)
			++nonZeroCount;
		if ( useMinutes && minutes>0)
			++nonZeroCount;
		if (nonZeroCount==0)
			strNotification = "Cannot leave all fields zero.";
		return(strNotification);
	}
	
	/** 
	 * if the user selected dontKnow or refused to answer a question
	 * don't bother counting the responses.
	 * returns TRUE for now as ranges can have virtually any value, 
	 * but may be used in the future to make certain they are reasonable
	 */
	public boolean rangeCheckOkay() {
		int nonZeroCount = 0;
		
		if ( dontKnow() || refused())
			return (true);
		if ( useYears && years>0)
			++nonZeroCount;
		if ( useMonths && months>0)
			++nonZeroCount;
		if ( useWeeks && weeks>0)
			++nonZeroCount;
		if ( useDays && days>0)
			++nonZeroCount;
		if ( useHours && hours>0)
			++nonZeroCount;
		if ( useMinutes && minutes>0)
			++nonZeroCount;
		if (nonZeroCount==0)
			return(false);
		return(true);
	}

	public void setYears ( Integer years ) {
		this.years = (years==null) ? new Integer(0) : years;
	}
	public Integer getYears() { return(years);}
	
	public void setMonths ( Integer months ) {
		this.months = (months==null) ? new Integer(0) : months;
	}
	public Integer getMonths() { return(months);}
	
	public void setWeeks ( Integer weeks ) {
		this.weeks = (weeks==null) ? new Integer(0) : weeks;
	}
	public Integer getWeeks() { return(weeks);}
	
	public void setDays ( Integer days ) {
		this.days = (days==null) ? new Integer(0) : days;
	}
	public Integer getDays() { return(days); }

	public void setHours ( Integer hours ) {
		this.hours = (hours==null) ? new Integer(0) : hours;
	}
	public Integer getHours() { return(hours); }
	
	public void setMinutes ( Integer minutes ) {
		this.minutes = (minutes==null) ? new Integer(0) : minutes;
	}
	public Integer getMinutes() { return(minutes); 
	}	
	public void setAutoFocus() {
		inputYears.add(new FocusOnLoadBehavior());
	}

	public boolean dontKnow() {
		return refDKCheck.getSelected().contains(dontKnow);
	}

	public boolean refused() {
		return refDKCheck.getSelected().contains(refuse);
	}
}
