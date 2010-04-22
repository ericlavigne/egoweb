package net.sf.egonet.web.panel;

import java.util.List;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Expressions;
import net.sf.egonet.persistence.Questions;

import net.sf.egonet.web.component.FocusOnLoadBehavior;
import net.sf.egonet.web.component.TextField;
import net.sf.functionalj.tuple.Triple;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class CountingExpressionEditorPanel extends Panel {
	
	private Expression expression;

	private Form form;
	private TextField expressionNameField, multiplierValueField;
	private CheckboxesPanel<Expression> expressionValueField;
	private CheckboxesPanel<Question> questionValueField;
	private Model expressionOperatorModel;
	
	public CountingExpressionEditorPanel(String id, Expression expression) {
		super(id);
		this.expression = expression;
		if(! expression.getType().equals(Expression.Type.Counting)) {
			throw new RuntimeException(
					"Trying to use a counting expression editor for an expression of type "+
					expression.getType());
		}
		build();
	}
	
	private void build() {
		
		form = new Form("form");
		
		form.add(new FeedbackPanel("feedback"));
		
		expressionNameField = new TextField("expressionNameField", new Model(expression.getName()));
		expressionNameField.setRequired(true);
		expressionNameField.add(new FocusOnLoadBehavior());
		form.add(expressionNameField);
		
		Triple<Integer,List<Long>,List<Long>> numberExprsQuests = 
			(Triple<Integer,List<Long>,List<Long>>) expression.getValue();
		
		multiplierValueField = 
			new TextField("multiplierValueField", new Model(numberExprsQuests.getFirst()));
		multiplierValueField.setRequired(true);
		multiplierValueField.add(new PatternValidator("\\-?[0-9]+"));
		form.add(multiplierValueField);
		
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
			for(Long expressionId : numberExprsQuests.getSecond()) {
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

		List<Question> allQuestions = Questions.getQuestionsForStudy(expression.getStudyId(),null);
		List<Question> selectedQuestions = Lists.newArrayList();
		for(Question question : allQuestions) {
			for(Long questionId : numberExprsQuests.getThird()) {
				if(question.getId().equals(questionId)) {
					selectedQuestions.add(question);
				}
			}
		}
		
		questionValueField = new CheckboxesPanel<Question>(
				"questionValueField", allQuestions, selectedQuestions) {
			protected String showItem(Question question) {
				return question.getTitle();
			}
		};
		form.add(questionValueField);
		
		form.add(
			new Button("saveExpression")
            {
				@Override
				public void onSubmit()
                {
					expression.setName(expressionNameField.getText());
					expression.setOperator((Expression.Operator) expressionOperatorModel.getObject());
					
					Integer multiplier = Integer.parseInt(multiplierValueField.getText());

					List<Expression> selectedExpressions = expressionValueField.getSelected();
					List<Long> selectedExpressionIds = Lists.transform(selectedExpressions,
							new Function<Expression,Long>() {
						public Long apply(Expression expression) {
							return expression.getId();
						}
					});
					
					List<Question> selectedQuestions = questionValueField.getSelected();
					List<Long> selectedQuestionIds = Lists.transform(selectedQuestions,
							new Function<Question,Long>() {
						public Long apply(Question question) {
							return question.getId();
						}
					});
					
					expression.setValue(new Triple<Integer,List<Long>,List<Long>>(multiplier,selectedExpressionIds,selectedQuestionIds));
					DB.save(expression);
					form.setVisible(false);
				}
			}
        );
		
		add(form);
	}
}
