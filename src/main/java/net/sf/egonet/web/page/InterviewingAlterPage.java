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
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Answers;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;
import net.sf.functionalj.tuple.Pair;

public class InterviewingAlterPage extends EgonetPage {
	
	private Long interviewId;
	private Question question;
	private ArrayList<Alter> alters;

	public ArrayList<AnswerFormFieldPanel> answerFields;
	
	private ListView questionsView;
	
	public InterviewingAlterPage(Long interviewId, Question question, ArrayList<Alter> alters) {
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		this.interviewId = interviewId;
		this.question = question;
		this.alters = alters;
		build();
	}
	
	private void build() {
		
		ArrayList<Alter> promptAlters = 
			new Integer(1).equals(alters.size()) ?
					Lists.newArrayList(alters.get(0)) : new ArrayList<Alter>();
		add(new MultiLineLabel("prompt", question.individualizePrompt(promptAlters)));
		
		answerFields = Lists.newArrayList();
		for(Alter alter : alters) {
			ArrayList<Alter> alters = Lists.newArrayList(alter);
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
		
		Form form = new Form("form") {
			public void onSubmit() {
				for(AnswerFormFieldPanel answerField : answerFields) {
					String answerString = answerField.getAnswer();
					if(answerString != null) {
						Answers.setAnswerForInterviewQuestionAlters(
								interviewId, question, answerField.getAlters(), answerString);
					}
				}
				setResponsePage(askNextUnanswered(interviewId,question,getLastAlter()));
			}
		};
		questionsView = new ListView("questions",answerFields) {
			protected void populateItem(ListItem item) {
				AnswerFormFieldPanel wrapper = (AnswerFormFieldPanel) item.getModelObject();
				item.add(wrapper);
				item.add(new Label("alter",wrapper.getAlters().get(0).getName()));
			}
		};
		questionsView.setReuseItems(true);
		form.add(questionsView);
		add(form);

		add(new Link("backwardLink") {
			public void onClick() {
				EgonetPage page = askPrevious(interviewId,question,getFirstAlter());
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
		add(new Link("forwardLink") {
			public void onClick() {
				EgonetPage page = askNext(interviewId,question,getLastAlter());
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
	}

	public Alter getFirstAlter() {
		return alters.get(0);
	}
	public Alter getLastAlter() {
		return alters.get(alters.size()-1);
	}
	
	private static final Integer altersPerPage = 20;
	
	public static EgonetPage askNextUnanswered(Long interviewId,Question currentQuestion, Alter currentAlter) {
		Pair<Question,ArrayList<Alter>> nextQuestionAndAlters =
			Interviewing.nextAlterQuestionForInterview(
					interviewId, currentQuestion, currentAlter, true, true, altersPerPage);
		if(nextQuestionAndAlters != null) {
			return new InterviewingAlterPage(interviewId, 
					nextQuestionAndAlters.getFirst(), 
					nextQuestionAndAlters.getSecond());
		}
		return InterviewingAlterPairPage.askNextUnanswered(interviewId,null,null);
	}
	public static EgonetPage askNext(Long interviewId, Question currentQuestion, Alter currentAlter) {
		Pair<Question,ArrayList<Alter>> nextQuestionAndAlters =
			Interviewing.nextAlterQuestionForInterview(
					interviewId, currentQuestion, currentAlter, true, false, altersPerPage);
		if(nextQuestionAndAlters != null) {
			return new InterviewingAlterPage(interviewId, 
					nextQuestionAndAlters.getFirst(), 
					nextQuestionAndAlters.getSecond());
		}
		return InterviewingAlterPairPage.askNext(interviewId,null,null);
	}
	public static EgonetPage askPrevious(Long interviewId, Question currentQuestion, Alter currentAlter) {
		Pair<Question,ArrayList<Alter>> previousQuestionAndAlters =
			Interviewing.nextAlterQuestionForInterview(
					interviewId, currentQuestion, currentAlter, false, false, altersPerPage);
		if(previousQuestionAndAlters != null) {
			return new InterviewingAlterPage(interviewId, 
					previousQuestionAndAlters.getFirst(), 
					previousQuestionAndAlters.getSecond());
		}
		Study study = Studies.getStudyForInterview(interviewId);
		Integer max = study.getMaxAlters();
		if(max != null && max > 0) {
			return new InterviewingAlterPromptPage(interviewId);
		}
		return InterviewingEgoPage.askPrevious(interviewId, null); 
	}
}
