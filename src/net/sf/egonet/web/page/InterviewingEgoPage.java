package net.sf.egonet.web.page;

import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.form.Button;
import com.google.common.collect.Lists;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Answers;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;
import net.sf.egonet.web.panel.InterviewingPanel;

import static net.sf.egonet.web.page.InterviewingQuestionIntroPage.possiblyReplaceNextQuestionPageWithPreface;

public class InterviewingEgoPage extends InterviewingPage {
	
	private Long interviewId;
	private Question question;
	private InterviewingPanel interviewingPanel;
    private boolean gotoNextUnAnswered;
    
	public InterviewingEgoPage(Long interviewId, Question question) {
		super(interviewId);
		this.interviewId = interviewId;
		this.question = question;
		gotoNextUnAnswered = false;
		build();
		setQuestionId("Question: " + question.getTitle());
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
		
		AnswerFormFieldPanel field = AnswerFormFieldPanel.getInstance("question",question,interviewId);
		Answer answer = Answers.getAnswerForInterviewAndQuestion(interviewId, question);
		if(answer != null) {
			field = AnswerFormFieldPanel.getInstance("question",
					question,answer.getValue(),answer.getOtherSpecifyText(),answer.getSkipReason(),interviewId);
		}
		field.setAutoFocus();
		form.add(field);
		
		interviewingPanel = 
			new InterviewingPanel("interviewingPanel",question,Lists.newArrayList(field),interviewId);
		form.add(interviewingPanel);
		
		add(form);

		add(new Link("backwardLink") {
			public void onClick() {
				EgonetPage page = 
					askPrevious(interviewId,question,new InterviewingEgoPage(interviewId,question));
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
		Link forwardLink = new Link("forwardLink") {
			public void onClick() {
				EgonetPage page = 
					askNext(interviewId,question,new InterviewingEgoPage(interviewId,question));
				if(page != null) {
					setResponsePage(page);
				}
			}
		};
		add(forwardLink);
		if(! AnswerFormFieldPanel.okayToContinue(
				interviewingPanel.getAnswerFields(),
				interviewingPanel.pageFlags())) 
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
		for(AnswerFormFieldPanel field : interviewingPanel.getAnswerFields()) {
			if ( !multipleSelectionsOkay ) {
				field.setNotification(field.getRangeCheckNotification());
			} else if(okayToContinue) {
					Answers.setAnswerForInterviewAndQuestion(interviewId, question, 
							field.getAnswer(),field.getOtherText(),
							field.getSkipReason(pageFlags));
			} else if(consistent) {
				field.setNotification(
						field.answeredOrRefused(pageFlags) ?
								"" : "Unanswered");
			} else {
				field.setNotification(
						field.consistent(pageFlags) ?
								"" : field.inconsistencyReason(pageFlags));
			}
		}
		if(okayToContinue) {
			if ( gotoNextUnAnswered ) {
			setResponsePage(
					askNextUnanswered(interviewId,question,
							new InterviewingEgoPage(interviewId,question)));
			} else {
				EgonetPage page = 
					askNext(interviewId,question,new InterviewingEgoPage(interviewId,question));
				if(page != null) {
					setResponsePage(page);
				}
			}
		}
	}


	//                    forward, unansweredOnly
	// askNextUnanswered: true,    true
	//           askNext: true,    false
	//       askPrevious: false,   false
	
	public static EgonetPage askNextUnanswered(
			Long interviewId,Question currentQuestion, EgonetPage comeFrom) 
	{
		Question nextEgoQuestion = 
			Interviewing.nextEgoQuestionForInterview(interviewId,currentQuestion,true,true);
		if(nextEgoQuestion != null) {
			EgonetPage nextEgoPage = new InterviewingEgoPage(interviewId, nextEgoQuestion);
			return possiblyReplaceNextQuestionPageWithPreface(
					interviewId,nextEgoPage,currentQuestion,nextEgoQuestion,
					comeFrom,nextEgoPage);
		}
		return InterviewingAlterPromptPage.askNextUnanswered(interviewId,comeFrom);
	}
	public static EgonetPage askNext(Long interviewId,Question currentQuestion, EgonetPage comeFrom) 
	{
		Question nextEgoQuestion = 
			Interviewing.nextEgoQuestionForInterview(interviewId,currentQuestion,true,false);
		if(nextEgoQuestion != null) {
			EgonetPage nextPage = new InterviewingEgoPage(interviewId, nextEgoQuestion);
			return possiblyReplaceNextQuestionPageWithPreface(
					interviewId,nextPage,currentQuestion,nextEgoQuestion,
					comeFrom,nextPage);
		}
		Study study = Studies.getStudyForInterview(interviewId);
		Integer max = study.getMaxAlters();
		if(max != null && max > 0) {
			return new InterviewingAlterPromptPage(interviewId);
		}
		// KCN May 7 - using the >> button at the bottom of the screen
		// to advance through the ego questions to the first alter question
		// was causing the program to crash, this should fix that problem:
		return InterviewingAlterPage.askNext(interviewId,null,false,comeFrom);
		// return InterviewingAlterPage.askNextNEW(interviewId,null,false,comeFrom);
	}
	public static EgonetPage askPrevious(Long interviewId,Question currentQuestion, EgonetPage comeFrom) {
		Question previousEgoQuestion = 
			Interviewing.nextEgoQuestionForInterview(interviewId,currentQuestion,false,false);
		EgonetPage previousPage = previousEgoQuestion == null ? null :
			new InterviewingEgoPage(interviewId, previousEgoQuestion);
		return possiblyReplaceNextQuestionPageWithPreface(
				interviewId,previousPage,previousEgoQuestion,currentQuestion,
				previousPage,comeFrom);
	}
	
	public String toString() {
		return question.getType()+" : "+question.getTitle();
	}
}
