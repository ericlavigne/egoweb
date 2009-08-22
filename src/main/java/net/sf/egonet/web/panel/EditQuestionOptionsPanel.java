package net.sf.egonet.web.panel;

import net.sf.egonet.model.Question;

import org.apache.wicket.markup.html.panel.Panel;

public class EditQuestionOptionsPanel extends Panel {

	private Question question;
	
	public EditQuestionOptionsPanel(String id, Question question) {
		super(id);
		this.question = question;
		build();
	}
	
	private void build() {
		
	}
}
