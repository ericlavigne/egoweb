package net.sf.egonet.web.panel;

import java.util.ArrayList;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Study;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

public class StudySettingsPanel extends Panel {
	
	private Study study;
	
	private Form form;
	
	private TextArea introductionField;
	private TextArea egoIdField;
	private TextArea alterPromptField;
	private TextArea conclusionField;

	private TextField minAltersField;
	private TextField maxAltersField;
	
	private Model adjacencyModel;
	
	public StudySettingsPanel(String id, Study study) {
		super(id);
		this.study = study;
		build();
	}
	
	private void build() {
		add(new FeedbackPanel("feedback"));
		
		form = new Form("form");
		
		introductionField = new TextArea("introductionField", new Model(""));
		egoIdField = new TextArea("egoIdField", new Model(""));
		alterPromptField = new TextArea("alterPromptField", new Model(""));
		conclusionField = new TextArea("conclusionField", new Model(""));
		
		egoIdField.setRequired(true);
		alterPromptField.setRequired(true);
		conclusionField.setRequired(true);
		
		form.add(introductionField);
		form.add(egoIdField);
		form.add(alterPromptField);
		form.add(conclusionField);

		minAltersField = new TextField("minAltersField", new Model(""));
		maxAltersField = new TextField("maxAltersField", new Model(""));

		minAltersField.add(new PatternValidator("[0-9]*"));
		maxAltersField.add(new PatternValidator("[0-9]*"));
		
		form.add(minAltersField);
		form.add(maxAltersField);
		
		adjacencyModel = new Model();
		form.add(new DropDownChoice("adjacencyField",adjacencyModel,new ArrayList<Expression>()));
		
		Button button = new Button("saveButton") {
			@Override
			public void onSubmit() {
				
			}
		};
		form.add(button);
		add(form);
	}
}
