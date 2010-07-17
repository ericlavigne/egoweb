package net.sf.egonet.web.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Iterator;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.basic.Label;
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
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;
import net.sf.egonet.web.panel.InterviewingPanel;

import static net.sf.egonet.web.page.InterviewingQuestionIntroPage.possiblyReplaceNextQuestionPageWithPreface;

public class InterviewingAlterPage extends InterviewingPage {
	
	public static class Subject implements Serializable, Comparable<Subject> {
		// eventually need a way for one of these to represent a question intro
		public Long interviewId;
		public Question question;
		public ArrayList<Question> questionList; // used for multiple questions per page
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
    private Label pageLevelPrompt;
    private boolean gotoNextUnAnswered;
    private boolean multipleQuestionsPerPage = false;
    
	public InterviewingAlterPage(Subject subject) {
		super(subject.interviewId);
		this.subject = subject;
		gotoNextUnAnswered = multipleQuestionsPerPage = false;
		build();
		setQuestionId("Question: " + subject.question.getTitle());
	}
	
	/**
	 * had to add the nextQuestion button explicitly instead of simply creating
	 * a form and having the submit button created implicitly because we need an
	 * object to add FocusOnLoadBehavior to.
	 * There are three presentation options:
	 * 1. One Question and One Alter
	 * 2. One Question and a list of Alters
	 * 3. A list of questions and One Alter 
	 */
	
	private void build() {
		Button nextQuestion;
		Button nextUnanswered;
		
		Form form = new Form("form"); // {
	//		public void onSubmit() {
	//			onSave(gotoNextUnAnswered);
	//		}
	//	};
		
		nextQuestion = new Button("nextQuestion") {
			public void onSubmit() {
				onSave(gotoNextUnAnswered);
			}
		};
		nextUnanswered = new Button("nextUnanswered") {
			public void onSubmit() {
				gotoNextUnAnswered = true;
			}
		};
		form.add(nextQuestion);
		form.add(nextUnanswered);
		nextQuestion.add(new FocusOnLoadBehavior()); // whole reason for nextQuestion button
		
		pageLevelPrompt = new Label("pageLevelPrompt","");
		form.add(pageLevelPrompt);
		setPageLevelPrompt ( subject.question.getListRangePrompt());
		
		ArrayList<AnswerFormFieldPanel> answerFields = Lists.newArrayList();
		multipleQuestionsPerPage = getQuestionsForOnePage ( subject.question, subject );
	     
		if ( subject.alters.size()==1 && multipleQuestionsPerPage ) {
		     ArrayList<Alter> alters = Lists.newArrayList(subject.alters.get(0));
		     for ( Question question : subject.questionList ) {
		         Answer answer = 
				    Answers.getAnswerForInterviewQuestionAlters(Interviews.getInterview(subject.interviewId), 
						    question, alters);
				if(answer == null) {
				    answerFields.add(AnswerFormFieldPanel.getInstance("question", question, alters, subject.interviewId));
				} else {
				    answerFields.add(AnswerFormFieldPanel.getInstance("question", 
						    question, answer.getValue(), answer.getOtherSpecifyText(), answer.getSkipReason(), alters, subject.interviewId));
				}		    	 
		    }
		} else {
		for(Alter alter : subject.alters) {
			ArrayList<Alter> alters = Lists.newArrayList(alter);
			Answer answer = 
				Answers.getAnswerForInterviewQuestionAlters(Interviews.getInterview(subject.interviewId), 
					subject.question, alters);
			if(answer == null) {
				answerFields.add(AnswerFormFieldPanel.getInstance("question", subject.question, alters, subject.interviewId));
			} else {
				answerFields.add(AnswerFormFieldPanel.getInstance("question", 
						subject.question, answer.getValue(), answer.getOtherSpecifyText(), answer.getSkipReason(), alters, subject.interviewId));
			}
		    }
			if(! answerFields.isEmpty()) {
				answerFields.get(0).setAutoFocus();
			}
		}
		
		if ( multipleQuestionsPerPage ) {
			interviewingPanel = new InterviewingPanel("interviewingPanel",answerFields,subject.interviewId);		
		} else {
		interviewingPanel = 
			new InterviewingPanel("interviewingPanel",subject.question,answerFields,subject.interviewId);
		}
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
					askNext(subject.interviewId, subject, false, new InterviewingAlterPage(subject));
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
	List<AnswerFormFieldPanel> answerFields = interviewingPanel.getAnswerFields();
	boolean okayToContinue = 
		AnswerFormFieldPanel.okayToContinue(answerFields, pageFlags);
	boolean consistent = 
		AnswerFormFieldPanel.allConsistent(answerFields, pageFlags);
	boolean multipleSelectionsOkay = 
		AnswerFormFieldPanel.allRangeChecksOkay(answerFields);
	boolean countOfListItemOkay = (subject.question==null) ? true :
		AnswerFormFieldPanel.checkCountOfListItem(subject.question, answerFields);
	if ( countOfListItemOkay ) {
		setPageLevelPrompt(" ");
	} else {
		okayToContinue = false;
		setPageLevelPrompt(AnswerFormFieldPanel.getStatusCountOfListItem(subject.question, answerFields));
	}
	for(AnswerFormFieldPanel answerField : answerFields) {
		if ( !multipleSelectionsOkay ) {
			answerField.setNotification(answerField.getRangeCheckNotification());
		} else if(okayToContinue) {
			Answers.setAnswerForInterviewQuestionAlters(
					subject.interviewId, /*subject.question,*/answerField.getQuestion(), answerField.getAlters(), 
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
		EgonetPage page = 
			askNext(subject.interviewId, subject, gotoNextUnAnswered, new InterviewingAlterPage(subject));
		if(page != null) {
			setResponsePage(page);
		}
	}
}
	
	public String toString() {
		return subject.toString();
	}
	
	/**
	 * advances to the next alter question or alterPair question if we're
	 * on the last meaningful question.  This version calls nextAlterPageForInterviewNEW
	 * which should largely eliminate the delay between questions
	 * @param interviewId this interview
	 * @param currentSubject
	 * @param unansweredOnly - if true, we only want unanswered questions (duh)
	 * @param comeFrom
	 * @return
	 */
//	public static EgonetPage askNextNEW(Long interviewId, Subject currentSubject, 
//			Boolean unansweredOnly, EgonetPage comeFrom) 
//	{
//		Subject nextSubject =
//			Interviewing.nextAlterPageForInterviewNEW(
//					interviewId, currentSubject, true, unansweredOnly);
//		if(nextSubject != null) {
//			EgonetPage nextPage = new InterviewingAlterPage(nextSubject);
//			return possiblyReplaceNextQuestionPageWithPreface(
//					interviewId,nextPage,
//					currentSubject == null ? null : currentSubject.question,
//					nextSubject.question,
//					comeFrom,nextPage);
//		}
//		return InterviewingAlterPairPage.askNext(interviewId,null,unansweredOnly,comeFrom);
//	}
	
	public static EgonetPage askNext(Long interviewId, Subject currentSubject, 
			Boolean unansweredOnly, EgonetPage comeFrom) 
	{
		// to move forward we need to start searching on the last question
		if ( currentSubject!=null && currentSubject.questionList != null )
		    currentSubject.question = currentSubject.lastQuestionInList();
		
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
	
	/**
	 * new version of askPrevious which should greatly reduce the delays between questions
	 * @param interviewId
	 * @param currentSubject
	 * @param comeFrom
	 * @return
	 */
//	public static EgonetPage askPreviousNEW(Long interviewId, Subject currentSubject, EgonetPage comeFrom) 
//	{
//		Subject previousSubject =
//			Interviewing.nextAlterPageForInterviewNEW(
//					interviewId, currentSubject, false, false);
//		EgonetPage previousPage;
//		if(previousSubject != null) {
//			previousPage = new InterviewingAlterPage(previousSubject);
//		} else {
//			Study study = Studies.getStudyForInterview(interviewId);
//			Integer max = study.getMaxAlters();
//			if(max != null && max > 0) {
//				previousPage = new InterviewingAlterPromptPage(interviewId);
//			} else {
//				previousPage = InterviewingEgoPage.askPrevious(interviewId, null, comeFrom); 
//			}
//		}
//		return possiblyReplaceNextQuestionPageWithPreface(
//				interviewId,previousPage,
//				previousSubject == null ? null : previousSubject.question, 
//				currentSubject == null ? null : currentSubject.question,
//				previousPage,comeFrom);
//	}
	
	
	public static EgonetPage askPrevious(Long interviewId, Subject currentSubject, EgonetPage comeFrom) 
	{
		// to move backward we need to start searching on the first question
		if ( currentSubject!=null && currentSubject.questionList != null )
		    currentSubject.question = currentSubject.firstQuestionInList();
		
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
	
	/**
	 * this is used in list-of-alters pages, where one prompt
	 * will apply to the screen as a whole, not individual answers.
	 * Examples are "Select Yes once" or "Yes selected too many times"
	 * @param text - string to display
	 */
	public void setPageLevelPrompt(String text) {
		pageLevelPrompt.setModelObject(text);
	}
}
