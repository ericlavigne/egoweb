package net.sf.egonet.web.panel;

import java.util.List;

import net.sf.egonet.model.Expression;
import net.sf.egonet.persistence.Questions;

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

import com.google.common.collect.Lists;

public class ExpressionsPanel extends Panel {
	
	private Long studyId;
	
	public ExpressionsPanel(String id, Long studyId) {
		super(id);
		this.studyId = studyId;
		build();
	}
	
	public List<Expression> getExpressions() {
		return Lists.newArrayList(); // TODO: expressions for this studyId
	}
	
	private Form form;
	private Panel editExpressionPanel;
	private Model questionSelectionModel;
	
	private void build() {
		form = new Form("form");
		
		ListView expressions = new ListView("expressions", new PropertyModel(this,"expressions"))
        {
			protected void populateItem(ListItem item) {
				final Expression expression = (Expression) item.getModelObject();

				Link editExpressionLink = new Link("editExpressionLink")
                {
					public void onClick() {
						// TODO
					}
				};
				editExpressionLink.add(new Label("expressionName", expression.getName()));
				item.add(editExpressionLink);
				
				item.add(new Label("expressionDescription", expression.getValue()+""));
				
				item.add(new Link("deleteExpressionLink") {
					public void onClick() {
						// TODO: delete this expression
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
					// TODO: Open up an expression editor panel (which doesn't yet exist... hint, hint)
				}
			}
        );
		
		add(form);
		
		editExpressionPanel = new EmptyPanel("editExpressionPanel");
		add(editExpressionPanel);
	}
}
