package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.Expressions;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.persistence.Studies;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.joda.time.DateTime;

public class ExpressionsPanel extends Panel {
	
	private Long studyId;
	
	public ExpressionsPanel(String id, Long studyId) {
		super(id);
		this.studyId = studyId;
		build();
	}
	
	private ArrayList<Expression> expressions;
	private DateTime expressionsLastRefreshed;
	
	public List<Expression> getExpressions() {
		DateTime now = new DateTime();
		if(expressions == null || expressionsLastRefreshed.isBefore(now.minusSeconds(1))) {
			expressions = new ArrayList<Expression>(Expressions.forStudy(studyId));
			expressionsLastRefreshed = now;
		}
		return expressions;
	}
	
	private Form form;
	private Panel editExpressionPanel;
	private String panelId = "editExpressionPanel";
	private Model questionSelectionModel,comparisonTopicModel;
	
	private void build() {
		form = new Form("form");
		
		ListView expressions = new ListView("expressions", new PropertyModel(this,"expressions"))
        {
			protected void populateItem(ListItem item) {
				final Expression expression = (Expression) item.getModelObject();

				Link editExpressionLink = new Link("editExpressionLink")
                {
					public void onClick() {
						editExpression(expression);
					}
				};
				editExpressionLink.add(new Label("expressionName", expression.getName()));
				item.add(editExpressionLink);
				
				item.add(new Label("expressionDescription", expression.getValue()+""));
				
				item.add(new Link("deleteExpressionLink") {
					public void onClick() {
						Expressions.delete(expression);
						replaceExpressionEditorWith(new EmptyPanel(panelId));
					}
				});
			}
		};
		form.add(expressions);
		

		questionSelectionModel = new Model(); 
		form.add(new DropDownChoice(
				"questionField",
				questionSelectionModel,
				Questions.getQuestionsForStudy(studyId, null)));

		form.add(
			new Button("newSimpleExpression")
            {
				@Override
				public void onSubmit()
                {
					Question question = (Question) questionSelectionModel.getObject();
					if(question != null) {
						editExpression(new Expression(question));
					}
				}
			}
        );
		form.add(
				new Button("newCompoundExpression")
	            {
					@Override
					public void onSubmit()
	                {
						editExpression(new Expression(Studies.getStudy(studyId)));
					}
				}
	        );

		comparisonTopicModel = new Model(); 
		form.add(new DropDownChoice(
				"comparisonTopicField",
				comparisonTopicModel,
				new PropertyModel(this,"expressions")));

		form.add(
				new Button("newComparisonExpression")
	            {
					@Override
					public void onSubmit()
	                {
						Expression comparisonTopic = (Expression) comparisonTopicModel.getObject();
						if(comparisonTopic != null) {
							editExpression(Expression.comparisonAbout(comparisonTopic));
						}
					}
				}
	        );
		
		form.add(
				new Button("newCountingExpression")
	            {
					@Override
					public void onSubmit()
	                {
						editExpression(Expression.countingForStudy(Studies.getStudy(studyId)));
					}
				}
	        );
		
		add(form);
		
		editExpressionPanel = new EmptyPanel(panelId);
		add(editExpressionPanel);
	}

	private void replaceExpressionEditorWith(Panel panel) {
		editExpressionPanel.replaceWith(panel);
		editExpressionPanel = panel;
	}
	
	private Panel getExpressionEditor(Expression expression) {
		if(expression.getType().equals(Expression.Type.Text) 
				|| expression.getType().equals(Expression.Type.Number)) {
			return new TextExpressionEditorPanel(panelId,expression);
		}
		if(expression.getType().equals(Expression.Type.Selection)) {
			return new SelectionExpressionEditorPanel(panelId,expression);
		}
		if(expression.getType().equals(Expression.Type.Compound)) {
			return new CompoundExpressionEditorPanel(panelId,expression);
		}
		if(expression.getType().equals(Expression.Type.Comparison)) {
			return new ComparisonExpressionEditorPanel(panelId,expression);
		}
		if(expression.getType().equals(Expression.Type.Counting)) {
			return new CountingExpressionEditorPanel(panelId,expression);
		}
		return new EmptyPanel(panelId);
	}
	
	private void editExpression(Expression expression) {
		replaceExpressionEditorWith(getExpressionEditor(expression));
	}
}
