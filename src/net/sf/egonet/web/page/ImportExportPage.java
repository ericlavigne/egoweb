package net.sf.egonet.web.page;

import java.util.ArrayList;

import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Archiving;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.component.TextField;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;

public class ImportExportPage extends EgonetPage {
	
	private ArrayList<Study> studies;
	
	public ImportExportPage() {

		studies = new ArrayList<Study>(Studies.getStudies());
		
		add(buildStudyImportForm());
		add(buildStudyModifyForm());
		add(buildRespondentDataImportForm());
		add(buildExportForm());
	}
	
	private DropDownChoice createStudyDropdown(String wicketId) {
		DropDownChoice dropdown = 
			new DropDownChoice(wicketId,
					studies.size() == 1 ? new Model(studies.get(0)) : new Model(),
					studies);
		dropdown.setRequired(true);
		return dropdown;
	}
	
	private FileUploadField createFileUpload(String wicketId) {
		FileUploadField field = new FileUploadField(wicketId);
		field.setRequired(true);
		return field;
	}
	
	private Study getStudy(DropDownChoice studyDropdown) {
		return (Study) studyDropdown.getModelObject();
	}

	private Form buildStudyImportForm() {
		final TextField studyImportNameField = new TextField("studyImportNameField",new Model());
		final FileUploadField studyImportFileField = createFileUpload("studyImportFileField");
		Form studyImportForm = new Form("studyImportForm") {
			public void onSubmit() {
				try {
					String uploadText = uploadText(studyImportFileField);
					if(uploadText != null) {
						Archiving.loadStudyXML(null, uploadText,studyImportNameField.getText());
					}
					setResponsePage(new ImportExportPage());
				} catch(Exception ex) {
					throw new RuntimeException("Exception while trying to import study.",ex);
				}
			}
		};
		studyImportForm.add(studyImportNameField);
		studyImportForm.setMultiPart(true);
		studyImportForm.add(studyImportFileField);
		studyImportForm.setMaxSize(Bytes.megabytes(100));
		
		return studyImportForm;
	}
	
	private Form buildStudyModifyForm() {

		add(new FeedbackPanel("feedback"));
		
		final DropDownChoice studyToModify = createStudyDropdown("studyToModify");
		
		final FileUploadField studyImportField = createFileUpload("studyModifyField");
		Form studyImportForm = new Form("studyModifyForm") {
			public void onSubmit() {
				try {
					String uploadText = uploadText(studyImportField);
					Study study = getStudy(studyToModify);
					if(uploadText != null && study != null) {
						Archiving.loadStudyXML(study, uploadText,null);
						setResponsePage(new ImportExportPage());
					}
					if(uploadText == null) {
						throw new RuntimeException("Need to specify a study settings file.");
					}
					if(study == null) {
						throw new RuntimeException("Need to specify a study.");
					}
				} catch(Exception ex) {
					throw new RuntimeException("Exception while trying to import study.",ex);
				}
			}
		};
		studyImportForm.setMultiPart(true);
		studyImportForm.add(studyToModify);
		studyImportForm.add(studyImportField);
		studyImportForm.setMaxSize(Bytes.megabytes(100));
		
		return studyImportForm;
	}
	
	private Form buildRespondentDataImportForm() {

		final DropDownChoice studyToPopulate = createStudyDropdown("studyToPopulate");
		final FileUploadField respondentDataImportField = createFileUpload("respondentDataImportField");
		
		Form respondentDataImportForm = new Form("respondentDataImportForm") {
			public void onSubmit() {
				try {
					String uploadText = uploadText(respondentDataImportField);
					Study study = getStudy(studyToPopulate);
					if(uploadText != null && study != null) {
						Archiving.loadRespondentXML(study, uploadText);
					}
				} catch(Exception ex) {
					throw new RuntimeException("Exception while trying to import respondent data.",ex);
				}
			}
		};
		respondentDataImportForm.setMultiPart(true);
		respondentDataImportForm.add(studyToPopulate);
		respondentDataImportForm.add(respondentDataImportField);
		respondentDataImportForm.setMaxSize(Bytes.megabytes(100));
		return respondentDataImportForm;
	}
	
	private DropDownChoice exportDropdown;
	
	private Form buildExportForm() {
		Form exportForm = new Form("exportForm");
		exportDropdown = createStudyDropdown("studyToExport");
		exportForm.add(exportDropdown);
		exportForm.add(buildStudyExportButton());
		exportForm.add(buildRespondentDataExportButton());
		return exportForm;
	}
	
	private Button buildStudyExportButton() {
		return new Button("studyExport") {
			public void onSubmit() {
				Study study = getStudy(exportDropdown);
				downloadText(
						study.getName()+".study",
						"application/octet-stream",
						Archiving.getStudyXML(study));
			}
		};
	}
	
	private Button buildRespondentDataExportButton() {
		return new Button("respondentDataExport") {
			public void onSubmit() {
				Study study = getStudy(exportDropdown);
				downloadText(
						study.getName()+".responses",
						"application/octet-stream",
						Archiving.getRespondentDataXML(study));
			}
		};
	}
}
