package net.sf.egonet.web.panel;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;

import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.persistence.Options;

public class SelectionAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private Model answer;
	
	public SelectionAnswerFormFieldPanel(String id, Question question) {
		super(id,question);
		this.answer = new Model();
		build();
	}
	
	public SelectionAnswerFormFieldPanel(String id, Question question, String answer) {
		super(id,question);
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
		add(new Label("prompt",question.getPrompt()));
		add(new DropDownChoice("answer",answer,getOptions()));
	}

	public String getAnswer() {
		QuestionOption option = (QuestionOption) answer.getObject();
		return option == null ? null : option.getId().toString();
	}
	
	public List<QuestionOption> getOptions() {
		return Options.getOptionsForQuestion(getQuestion().getId());
	}
}
