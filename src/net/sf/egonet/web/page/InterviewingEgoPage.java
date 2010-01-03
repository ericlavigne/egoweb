package net.sf.egonet.web.page;

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Answers;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;

import static net.sf.egonet.web.page.InterviewingQuestionIntroPage.possiblyReplaceNextQuestionPageWithPreface;

public class InterviewingEgoPage extends InterviewingPage {
	
	private Long interviewId;
	private Question question;
	private AnswerFormFieldPanel field;
	
	public InterviewingEgoPage(Long interviewId, Question question) {
		super(interviewId);
		this.interviewId = interviewId;
		this.question = question;
		build();
	}

	private void build() {
		
		add(new MultiLineLabel("prompt", question.getPrompt()));
		
		Form form = new Form("form") {
			public void onSubmit() {
				String answerString = field.getAnswer();
				if(answerString != null) {
					Answers.setAnswerForInterviewAndQuestion(interviewId, question, answerString);
					setResponsePage(
							askNextUnanswered(interviewId,question,
									new InterviewingEgoPage(interviewId,question)));
				}
			}
		};
		
		Answer answer = Answers.getAnswerForInterviewAndQuestion(interviewId, question);
		if(answer == null) {
			field = AnswerFormFieldPanel.getInstance("question",question);
		} else {
			field = AnswerFormFieldPanel.getInstance("question",question,answer.getValue());
		}
		field.setAutoFocus();
		form.add(field);
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
		add(new Link("forwardLink") {
			public void onClick() {
				EgonetPage page = 
					askNext(interviewId,question,new InterviewingEgoPage(interviewId,question));
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
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
		return InterviewingAlterPage.askNext(interviewId,null,null,comeFrom);
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
