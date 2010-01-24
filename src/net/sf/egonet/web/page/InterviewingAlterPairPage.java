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
import net.sf.egonet.persistence.Answers;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;
import net.sf.egonet.web.panel.CheckboxesPanel;

import static net.sf.egonet.web.page.InterviewingQuestionIntroPage.possiblyReplaceNextQuestionPageWithPreface;

public class InterviewingAlterPairPage extends InterviewingPage {

	public static class Subject implements Serializable, Comparable<Subject> {
		// TODO: need a way for one of these to represent a question intro: firstAlter -> null
		public Long interviewId;
		public Question question;
		public Alter firstAlter;
		public ArrayList<Alter> secondAlters; // only one alter when not list style, never empty
		public TreeSet<Question> sectionQuestions;
		
		public Alter getSecondAlter() {
			return question.getAskingStyleList() ? null : secondAlters.get(0);
		}
		
		@Override
		public String toString() {
			return question.getType()+" : "+question.getTitle()+" : "+firstAlter.getName()+
			(getSecondAlter() == null ? "" : " : "+getSecondAlter().getName());
		}
		@Override
		public int hashCode() {
			return question.hashCode()+firstAlter.hashCode()+
				(question.getAskingStyleList() ? 0 : getSecondAlter().hashCode());
		}
		@Override
		public boolean equals(Object object) {
			return object instanceof Subject && equals((Subject) object);
		}
		public boolean equals(Subject subject) {
			return interviewId.equals(subject.interviewId) &&
				question.equals(subject.question) &&
				firstAlter.equals(subject.firstAlter) &&
				(question.getAskingStyleList() || getSecondAlter().equals(subject.getSecondAlter()));
		}

		@Override
		public int compareTo(Subject subject) {
			if(sectionQuestions.contains(subject.question)) {
				int firstAlterCompare = firstAlter.compareTo(subject.firstAlter);
				if(firstAlterCompare != 0) {
					return firstAlterCompare;
				}
				boolean list = question.getAskingStyleList() || subject.question.getAskingStyleList();
				int secondAlterCompare = list ? 0 : getSecondAlter().compareTo(subject.getSecondAlter());
				if(secondAlterCompare != 0) {
					return secondAlterCompare;
				}
			}
			return question.compareTo(subject.question);
		}
		
	}

	private Subject subject;

	public ArrayList<AnswerFormFieldPanel> answerFields;
	
	private ListView questionsView;
	
	private CheckboxesPanel<String> refDKCheck;
	
	public InterviewingAlterPairPage(Subject subject) 
	{
		super(subject.interviewId);
		this.subject = subject;
		build();
	}
	
	private void build() {
		answerFields = Lists.newArrayList();
		for(Alter secondAlter : subject.secondAlters) {
			ArrayList<Alter> alters = Lists.newArrayList(subject.firstAlter,secondAlter);
			Answer answer = 
				Answers.getAnswerForInterviewQuestionAlters(
						Interviews.getInterview(subject.interviewId), 
						subject.question, alters);
			if(answer == null) {
				answerFields.add(
						AnswerFormFieldPanel.getInstance("question", 
								subject.question, alters));
			} else {
				answerFields.add(
						AnswerFormFieldPanel.getInstance("question", 
								subject.question, answer.getValue(), answer.getSkipReason(), alters));
			}
			if(! answerFields.isEmpty()) {
				answerFields.get(0).setAutoFocus();
			}
		}
		
		Boolean singleAlterPair = subject.secondAlters.size() < 2;
		
		ArrayList<Alter> altersInPrompt = 
			singleAlterPair ? Lists.newArrayList(subject.firstAlter,subject.secondAlters.get(0)) : 
				Lists.newArrayList(subject.firstAlter);
		add(new MultiLineLabel("prompt", subject.question.individualizePrompt(altersInPrompt)));
		
		Form form = new Form("form") {
			public void onSubmit() {
				for(AnswerFormFieldPanel answerField : answerFields) {
					String answerString = answerField.getAnswer();
					if(answerString != null) {
						Answers.setAnswerForInterviewQuestionAlters(
								subject.interviewId, subject.question, answerField.getAlters(), answerString);
					}
				}
				setResponsePage(
						askNext(subject.interviewId,subject,true,new InterviewingAlterPairPage(subject)));
			}
		};
		questionsView = new ListView("questions",answerFields) {
			protected void populateItem(ListItem item) {
				AnswerFormFieldPanel wrapper = (AnswerFormFieldPanel) item.getModelObject();
				item.add(wrapper);
				item.add(new Label("alter",
						subject.secondAlters.size() < 2 ? 
								"" : wrapper.getAlters().get(1).getName()));
			}
		};
		questionsView.setReuseItems(true);
		form.add(questionsView);
		
		ArrayList<String> allOptions = Lists.newArrayList();
		ArrayList<String> selectedOptions = Lists.newArrayList(); // TODO: populate this
		if(subject.question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION)) {
			allOptions.add(none);
		}
		if(! singleAlterPair) {
			allOptions.addAll(Lists.newArrayList(dontKnow,refuse));
		}
		refDKCheck = new CheckboxesPanel<String>("refDKCheck",allOptions,selectedOptions);
		form.add(refDKCheck);
		
		add(form);
		
		add(new Link("backwardLink") {
			public void onClick() {
				EgonetPage page = 
					askPrevious(subject.interviewId,subject,new InterviewingAlterPairPage(subject));
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
		Link forwardLink = new Link("forwardLink") {
			public void onClick() {
				EgonetPage page = 
					askNext(subject.interviewId,subject,false,new InterviewingAlterPairPage(subject));
				if(page != null) {
					setResponsePage(page);
				}
			}
		};
		add(forwardLink);
		if(! AnswerFormFieldPanel.okayToContinue(answerFields,refDKCheck.getSelected())) {
			forwardLink.setVisible(false);
		}
	}

	public String toString() {
		return subject.toString();
	}
	
	public static EgonetPage askNext(
			Long interviewId, Subject currentPage, boolean unansweredOnly, EgonetPage comeFrom) 
	{
		Subject nextSubject = 
			Interviewing.nextAlterPairPageForInterview(interviewId, currentPage, true, unansweredOnly);
		if(nextSubject != null) {
			EgonetPage nextPage = new InterviewingAlterPairPage(nextSubject);
			return possiblyReplaceNextQuestionPageWithPreface(
					interviewId,nextPage,
					currentPage == null ? null : currentPage.question, 
					nextSubject.question,
					comeFrom,nextPage);
		}
		return new InterviewingConclusionPage(interviewId);
	}
	public static EgonetPage askPrevious(Long interviewId, Subject currentSubject, EgonetPage comeFrom) {
		Subject previousSubject =
			Interviewing.nextAlterPairPageForInterview(interviewId, currentSubject, false, false);
		EgonetPage previousPage = 
			previousSubject != null ? 
					new InterviewingAlterPairPage(previousSubject) : 
						InterviewingAlterPage.askPrevious(interviewId, null, comeFrom);
		return possiblyReplaceNextQuestionPageWithPreface(
				interviewId,previousPage,
				previousSubject == null ? null : previousSubject.question,
				currentSubject == null ? null : currentSubject.question,
				previousPage,comeFrom);
	}
}
