package net.sf.egonet.web.panel;

import java.util.List;

import net.sf.egonet.model.Expression;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Expressions;

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

public class CompoundExpressionEditorPanel extends Panel {
	
	private Expression expression;

	private Form form;
	private TextField expressionNameField;
	private CheckboxesPanel<Expression> expressionValueField;
	private Model expressionOperatorModel;
	
	public CompoundExpressionEditorPanel(String id, Expression expression) {
		super(id);
		this.expression = expression;
		if(! expression.getType().equals(Expression.Type.Compound)) {
			throw new RuntimeException(
					"Trying to use a compound expression editor for an expression of type "+
					expression.getType());
		}
		build();
	}
	
	@SuppressWarnings("unchecked")
	private void build() {
		
		form = new Form("form");
		
		form.add(new Label("editorLegend", "Compound expression")); 
		
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
		
		List<Expression> allExpressions = Expressions.forStudy(expression.getStudyId());
		List<Expression> selectedExpressions = Lists.newArrayList();
		for(Expression expression : allExpressions) {
			for(Long expressionId : (List<Long>) this.expression.getValue()) {
				if(expression.getId().equals(expressionId)) {
					selectedExpressions.add(expression);
				}
			}
		}
		
		expressionValueField = new CheckboxesPanel<Expression>(
				"expressionValueField", allExpressions, selectedExpressions) {
			protected String showItem(Expression expression) {
				return expression.getName();
			}
		};
		form.add(expressionValueField);
		
		form.add(
			new Button("saveExpression")
            {
				@Override
				public void onSubmit()
                {
					expression.setName(expressionNameField.getText());
					expression.setOperator((Expression.Operator) expressionOperatorModel.getObject());
					List<Expression> selectedOptions = expressionValueField.getSelected();
					List<Long> selectedIds = Lists.transform(selectedOptions,
							new Function<Expression,Long>() {
						public Long apply(Expression expression) {
							return expression.getId();
						}
					});
					expression.setValue(selectedIds);
					DB.save(expression);
					form.setVisible(false);
				}
			}
        );
		
		add(form);
	}
}
