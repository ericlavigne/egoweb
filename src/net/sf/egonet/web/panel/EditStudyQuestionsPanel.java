package net.sf.egonet.web.panel;

import java.util.List;

import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Answer.AnswerType;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.persistence.Studies;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class EditStudyQuestionsPanel extends Panel {

	private Long studyId;
	private Question.QuestionType questionType;
	
	private Form questionsContainer;
	private Form editQuestionPanelContainer;
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

		questionsContainer = new Form("questionsContainer");
		questionsContainer.setOutputMarkupId(true);
		
		questionsContainer.add(new Label("caption",questionType+" Questions"));

		ListView questions = new ListView("questions", new PropertyModel(this,"questions"))
        {
			protected void populateItem(final ListItem item) {
				final Question question = (Question) item.getModelObject();

				Link questionLink = new AjaxFallbackLink("questionLink")
                {
					public void onClick(AjaxRequestTarget target) {
						editQuestion(question);
						target.addComponent(editQuestionPanelContainer);
					}
				};
				
				final Boolean selectionQuestion = 
					question.getAnswerType().equals(AnswerType.SELECTION) || 
					question.getAnswerType().equals(AnswerType.MULTIPLE_SELECTION);
				
				Link questionOptionsLink = new AjaxFallbackLink("questionOptionsLink") {
					public void onClick(AjaxRequestTarget target) {
						if(selectionQuestion) {
							editQuestionOptions(question);
							target.addComponent(editQuestionPanelContainer);
						}
					}
				};

				questionLink.add(new Label("questionTitle", question.getTitle()));
				item.add(questionLink);
				questionOptionsLink.add(
						new Label("questionOptionsLabel", selectionQuestion ? "Options ("+Options.getOptionsForQuestion(question.getId()).size()+")" : ""));
				item.add(questionOptionsLink);
				item.add(new Label("questionPrompt", question.getPrompt()));
				item.add(new Label("questionResponseType", question.getAnswerType().toString()));
				AjaxFallbackLink questionPreview = new AjaxFallbackLink("questionPreview") {
					public void onClick(AjaxRequestTarget target) {
						previewQuestion(question);
						target.addComponent(editQuestionPanelContainer);
					}
				};
				item.add(questionPreview);
				if(QuestionType.EGO_ID.equals(question.getType())) {
					questionPreview.setVisible(false);
				}
				item.add(new AjaxFallbackLink("questionMoveUp") {
					public void onClick(AjaxRequestTarget target) {
						Questions.moveEarlier(question);
						target.addComponent(questionsContainer);
					}
				});
				item.add(new AjaxFallbackLink("questionDelete") {
					public void onClick(AjaxRequestTarget target) {
						Questions.delete(question);
						EmptyPanel empty = new EmptyPanel("editQuestionPanel");
						editQuestionPanel.replaceWith(empty);
						editQuestionPanel = empty;
						target.addComponent(questionsContainer);
						target.addComponent(editQuestionPanelContainer);
					}
				});
			}
		};
		questionsContainer.add(questions);

		questionsContainer.add(new AjaxFallbackLink("newQuestion") {
			public void onClick(AjaxRequestTarget target) {
				editQuestion(new Question());
				target.addComponent(editQuestionPanelContainer);
			}
		});
		
		add(questionsContainer);
		
		editQuestionPanelContainer = new Form("editQuestionPanelContainer");
		editQuestionPanelContainer.setOutputMarkupId(true);
		
		editQuestionPanel = new EmptyPanel("editQuestionPanel");
		editQuestionPanelContainer.add(editQuestionPanel);
		
		add(editQuestionPanelContainer);
	}
	private void editQuestion(Question question) {
		Panel newPanel = new EditQuestionPanel("editQuestionPanel", questionsContainer, question, questionType, studyId);
		editQuestionPanel.replaceWith(newPanel);
		editQuestionPanel = newPanel;
	}
	private void editQuestionOptions(Question question) {
		Panel newPanel = new EditQuestionOptionsPanel("editQuestionPanel",questionsContainer,question);
		editQuestionPanel.replaceWith(newPanel);
		editQuestionPanel = newPanel;
	}
	private void previewQuestion(Question question) {
		Panel newPanel = InterviewingPanel.createExample("editQuestionPanel", question);
		editQuestionPanel.replaceWith(newPanel);
		editQuestionPanel = newPanel;
	}
}
