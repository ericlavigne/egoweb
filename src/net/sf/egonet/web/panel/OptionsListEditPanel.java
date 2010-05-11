package net.sf.egonet.web.panel;

import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;

import net.sf.egonet.web.component.TextField;
import net.sf.egonet.model.AnswerListMgr;
import net.sf.egonet.model.NameAndValue;

public class OptionsListEditPanel extends Panel {

	private Long studyId; 
	private Form outerForm;
	private Form titlesForm;
	private Form optionsForm;
	private Form editOptionForm;
	private WebMarkupContainer editOptionContainer;
	private ArrayList<String> listPresetTitles;
	private ArrayList<NameAndValue> listOptionNames; // for the Currently Selected preset
	private String strCurrentListTitle;
	private ListView lvPresetTitles;
	private ListView lvPresetOptionNames;
	private Label lblTitle;
	private String newTitle;
	private String newOptionName;
	private Integer newOptionValue;
	private String editOptionName;
	private String editOptionOldName;
	private Integer editOptionValue;
	private Integer editOptionOldValue;
	private TextField textNewTitle;
	private TextField textNewOptionName;
	private TextField textNewOptionValue;
	private TextField textEditOptionName;
	private TextField textEditOptionValue;
	
	
	/**
	 * constructor
	 * @param id string for wicket indentification
	 * @param studyId the study we are dealing with, each study will have its
	 * own set of sets of preset options
	 */
	public OptionsListEditPanel ( String id, Long studyId) {
		super(id);

		this.studyId = studyId;
		AnswerListMgr.loadAnswerListsForStudy(this.studyId);
		listPresetTitles = AnswerListMgr.getTitlesAsList(studyId);
		if ( !listPresetTitles.isEmpty() ) {
			strCurrentListTitle = listPresetTitles.get(0);
			setListOptionNames( AnswerListMgr.getOptionNamesAsList(strCurrentListTitle, studyId));
		}
		setNewTitle("(new title)");
		setNewOptionName("(new name)");
		setNewOptionValue(0);		
		build();
	}
	
	/**
	 * constructs the java objects and binds them to the associated
	 * HTML file using the wicket ID strings
	 */
	private void build() {
		outerForm = new Form("outerForm");
		titlesForm = new Form("titlesForm");
		optionsForm = new Form("optionsForm");
		editOptionForm = new Form("editOptionForm");
		titlesForm.setOutputMarkupId(true);
		optionsForm.setOutputMarkupId(true);
		editOptionForm.setOutputMarkupId(true);
		this.setOutputMarkupId(true);
		
		//===========================================================
		// Construct the form that fills the table on the left,
		// that will have the
		// names of the preset options lists
		lvPresetTitles = new ListView ("presetTitlesList", new PropertyModel(this,"listPresetTitles")) 		
	       {
			protected void populateItem(final ListItem item) {
				final String strPresetName = item.getModelObjectAsString();
				// add the link that will pull values for this list
				// up on the right side
				Link presetTitleLink = new AjaxFallbackLink("presetTitleLink")
				{
					public void onClick(AjaxRequestTarget target) {
						setStrCurrentListTitle(strPresetName);
						setListOptionNames(AnswerListMgr.getOptionNamesAsList(strCurrentListTitle, studyId));
						target.addComponent(titlesForm);
						target.addComponent(optionsForm);
						editOptionForm.setVisible(false);
					}
				};
				presetTitleLink.add(new Label("presetTitle", strPresetName));
				item.add(presetTitleLink);
				// add the link that will delete this list
				Link deleteTitleLink = new AjaxFallbackLink("deleteTitleLink")
				{
					public void onClick(AjaxRequestTarget target) {
						if (deleteTitle(strPresetName)) {
						    target.addComponent(titlesForm);
						    target.addComponent(optionsForm);
						}
						editOptionForm.setVisible(false);
					}
				};
				deleteTitleLink.add(new Label("deleteTitle", "delete"));
				item.add(deleteTitleLink);
			};
		};
		lvPresetTitles.setOutputMarkupId(true);
		titlesForm.add(lvPresetTitles);
		
		// add the text field users can enter the name of a new group
		textNewTitle = new TextField("newTitleEntry", new PropertyModel(this, "newTitle"), String.class);
		textNewTitle.setOutputMarkupId(true);
		titlesForm.add(textNewTitle); 
		
		// add the button to add a new title - a new list
		AjaxFallbackButton btnAddTitle = new AjaxFallbackButton ("btnAddTitle",titlesForm) 
		{
			protected void onSubmit ( AjaxRequestTarget target, Form f) {
				if ( addNewTitle()) {
					target.addComponent(titlesForm);	
					target.addComponent(optionsForm);
				}
				editOptionForm.setVisible(false);
			}
		};
		titlesForm.add(btnAddTitle);
		
		// =======================================================================
		// now create the middle form that will have a table listing all the
		// option names for the selected group of presets
		// the 'title' over the middle
		lblTitle = new Label ("valuesTitle", new PropertyModel(this, "strCurrentListTitle"));
		lblTitle.setOutputMarkupId(true);
		optionsForm.add(lblTitle);
		
		
		// construct the table on the right, that will have the
		// values available for the currently selected preset list			
		lvPresetOptionNames = new ListView ("presetNamesList", new PropertyModel(this, "listOptionNames"))
			{
			protected void populateItem(final ListItem item) {
				final NameAndValue nameAndValue = (NameAndValue)(item.getModelObject());
				final String strName = nameAndValue.getName();
				final String strValue = nameAndValue.getValue().toString();
				
				item.add(new Label("presetName", strName));
				item.add(new Label("presetValue", strValue));
			    // add the link that will delete this value
			    Link deleteValueLink = new AjaxFallbackLink("deleteValueLink")
			    {
				    public void onClick(AjaxRequestTarget target) {
					    if (deleteValue(strName,strValue))
					         target.addComponent(optionsForm);
					    editOptionForm.setVisible(false);
				    }
			     };
			    deleteValueLink.add(new Label("deleteValue", "delete"));
			    item.add(deleteValueLink);
	            // add the link that will move this value up one
			    // this will be used to reorganize value lists
			    Link moveValueLink = new AjaxFallbackLink ("moveUpLink")
			    {
			    	public void onClick(AjaxRequestTarget target) {
			    		if (moveUpValue(strName, strValue))
			    			target.addComponent(optionsForm);
			    		editOptionForm.setVisible(false);
			    	}
			    };
			    moveValueLink.add(new Label("moveUp", "Move Up"));
			    item.add(moveValueLink);
			    // this link will be used for editing, 
			    // it will make the edit form on the far right visible
			    Link editOptionLink = new AjaxFallbackLink("editOptionLink")
			    {
			    	public void onClick(AjaxRequestTarget target) {
			    		editOptionForm.setVisible(true);
			    		beginEdit(strName, strValue);
			    		target.addComponent(editOptionForm);
			    		target.addComponent(editOptionContainer);
			    	}
			    };
			    editOptionLink.add(new Label("editOption","Edit"));
			    item.add(editOptionLink);
			};
		};
		lvPresetOptionNames.setOutputMarkupId(true);
		optionsForm.add(lvPresetOptionNames);

		// now add the text edit field were
		// users can enter a new option name
		textNewOptionName = new TextField("textNewOptionName", new PropertyModel(this, "newOptionName"), String.class); 
		textNewOptionName.setOutputMarkupId(true);
		optionsForm.add(textNewOptionName);

		textNewOptionValue = new TextField("textNewOptionValue", new PropertyModel(this, "newOptionValue"), Integer.class); 
		textNewOptionValue.setOutputMarkupId(true);
		optionsForm.add(textNewOptionValue);		
		
		// and the button that will take care of adding the
		// name entered in textNewOptionName to the current list
		AjaxFallbackButton btnAddOptionName = new AjaxFallbackButton("newValueButton",optionsForm)
		{
			protected void onSubmit ( AjaxRequestTarget target, Form f) {
				if ( addNewValue())
					target.addComponent(optionsForm);
				editOptionForm.setVisible(false);
			}
		};
		optionsForm.add(btnAddOptionName);
		
		// ===================================================================
		// construct the form on the far right side which will only
		// be visible when the user selects to edit an option name
		// the text edit box to edit an existing name
		editOptionContainer = new WebMarkupContainer("editOptionContainer");
		editOptionContainer.setOutputMarkupId(true);
		textEditOptionName = new TextField("editOptionName", new PropertyModel(this, "editOptionName"), String.class); 
		textEditOptionName.setOutputMarkupId(true);
		editOptionForm.add(textEditOptionName);
		
		textEditOptionValue = new TextField("editOptionValue", new PropertyModel(this, "editOptionValue"), Integer.class); 
		textEditOptionValue.setOutputMarkupId(true);
		editOptionForm.add(textEditOptionValue);

		// and the button that will take care of adding the
		// name entered in textNewOptionName to the current list
		AjaxFallbackButton btnOkayEdit = new AjaxFallbackButton("btnOkayEdit",editOptionForm)
		{
			protected void onSubmit ( AjaxRequestTarget target, Form f) {
				endEdit(true);
				editOptionForm.setVisible(false);
				target.addComponent(optionsForm);
				target.addComponent(editOptionForm);
				target.addComponent(editOptionContainer);
			}
		};
		btnOkayEdit.setOutputMarkupId(true);
		editOptionForm.add(btnOkayEdit);
		
		// and the button that will take care of adding the
		// name entered in textNewOptionName to the current list
		AjaxFallbackButton btnCancelEdit = new AjaxFallbackButton("btnCancelEdit",editOptionForm)
		{
			protected void onSubmit ( AjaxRequestTarget target, Form f) {
				endEdit(false);
				editOptionForm.setVisible(false);
				target.addComponent(optionsForm);	
				target.addComponent(editOptionForm);
				target.addComponent(editOptionContainer);
			}
		};
		btnCancelEdit.setOutputMarkupId(true);
		editOptionForm.add(btnCancelEdit);
		editOptionContainer.add(editOptionForm);
		editOptionForm.setVisible(false);
		// add the Save button below the table
		AjaxFallbackButton btnSave = new AjaxFallbackButton("saveButton",outerForm)
		{
			protected void onSubmit ( AjaxRequestTarget target, Form f) {
				saveAllEdits();
			}
		};
		btnSave.setOutputMarkupId(true);
		// add all three forms to the page
		outerForm.add(titlesForm);
		outerForm.add(optionsForm);
		outerForm.add(editOptionContainer);
		outerForm.add(btnSave);
		add(outerForm);
	}
	
	/**
	 * reacts to 'add' in the title table getting clicked
	 * to create a new list of words
	 * @return true if newTitleEntry text box contains a non-empty
	 * string that is not already in the list of titles
	 */
	private boolean addNewTitle() {
	
		if ( newTitle==null )
			return(false);
		newTitle = newTitle.trim();
		if ( newTitle.length()==0)
			return(false);
		if ( !AnswerListMgr.addTitle(newTitle, studyId)) {
			setNewTitle("");
			return(false);
		}
		setStrCurrentListTitle(newTitle);
		listPresetTitles = AnswerListMgr.getTitlesAsList(studyId);
		setListOptionNames(AnswerListMgr.getOptionNamesAsList(strCurrentListTitle, studyId));
		setNewTitle("");	
		return(true);
	}
	
	/**
	 * deletes the list of presets specified by the name strTitle
	 * @param strTitle the name of the list to delete
	 * @return true if deletion takes place false otherwise
	 */
	private boolean deleteTitle(String strTitle) {
		int index;
		
		if ( strTitle==null || strTitle.length()==0)
			return(false);
		
		index = AnswerListMgr.removeTitle(strTitle, studyId);
		if ( index<0 )
			return(false);
		
		listPresetTitles = AnswerListMgr.getTitlesAsList(studyId);
		// if the list we deleted is the currently display one
		// need to do extra work to display a different one...
		if (strCurrentListTitle.equals(strTitle)) {
			// System.out.println ( "strCurrentListTitle=" + strCurrentListTitle);
			// System.out.println ( "strTitle=" + strTitle);
			if (index<0 || index>listPresetTitles.size()) {
				index = listPresetTitles.size()-1;
			}
			if ( listPresetTitles.size()==0 ) {
				setStrCurrentListTitle("");
				setListOptionNames(new ArrayList<NameAndValue>(0));
			} else {
				setStrCurrentListTitle( listPresetTitles.get(index));
				setListOptionNames(AnswerListMgr.getOptionNamesAsList(strCurrentListTitle, studyId));				
			}
		}
		return(true);
	}
	
	/**
	 * reacts to 'add' in the values table getting clicked
	 * to add a word to the current list of words
	 * @return true if newValueEntry text box contains a non-empty
	 * string that is not already in the list of values for the
	 * current list
	 */
	private boolean addNewValue() {
		
		if ( newOptionName==null )
			return(false);
		newOptionName = newOptionName.trim();
		if ( newOptionName.length()==0)
			return(false);
		
		if ( newOptionValue==null )
			newOptionValue = new Integer(0);
		
		if ( !AnswerListMgr.addOptionName(strCurrentListTitle, studyId, newOptionName, newOptionValue)) {
			setNewOptionName("");
			return(false);
		}
		setListOptionNames(AnswerListMgr.getOptionNamesAsList(strCurrentListTitle, studyId));
		setNewOptionName("");
		return(true);
	}
	

	/**
	 * deletes a value from the list of presets currently dealt with
	 * @param strValue the value to delete
	 * @return true if deletion takes place, false otherwise
	 */
	private boolean deleteValue(String strName, String strValue) {
		Integer value;
		
		if ( strName==null || strValue==null || strName.length()==0 || strValue.length()==0 )
			return(false);
		try {
			value = Integer.parseInt(strValue);
		} catch ( NumberFormatException nfe) {
			System.out.println ( "OptionsListEditPanel.deleteValue " + strValue + " invalid integer");
			value = 0;
		}
		if ( !AnswerListMgr.removeOptionName(strCurrentListTitle, studyId, strName, value))
			return(false);
		setListOptionNames(AnswerListMgr.getOptionNamesAsList(strCurrentListTitle, studyId));			
		return(true);
	}
	
	/**
	 * responds to the 'move up' link so users can rearrange
	 * lists of option values
	 * @param strValue the string values to move up in the list
	 * @return true if the string is found and moved, false otherwise
	 */
	private boolean moveUpValue(String strName, String strValue) {
		Integer value;
		
		if ( strName==null || strValue==null || strName.length()==0 || strValue.length()==0 )
			return(false);
		try {
			value = Integer.parseInt(strValue);
		} catch ( NumberFormatException nfe) {
			System.out.println ( "OptionsListEditPanel.moveUpValue " + strValue + " invalid integer");
			value = 0;
		}
		if ( !AnswerListMgr.moveOptionNameUp(strCurrentListTitle, studyId, strName, value))
			return(false);
		setListOptionNames(AnswerListMgr.getOptionNamesAsList(strCurrentListTitle, studyId));			
		return(true);
	}
	
	/**
	 * responds to an edit link for an option name getting clicked,
	 * brings the option name into the edit field as the edit panel
	 * is made visible
	 * @param strValue the option Name to edit
	 * @return true
	 */
	private boolean beginEdit(String strName, String strValue) {
		editOptionOldName = strName;
		try {
			editOptionOldValue = Integer.parseInt(strValue);
		} catch ( NumberFormatException nfe ) {
			editOptionOldValue = 0;
		}
		setEditOptionName(strName);
		setEditOptionValue(editOptionOldValue);
		return(true);
	} 
	
	/**
	 * called when the edit panel is closed
	 * @param save if true we want to use the new string value, 
	 *  if false discard.
	 *  as this function is called the editOptionPanel will 
	 *  be made invisible again.
	 * @return true
	 */
	private boolean endEdit(boolean save) {
		if ( save ) {
			if ( AnswerListMgr.replaceOptionName(strCurrentListTitle, studyId, 
					editOptionOldName, editOptionOldValue, editOptionName, editOptionValue ))
			    setListOptionNames(AnswerListMgr.getOptionNamesAsList(strCurrentListTitle, studyId));
		}
		return(true);
	}
	
	 
	/**
	 * save all the changes performed on this page
	 * @return true
	 */
	private boolean saveAllEdits() {
		boolean bRetVal;
		
		bRetVal = AnswerListMgr.saveAllAnswerListsForStudy(studyId);
		return(bRetVal);
	}
	
	/**
	 * getters/setters
	 */
	public void setStrCurrentListTitle( String strCurrentListTitle ) {
		this.strCurrentListTitle = strCurrentListTitle;
	}
	public String getStrCurrentListTitle() {
		return(strCurrentListTitle);
	}
	
	public void setEditOptionName ( String editOptionName ) {
		this.editOptionName = editOptionName;
	}
	public String getEditOptionName() {
		return(editOptionName);
	}
	
	public void setEditOptionValue ( Integer editOptionValue ) {
		this.editOptionValue = editOptionValue;
	}
	public Integer getEditOptionValue() {
		return(editOptionValue);
	}
	
	public void setListOptionNames ( ArrayList<NameAndValue> listOptionNames ) {
		this.listOptionNames = listOptionNames;
	}
	public ArrayList<NameAndValue> getListOptionNames() { 
		return(listOptionNames);
	}
	
	public void setNewTitle ( String newTitle ) {
		this.newTitle = newTitle;
	}
	public String getNewTitle() {
		return(newTitle);
	}
	
	public void setNewOptionName ( String newOptionName ) {
		this.newOptionName = (newOptionName==null) ? "" : newOptionName;
	}
	public String getNewOptionName() {
		return(newOptionName);
	}

	public void setNewOptionValue ( Integer newOptionValue ) {
		this.newOptionValue = (newOptionValue==null) ? 0 : newOptionValue;
	}
	public Integer getNewOptionValue () {
		return(newOptionValue);
	}
}
