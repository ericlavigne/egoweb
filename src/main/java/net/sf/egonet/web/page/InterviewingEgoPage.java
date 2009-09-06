package net.sf.egonet.web.page;

import org.apache.wicket.markup.html.form.Form;

import net.sf.egonet.model.Question;
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
				}
				setResponsePage(askNextUnanswered(interviewId));
			}
		};
		field = AnswerFormFieldPanel.getInstance("question",question);
		form.add(field);
		add(form);
	}
	
	public static EgonetPage askNextUnanswered(Long interviewId) {
		Question nextEgoQuestion = Interviewing.nextUnansweredEgoQuestionForInterview(interviewId);
		if(nextEgoQuestion != null) {
			return new InterviewingEgoPage(interviewId, nextEgoQuestion);
		}
		return new InterviewingAlterPromptPage(interviewId);
	}
}
