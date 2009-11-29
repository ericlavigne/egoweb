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
		
		analysisForm.add(new Button("csvAlterExport") {
			public void onSubmit() {
				downloadText(
						getStudy().getName()+"-alter-data.csv",
						"text/csv",
						Analysis.getEgoAndAlterCSVForStudy(getStudy(),getConnectionReason()));
			}
		});
		
		analysisForm.add(new Button("csvAlterPairExport") {
			public void onSubmit() {
				downloadText(
						getStudy().getName()+"-alter-pair-data.csv",
						"text/csv",
						Analysis.getAlterPairCSVForStudy(getStudy(),getConnectionReason()));
			}
		});
		
		analysisForm.add(new ListView("interviews", new PropertyModel(this, "interviews")) {
			protected void populateItem(ListItem item) {
				final Interview interview = (Interview) item.getModelObject();
				item.add(new Label("interviewName",
						Interviews.getEgoNameForInterview(interview.getId())));
				item.add(new Button("interviewReview") {
					public void onSubmit() {
						EgonetPage page = InterviewingEgoPage.askNext(interview.getId(), null);
						if(page != null) {
							setResponsePage(page);
						}
					}
				});
				item.add(new Button("interviewVisualize") {
					public void onSubmit() {
						// TODO: Would be better as embedded image with size/color/adjacency controls. See p233 of Wicket in Action.
						
						downloadImage(interview.getId()+".jpg", 
								Analysis.getImageForInterview(interview, getConnectionReason()));
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

