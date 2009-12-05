package net.sf.egonet.web.page;

import java.util.List;

import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Archiving;
import net.sf.egonet.persistence.Studies;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;

public class ImportExportPage extends EgonetPage {
	
	private Model studyModel;
	
	public ImportExportPage() {

		add(buildStudyImportForm());
		
		add(buildRespondentDataImportForm());
		
		// Drop down for selecting study from which to export.
		List<Study> studies = Studies.getStudies();
		studyModel = studies.size() == 1 ? new Model(studies.get(0)) : new Model();
		DropDownChoice studyDropdown = new DropDownChoice("studyDropdown",studyModel,studies);

		Form exportForm = new Form("exportForm");
		exportForm.add(studyDropdown);
		exportForm.add(buildStudyExportButton());
		exportForm.add(buildRespondentDataExportButton());
		add(exportForm);
	}
	
	private Study getStudy() {
		return (Study) studyModel.getObject();
	}
	
	private Form buildStudyImportForm() {

		final FileUploadField studyImportField = new FileUploadField("studyImportField");
		Form studyImportForm = new Form("studyImportForm") {
			public void onSubmit() {
				// TODO: import study (the code below is filler)
				try {
					String uploadText = uploadText(studyImportField);
					if(uploadText != null) {
						downloadText("Uploaded-file.txt","text/plain",
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
		
		return studyImportForm;
	}
	
	private Form buildRespondentDataImportForm() {

		final FileUploadField respondentDataImportField = new FileUploadField("respondentDataImportField");
		Form respondentDataImportForm = new Form("respondentDataImportForm") {
			public void onSubmit() {
				// TODO: import respondent data
			}
		};
		respondentDataImportForm.setMultiPart(true);
		respondentDataImportForm.add(respondentDataImportField);
		respondentDataImportForm.setMaxSize(Bytes.megabytes(100));
		return respondentDataImportForm;
	}
	
	private Button buildStudyExportButton() {
		return new Button("studyExport") {
			public void onSubmit() {
				downloadText(
						getStudy().getName()+".study", // changed .xml -> .study to prefer save rather than open.
						"application/octet-stream", // "text/xml", changed to prefer save rather than open.
						Archiving.getStudyXML(getStudy()));
			}
		};
	}
	
	private Button buildRespondentDataExportButton() {
		return new Button("respondentDataExport") {
			public void onSubmit() {
				downloadText(
						getStudy().getName()+".study", // changed .xml -> .study to prefer save rather than open.
						"application/octet-stream", // "text/xml", changed to prefer save rather than open.
						Archiving.getRespondentDataXML(getStudy()));
			}
		};
	}
}
