package net.sf.egonet.web.page;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.StringResourceStream;

import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Studies;

public class AnalysisStudyPage extends EgonetPage {
	
	private Long studyId;
	
	public AnalysisStudyPage(Study study) {
		super("Analysis for "+study.getName());
		studyId = study.getId();
		build();
	}
	
	public Study getStudy() {
		return Studies.getStudy(studyId);
	}
	
	private void build() {
		Form studyImportForm = new Form("studyImportForm") {
			public void onSubmit() {
				
			}
		};
		add(studyImportForm);
		
		Form interviewImportForm = new Form("interviewImportForm") {
			public void onSubmit() {
				
			}
		};
		add(interviewImportForm);
		
		Form analysisForm = new Form("analysisForm");
		analysisForm.add(new Button("statisticsButton") {
			public void onSubmit() {
				downloadFile(
						getStudy().getName()+"-statistics.csv",
						"text/plain",
						"statistics csv for all interviews in study "+getStudy().getName());
			}
		});
		add(analysisForm);
	}
	
	private void downloadFile(String name, String mimeType, CharSequence contents) {
		// See example on p231 of Wicket in Action
		ResourceStreamRequestTarget target =
			new ResourceStreamRequestTarget(
					new StringResourceStream(contents, mimeType));
		target.setFileName(name);
		RequestCycle.get().setRequestTarget(target);
	}
}

