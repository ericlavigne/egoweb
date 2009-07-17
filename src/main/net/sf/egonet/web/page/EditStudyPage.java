package net.sf.egonet.web.page;

import java.util.ArrayList;

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
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
//import net.sf.egonet.model.Section;
import net.sf.egonet.model.Study;
import net.sf.egonet.web.model.EntityModel;

public class EditStudyPage extends EgonetPage
{
	private final EntityModel study;

	public EditStudyPage(Study study)
    {
		super("Study: "+study.getName());
		this.study = new EntityModel(study);
		build();
	}

	private void build()
    {
		add(new FeedbackPanel("feedback"));

		ListView questions = new ListView("questions", new PropertyModel(study,"questionList"))
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
		ArrayList<Answer.AnswerType> responseTypeOptions = new ArrayList<Answer.AnswerType>();
		for (Answer.AnswerType responseType : Answer.AnswerType.values()) {
			responseTypeOptions.add(responseType);
		}
		form.add(new DropDownChoice("questionResponseTypeField",questionResponseTypeModel,responseTypeOptions));

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

					((Study) study.getObject()).addQuestion(question);
					study.save();
				}
			}
        );
		add(form);
	}
}
