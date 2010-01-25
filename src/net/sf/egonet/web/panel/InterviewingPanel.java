package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.collect.Lists;

public class InterviewingPanel extends Panel {

	private Question question;
	private ArrayList<AnswerFormFieldPanel> answerFields;
	private CheckboxesPanel<String> refDKCheck;
	
	private final String 
		dontKnow = AnswerFormFieldPanel.dontKnow,
		refuse = AnswerFormFieldPanel.refuse,
		none = AnswerFormFieldPanel.none;
	
	public InterviewingPanel(String id, 
			Question question, ArrayList<AnswerFormFieldPanel> answerFields) 
	{
		super(id);
		this.question = question;
		this.answerFields = answerFields;
		build();
	}

	private void build() {

		ArrayList<Alter> altersInPrompt = Lists.newArrayList();
		if(answerFields.size() < 2 && ! answerFields.isEmpty()) {
			altersInPrompt = answerFields.get(0).getAlters();
		}
		if(answerFields.size() > 1 && answerFields.get(0).getAlters().size() > 1) {
			altersInPrompt = Lists.newArrayList(answerFields.get(0).getAlters().get(0));
		}
		add(new MultiLineLabel("prompt", question.individualizePrompt(altersInPrompt)));
		
		ListView questionsView = new ListView("questions",answerFields) {
			protected void populateItem(ListItem item) {
				AnswerFormFieldPanel wrapper = (AnswerFormFieldPanel) item.getModelObject();
				item.add(wrapper);
				ArrayList<Alter> alters = wrapper.getAlters();
				Alter lastAlter = alters.isEmpty() ? null : alters.get(alters.size()-1);
				item.add(new Label("alter",
						answerFields.size() < 2 || lastAlter == null ? 
								"" : lastAlter.getName()));
			}
		};
		questionsView.setReuseItems(true);
		add(questionsView);
		
		ArrayList<String> allOptions = Lists.newArrayList();
		ArrayList<String> selectedOptions = Lists.newArrayList(); // TODO: populate this
		if(question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION)) {
			allOptions.add(none);
		}
		if(answerFields.size() > 1) {
			allOptions.addAll(Lists.newArrayList(dontKnow,refuse));
		}
		refDKCheck = new CheckboxesPanel<String>("refDKCheck",allOptions,selectedOptions);
		add(refDKCheck);
	}
	
	public List<String> pageFlags() {
		return refDKCheck.getSelected();
	}
	
	public ArrayList<AnswerFormFieldPanel> getAnswerFields() {
		return this.answerFields;
	}
}
