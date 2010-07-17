package net.sf.egonet.web.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Iterator;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.Answers;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;
import net.sf.egonet.web.panel.InterviewingPanel;

import static net.sf.egonet.web.page.InterviewingQuestionIntroPage.possiblyReplaceNextQuestionPageWithPreface;

public class InterviewingAlterPairPage extends InterviewingPage {

	public static class Subject implements Serializable, Comparable<Subject> {
		// TODO: need a way for one of these to represent a question intro: firstAlter -> null
		public Long interviewId;
		public Question question;
		public ArrayList<Question> questionList; // used for multiple questions per page
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
		
		public Question lastQuestionInList() {
			if ( questionList==null || questionList.isEmpty())
				return(null);
			return ( questionList.get(questionList.size()-1));
		}
		
		public Question firstQuestionInList() {
			if ( questionList==null || questionList.isEmpty())
				return(null);
			return ( questionList.get(0));
		}	
	}

	private Subject subject;
	private InterviewingPanel interviewingPanel;
    private boolean gotoNextUnAnswered;
    private boolean multipleQuestionsPerPage;
    
	public InterviewingAlterPairPage(Subject subject) 
	{
		super(subject.interviewId);
		this.subject = subject;
		gotoNextUnAnswered = multipleQuestionsPerPage = false;
		build();
		setQuestionId("Question: " + subject.question.getTitle());
	}
	
	private void build() {
		Button nextUnanswered;
		
		Form form = new Form("form") {
			public void onSubmit() {
				onSave(gotoNextUnAnswered);
			}
		};
		
		nextUnanswered = new Button("nextUnanswered") {
			public void onSubmit() {
				gotoNextUnAnswered = true;
			}
		};
		form.add(nextUnanswered);	
		
		ArrayList<AnswerFormFieldPanel> answerFields = Lists.newArrayList();
		multipleQuestionsPerPage = getQuestionsForOnePage ( subject.question, subject );
		
		if ( subject.secondAlters.size()==1 && multipleQuestionsPerPage ) {
		     // ArrayList<Alter> alters = Lists.newArrayList(subject.secondAlters.get(0));
		     ArrayList<Alter> alters = Lists.newArrayList(subject.firstAlter,subject.secondAlters.get(0));
		     for ( Question quest : subject.questionList ) {
		         Answer answer = 
				    Answers.getAnswerForInterviewQuestionAlters(Interviews.getInterview(subject.interviewId), 
						    quest, alters);
				if(answer == null) {
				    answerFields.add(AnswerFormFieldPanel.getInstance("question", quest, alters, subject.interviewId));
				} else {
				    answerFields.add(AnswerFormFieldPanel.getInstance("question", 
						    quest, answer.getValue(), answer.getOtherSpecifyText(), answer.getSkipReason(), alters, subject.interviewId));
				}		    	 
		    }
			interviewingPanel = new InterviewingPanel("interviewingPanel",answerFields,subject.interviewId);		     
		} else {
		for(Alter secondAlter : subject.secondAlters) {
			ArrayList<Alter> alters = Lists.newArrayList(subject.firstAlter,secondAlter);
			Answer answer = 
				Answers.getAnswerForInterviewQuestionAlters(
						Interviews.getInterview(subject.interviewId), 
						subject.question, alters);
			if(answer == null) {
				answerFields.add(
						AnswerFormFieldPanel.getInstance("question", 
								subject.question, alters, subject.interviewId));
			} else {
				answerFields.add(
						AnswerFormFieldPanel.getInstance("question", 
								subject.question, answer.getValue(), answer.getOtherSpecifyText(),
								answer.getSkipReason(), alters, subject.interviewId));
			}
			if(! answerFields.isEmpty()) {
				answerFields.get(0).setAutoFocus();
			}
		}
		 
		interviewingPanel = 
			new InterviewingPanel("interviewingPanel",subject.question,answerFields,subject.interviewId);
		}
		form.add(interviewingPanel);
		
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
		if(! AnswerFormFieldPanel.okayToContinue(answerFields,interviewingPanel.pageFlags())) {
			forwardLink.setVisible(false);
			nextUnanswered.setVisible(false);
		}
	}

	/**
	 * both the "Next Question" and "Next UnAnswered Question" buttons call this
	 * to save the current data and advance
	 * @param gotoNextUnAnswered if true proceed to next UNANSWERED question
	 * if false proceed to next questions
	 */
	public void onSave(boolean gotoNextUnAnswered) {
		List<String> pageFlags = interviewingPanel.pageFlags();
		ArrayList<AnswerFormFieldPanel> answerFields = interviewingPanel.getAnswerFields();
		boolean okayToContinue = 
			AnswerFormFieldPanel.okayToContinue(answerFields, pageFlags);
		boolean consistent = 
			AnswerFormFieldPanel.allConsistent(answerFields, pageFlags);
		boolean multipleSelectionsOkay = 
			AnswerFormFieldPanel.allRangeChecksOkay(answerFields);				
		for(AnswerFormFieldPanel answerField : answerFields) {
			if ( !multipleSelectionsOkay ) {
				answerField.setNotification(answerField.getRangeCheckNotification());
			} else if(okayToContinue) {
				Answers.setAnswerForInterviewQuestionAlters(
						subject.interviewId, /*subject.question*/ answerField.getQuestion(), answerField.getAlters(), 
						answerField.getAnswer(), answerField.getOtherText(),
						answerField.getSkipReason(pageFlags));
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
			if (gotoNextUnAnswered) {
				setResponsePage(
						askNext(subject.interviewId,subject,true,new InterviewingAlterPairPage(subject)));
			} else {
				EgonetPage page = 
					askNext(subject.interviewId,subject,false,new InterviewingAlterPairPage(subject));
				if(page != null) {
					setResponsePage(page);	
				}
			}
		}
	}

	public String toString() {
		return subject.toString();
	}
	
	public static EgonetPage askNext(
			Long interviewId, Subject currentPage, boolean unansweredOnly, EgonetPage comeFrom) 
	{
		// to move forward we need to start searching on the last question
		if ( currentPage!=null && currentPage.questionList != null )
			currentPage.question = currentPage.lastQuestionInList();
		
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
		return InterviewingNetworkPage.askNext(interviewId, null, comeFrom);
	}
	
	public static EgonetPage askPrevious(Long interviewId, Subject currentSubject, EgonetPage comeFrom) {
		
		// to move backward we need to start searching on the first question
		if ( currentSubject!=null && currentSubject.questionList != null )
		    currentSubject.question = currentSubject.firstQuestionInList();
		
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
	
	/**
	 * the subject has a treeSet sectionQuestions, which is the group of questions this
	 * question (firstQuestion) belongs to.  The end of the group is the last question within
	 * this treeset.  Some questions might have a 'keepOnSamePage' flag set, and we will want to 
	 * group all of those together into an array of answerPanels. 
	 * @param firstQuestion - just what is says, first question on this 'page'
	 * @param subject - an object with a question and an alter and the listOfQuestions
	 * @return true if we will need to display more than one question
	 */
	private boolean getQuestionsForOnePage ( Question firstQuestion, Subject subject ) {
		Iterator<Question> iter;
		Question question;
		boolean moreThanOne = false;
		boolean hit = false;
		boolean done = false;
		
		subject.questionList = new ArrayList<Question>();
		// If we don't allow multipe questions per page, 
		// just return a list with one Question and a false value
		if ( !Question.ALLOW_MULTIPLE_QUESTIONS_PER_PAGE ) {
			subject.questionList.add(firstQuestion);
			return(false);
		}
		iter = subject.sectionQuestions.iterator();
		while ( iter.hasNext() && !done ) {
			question = iter.next();
			if ( question.equals(firstQuestion)) {
				subject.questionList.add(question);
				hit = true;
			} else if (hit) {
				if ( question.getKeepOnSamePage()) {
					subject.questionList.add(question);
					moreThanOne = true;
				} else {
					done = true;
				}
			}	
		}
		// in a similar manner, if we are going backwards and are on
		// a question that needs to stay on the same page as the previous one, 
		// we will need to go back 
		if ( firstQuestion.getKeepOnSamePage()) {
			iter = subject.sectionQuestions.descendingIterator();
			hit = done = false;
			while ( iter.hasNext() && !done ) {
				question = iter.next();
				if ( question.equals(firstQuestion)) {
					hit = true;
				} else if (hit) {
					if ( question.getKeepOnSamePage()) {
						subject.questionList.add(0,question);
						moreThanOne = true;
					} else {
						subject.questionList.add(0,question);
						moreThanOne = true;
						done = true;
					}
				}	
			}
		}
		
		// System.out.println ( "Exitting InterviewingAlterPage.getQuestionsForOnePage");
		// for ( Question q : subject.questionList) {
		// 	System.out.println ( "Contains " + q.getTitle());
		// }
		return(moreThanOne);
	}

}
