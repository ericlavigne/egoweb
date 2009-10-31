package net.sf.egonet.web.page;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Answers;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;

public class InterviewingEgoPage extends EgonetPage {
	
	private Long interviewId;
	private Question question;
	private AnswerFormFieldPanel field;
	
	public InterviewingEgoPage(Long interviewId, Question question) {
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		this.interviewId = interviewId;
		this.question = question;
		build();
	}

	private void build() {
		Form form = new Form("form") {
			public void onSubmit() {
				String answerString = field.getAnswer();
				if(answerString != null) {
					Answers.setAnswerForInterviewAndQuestion(interviewId, question, answerString);
					setResponsePage(askNextUnanswered(interviewId,question));
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
				EgonetPage page = askPrevious(interviewId,question);
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
		add(new Link("forwardLink") {
			public void onClick() {
				EgonetPage page = askNext(interviewId,question);
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
	}

	public static EgonetPage askNextUnanswered(Long interviewId,Question currentQuestion) {
		Question nextEgoQuestion = 
			Interviewing.nextEgoQuestionForInterview(interviewId,currentQuestion,true,true);
		if(nextEgoQuestion != null) {
			return new InterviewingEgoPage(interviewId, nextEgoQuestion);
		}
		return InterviewingAlterPromptPage.askNextUnanswered(interviewId);
	}
	public static EgonetPage askNext(Long interviewId,Question currentQuestion) {
		Question nextEgoQuestion = 
			Interviewing.nextEgoQuestionForInterview(interviewId,currentQuestion,true,false);
		if(nextEgoQuestion != null) {
			return new InterviewingEgoPage(interviewId, nextEgoQuestion);
		}
		Study study = Studies.getStudyForInterview(interviewId);
		Integer max = study.getMaxAlters();
		if(max != null && max > 0) {
			return new InterviewingAlterPromptPage(interviewId);
		}
		return InterviewingAlterPage.askNext(interviewId,null,null);
	}
	public static EgonetPage askPrevious(Long interviewId,Question currentQuestion) {
		Question previousEgoQuestion = 
			Interviewing.nextEgoQuestionForInterview(interviewId,currentQuestion,false,false);
		if(previousEgoQuestion != null) {
			return new InterviewingEgoPage(interviewId, previousEgoQuestion);
		}
		return null; 
	}
}
