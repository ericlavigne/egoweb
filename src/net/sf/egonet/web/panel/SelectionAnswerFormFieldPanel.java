package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.web.component.FocusOnLoadBehavior;

public class SelectionAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private Model answer;
	private DropDownChoice dropDownChoice;
	private Label otherSpecifyLabel;
	private TextField otherSpecifyTextField;
	private String otherText;
	
	public SelectionAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters, Long interviewId) {
		super(id,question,Answer.SkipReason.NONE,alters, interviewId);
		this.answer = new Model();
		build();
	}
	
	public SelectionAnswerFormFieldPanel(String id, 
			Question question, String answer, Answer.SkipReason skipReason, ArrayList<Alter> alters, Long interviewId) 
	{
		super(id,question,skipReason,alters,interviewId);
		this.answer = new Model();
		if(skipReason.equals(Answer.SkipReason.DONT_KNOW)) {
			this.answer = new Model(dontKnow);
		} else if(skipReason.equals(Answer.SkipReason.REFUSE)) {
			this.answer = new Model(refuse);
		} else {
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
		}
		build();
	}
	
	private void build() {
		List<Object> choices = Lists.newArrayList();
		choices.addAll(getOptions());
		if(! question.getType().equals(Question.QuestionType.EGO_ID)) {
			choices.addAll(Lists.newArrayList(dontKnow,refuse));
		}
		dropDownChoice = new DropDownChoice("answer",answer,choices);
		add(dropDownChoice);
		// features that will be visible only for
		// 'other/specify' questions
//		otherSpecifyLabel = new Label("otherSpecifyLabel", "Specify Other: ");
//		otherSpecifyTextField = new TextField("otherSpecifyTextField", new PropertyModel(this, "otherText"));
//		add(otherSpecifyLabel);
//		add(otherSpecifyTextField);
	}

	public String getAnswer() {
		Object selected = answer.getObject();
		return selected != null && selected instanceof QuestionOption ? 
				((QuestionOption) selected).getId().toString() : null;
	}
	
	public List<QuestionOption> getOptions() {
		return Options.getOptionsForQuestion(getQuestion().getId());
	}

	public void setAutoFocus() {
		dropDownChoice.add(new FocusOnLoadBehavior());
	}

	@Override
	public boolean dontKnow() {
		return answer.getObject() != null && answer.getObject().equals(dontKnow);
	}

	@Override
	public boolean refused() {
		return answer.getObject() != null && answer.getObject().equals(refuse);
	}
	
	public void setOtherText ( String otherText ) {
		this.otherText = (otherText==null) ? "" : otherText;
	}
	public String getOtherText() {
		return (( otherText==null) ? "" : otherText ) ;
	}

}
