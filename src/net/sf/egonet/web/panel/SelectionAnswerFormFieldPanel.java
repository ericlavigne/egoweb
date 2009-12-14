package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.web.component.FocusOnLoadBehavior;

public class SelectionAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private Model answer;
	private DropDownChoice dropDownChoice;
	
	public SelectionAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters) {
		super(id,question,alters);
		this.answer = new Model();
		build();
	}
	
	public SelectionAnswerFormFieldPanel(String id, Question question, String answer, ArrayList<Alter> alters) {
		super(id,question,alters);
		this.answer = new Model();
		try {
			Long optionId = Long.parseLong(answer);
			for(QuestionOption option : getOptions()) {
				if(option.getId().equals(optionId)) {
					this.answer = new Model(option);
				}
			}
		} catch(Exception ex) {
			// Most likely failed to parse answer. Fall back to no existing answer.
		}
		build();
	}
	
	private void build() {
		dropDownChoice = new DropDownChoice("answer",answer,getOptions());
		add(dropDownChoice);
	}

	public String getAnswer() {
		QuestionOption option = (QuestionOption) answer.getObject();
		return option == null ? null : option.getId().toString();
	}
	
	public List<QuestionOption> getOptions() {
		return Options.getOptionsForQuestion(getQuestion().getId());
	}

	public void setAutoFocus() {
		dropDownChoice.add(new FocusOnLoadBehavior());
	}
}
