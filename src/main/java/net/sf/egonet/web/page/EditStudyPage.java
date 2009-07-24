package net.sf.egonet.web.page;

import java.util.Arrays;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

import net.sf.egonet.model.Study;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.web.panel.EditStudyQuestionsPanel;

public class EditStudyPage extends EgonetPage
{
	private final Long studyId;
	private Panel questionEditorPanel;

	public EditStudyPage(Study study)
    {
		super("Study: "+study.getName());
		this.studyId = study.getId();
		build();
	}

	private void build()
    {
		ListView questionTypes = new ListView("questionTypes", Arrays.asList(QuestionType.values()))
        {
			protected void populateItem(ListItem item) {
				final QuestionType questionType = (QuestionType) item.getModelObject();

				Link questionLink = new Link("questionTypeLink")
                {
					public void onClick() {
						Panel newPanel = 
							new EditStudyQuestionsPanel(
									"questionEditor", 
									DB.getStudy(studyId),
									questionType);
						questionEditorPanel.replaceWith(newPanel);
						questionEditorPanel = newPanel;
					}
				};

				questionLink.add(new Label("questionTypeName", questionType.toString()));
				item.add(questionLink);
			}
		};
		add(questionTypes);

		questionEditorPanel = new EmptyPanel("questionEditor");
		add(questionEditorPanel);
	}
}
