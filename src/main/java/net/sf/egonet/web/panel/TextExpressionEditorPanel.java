package net.sf.egonet.web.panel;

import net.sf.egonet.model.Expression;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Questions;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

public class TextExpressionEditorPanel extends Panel {
	
	private Expression expression;

	private Form form;
	private TextField expressionNameField;
	private TextField expressionValueField;
	private Model expressionOperatorModel;
	
	public TextExpressionEditorPanel(String id, Expression expression) {
		super(id);
		this.expression = expression;
		if(! (expression.getType().equals(Expression.Type.Text) 
				|| expression.getType().equals(Expression.Type.Number))) {
			throw new RuntimeException(
					"Trying to use a text/number expression editor for an expression of type "+
					expression.getType());
		}
		build();
	}
	
	private void build() {
		
		form = new Form("form");
		
		form.add(new Label("questionName",Questions.getQuestion(expression.getQuestionId()).getTitle()));
		
		form.add(new FeedbackPanel("feedback"));
		
		expressionNameField = new TextField("expressionNameField", new Model(expression.getName()));
		expressionNameField.setRequired(true);
		form.add(expressionNameField);
		
		expressionOperatorModel = new Model(); // Expression.Operator.Equals ?
		DropDownChoice expressionOperatorField = new DropDownChoice(
				"expressionOperatorField",
				expressionOperatorModel,
				expression.allowedOperators());
		expressionOperatorField.setRequired(true);
		form.add(expressionOperatorField);
		
		expressionValueField = new TextField("expressionValueField", new Model(
				expression.getType().equals(Expression.Type.Text) ? 
						(String) expression.getValue() :
							(expression.getValue() == null ? "" : expression.getValue()+""))
		);
		if(expression.getType().equals(Expression.Type.Number)) {
			expressionValueField.setRequired(true);
			expressionValueField.add(new PatternValidator("[0-9]+"));
		}
		form.add(expressionValueField);
		
		form.add(
			new Button("saveExpression")
            {
				@Override
				public void onSubmit()
                {
					expression.setName(expressionNameField.getModelObjectAsString());
					expression.setOperator((Expression.Operator) expressionOperatorModel.getObject());
					String value = expressionValueField.getModelObjectAsString();
						expression.setValue(value == null ? "" : value);
					DB.save(expression);
					form.setVisible(false);
				}
			}
        );
		
		add(form);
	}
}
