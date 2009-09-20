package net.sf.egonet.web.page;

import java.util.ArrayList;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.Answers;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;
import net.sf.functionalj.tuple.Pair;
import net.sf.functionalj.tuple.PairUni;

public class InterviewingAlterPairPage extends EgonetPage {
	
	private Long interviewId;
	private Question question;
	private ArrayList<PairUni<Alter>> alterpairs;

	public ArrayList<AnswerFormFieldPanel> answerFields;
	
	private ListView questionsView;
	
	public InterviewingAlterPairPage(Long interviewId, Question question, ArrayList<PairUni<Alter>> alterpairs) 
	{
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		this.interviewId = interviewId;
		this.question = question;
		this.alterpairs = alterpairs;
		build();
	}
	
	private void build() {
		answerFields = Lists.newArrayList();
		for(PairUni<Alter> alterPair : alterpairs) {
			ArrayList<Alter> alters = Lists.newArrayList(alterPair.getFirst(),alterPair.getSecond());
			Answer answer = Answers.getAnswerForInterviewQuestionAlters(Interviews.getInterview(interviewId), 
					question, alters);
			if(answer == null) {
				answerFields.add(AnswerFormFieldPanel.getInstance("question", question, alters));
			} else {
				answerFields.add(AnswerFormFieldPanel.getInstance("question", question, answer.getValue(), alters));
			}
		}
		
		Form form = new Form("form") {
			public void onSubmit() {
				for(AnswerFormFieldPanel answerField : answerFields) {
					String answerString = answerField.getAnswer();
					if(answerString != null) {
						Answers.setAnswerForInterviewQuestionAlters(
								interviewId, question, answerField.getAlters(), answerString);
					}
				}
				setResponsePage(askNextUnanswered(interviewId));
			}
		};
		questionsView = new ListView("questions",answerFields) {
			protected void populateItem(ListItem item) {
				item.add((AnswerFormFieldPanel) item.getModelObject());
			}
		};
		questionsView.setReuseItems(true);
		form.add(questionsView);
		add(form);
	}

	public static EgonetPage askNextUnanswered(Long interviewId) {
		Pair<Question,ArrayList<PairUni<Alter>>> 
		questionAndAlterPairs = Interviewing.nextUnansweredAlterPairQuestionForInterview(interviewId);
		if(questionAndAlterPairs != null) {
			return new InterviewingAlterPairPage(interviewId, 
					questionAndAlterPairs.getFirst(),questionAndAlterPairs.getSecond());
		}
		return new InterviewingConclusionPage(interviewId);
	}
}
