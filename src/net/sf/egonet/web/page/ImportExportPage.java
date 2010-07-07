package net.sf.egonet.web.page;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Archiving;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.component.TextField;
import net.sf.egonet.web.panel.InterviewNavigationPanel.InterviewLink;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;

import com.google.common.collect.Lists;

public class ImportExportPage extends EgonetPage {
	
	private ArrayList<Study> studies;
	private List<CheckIncludeID> checkIncludeIDList;
	private Long studyId = null;
	private Form selectCasesForm;
	
	public ImportExportPage() {

		studies = new ArrayList<Study>(Studies.getStudies());
		
		add(buildStudyImportForm());
		add(buildStudyModifyForm());
		add(buildRespondentDataImportForm());
		add(buildExportForm());
		add(buildExportOSForm());
		selectCasesForm = buildSelectCasesForm();
		add(selectCasesForm);
	}
	
	private DropDownChoice createStudyDropdown(String wicketId) {
		DropDownChoice dropdown = 
			new DropDownChoice(wicketId,
					studies.size() == 1 ? new Model(studies.get(0)) : new Model(),
					studies);
		dropdown.setRequired(true);
		dropdown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
		    protected void onUpdate(AjaxRequestTarget target)
		    	{
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(false);
		    	}
		    });
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
				selectCasesForm.setVisible(false);
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
		studyImportForm.add( new AjaxFormSubmitBehavior(studyImportForm,"onsubmit") {
			protected void onSubmit(AjaxRequestTarget target) {
				// System.out.println ( "ImportExportPage onSubmit");
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(false);
			}
			protected void onError(AjaxRequestTarget target) {
				// System.out.println ( "ImportExportPage onError");
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(false);
			}
		});
		return studyImportForm;
	}
	
	private Form buildStudyModifyForm() {

		add(new FeedbackPanel("feedback"));
		
		final DropDownChoice studyToModify = createStudyDropdown("studyToModify");
		
		final FileUploadField studyImportField = createFileUpload("studyModifyField");
		Form studyImportForm = new Form("studyModifyForm") {
			public void onSubmit() {
				selectCasesForm.setVisible(false);
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
		studyImportForm.add( new AjaxFormSubmitBehavior(studyImportForm,"onsubmit") {
			protected void onSubmit(AjaxRequestTarget target) {
				// System.out.println ( "ImportExportPage onSubmit");
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(false);
			}
			protected void onError(AjaxRequestTarget target) {
				// System.out.println ( "ImportExportPage onError");
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(false);
			}
		});
		return studyImportForm;
	}
	
	private Form buildRespondentDataImportForm() {

		final DropDownChoice studyToPopulate = createStudyDropdown("studyToPopulate");
		final FileUploadField respondentDataImportField = createFileUpload("respondentDataImportField");
		
		Form respondentDataImportForm = new Form("respondentDataImportForm") {
			public void onSubmit() {
				selectCasesForm.setVisible(false);
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
		respondentDataImportForm.add( new AjaxFormSubmitBehavior(respondentDataImportForm,"onsubmit") {
			protected void onSubmit(AjaxRequestTarget target) {
				// System.out.println ( "ImportExportPage onSubmit");
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(false);
			}
			protected void onError(AjaxRequestTarget target) {
				// System.out.println ( "ImportExportPage onError");
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(false);
			}
		});
		return respondentDataImportForm;
	}
	
	private DropDownChoice exportOSDropdown;
	
	private Form buildExportOSForm() {
		Form exportOSForm = new Form("exportOSForm");
		exportOSDropdown = createStudyDropdown("studyToOSExport");
		exportOSForm.add(exportOSDropdown);
		exportOSForm.add(buildStudyOSExportButton(exportOSForm));
		exportOSForm.add( new AjaxFormSubmitBehavior(exportOSForm,"onsubmit") {
			protected void onSubmit(AjaxRequestTarget target) {
				// System.out.println ( "ImportExportPage onSubmit");
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(false);
			}
			protected void onError(AjaxRequestTarget target) {
				// System.out.println ( "ImportExportPage onError");
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(false);
			}
		});		
		return exportOSForm;
	} 
	
	private AjaxFallbackButton buildStudyOSExportButton(Form form) {
		return new AjaxFallbackButton("studyOSExport", form) {
			public void onSubmit(AjaxRequestTarget target, Form f) {
				Study study = getStudy(exportOSDropdown);
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(false);
				if ( study==null )
					return;
				downloadText(
						study.getName()+"OS.txt",
						"application/octet-stream",
						Archiving.getOtherSpecifyReport(study));
			}
		};
	}
	private DropDownChoice exportDropdown;
	
	private Form buildExportForm() {
		Form exportForm = new Form("exportForm");
		exportDropdown = createStudyDropdown("studyToExport");
		exportForm.add(exportDropdown);
		exportForm.add(buildStudyExportButton());
		exportForm.add(buildRespondentDataExportButton());
		exportForm.add(buildSelectCasesButton(exportForm));
		return exportForm;
	}
	
	private AjaxFallbackButton buildSelectCasesButton(Form form) {
		return new AjaxFallbackButton("selectCases",form) {
			protected void onSubmit(AjaxRequestTarget target, Form f) {
				Study study = getStudy(exportDropdown);
				studyId = study.getId();
				target.addComponent(selectCasesForm);
				selectCasesForm.setVisible(true);
			}
		};
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
						Archiving.getRespondentDataXML(study,checkIncludeIDList));
			}
		};
	}

	
	private Form buildSelectCasesForm() {
		selectCasesForm = new Form("selectCasesForm");
		selectCasesForm.setOutputMarkupId(true);
		selectCasesForm.setOutputMarkupPlaceholderTag(true);
		checkIncludeIDList = Lists.newArrayList();
		
		selectCasesForm.add(new ListView("interviews", new PropertyModel(this, "interviews")) {
			protected void populateItem(ListItem item) {
				final Interview interview = (Interview) item.getModelObject();
				ArrayList<InterviewLink> links;
				
				item.add(new Label("interviewName",
						Interviews.getEgoNameForInterview(interview.getId())));

				item.add(new Label("interviewActive", (interview.getCompleted()?"Complete":"Incomplete")));
				CheckIncludeID checkIncludeID = new CheckIncludeID(interview.getId(),interview.getCompleted());
				checkIncludeIDList.add(checkIncludeID);
				item.add(new CheckBox("interviewInclude", new PropertyModel(checkIncludeID,"selected")));
				links = Lists.newArrayList(Interviewing.getAnsweredPagesForInterview(interview.getId()));
				if ( links.isEmpty())
				    item.add(new Label("lastQuestion", "(none)"));
				else
					item.add(new Label("lastQuestion", links.get(links.size()-1).toString()));			
			}});
		
		selectCasesForm.setVisible(false);
		return selectCasesForm;
	}
	
	public List<Interview> getInterviews() {
		if ( studyId==null )
			return ( (List<Interview>)(null));
		return Interviews.getInterviewsForStudy(studyId);
	}
}
