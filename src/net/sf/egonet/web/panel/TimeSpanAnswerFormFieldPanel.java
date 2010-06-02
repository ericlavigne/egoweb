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
	private String years;
	private String months;
	private String weeks;
	private String days;
	private String hours;
	private String minutes;
	private int yearsValue;
	private int monthsValue;
	private int weeksValue;
	private int daysValue;
	private int hoursValue;
	private int minutesValue;
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
		String strValue = "";

		years = "";
		months = "";
		weeks = "";
		days = "";
		hours = "";
		minutes = "";
		
		strTimeSpan = strTimeSpan.trim();
		if ( strTimeSpan==null || strTimeSpan.length()==0 )
			return(okay);
		
		strWords = strTimeSpan.split(" ");
		for ( String str : strWords ) {
			if ( str.equalsIgnoreCase("years")) {
				years = strValue;
				strValue = "";
			} else if ( str.equalsIgnoreCase("months")) {
				months = strValue;
				strValue = "";
			} else if ( str.equalsIgnoreCase("weeks")) {
				weeks = strValue;
				strValue = "";
			} else if ( str.equalsIgnoreCase("days")) {
				days = strValue;
				strValue = "";
			} else if ( str.equalsIgnoreCase("hours")) {
				hours = strValue;
				strValue = "";
			} else if ( str.equalsIgnoreCase("minutes")) {
				minutes = strValue;
				strValue = "";
			} else {
				strValue = str;
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
		
		if ( useYears && years!=null )
			strResult += " " + years + " years";
		if ( useMonths && months!=null )
			strResult += " " + months + " months";
		if ( useWeeks && weeks!=null )
			strResult += " " + weeks + " weeks";
		if ( useDays && days!=null )
			strResult += " " + days + " days";	
		if ( useHours && hours!=null )
			strResult += " " + hours + " hours";
		if ( useMinutes && minutes!=null )
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
		
		inputYears   = new TextField("Years",   new PropertyModel(this,"years"), String.class);
		inputMonths  = new TextField("Months",  new PropertyModel(this,"months"), String.class);	
		inputWeeks   = new TextField("Weeks",   new PropertyModel(this,"weeks"), String.class);		
		inputDays    = new TextField("Days",    new PropertyModel(this,"days"), String.class);
	    inputHours   = new TextField("Hours",   new PropertyModel(this,"hours"), String.class);
	    inputMinutes = new TextField("Minutes", new PropertyModel(this,"minutes"), String.class);

		add(inputMinutes);
		add(inputHours);	
		add(inputDays);		
		add(inputWeeks);		
		add(inputMonths);	
	    add(inputYears);
	    
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
		
		yearsValue = 0;
		monthsValue = 0;
		weeksValue = 0;
		daysValue = 0;
		hoursValue = 0;
		minutesValue = 0;
		
		if ( useYears ) {
			years = years.trim();
		    if ( years.length()>0 ) {
			    try { yearsValue = Integer.parseInt(years);
		        } catch ( NumberFormatException nfeYears ) { 
			        yearsValue = 0;
			        strNotification += " non-digits in years";
		        }
		    if( yearsValue>0 )
			    ++nonZeroCount;
		    }
		}
		
		if ( useMonths ) {
			months = months.trim();
		    if ( months.length()>0 ) {
		    	try { monthsValue = Integer.parseInt(months);
		    	} catch ( NumberFormatException nfeMonths ) {
			        yearsValue = 0;
			        strNotification += " non-digits in months";
		    	}
		    if ( monthsValue>0 )
		    	++nonZeroCount;
		    }
		}
	
		if ( useWeeks ) {
			weeks = weeks.trim();
			if ( weeks.length()>0 ) {
				try { weeksValue = Integer.parseInt(weeks);
				} catch ( NumberFormatException nfeWeeks ) {
			        yearsValue = 0;
			        strNotification += " non-digits in weeks";
				}
			if ( weeksValue>0 )
				++nonZeroCount;
			}
		}
			
		if ( useDays ) {
			days = days.trim();
			if ( days.length()>0 ) {
				try { daysValue = Integer.parseInt(days);
				} catch ( NumberFormatException nfeDays ) {
			        yearsValue = 0;
			        strNotification += " non-digits in days";
				}
			if ( daysValue>0 )
				++nonZeroCount;
			}
		}

		if ( useHours ) {
			hours = hours.trim();
			if ( hours.length()>0 ) {
				try { hoursValue = Integer.parseInt(hours);
				} catch ( NumberFormatException nfeHours ) {
			        yearsValue = 0;
			        strNotification += " non-digits in hours";
				}
			if ( hoursValue>0 )
				++nonZeroCount;
			}
		}
		
		if ( useMinutes ) {
			minutes = minutes.trim();
			if ( minutes.length()>0 ) {
				try { minutesValue = Integer.parseInt(minutes);
				} catch ( NumberFormatException nfeMinutes ) {
			        yearsValue = 0;
			        strNotification += " non-digits in minutes";
				}
			if ( minutesValue>0 )
				++nonZeroCount;
			}
		}

		if (nonZeroCount==0)
			strNotification += " Cannot leave all fields zero.";
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
		
		yearsValue = 0;
		monthsValue = 0;
		weeksValue = 0;
		daysValue = 0;
		hoursValue = 0;
		minutesValue = 0;
		
		if ( useYears ) {
			years = years.trim();
		    if ( years.length()>0 ) {
			    try { yearsValue = Integer.parseInt(years);
		        } catch ( NumberFormatException nfeYears ) { 
			        return(false);
		        }
		    if( yearsValue>0 )
			    ++nonZeroCount;
		    }
		}
		
		if ( useMonths ) {
			months = months.trim();
		    if ( months.length()>0 ) {
		    	try { monthsValue = Integer.parseInt(months);
		    	} catch ( NumberFormatException nfeMonths ) {
		    		return(false);
		    	}
		    if ( monthsValue>0 )
		    	++nonZeroCount;
		    }
		}
	
		if ( useWeeks ) {
			weeks = weeks.trim();
			if ( weeks.length()>0 ) {
				try { weeksValue = Integer.parseInt(weeks);
				} catch ( NumberFormatException nfeWeeks ) {
					return(false);
				}
			if ( weeksValue>0 )
				++nonZeroCount;
			}
		}
			
		if ( useDays ) {
			days = days.trim();
			if ( days.length()>0 ) {
				try { daysValue = Integer.parseInt(days);
				} catch ( NumberFormatException nfeDays ) {
					return(false);
				}
			if ( daysValue>0 )
				++nonZeroCount;
			}
		}

		if ( useHours ) {
			hours = hours.trim();
			if ( hours.length()>0 ) {
				try { hoursValue = Integer.parseInt(hours);
				} catch ( NumberFormatException nfeHours ) {
					return(false);
				}
			if ( hoursValue>0 )
				++nonZeroCount;
			}
		}
		
		if ( useMinutes ) {
			minutes = minutes.trim();
			if ( minutes.length()>0 ) {
				try { minutesValue = Integer.parseInt(minutes);
				} catch ( NumberFormatException nfeMinutes ) {
					return(false);
				}
			if ( minutesValue>0 )
				++nonZeroCount;
			}
		}

		if (nonZeroCount==0)
			return(false);
		return(true);
	}

	public void setYears ( String years ) {
		this.years = (years==null) ? "" : years;
	}
	public String getYears() { return(years);}
	
	public void setMonths ( String months ) {
		this.months = (months==null) ? "" : months;
	}
	public String getMonths() { return(months);}
	
	public void setWeeks ( String weeks ) {
		this.weeks = (weeks==null) ? "" : weeks;
	}
	public String getWeeks() { return(weeks);}
	
	public void setDays ( String days ) {
		this.days = (days==null) ? "" : days;
	}
	public String getDays() { return(days); }

	public void setHours ( String hours ) {
		this.hours = (hours==null) ? "" : hours;
	}
	public String getHours() { return(hours); }
	
	public void setMinutes ( String minutes ) {
		this.minutes = (minutes==null) ? "" : minutes;
	}
	public String getMinutes() { return(minutes); 
	}	
	public void setAutoFocus() {
		if (inputMinutes.isVisible()) {
			inputMinutes.add(new FocusOnLoadBehavior());
			return;
		}
		if (inputHours.isVisible()) {
			inputHours.add(new FocusOnLoadBehavior());
			return;
		}		
		if (inputDays.isVisible()) {
			inputDays.add(new FocusOnLoadBehavior());
			return;
		}
		if (inputWeeks.isVisible()) {
			inputWeeks.add(new FocusOnLoadBehavior());
			return;
		}
		if (inputMonths.isVisible()) {
			inputMonths.add(new FocusOnLoadBehavior());
			return;
		}
		if (inputYears.isVisible()) {
			inputYears.add(new FocusOnLoadBehavior());
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
