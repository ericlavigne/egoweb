package net.sf.egonet.web.panel;

import java.util.List;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class SelectionExpressionEditorPanel extends Panel {
	
	private Expression expression;

	private Form form;
	private TextField expressionNameField;
	private CheckboxesPanel<QuestionOption> expressionValueField;
	private Model expressionOperatorModel;
	private Model expressionUnansweredModel;
	
	public SelectionExpressionEditorPanel(String id, Expression expression) {
		super(id);
		this.expression = expression;
		if(! expression.getType().equals(Expression.Type.Selection)) {
			throw new RuntimeException(
					"Trying to use a selection expression editor for an expression of type "+
					expression.getType());
		}
		build();
	}
	
	@SuppressWarnings("unchecked")
	private void build() {
		
		form = new Form("form");
		
		form.add(new Label("editorLegend",
				"Expression about selection question: " +
				Questions.getQuestion(expression.getQuestionId()).getTitle()));
		
		form.add(new FeedbackPanel("feedback"));
		
		expressionNameField = new TextField("expressionNameField", new Model(expression.getName()));
		expressionNameField.setRequired(true);
		expressionNameField.add(new FocusOnLoadBehavior());
		form.add(expressionNameField);
		
		expressionOperatorModel = new Model(expression.getOperator());
		DropDownChoice expressionOperatorField = new DropDownChoice(
				"expressionOperatorField",
				expressionOperatorModel,
				expression.allowedOperators());
		expressionOperatorField.setRequired(true);
		form.add(expressionOperatorField);
		
		List<QuestionOption> allOptions = Options.getOptionsForQuestion(expression.getQuestionId());
		List<QuestionOption> selectedOptions = Lists.newArrayList();
		for(QuestionOption option : allOptions) {
			for(Long optionId : (List<Long>) expression.getValue()) {
				if(option.getId().equals(optionId)) {
					selectedOptions.add(option);
				}
			}
		}
		
		expressionValueField = new CheckboxesPanel<QuestionOption>(
				"expressionValueField", allOptions, selectedOptions) {
			protected String showItem(QuestionOption option) {
				return option.getName();
			}
		};
		form.add(expressionValueField);

		expressionUnansweredModel = new Model(expression.getResultForUnanswered());
		List<Boolean> expressionUnansweredOptions = Lists.newArrayList(false,true);
		DropDownChoice expressionUnansweredField = new DropDownChoice(
				"expressionUnansweredField",
				expressionUnansweredModel,
				expressionUnansweredOptions);
		expressionUnansweredField.setRequired(true);
		form.add(expressionUnansweredField);
		
		form.add(
			new Button("saveExpression")
            {
				@Override
				public void onSubmit()
                {
					expression.setName(expressionNameField.getText());
					expression.setOperator((Expression.Operator) expressionOperatorModel.getObject());
					List<QuestionOption> selectedOptions = expressionValueField.getSelected();
					List<Long> selectedIds = Lists.transform(selectedOptions,
							new Function<QuestionOption,Long>() {
						public Long apply(QuestionOption option) {
							return option.getId();
						}
					});
					expression.setValue(selectedIds);
					expression.setResultForUnanswered((Boolean) expressionUnansweredModel.getObject());
					DB.save(expression);
					form.setVisible(false);
				}
			}
        );
		
		add(form);
	}
}
