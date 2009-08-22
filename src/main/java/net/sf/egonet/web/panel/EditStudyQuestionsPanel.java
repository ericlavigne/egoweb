package net.sf.egonet.web.panel;

import java.util.List;

import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.persistence.Studies;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class EditStudyQuestionsPanel extends Panel {

	private Long studyId;
	private Question.QuestionType questionType;
	
	private Panel editQuestionPanel;
	
	
	public EditStudyQuestionsPanel(String id, Study study, Question.QuestionType questionType) {
		super(id);
		this.studyId = study.getId();
		this.questionType = questionType;
		build();
	}
	
	public Study getStudy() {
		return Studies.getStudy(studyId);
	}

	public List<Question> getQuestions() {
		return Questions.getQuestionsForStudy(studyId,questionType);
	}
	
	private void build()
    {
		add(new Label("caption",questionType+" Questions"));

		ListView questions = new ListView("questions", new PropertyModel(this,"questions"))
        {
			protected void populateItem(ListItem item) {
				final Question question = (Question) item.getModelObject();

				Link questionLink = new Link("questionLink")
                {
					public void onClick() {
						editQuestion(question);
					}
				};
				Link questionOptionsLink = new Link("questionOptionsLink") {
					public void onClick() {
						editQuestionOptions(question);
					}
				};

				questionLink.add(new Label("questionTitle", question.getTitle()));
				item.add(questionLink);
				questionOptionsLink.add(new Label("questionOptionsLabel", "Options (0)"));
				item.add(questionOptionsLink);
				item.add(new Label("questionPrompt", question.getPrompt()));
				item.add(new Label("questionResponseType", question.getAnswerType().toString()));
			}
		};
		add(questions);

		add(new Link("newQuestion") {
			public void onClick() {
				editQuestion(new Question());
			}
		});
		
		editQuestionPanel = new EmptyPanel("editQuestionPanel");
		add(editQuestionPanel);
	}
	private void editQuestion(Question question) {
		Panel newPanel = new EditQuestionPanel("editQuestionPanel", question, questionType, studyId);
		editQuestionPanel.replaceWith(newPanel);
		editQuestionPanel = newPanel;
	}
	private void editQuestionOptions(Question question) {
		Panel newPanel = new EditQuestionOptionsPanel("editQuestionPanel",question);
		editQuestionPanel.replaceWith(newPanel);
		editQuestionPanel = newPanel;
	}
}
