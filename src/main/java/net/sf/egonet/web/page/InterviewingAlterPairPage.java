package net.sf.egonet.web.page;

import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
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
import net.sf.functionalj.tuple.PairUni;
import net.sf.functionalj.tuple.Triple;

public class InterviewingAlterPairPage extends EgonetPage {
	
	private Long interviewId;
	private Question question;
	private Alter firstAlter;
	private ArrayList<Alter> secondAlters;

	public ArrayList<AnswerFormFieldPanel> answerFields;
	
	private ListView questionsView;
	
	public InterviewingAlterPairPage(Long interviewId, Question question, Alter firstAlter, ArrayList<Alter> secondAlters) 
	{
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		this.interviewId = interviewId;
		this.question = question;
		this.firstAlter = firstAlter;
		this.secondAlters = secondAlters;
		build();
	}
	
	private void build() {
		answerFields = Lists.newArrayList();
		for(Alter secondAlter : secondAlters) {
			ArrayList<Alter> alters = Lists.newArrayList(firstAlter,secondAlter);
			Answer answer = Answers.getAnswerForInterviewQuestionAlters(Interviews.getInterview(interviewId), 
					question, alters);
			if(answer == null) {
				answerFields.add(AnswerFormFieldPanel.getInstance("question", question, alters));
			} else {
				answerFields.add(AnswerFormFieldPanel.getInstance("question", question, answer.getValue(), alters));
			}
			if(! answerFields.isEmpty()) {
				answerFields.get(0).setAutoFocus();
			}
		}
		
		ArrayList<Alter> altersInPrompt = 
			secondAlters.size() < 2 ? 
					Lists.newArrayList(firstAlter,secondAlters.get(0)) : Lists.newArrayList(firstAlter);
		add(new MultiLineLabel("prompt", question.individualizePrompt(altersInPrompt)));
		
		Form form = new Form("form") {
			public void onSubmit() {
				for(AnswerFormFieldPanel answerField : answerFields) {
					String answerString = answerField.getAnswer();
					if(answerString != null) {
						Answers.setAnswerForInterviewQuestionAlters(
								interviewId, question, answerField.getAlters(), answerString);
					}
				}
				setResponsePage(askNextUnanswered(interviewId,question,getLastAlterPair()));
			}
		};
		questionsView = new ListView("questions",answerFields) {
			protected void populateItem(ListItem item) {
				AnswerFormFieldPanel wrapper = (AnswerFormFieldPanel) item.getModelObject();
				item.add(wrapper);
				item.add(new Label("alter",
						secondAlters.size() < 2 ? 
								"" : wrapper.getAlters().get(1).getName()));
			}
		};
		questionsView.setReuseItems(true);
		form.add(questionsView);
		add(form);
		
		add(new Link("backwardLink") {
			public void onClick() {
				EgonetPage page = askPrevious(interviewId,question,getFirstAlterPair());
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
		add(new Link("forwardLink") {
			public void onClick() {
				EgonetPage page = askNext(interviewId,question,getLastAlterPair());
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
	}

	public PairUni<Alter> getFirstAlterPair() {
		return new PairUni<Alter>(firstAlter,secondAlters.get(0));
	}
	public PairUni<Alter> getLastAlterPair() {
		return new PairUni<Alter>(firstAlter,secondAlters.get(secondAlters.size()-1));
	}
	
	private static final Integer alterPairsPerPage = 20;
	
	public static EgonetPage askNextUnanswered(Long interviewId,Question currentQuestion, PairUni<Alter> currentAlterPair) {
		Triple<Question,Alter,ArrayList<Alter>> nextQuestionAndAlterPairs =
			Interviewing.nextAlterPairQuestionForInterview(
					interviewId, currentQuestion, currentAlterPair, true, true, alterPairsPerPage);
		if(nextQuestionAndAlterPairs != null) {
			return new InterviewingAlterPairPage(interviewId, 
					nextQuestionAndAlterPairs.getFirst(), 
					nextQuestionAndAlterPairs.getSecond(), 
					nextQuestionAndAlterPairs.getThird());
		}
		return new InterviewingConclusionPage(interviewId);
	}
	public static EgonetPage askNext(Long interviewId, Question currentQuestion, PairUni<Alter> currentAlterPair) {
		Triple<Question,Alter,ArrayList<Alter>> nextQuestionAndAlterPairs =
			Interviewing.nextAlterPairQuestionForInterview(
					interviewId, currentQuestion, currentAlterPair, true, false, alterPairsPerPage);
		if(nextQuestionAndAlterPairs != null) {
			return new InterviewingAlterPairPage(interviewId, 
					nextQuestionAndAlterPairs.getFirst(), 
					nextQuestionAndAlterPairs.getSecond(), 
					nextQuestionAndAlterPairs.getThird());
		}
		return new InterviewingConclusionPage(interviewId);
	}
	public static EgonetPage askPrevious(Long interviewId, Question currentQuestion, PairUni<Alter> currentAlterPair) {
		Triple<Question,Alter,ArrayList<Alter>> previousQuestionAndAlterPairs =
			Interviewing.nextAlterPairQuestionForInterview(
					interviewId, currentQuestion, currentAlterPair, false, false, alterPairsPerPage);
		if(previousQuestionAndAlterPairs != null) {
			return new InterviewingAlterPairPage(interviewId, 
					previousQuestionAndAlterPairs.getFirst(), 
					previousQuestionAndAlterPairs.getSecond(), 
					previousQuestionAndAlterPairs.getThird());
		}
		return InterviewingAlterPage.askPrevious(interviewId, null, null);
	}
}
