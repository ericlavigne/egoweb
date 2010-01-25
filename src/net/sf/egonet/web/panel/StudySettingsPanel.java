package net.sf.egonet.web.panel;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Expressions;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextArea;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
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

	private TextField refusedField;
	private TextField dontKnowField;
	private TextField logicalSkipField;
	private TextField notYetField;
	
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
		
		introductionField.add(new FocusOnLoadBehavior());
		
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
		form.add(new DropDownChoice("adjacencyField",adjacencyModel,Expressions.forStudy(study.getId())));
		
		refusedField = new TextField("refusedField", new Model(""));
		dontKnowField = new TextField("dontKnowField", new Model(""));
		logicalSkipField = new TextField("logicalSkipField", new Model(""));
		notYetField = new TextField("notYetField", new Model(""));
		
		form.add(refusedField);
		form.add(dontKnowField);
		form.add(logicalSkipField);
		form.add(notYetField);
		
		Button button = new Button("saveButton") {
			@Override
			public void onSubmit() {
				setStudyFieldsFromForm();
				DB.save(study);
				form.setVisible(false);
			}
		};
		form.add(button);
		setFormFieldsFromStudy();
		add(form);
	}
	
	private void setFormFieldsFromStudy() {
		introductionField.setModelObject(study.getIntroduction());
		egoIdField.setModelObject(study.getEgoIdPrompt());
		alterPromptField.setModelObject(study.getAlterPrompt());
		conclusionField.setModelObject(study.getConclusion());

		minAltersField.setModelObject(study.getMinAlters() == null ? "0" : study.getMinAlters().toString());
		maxAltersField.setModelObject(study.getMaxAlters() == null ? "" : study.getMaxAlters().toString());

		refusedField.setModelObject(study.getValueRefusal());
		dontKnowField.setModelObject(study.getValueDontKnow());
		logicalSkipField.setModelObject(study.getValueLogicalSkip());
		notYetField.setModelObject(study.getValueNotYetAnswered());
		
		Long adjacencyId = study.getAdjacencyExpressionId();
		if(adjacencyId != null) {
			adjacencyModel.setObject(Expressions.get(adjacencyId));
		}
	}
	
	private void setStudyFieldsFromForm() {
		study.setIntroduction(introductionField.getText());
		study.setEgoIdPrompt(egoIdField.getText());
		study.setAlterPrompt(alterPromptField.getText());
		study.setConclusion(conclusionField.getText());

		String minAltersString = minAltersField.getModelObjectAsString();
		String maxAltersString = maxAltersField.getModelObjectAsString();

		study.setMinAlters(
				minAltersString == null || minAltersString.isEmpty() ? 0 : 
					Integer.parseInt(minAltersString));
		study.setMaxAlters(
				maxAltersString == null || maxAltersString.isEmpty() ? null : 
					Integer.parseInt(maxAltersString));
		
		study.setValueRefusal(refusedField.getModelObjectAsString());
		study.setValueDontKnow(dontKnowField.getModelObjectAsString());
		study.setValueLogicalSkip(logicalSkipField.getModelObjectAsString());
		study.setValueNotYetAnswered(notYetField.getModelObjectAsString());
		
		Expression adjacencyReason = (Expression) adjacencyModel.getObject();
		if(adjacencyReason != null) {
			study.setAdjacencyExpressionId(adjacencyReason.getId());
		}
	}
}
