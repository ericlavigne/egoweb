package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.persistence.Options;

public class MultipleSelectionAnswerFormFieldPanel extends AnswerFormFieldPanel {

	private CheckboxesPanel<Object> answerField;
	private ArrayList<QuestionOption> originallySelectedOptions;
	
	public MultipleSelectionAnswerFormFieldPanel(String id, Question question, ArrayList<Alter> alters) {
		super(id,question,Answer.SkipReason.NONE,alters);
		originallySelectedOptions = Lists.newArrayList();
		build();
	}
	
	public MultipleSelectionAnswerFormFieldPanel(String id, 
			Question question, String answer, Answer.SkipReason skipReason, ArrayList<Alter> alters) 
	{
		super(id,question,skipReason,alters);
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
		List<Object> allItems = Lists.newArrayList();
		allItems.addAll(getOptions());
		if(! question.getType().equals(Question.QuestionType.EGO_ID)) { // Can't refuse EgoID question
			allItems.add(dontKnow);
			allItems.add(refuse);
		}
		List<Object> selectedItems = Lists.newArrayList();
		if(originalSkipReason.equals(Answer.SkipReason.NONE)) {
			selectedItems.addAll(originallySelectedOptions);
		} else if(originalSkipReason.equals(Answer.SkipReason.DONT_KNOW)) {
			selectedItems.add(dontKnow);
		} else if(originalSkipReason.equals(Answer.SkipReason.REFUSE)) {
			selectedItems.add(refuse);
		}
		answerField = new CheckboxesPanel<Object>("answer",allItems,selectedItems) 
		{
			protected String showItem(Object option) {
				return option instanceof QuestionOption ? 
						((QuestionOption) option).getName() : 
							option.toString();
			}
		};
		add(answerField);
	}

	public String getAnswer() {
		List<String> optionIdStrings = Lists.newArrayList();
		for(Object option : answerField.getSelected()) {
			if(option instanceof QuestionOption) {
				optionIdStrings.add(((QuestionOption) option).getId().toString());
			}
		}
		return Joiner.on(",").join(optionIdStrings);
	}
	
	public boolean dontKnow() {
		return answerField.getSelected().contains(dontKnow);
	}
	
	public boolean refused() {
		return answerField.getSelected().contains(refuse);
	}
	
	public List<QuestionOption> getOptions() {
		return Options.getOptionsForQuestion(getQuestion().getId());
	}
	
	public void setAutoFocus() {
		answerField.setAutoFocus();
	}
}
