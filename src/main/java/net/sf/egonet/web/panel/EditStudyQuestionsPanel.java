package net.sf.egonet.web.panel;

import java.util.Arrays;
import java.util.List;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.web.Main;
import net.sf.egonet.web.model.EntityModel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class EditStudyQuestionsPanel extends Panel {

	private EntityModel study;
	private Question.QuestionType questionType;
	
	public EditStudyQuestionsPanel(String id, Study study, Question.QuestionType questionType) {
		super(id);
		this.study = new EntityModel(study);
		this.questionType = questionType;
		build();
	}

	public List<Question> getQuestions() {
		return ((Study) study.getObject()).getQuestionList(questionType);
	}
	
	private void build()
    {
		add(new Label("caption",questionType+" Questions"));
		
		add(new FeedbackPanel("feedback"));

		ListView questions = new ListView("questions", new PropertyModel(this,"questions"))
        {
			protected void populateItem(ListItem item) {
				final Question question = (Question) item.getModelObject();

				Link questionLink = new Link("questionLink")
                {
					public void onClick() {
						//
					}
				};

				questionLink.add(new Label("questionTitle", question.getTitle()));
				item.add(questionLink);
				item.add(new Label("questionPrompt", question.getPrompt()));
				item.add(new Label("questionResponseType", question.getAnswerType().toString()));
			}
		};
		add(questions);

		Form form = new Form("questionForm");

		final TextField questionTitleField = new TextField("questionTitleField", new Model(""));
		questionTitleField.setRequired(true);
		form.add(questionTitleField);

		final TextArea questionPromptField = new TextArea("questionPromptField", new Model(""));
		questionPromptField.setRequired(true);
		form.add(questionPromptField);

		final TextArea questionCitationField = new TextArea("questionCitationField", new Model(""));
		form.add(questionCitationField);

		final Model questionResponseTypeModel = new Model(Answer.AnswerType.TEXTUAL); // Could also leave this null.
		form.add(new DropDownChoice(
				"questionResponseTypeField",
				questionResponseTypeModel,
				Arrays.asList(Answer.AnswerType.values())));

		form.add(
			new Button("submitQuestion")
            {
				@Override
				public void onSubmit()
                {
					Question question = new Question();
					question.setTitle((String) questionTitleField.getModelObject());
					question.setPrompt((String) questionPromptField.getModelObject());
					question.setCitation((String) questionTitleField.getModelObject());
					question.setAnswerType((Answer.AnswerType) questionResponseTypeModel.getObject());
					question.setType(questionType);

					Study studyObject = (Study) study.getObject();
					studyObject.addQuestion(question);
					study.save();

					Session session = Main.getDBSessionFactory().openSession();
					Transaction tx = session.beginTransaction();
					
					session.saveOrUpdate(question);
					
					tx.commit();
					session.close();
					
					//throw new RuntimeException("\nQuestion: "+question+"\nStudy: "+studyObject);
					
					//((Study) study.getObject()).addQuestion(question);
					//study.save(); // TODO: Figure out why saving doesn't work anymore. Change to EntityModel?
				}
			}
        );
		add(form);
	}

}
