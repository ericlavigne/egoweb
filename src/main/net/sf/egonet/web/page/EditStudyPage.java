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
//		build();
	}

	/*
	private void build()
    {
		add(new FeedbackPanel("feedback"));
		Form form = new Form("sectionForm");

		final TextField sectionNameField = new TextField("sectionNameField", new Model(""));
		sectionNameField.setRequired(true);
		form.add(sectionNameField);

		final Model sectionSubjectModel = new Model(Section.Subject.EGO); // Could also leave this null.
		ArrayList<Section.Subject> subjectOptions = new ArrayList<Section.Subject>();
		for (Section.Subject subject : Section.Subject.values()) {
			subjectOptions.add(subject);
		}
		form.add(new DropDownChoice("sectionSubjectField",sectionSubjectModel,subjectOptions));

		form.add(
			new Button("createSection")
            {
				@Override
				public void onSubmit()
                {
					String name = (String) sectionNameField.getModelObject();
					Section.Subject subject = (Section.Subject) sectionSubjectModel.getObject();
					((Study) study.getObject()).addSection(name,subject);
					study.save();
				}
			}
        );
		add(form);

		ListView sectionsView = new ListView("sections", new PropertyModel(study,"sections"))
        {
			protected void populateItem(ListItem item) {
				final Section section = (Section) item.getModelObject();

				Link sectionLink = new Link("sectionLink")
                {
					public void onClick() {
						// setResponsePage(new EditSectionPage(section));
					}
				};

				sectionLink.add(new Label("sectionName", section.name));
				item.add(sectionLink);
				item.add(new Label("sectionSubject", section.subject.toString()));
			}
		};
		add(sectionsView);

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
				item.add(new Label("questionResponseType", question.getResponseType().toString()));
			}
		};
		add(questions);

		form = new Form("questionForm");

		final TextField questionTitleField = new TextField("questionTitleField", new Model(""));
		questionTitleField.setRequired(true);
		form.add(questionTitleField);

		final TextArea questionPromptField = new TextArea("questionPromptField", new Model(""));
		questionPromptField.setRequired(true);
		form.add(questionPromptField);

		final TextArea questionCitationField = new TextArea("questionCitationField", new Model(""));
		form.add(questionCitationField);

		final Model questionResponseTypeModel = new Model(Question.ResponseType.TEXT); // Could also leave this null.
		ArrayList<Question.ResponseType> responseTypeOptions = new ArrayList<Question.ResponseType>();
		for (Question.ResponseType responseType : Question.ResponseType.values()) {
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
					question.setResponseType((Question.ResponseType) questionResponseTypeModel.getObject());

					((Study) study.getObject()).addQuestion(question);
					study.save();
				}
			}
        );
		add(form);
	}
*/
}