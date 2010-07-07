package net.sf.egonet.web.panel;

import java.util.List;
import java.util.ArrayList;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.web.component.TextField;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
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
	
	private Boolean noneButton;
	private Boolean allButton;  
	private Boolean pageLevelDontKnowButton;
	private Boolean pageLevelRefuseButton;  
	
	private CheckBox cbNone;
	private CheckBox cbAll;
	private CheckBox cbDontKnow;
	private CheckBox cbRefuse;
    
    /**
     * standard constructor
     * @param id - wicket id
     * @param question question this panel deals with
     */
	public ListLimitsPanel ( String id, Question question ) {
		super(id);
		setQuestion(question);
        build();
	    setAnswerType(question.getAnswerType());
    }

	/**
	 * loads the pertinent data from the question, fills appropriate GUI widgets
	 * @param question the question this panel deals with, the 
	 * one currently being editted
	 */
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
		    
		    setNoneButton(question.getNoneButton());
		    setAllButton(question.getAllButton());
		    setPageLevelDontKnowButton(question.getPageLevelDontKnowButton());
		    setPageLevelRefuseButton(question.getPageLevelRefuseButton());
		    
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
		    	listRangeOption =  new QuestionOption(qID, "(none)");
		    	listOfOptions.add(listRangeOption);
		    }
		}
	}
	
	/**
	 * constructs all the wicket GUI widgets
	 */
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
        
        cbNone = new CheckBox("none", new PropertyModel(this,"noneButton"));
    	cbAll  = new CheckBox("all",  new PropertyModel(this,"allButton"));
    	cbDontKnow = new CheckBox("dontknow", new PropertyModel(this,"pageLevelDontKnowButton"));
    	cbRefuse   = new CheckBox("refuse",   new PropertyModel(this,"pageLevelRefuseButton"));
    	
    	cbNone.setOutputMarkupId(true);
    	cbAll.setOutputMarkupId(true);
    	cbDontKnow.setOutputMarkupId(true);
    	cbRefuse.setOutputMarkupId(true);
    	
    	cbNone.setOutputMarkupPlaceholderTag(true);
    	cbAll.setOutputMarkupPlaceholderTag(true);
    	cbDontKnow.setOutputMarkupPlaceholderTag(true);
    	cbRefuse.setOutputMarkupPlaceholderTag(true);
    	
    	listLimitsForm.add(cbNone);
    	listLimitsForm.add(cbAll);
    	listLimitsForm.add(cbDontKnow);
    	listLimitsForm.add(cbRefuse);
    	
	    add(listLimitsForm);
	}
		
	public void setAnswerType ( Answer.AnswerType aType ) {
	
		if ( aType==null )
			aType = Answer.AnswerType.MULTIPLE_SELECTION;
		
		switch ( aType ) {
		    case MULTIPLE_SELECTION:
		    	 cbWithListRange.setEnabled(true);
		         dropListRangeString.setEnabled(true);
		    	 txtMinListRange.setEnabled(true);
		    	 txtMaxListRange.setEnabled(true);
		    	 cbNone.setEnabled(true);
		         cbAll.setEnabled(true);
		         break;
		    default:
		    	 cbWithListRange.setEnabled(false);
	         	 dropListRangeString.setEnabled(false);
	         	 txtMinListRange.setEnabled(false);
	         	 txtMaxListRange.setEnabled(false);
	         	 cbNone.setEnabled(false);
	         	 cbAll.setEnabled(false);
	         	 break;		    	
		}
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
	
	public void setNoneButton ( Boolean noneButton) {
		this.noneButton = (noneButton==null) ? false : noneButton;
	}
	public Boolean getNoneButton() {
		if ( noneButton==null)
			noneButton = false;
		return(noneButton);
	}
	
	public void setAllButton ( Boolean allButton) {
		this.allButton = (allButton==null) ? false : allButton;
	}
	public Boolean getAllButton() {
		if ( allButton==null)
			allButton = false;
		return(allButton);
	}
	
	public void setPageLevelDontKnowButton ( Boolean pageLevelDontKnowButton ) {
		this.pageLevelDontKnowButton  = (pageLevelDontKnowButton ==null) ? false : pageLevelDontKnowButton;
	}
	public Boolean getPageLevelDontKnowButton() {
		if ( pageLevelDontKnowButton ==null)
			pageLevelDontKnowButton  = false;
		return(pageLevelDontKnowButton );
	}
	
	public void setPageLevelRefuseButton( Boolean pageLevelRefuseButton ) {
		this.pageLevelRefuseButton = (pageLevelRefuseButton==null) ? false : pageLevelRefuseButton; 
	}
	public Boolean getPageLevelRefuseButton() {
		if ( pageLevelRefuseButton==null)
			pageLevelRefuseButton = false;
		return(pageLevelRefuseButton);
	}	
}
