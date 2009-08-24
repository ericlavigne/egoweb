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
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.panel.EditStudyQuestionsPanel;
import net.sf.egonet.web.panel.ExpressionsPanel;

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
		add(new Link("settingsLink") {
			public void onClick() {
				// TODO
			}
		});
		
		ListView questionTypes = new ListView("questionTypes", Arrays.asList(QuestionType.values()))
        {
			protected void populateItem(ListItem item) {
				final QuestionType questionType = (QuestionType) item.getModelObject();

				Link questionLink = new Link("questionTypeLink")
                {
					public void onClick() {
						setSubPanel(
								new EditStudyQuestionsPanel(
									"questionEditor", 
									Studies.getStudy(studyId),
									questionType));
					}
				};

				questionLink.add(new Label("questionTypeName", questionType.toString()));
				item.add(questionLink);
			}
		};
		add(questionTypes);
		
		add(new Link("expressionLink") {
			public void onClick() {
				setSubPanel(new ExpressionsPanel("questionEditor",studyId));
			}
		});

		questionEditorPanel = new EmptyPanel("questionEditor");
		add(questionEditorPanel);
	}
	
	private void setSubPanel(Panel panel) {
		questionEditorPanel.replaceWith(panel);
		questionEditorPanel = panel;
	}
}
