package net.sf.egonet.web.panel;

import java.util.ArrayList;

import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Answer.SkipReason;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;

public class TimeSpanAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private boolean useYears;
	private boolean useMonths;
	private boolean useWeeks;
	private boolean useDays;
	private Integer years;
	private Integer months;
	private Integer weeks;
	private Integer days;
	private TextField inputYears;
	private TextField inputMonths;
	private TextField inputWeeks;
	private TextField inputDays;
	private CheckboxesPanel<String> refDKCheck;
	
	public TimeSpanAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters, Long interviewId) {
		super(id,question,Answer.SkipReason.NONE,alters, interviewId);
		stringToTimeSpan("");
		useYears = true;
		useMonths = true;
		useWeeks = true;
		useDays = true;
		build("");
	}
	
	public TimeSpanAnswerFormFieldPanel(String id, 
			Question question, String answer, Answer.SkipReason skipReason, ArrayList<Alter> alters, Long interviewId) 
	{
		super(id,question,skipReason,alters,interviewId);
		stringToTimeSpan(answer);
		useYears = true;
		useMonths = true;
		useWeeks = true;
		useDays = true;
		build(answer);
	}

	/**
	 * Time Spans will be kept in a string of the format
	 * "(YY) years (MM) months (WW) weeks (dd) days"
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
		
		if ( years!=null && years.intValue()>0 )
			strResult += " " + years + " years";
		if ( months!=null && months.intValue()>0 )
			strResult += " " + months + " months";
		if ( weeks!=null && weeks.intValue()>0 )
			strResult += " " + weeks + " weeks";
		if ( days!=null && days.intValue()>0 )
			strResult += " " + days + " days";		
		strResult += " ";
		return(strResult);
	}
	
	private void build(String answer) {
		
		inputYears = new TextField ("Years", new PropertyModel(this, "years"), Integer.class);
		inputMonths = new TextField ("Months", new PropertyModel(this,"months"), Integer.class);	
		inputWeeks = new TextField("Weeks", new PropertyModel(this,"weeks"), Integer.class);		
		inputDays = new TextField("Days", new PropertyModel(this,"days"), Integer.class);
	
		add(inputYears);
		add(inputMonths);
		add(inputWeeks);
		add(inputDays);
		
		// check against everything being disallowed
		if ( !useYears && !useMonths && !useDays )
			useWeeks = true;
		
		inputYears.setVisible(useYears);
		inputMonths.setVisible(useMonths);
		inputWeeks.setVisible(useWeeks);
		inputDays.setVisible(useDays);
		
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
		
		if ( dontKnow() || refused())
			return(strNotification);
		return(strNotification);
	}
	
	/** 
	 * if the user selected dontKnow or refused to answer a question
	 * don't bother counting the responses.
	 * returns TRUE for now as ranges can have virtually any value, 
	 * but may be used in the future to make certain they are reasonable
	 */
	public boolean rangeCheckOkay() {
		
		if ( dontKnow() || refused())
			return (true);
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
