package net.sf.egonet.web.page;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.StringResourceStream;

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
		final FileUploadField studyImportField = new FileUploadField("studyImportField");
		Form studyImportForm = new Form("studyImportForm") {
			public void onSubmit() {
				try {
					String uploadText = uploadText(studyImportField);
					if(uploadText != null) {
						downloadFile("Uploaded-file.txt","text/plain",
								"Uploaded file contents: \n\n"+uploadText);
					}
				} catch(Exception ex) {
					throw new RuntimeException("Exception while trying to import study.",ex);
				}
			}
		};
		studyImportForm.setMultiPart(true);
		studyImportForm.add(studyImportField);
		studyImportForm.setMaxSize(Bytes.megabytes(100));
		add(studyImportForm);
		
		Form interviewImportForm = new Form("interviewImportForm") {
			public void onSubmit() {
				
			}
		};
		add(interviewImportForm);
		
		Form analysisForm = new Form("analysisForm");

		Long adjacencyExpressionId = getStudy().getAdjacencyExpressionId();
		adjacencyReason = adjacencyExpressionId == null ? new Model() : 
			new Model(Expressions.get(adjacencyExpressionId));
		analysisForm.add(new DropDownChoice("adjacency",adjacencyReason,Expressions.forStudy(getStudy().getId())));
		
		analysisForm.add(new Button("csvAlterExport") {
			public void onSubmit() {
				downloadFile(
						getStudy().getName()+"-alter-data.csv",
						"text/plain",
						Analysis.getEgoAndAlterCSVForStudy(getStudy()));
			}
		});
		
		analysisForm.add(new Button("csvAlterPairExport") {
			public void onSubmit() {
				downloadFile(
						getStudy().getName()+"-alter-pair-data.csv",
						"text/plain",
						Analysis.getAlterPairCSVForStudy(getStudy()));
			}
		});
		
		analysisForm.add(new Button("statisticsButton") {
			public void onSubmit() {
				downloadFile(
						getStudy().getName()+"-statistics.csv",
						"text/plain",
						"statistics csv for all interviews in study "+getStudy().getName());
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
						
						Expression connectionReason = (Expression) adjacencyReason.getObject();
						if(connectionReason != null) {
							downloadImage(interview.getId()+".jpg", 
									Analysis.getImageForInterview(interview, connectionReason));
						}
					}
				});
			}});
		
		add(analysisForm);
	}
	
	private void downloadImage(String name, BufferedImage image) {
		ResourceStreamRequestTarget target =
			new ResourceStreamRequestTarget(
					new Analysis.ImageResourceStream(image));
		target.setFileName(name);
		RequestCycle.get().setRequestTarget(target);
	}
	
	private void downloadFile(String name, String mimeType, CharSequence contents) {
		// See example on p231 of Wicket in Action
		ResourceStreamRequestTarget target =
			new ResourceStreamRequestTarget(
					new StringResourceStream(contents, mimeType));
		target.setFileName(name);
		RequestCycle.get().setRequestTarget(target);
	}
	
	private static String uploadText(FileUploadField field) throws IOException {
		FileUpload upload = field.getFileUpload();
		if(upload != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(upload.getInputStream()));
			StringBuffer buffer = new StringBuffer();
			String line = null;
			while((line = reader.readLine()) != null) {
				buffer.append(line+"\n");
			}
			return buffer.toString();
		}
		return null;
	}
	
	public List<Interview> getInterviews() {
		return Interviews.getInterviewsForStudy(studyId);
	}
}

