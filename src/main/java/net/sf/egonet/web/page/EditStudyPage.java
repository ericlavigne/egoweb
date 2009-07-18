package net.sf.egonet.web.page;

import net.sf.egonet.model.Study;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.web.model.EntityModel;
import net.sf.egonet.web.panel.EditStudyQuestionsPanel;

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
		add(new EditStudyQuestionsPanel("questionEditor", (Study) study.getObject(),QuestionType.EGO));
	}
}
