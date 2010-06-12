package net.sf.egonet.web.page;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Analysis;
import net.sf.egonet.persistence.Expressions;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;

public class AnalysisStudyPage extends EgonetPage {
	
	private Long studyId;
	private Model adjacencyReason;
	private Label errorMessage;
	
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
						Analysis.getEgoAndAlterCSVForStudy(getStudy(),connectionExpression));
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
						Analysis.getAlterPairCSVForStudy(getStudy(),connectionExpression));
			}
		});
		
		analysisForm.add(new ListView("interviews", new PropertyModel(this, "interviews")) {
			protected void populateItem(ListItem item) {
				final Interview interview = (Interview) item.getModelObject();
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

