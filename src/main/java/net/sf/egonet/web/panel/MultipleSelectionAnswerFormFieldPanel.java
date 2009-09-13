package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.persistence.Options;

public class MultipleSelectionAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private CheckboxesPanel<QuestionOption> answerField;
	private ArrayList<QuestionOption> originallySelectedOptions;
	
	public MultipleSelectionAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters) {
		super(id,question,alters);
		originallySelectedOptions = Lists.newArrayList();
		build();
	}
	
	public MultipleSelectionAnswerFormFieldPanel(String id, Question question, String answer, ArrayList<Alter> alters) {
		super(id,question,alters);
		originallySelectedOptions = Lists.newArrayList();
		try {
			for(String answerIdString : answer.split(",")) {
				Long answerId = Long.parseLong(answerIdString);
				for(QuestionOption option : getOptions()) {
					if(answerId.equals(option.getId())) {
						originallySelectedOptions.add(option);
					}
				}
			}
		} catch(Exception ex) {
			// Most likely failed to parse answer. Fall back to no existing answer.
		}
		build();
	}
	
	private void build() {
		add(new Label("prompt",getPrompt()));
		answerField = new CheckboxesPanel<QuestionOption>("answer",getOptions(),originallySelectedOptions) 
		{
			protected String showItem(QuestionOption option) {
				return option.getName();
			}
		};
		add(answerField);
	}

	public String getAnswer() {
		List<String> optionIdStrings = Lists.newArrayList();
		for(QuestionOption option : answerField.getSelected()) {
			optionIdStrings.add(option.getId().toString());
		}
		return Joiner.on(",").join(optionIdStrings);
	}
	
	public List<QuestionOption> getOptions() {
		return Options.getOptionsForQuestion(getQuestion().getId());
	}
}
