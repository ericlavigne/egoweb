package net.sf.egonet.web.panel;

import java.util.ArrayList;

import org.apache.wicket.model.Model;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Answer.SkipReason;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextArea;

public class TextAreaAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private TextArea textArea;
	private CheckboxesPanel<String> refDKCheck;
	
	public TextAreaAnswerFormFieldPanel (String id, Question question, ArrayList<Alter> alters, Long interviewId) {
		super(id,question,Answer.SkipReason.NONE,alters,interviewId);
		build("");
	}
	
	public TextAreaAnswerFormFieldPanel (String id, 
			Question question, String answer, Answer.SkipReason skipReason, ArrayList<Alter> alters, Long interviewId) 
	{
		super(id,question,skipReason,alters,interviewId);
		build(answer);
	}
	
	private void build(String answer) {
		this.textArea = new TextArea("answer", new Model(answer));
		add(textArea);

		ArrayList<String> refAndDK = Lists.newArrayList();
		if (!question.getType().equals(Question.QuestionType.EGO_ID))
		{
			if (question.getDontKnowButton())
				refAndDK.add(dontKnow);
			if (question.getRefuseButton())
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

	public String getAnswer() {
		return textArea.getText();
	}
	
	public void setAutoFocus() {
		textArea.add(new FocusOnLoadBehavior());
	}

	public boolean dontKnow() {
		return refDKCheck.getSelected().contains(dontKnow);
	}

	public boolean refused() {
		return refDKCheck.getSelected().contains(refuse);
	}
}
