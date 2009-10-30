package net.sf.egonet.web.panel;

import java.util.List;

import net.sf.egonet.model.Expression;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.web.component.TextField;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

import com.google.common.collect.Lists;

public class TextExpressionEditorPanel extends Panel {
	
	private Expression expression;

	private Form form;
	private TextField expressionNameField;
	private TextField expressionValueField;
	private Model expressionOperatorModel;
	private Model expressionUnansweredModel;
	private Boolean textual;
	private Boolean numerical;
	
	public TextExpressionEditorPanel(String id, Expression expression) {
		super(id);
		this.expression = expression;
		this.textual = expression.getType().equals(Expression.Type.Text);
		this.numerical = expression.getType().equals(Expression.Type.Number);
		if(! (textual || numerical)) {
			throw new RuntimeException(
					"Trying to use a text/number expression editor for an expression of type "+
					expression.getType());
		}
		build();
	}
	
	private void build() {
		
		form = new Form("form");
		
		form.add(new Label("editorLegend",
				"Expression about " +
				(numerical ? "numerical" : "textual") + " question: " +
				Questions.getQuestion(expression.getQuestionId()).getTitle()));
		
		form.add(new FeedbackPanel("feedback"));
		
		expressionNameField = new TextField("expressionNameField", new Model(expression.getName()));
		expressionNameField.setRequired(true);
		form.add(expressionNameField);
		

		form.add(new Label("expressionOperatorPreface", 
				"Expression is true for an answer that"+(numerical ? " is" : "")));
		
		expressionOperatorModel = new Model(expression.getOperator());
		DropDownChoice expressionOperatorField = new DropDownChoice(
				"expressionOperatorField",
				expressionOperatorModel,
				expression.allowedOperators());
		expressionOperatorField.setRequired(true);
		form.add(expressionOperatorField);
		
		expressionValueField = new TextField("expressionValueField", new Model(
				textual ? 
					(String) expression.getValue() :
					(expression.getValue() == null ? "" : expression.getValue()+""))
		);
		if(numerical) {
			expressionValueField.setRequired(true);
			expressionValueField.add(new PatternValidator("[0-9]+"));
		}
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
					String value = expressionValueField.getText();
					expression.setValue(value == null ? "" : value);
					expression.setResultForUnanswered((Boolean) expressionUnansweredModel.getObject());
					DB.save(expression);
					form.setVisible(false);
				}
			}
        );
		
		add(form);
	}
}
