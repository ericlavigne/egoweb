package net.sf.egonet.web.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;

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

public class InterviewingAlterPage extends EgonetPage {
	
	public static class Subject implements Serializable, Comparable<Subject> {
		// eventually need a way for one of these to represent a question intro
		public Long interviewId;
		public Question question;
		public ArrayList<Alter> alters; // only one alter when not list style, never empty
		public TreeSet<Question> sectionQuestions;
		
		public Alter getAlter() {
			return question.getAskingStyleList() ? null : alters.get(0);
		}
		
		@Override
		public String toString() {
			return question.getType()+" : "+question.getTitle()+
			(getAlter() == null ? "" : " : "+getAlter().getName());
		}
		@Override
		public int hashCode() {
			return question.hashCode()+(question.getAskingStyleList() ? 0 : getAlter().hashCode());
		}
		@Override
		public boolean equals(Object object) {
			return object instanceof Subject && equals((Subject) object);
		}
		public boolean equals(Subject subject) {
			return interviewId.equals(subject.interviewId) &&
				question.equals(subject.question) &&
				(question.getAskingStyleList() || getAlter().equals(subject.getAlter()));
		}

		@Override
		public int compareTo(Subject subject) {
			if(getAlter() != null && subject.getAlter() != null && 
					(! getAlter().equals(subject.getAlter())) && 
					sectionQuestions.contains(subject.question)) 
			{
				return getAlter().compareTo(subject.getAlter());
			}
			return question.compareTo(subject.question);
		}
		
	}

	private Subject subject;

	public ArrayList<AnswerFormFieldPanel> answerFields;
	
	private ListView questionsView;

	public InterviewingAlterPage(Subject subject) {
		super(Studies.getStudyForInterview(subject.interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(subject.interviewId)
				+" (respondent #"+subject.interviewId+")");
		this.subject = subject;
		build();
	}
	
	private void build() {
		
		ArrayList<Alter> promptAlters = 
			new Integer(1).equals(subject.alters.size()) ?
					Lists.newArrayList(subject.alters.get(0)) : new ArrayList<Alter>();
		add(new MultiLineLabel("prompt", subject.question.individualizePrompt(promptAlters)));
		
		answerFields = Lists.newArrayList();
		for(Alter alter : subject.alters) {
			ArrayList<Alter> alters = Lists.newArrayList(alter);
			Answer answer = 
				Answers.getAnswerForInterviewQuestionAlters(Interviews.getInterview(subject.interviewId), 
					subject.question, alters);
			if(answer == null) {
				answerFields.add(AnswerFormFieldPanel.getInstance("question", subject.question, alters));
			} else {
				answerFields.add(AnswerFormFieldPanel.getInstance("question", 
						subject.question, answer.getValue(), alters));
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
								subject.interviewId, subject.question, answerField.getAlters(), answerString);
					}
				}
				setResponsePage(askNext(subject.interviewId,subject,true));
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
				EgonetPage page = askPrevious(subject.interviewId,subject);
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
		add(new Link("forwardLink") {
			public void onClick() {
				EgonetPage page = askNext(subject.interviewId,subject,false);
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
	}
	
	public static EgonetPage askNext(Long interviewId, Subject currentSubject, Boolean unansweredOnly) {
		Subject nextPage =
			Interviewing.nextAlterPageForInterview(
					interviewId, currentSubject, true, unansweredOnly);
		if(nextPage != null) {
			return new InterviewingAlterPage(nextPage);
		}
		return InterviewingAlterPairPage.askNext(interviewId,null,unansweredOnly);
	}
	public static EgonetPage askPrevious(Long interviewId, Subject currentSubject) {
		Subject previousSubject =
			Interviewing.nextAlterPageForInterview(
					interviewId, currentSubject, false, false);
		if(previousSubject != null) {
			return new InterviewingAlterPage(previousSubject);
		}
		Study study = Studies.getStudyForInterview(interviewId);
		Integer max = study.getMaxAlters();
		if(max != null && max > 0) {
			return new InterviewingAlterPromptPage(interviewId);
		}
		return InterviewingEgoPage.askPrevious(interviewId, null); 
	}
}
