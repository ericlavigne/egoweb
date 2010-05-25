package net.sf.egonet.web.panel;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.PropertyModel;

import net.sf.egonet.web.panel.TimeSpanAnswerFormFieldPanel;

public class TimeUnitsPanel extends Panel {

	private Question question;
	private int timeUnitBits;
	
	private Boolean useYears;
	private Boolean useMonths;
	private Boolean useWeeks;
	private Boolean useDays;
	private Boolean useHours;
	private Boolean useMinutes;
	
	private CheckBox cbYears;
	private CheckBox cbMonths;
	private CheckBox cbWeeks;
	private CheckBox cbDays;
	private CheckBox cbHours;
	private CheckBox cbMinutes;
	private Label    lblWeeks;
	private Form timeUnitsForm;
	
	public TimeUnitsPanel (String id, Question question) {
		super(id);
		this.question = question;
		timeUnitBits = question.getTimeUnits();
		useYears 	= ((timeUnitBits & TimeSpanAnswerFormFieldPanel.BIT_YEAR)   >0 ) ? true : false;
		useMonths 	= ((timeUnitBits & TimeSpanAnswerFormFieldPanel.BIT_MONTH)  >0 ) ? true : false;
		useWeeks 	= ((timeUnitBits & TimeSpanAnswerFormFieldPanel.BIT_WEEK)   >0 ) ? true : false;
		useDays 	= ((timeUnitBits & TimeSpanAnswerFormFieldPanel.BIT_DAY)    >0 ) ? true : false;
		useHours 	= ((timeUnitBits & TimeSpanAnswerFormFieldPanel.BIT_HOUR)   >0 ) ? true : false;
		useMinutes 	= ((timeUnitBits & TimeSpanAnswerFormFieldPanel.BIT_MINUTE) >0 ) ? true : false;
		build();
	}
	
	/**
	 * builds the panel
	 * creates the form an all input widgets
	 */ 
	private void build() {	
		timeUnitsForm = new Form("timeUnitsForm");
		setOutputMarkupId(true);
		timeUnitsForm.setOutputMarkupId(true);
		cbYears 	= new CheckBox ("yrs",  new PropertyModel(this,"useYears"));
		cbMonths 	= new CheckBox ("mons", new PropertyModel(this,"useMonths"));
		cbWeeks 	= new CheckBox ("wks",  new PropertyModel(this,"useWeeks"));
		cbDays 		= new CheckBox ("days", new PropertyModel(this,"useDays"));
		cbHours 	= new CheckBox ("hrs",  new PropertyModel(this,"useHours"));
		cbMinutes 	= new CheckBox ("mins", new PropertyModel(this,"useMinutes"));
		lblWeeks    = new Label    ("wksLabel", "wks");
		
		timeUnitsForm.add(cbYears);
		timeUnitsForm.add(cbMonths);
		timeUnitsForm.add(cbWeeks);
		timeUnitsForm.add(cbDays);
		timeUnitsForm.add(cbHours);
		timeUnitsForm.add(cbMinutes);
		timeUnitsForm.add(lblWeeks);
		
		cbWeeks.setOutputMarkupId(true);
		lblWeeks.setOutputMarkupId(true);
		if ( question.getAnswerType()==Answer.AnswerType.DATE) {
			cbWeeks.setVisible(false);
			lblWeeks.setVisible(false);
		}
		add(timeUnitsForm);
	}

	/**
	 * will react to the user changing the question type in the
	 * EditQuestionPanel
	 * TIME_SPANS will have a week option, 
	 * DATEs will not
	 * @param weeksVisible - if true, weeks checkbox should be visible
	 */
	public void setWeeksVisible (boolean weeksVisible ) {
		cbWeeks.setVisible(weeksVisible);
		lblWeeks.setVisible(weeksVisible);		
	}
	
	/**
	 * collects all the time unit checkbox options and places them in
	 * one bitfield integer
	 * @return an integer with bits corresponding to the time fields we
	 * want to use in a DATE or TIME_SPAN question
	 */
	public int getTimeUnits() {
		timeUnitBits = 0;
		if ( useYears )
			timeUnitBits += TimeSpanAnswerFormFieldPanel.BIT_YEAR;
		if ( useMonths )
			timeUnitBits += TimeSpanAnswerFormFieldPanel.BIT_MONTH;
		if ( useWeeks )
			timeUnitBits += TimeSpanAnswerFormFieldPanel.BIT_WEEK;
		if ( useDays )
			timeUnitBits += TimeSpanAnswerFormFieldPanel.BIT_DAY;
		if ( useHours )
			timeUnitBits += TimeSpanAnswerFormFieldPanel.BIT_HOUR;
		if ( useMinutes )
			timeUnitBits += TimeSpanAnswerFormFieldPanel.BIT_MINUTE;
		if ( timeUnitBits==0 )
			timeUnitBits = TimeSpanAnswerFormFieldPanel.BIT_DAY;
		return(timeUnitBits);
	}
}
