package net.sf.egonet.web.page;

import java.util.ArrayList;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.Alters;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;

public class InterviewingAlterPage extends EgonetPage {
	
	private Long interviewId;
	private Question question;

	public ArrayList<AnswerFormFieldPanel> questions;
	
	private ListView questionsView;
	
	public InterviewingAlterPage(Long interviewId, Question question) {
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		this.interviewId = interviewId;
		this.question = question;
		build();
	}
	
	private void build() {
		questions = Lists.newArrayList();
		for(Alter alter : Alters.getForInterview(interviewId)) {
			// TODO: check if question already answered for this alter
			// TODO: answer form field panel needs the alter info, too
			questions.add(AnswerFormFieldPanel.getInstance("question", question));
		}
		
		Form form = new Form("form") {
			public void onSubmit() {
				// TODO: submit the answers
			}
		};
		questionsView = new ListView("questions",questions) {
			protected void populateItem(ListItem item) {
				item.add((AnswerFormFieldPanel) item.getModelObject());
			}
		};
		questionsView.setReuseItems(true);
		form.add(questionsView);
		add(form);
	}

	public static EgonetPage askNextUnanswered(Long interviewId) {
		Question nextAlterQuestion = Interviewing.nextUnansweredAlterQuestionForInterview(interviewId);
		if(nextAlterQuestion != null) {
			return new InterviewingAlterPage(interviewId, nextAlterQuestion);
		}
		return new InterviewingConclusionPage(interviewId);
	}
}
