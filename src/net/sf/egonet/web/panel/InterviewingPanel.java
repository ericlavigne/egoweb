package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
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
	private Long interviewId;
	
	private final String 
		dontKnow = AnswerFormFieldPanel.dontKnow,
		refuse = AnswerFormFieldPanel.refuse,
		none = AnswerFormFieldPanel.none;
	
	public InterviewingPanel(String id, 
			Question question, ArrayList<AnswerFormFieldPanel> answerFields, Long interviewId) 
	{
		super(id);
		this.question = question;
		this.answerFields = answerFields;
		this.interviewId = interviewId;
		build();
	}

	private void build() {
		String strPrompt;
		
		ArrayList<Alter> altersInPrompt = Lists.newArrayList();
		if(answerFields.size() < 2 && ! answerFields.isEmpty()) {
			altersInPrompt = answerFields.get(0).getAlters();
		}
		if(answerFields.size() > 1 && answerFields.get(0).getAlters().size() > 1) {
			altersInPrompt = Lists.newArrayList(answerFields.get(0).getAlters().get(0));
		}
		
		strPrompt = question.individualizePrompt(altersInPrompt);
		strPrompt = question.answerCountInsertion(strPrompt, interviewId);
		strPrompt = question.calculationInsertion(strPrompt, interviewId, altersInPrompt);
		strPrompt = question.variableInsertion(strPrompt,interviewId, altersInPrompt);
		strPrompt = question.conditionalTextInsertion(strPrompt, interviewId, altersInPrompt);
		add(new MultiLineLabel("prompt", strPrompt));
		
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
	
	public static InterviewingPanel createExample(String id, Question question) {
		ArrayList<AnswerFormFieldPanel> panels = Lists.newArrayList();
		Interview interview = new Interview();
		Alter oneAlter = new Alter(interview,"Jacob");
		List<String> names = 
			Lists.newArrayList("Emma","Michael","Ethan","Isabella","Joshua");
		for(int i = 0; i < (question.getAskingStyleList() ? 5 : 1); i++) {
			ArrayList<Alter> alters = Lists.newArrayList();
			Alter otherAlter = new Alter(interview,names.get(i));
			if(question.getType().equals(Question.QuestionType.ALTER)) {
				alters.add(otherAlter);
			}
			if(question.getType().equals(Question.QuestionType.ALTER_PAIR)) {
				alters.add(oneAlter);
				alters.add(otherAlter);
			}
			panels.add(AnswerFormFieldPanel.getInstance("question", question, alters, interview.getId()));
		}
		return new InterviewingPanel(id,question,panels, interview.getId());
	}
}
