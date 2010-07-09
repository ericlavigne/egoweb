package net.sf.egonet.web.page;

import java.util.List;
import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.AjaxRequestTarget;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Analysis;
import net.sf.egonet.persistence.Expressions;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Studies;

import net.sf.egonet.web.page.CheckIncludeID;
import net.sf.egonet.web.panel.InterviewNavigationPanel.InterviewLink;

public class AnalysisStudyPage extends EgonetPage {
	
	private Long studyId;
	private Model adjacencyReason;
	private Label errorMessage;
	private List<CheckIncludeID> checkIncludeIDList;
	
	public AnalysisStudyPage(Study study) {
		super("Analysis for "+study.getName());
		studyId = study.getId();
		build();
	}
	
	public Study getStudy() {
		return Studies.getStudy(studyId);
	}
	
	private void build() {
		Form analysisForm = new Form("analysisForm");

		checkIncludeIDList = Lists.newArrayList();
		Long adjacencyExpressionId = getStudy().getAdjacencyExpressionId();
		adjacencyReason = adjacencyExpressionId == null ? new Model() : 
			new Model(Expressions.get(adjacencyExpressionId));
		analysisForm.add(new DropDownChoice("adjacency",adjacencyReason,Expressions.forStudy(getStudy().getId())));
		errorMessage = new Label ("errorMessage", "");
		analysisForm.add(errorMessage);
		analysisForm.add(new Button("csvAlterExport") {
			public void onSubmit() {
				Expression connectionExpression = getConnectionReason();
				if ( connectionExpression == null ) {
					errorMessage.setModelObject("Need to select adjacency!");
					return;
				}
				errorMessage.setModelObject("");
				downloadText(
						getStudy().getName()+"-alter-data.csv",
						"text/csv",
						Analysis.getEgoAndAlterCSVForStudy(getStudy(),connectionExpression,checkIncludeIDList));
			}
		});
		
		analysisForm.add(new Button("csvAlterPairExport") {
			public void onSubmit() {
				Expression connectionExpression = getConnectionReason();
				if ( connectionExpression == null ) {
					errorMessage.setModelObject("Need to select adjacency!");
					return;
				}
				errorMessage.setModelObject("");
				downloadText(
						getStudy().getName()+"-alter-pair-data.csv",
						"text/csv",
						Analysis.getAlterPairCSVForStudy(getStudy(),connectionExpression,checkIncludeIDList));
			}
		});
		
		analysisForm.add(new ListView("interviews", new PropertyModel(this, "interviews")) {
			protected void populateItem(ListItem item) {
				final Interview interview = (Interview) item.getModelObject();
				AjaxCheckBox cBox;
				ArrayList<InterviewLink> links;

				item.add(new Label("interviewName",
						Interviews.getEgoNameForInterview(interview.getId())));
				item.add(new Button("interviewReview") {
					public void onSubmit() {
						EgonetPage page = InterviewingEgoPage.askNext(interview.getId(), null,null);
						if(page != null) {
							setResponsePage(page);
						}
					}
				});
				item.add(new Button("interviewVisualize") {
					public void onSubmit() {
						//downloadImage(interview.getId()+".jpg", 
						//		Analysis.getImageForInterview(interview, getConnectionReason()));
						setResponsePage(
								new NetworkVisualizationPage(
										interview,
										getConnectionReason()));
					}
				});
				item.add(new Label("interviewActive", (interview.getCompleted()?"Complete":"Incomplete")));
				CheckIncludeID checkIncludeID = new CheckIncludeID(interview.getId(), interview.getCompleted());
				checkIncludeIDList.add(checkIncludeID);
				// item.add(new CheckBox("interviewInclude", new PropertyModel(checkIncludeID,"selected")));
				cBox = new AjaxCheckBox("interviewInclude", new PropertyModel(checkIncludeID,"selected"))	{
					 protected void onUpdate(AjaxRequestTarget target) {
					 }
					 };
				cBox.setOutputMarkupId(true);
				item.add(cBox);
				links = Lists.newArrayList(Interviewing.getAnsweredPagesForInterview(interview.getId()));
				if ( links.isEmpty())
				    item.add(new Label("lastQuestion", "(none)"));
				else
					item.add(new Label("lastQuestion", links.get(links.size()-1).toString()));
			}});
		
		add(analysisForm);
	}
	
	private Expression getConnectionReason() {
		return (Expression) adjacencyReason.getObject();
	}
	
	public List<Interview> getInterviews() {
		return Interviews.getInterviewsForStudy(studyId);
	}
}



