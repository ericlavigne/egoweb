package net.sf.egonet.web.panel;

import java.util.List;
import java.util.ArrayList;

import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.web.component.TextField;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;


public class ListLimitsPanel extends Panel {
	private Question question;
	
	private Boolean withListRange; 
	private String  listRangeString;
	private QuestionOption listRangeOption;
	private Integer minListRange;
	private Integer maxListRange;

	private CheckBox cbWithListRange;
    private DropDownChoice dropListRangeString;
	private TextField txtMinListRange;
	private TextField txtMaxListRange;
	private Form      listLimitsForm;
	private List<QuestionOption> listOfOptions;
	
	public ListLimitsPanel ( String id, Question question ) {
		super(id);
		setQuestion(question);
        build();
    }

	public void setQuestion ( Question question ) {
		Long qID;
		
		if ( question!=null ) {
			this.question = question;
			qID = question.getId();
			if (qID!=null) {
		  	    listOfOptions = Options.getOptionsForQuestion(qID);
			} else {
		    	listOfOptions = new ArrayList<QuestionOption>(1);
			}
		    setWithListRange(question.getWithListRange());
		    setListRangeString(question.getListRangeString());
		    setMinListRange(question.getMinListRange());
		    setMaxListRange(question.getMaxListRange());
		    // find the question option that matches the listRangeString
		    if ( qID != null ) {
		    	for ( QuestionOption qo : listOfOptions ) {
		    		if ( listRangeString.equalsIgnoreCase(qo.getName()))
		    			listRangeOption = qo;
		    	}
		    	if ( listRangeOption==null && !listOfOptions.isEmpty())
		    		listRangeOption = listOfOptions.get(0);
		    } // end of if qId != null
		    
		    // this next part might happen with new questions
		    // that have no options yet:
		    if ( listRangeOption==null ) {
		    	if (qID==null)
		    		qID = new Long(0);
		    	listRangeOption =  new QuestionOption(qID, "(no options yet!)");
		    	listOfOptions.add(listRangeOption);
		    }
		}
	}
	
	private void build() {
		setOutputMarkupId(true);
		setOutputMarkupPlaceholderTag(true);
        listLimitsForm = new Form("listLimitsForm");
        listLimitsForm.setOutputMarkupId(true);
        listLimitsForm.setOutputMarkupPlaceholderTag(true);
        
	    cbWithListRange = new CheckBox("withListRange", new PropertyModel(this,"withListRange"));
	    dropListRangeString = new DropDownChoice("listRangeString", 
	    		new PropertyModel(this,"listRangeOption"), listOfOptions);
	    txtMinListRange = new TextField("min", new PropertyModel(this,"minListRange"), Integer.class);
	    txtMaxListRange = new TextField("max", new PropertyModel(this,"maxListRange"), Integer.class);

	    cbWithListRange.setOutputMarkupId(true);
	    dropListRangeString.setOutputMarkupId(true);
	    txtMinListRange.setOutputMarkupId(true);
        txtMaxListRange.setOutputMarkupId(true);
        
	    cbWithListRange.setOutputMarkupPlaceholderTag(true);
	    dropListRangeString.setOutputMarkupPlaceholderTag(true);
	    txtMinListRange.setOutputMarkupPlaceholderTag(true);
        txtMaxListRange.setOutputMarkupPlaceholderTag(true);
        
	    listLimitsForm.add(cbWithListRange);
	    listLimitsForm.add(dropListRangeString);
	    listLimitsForm.add(txtMinListRange);
        listLimitsForm.add(txtMaxListRange);
	    add(listLimitsForm);
	}
		
	/**
	 * setters / getters
	 */
	public void setWithListRange( Boolean withListRange ) {
		this.withListRange = (withListRange==null) ? new Boolean(false) : withListRange;
	}
	public Boolean getWithListRange() {
		return(withListRange);
	}
	
	public void setListRangeString( String listRangeString ) {
		this.listRangeString = (listRangeString==null) ? new String("") : listRangeString;
	}
	public String getListRangeString() {
		listRangeString = listRangeOption.getName();
		return(listRangeString);
	}
	
	public void setListRangeOption( QuestionOption listRangeOption ) {
		this.listRangeOption = listRangeOption;
	}
	public QuestionOption getListRangeOption() {
		return(listRangeOption);
	}
	
	public void setMinListRange( Integer minListRange ) {
		this.minListRange = (minListRange==null) ? new Integer(0) : minListRange;
	}
	public Integer getMinListRange() {
		return(minListRange);
	}
	
	public void setMaxListRange ( Integer maxListRange ) {
	    this.maxListRange = (maxListRange==null) ? new Integer(100) : maxListRange;
	}
	public Integer getMaxListRange() {
		return(maxListRange);
	}	
}
