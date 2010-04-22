package net.sf.egonet.web.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;

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
import net.sf.egonet.web.panel.InterviewingPanel;

import static net.sf.egonet.web.page.InterviewingQuestionIntroPage.possiblyReplaceNextQuestionPageWithPreface;

public class InterviewingAlterPage extends InterviewingPage {
	
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
	private InterviewingPanel interviewingPanel;

	public InterviewingAlterPage(Subject subject) {
		super(subject.interviewId);
		this.subject = subject;
		build();
	}
	
	private void build() {
		
		Form form = new Form("form") {
			public void onSubmit() {
				List<String> pageFlags = interviewingPanel.pageFlags();
				List<AnswerFormFieldPanel> answerFields = interviewingPanel.getAnswerFields();
				boolean okayToContinue = 
					AnswerFormFieldPanel.okayToContinue(answerFields, pageFlags);
				boolean consistent = 
					AnswerFormFieldPanel.allConsistent(answerFields, pageFlags);
				boolean multipleSelectionsOkay = 
					AnswerFormFieldPanel.allMultipleSelectionOkay(answerFields);
				for(AnswerFormFieldPanel answerField : answerFields) {
					if ( !multipleSelectionsOkay ) {
						answerField.setNotification(answerField.getMultipleSelectionNotification());
					} else if(okayToContinue) {
						Answers.setAnswerForInterviewQuestionAlters(
								subject.interviewId, subject.question, answerField.getAlters(), 
								answerField.getAnswer(), answerField.getSkipReason(pageFlags));
					} else if(consistent) {
						answerField.setNotification(
								answerField.answeredOrRefused(pageFlags) ?
										"" : "Unanswered");
					} else {
						answerField.setNotification(
								answerField.consistent(pageFlags) ?
										"" : answerField.inconsistencyReason(pageFlags));
					}
				}
				if(okayToContinue) {
					setResponsePage(
							askNext(subject.interviewId,subject,true, 
									new InterviewingAlterPage(subject)));
				}
			}
		};

		ArrayList<AnswerFormFieldPanel> answerFields = Lists.newArrayList();
		for(Alter alter : subject.alters) {
			ArrayList<Alter> alters = Lists.newArrayList(alter);
			Answer answer = 
				Answers.getAnswerForInterviewQuestionAlters(Interviews.getInterview(subject.interviewId), 
					subject.question, alters);
			if(answer == null) {
				answerFields.add(AnswerFormFieldPanel.getInstance("question", subject.question, alters, subject.interviewId));
			} else {
				answerFields.add(AnswerFormFieldPanel.getInstance("question", 
						subject.question, answer.getValue(), answer.getSkipReason(), alters, subject.interviewId));
			}
			if(! answerFields.isEmpty()) {
				answerFields.get(0).setAutoFocus();
			}
		}
		
		interviewingPanel = 
			new InterviewingPanel("interviewingPanel",subject.question,answerFields,subject.interviewId);
		form.add(interviewingPanel);
		
		add(form);
		
		add(new Link("backwardLink") {
			public void onClick() {
				EgonetPage page = 
					askPrevious(subject.interviewId,subject, new InterviewingAlterPage(subject));
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
		Link forwardLink = new Link("forwardLink") {
			public void onClick() {
				EgonetPage page = 
					askNext(subject.interviewId,subject,false, new InterviewingAlterPage(subject));
				if(page != null) {
					setResponsePage(page);
				}
			}
		};
		add(forwardLink);
		if(! AnswerFormFieldPanel.okayToContinue(
				interviewingPanel.getAnswerFields(),interviewingPanel.pageFlags())) 
		{
			forwardLink.setVisible(false);
		}
	}

	public String toString() {
		return subject.toString();
	}
	
	public static EgonetPage askNext(Long interviewId, Subject currentSubject, 
			Boolean unansweredOnly, EgonetPage comeFrom) 
	{
		Subject nextSubject =
			Interviewing.nextAlterPageForInterview(
					interviewId, currentSubject, true, unansweredOnly);
		if(nextSubject != null) {
			EgonetPage nextPage = new InterviewingAlterPage(nextSubject);
			return possiblyReplaceNextQuestionPageWithPreface(
					interviewId,nextPage,
					currentSubject == null ? null : currentSubject.question,
					nextSubject.question,
					comeFrom,nextPage);
		}
		return InterviewingAlterPairPage.askNext(interviewId,null,unansweredOnly,comeFrom);
	}
	public static EgonetPage askPrevious(Long interviewId, Subject currentSubject, EgonetPage comeFrom) 
	{
		Subject previousSubject =
			Interviewing.nextAlterPageForInterview(
					interviewId, currentSubject, false, false);
		EgonetPage previousPage;
		if(previousSubject != null) {
			previousPage = new InterviewingAlterPage(previousSubject);
		} else {
			Study study = Studies.getStudyForInterview(interviewId);
			Integer max = study.getMaxAlters();
			if(max != null && max > 0) {
				previousPage = new InterviewingAlterPromptPage(interviewId);
			} else {
				previousPage = InterviewingEgoPage.askPrevious(interviewId, null, comeFrom); 
			}
		}
		return possiblyReplaceNextQuestionPageWithPreface(
				interviewId,previousPage,
				previousSubject == null ? null : previousSubject.question, 
				currentSubject == null ? null : currentSubject.question,
				previousPage,comeFrom);
	}
}
