package net.sf.egonet.web.panel;

import net.sf.egonet.model.Expression;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Expressions;
import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;
import net.sf.functionalj.tuple.Pair;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

public class ComparisonExpressionEditorPanel extends Panel {
	
	private Expression expression;

	private Form form;
	private TextField expressionNameField;
	private TextField expressionValueField;
	private Model expressionOperatorModel;
	
	public ComparisonExpressionEditorPanel(String id, Expression expression) {
		super(id);
		this.expression = expression;
		if(! expression.getType().equals(Expression.Type.Comparison)) {
			throw new RuntimeException(
					"Trying to use a comparison expression editor for an expression of type "+
					expression.getType());
		}
		build();
	}
	
	private Expression comparisonTopic() {
		return Expressions.get(((Pair<Integer,Long>)expression.getValue()).getSecond());
	}
	
	private Integer comparisonNumber() {
		return ((Pair<Integer,Long>)expression.getValue()).getFirst();
	}
	
	private void build() {
		String comparisonTopicName = comparisonTopic().getName();
		
		form = new Form("form");
		
		form.add(new Label("editorLegend",
				"Comparison expression about " + comparisonTopicName));
		
		form.add(new FeedbackPanel("feedback"));
		
		expressionNameField = new TextField("expressionNameField", new Model(expression.getName()));
		expressionNameField.setRequired(true);
		expressionNameField.add(new FocusOnLoadBehavior());
		form.add(expressionNameField);
		
		form.add(new Label("expressionOperatorPreface", 
				"Expression is true if "+comparisonTopicName+" is "));
		
		expressionOperatorModel = new Model(expression.getOperator());
		DropDownChoice expressionOperatorField = new DropDownChoice(
				"expressionOperatorField",
				expressionOperatorModel,
				expression.allowedOperators());
		expressionOperatorField.setRequired(true);
		form.add(expressionOperatorField);
		
		expressionValueField = 
			new TextField("expressionValueField", 
					new Model(comparisonNumber().toString()));
		expressionValueField.setRequired(true);
		expressionValueField.add(new PatternValidator("[0-9]+"));
		form.add(expressionValueField);
		
		form.add(
			new Button("saveExpression")
            {
				@Override
				public void onSubmit()
                {
					expression.setName(expressionNameField.getText());
					expression.setOperator((Expression.Operator) expressionOperatorModel.getObject());
					Integer numValue = Integer.parseInt(expressionValueField.getText());
					Pair<Integer,Long> numExpr = (Pair<Integer,Long>) expression.getValue();
					expression.setValue(new Pair<Integer,Long>(numValue,numExpr.getSecond()));
					DB.save(expression);
					form.setVisible(false);
				}
			}
        );
		
		add(form);
	}
}
