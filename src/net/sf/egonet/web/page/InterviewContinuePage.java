package net.sf.egonet.web.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;

import net.sf.egonet.model.Study;
import net.sf.functionalj.tuple.Pair;
import net.sf.egonet.model.Interview;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.panel.InterviewNavigationPanel.InterviewLink;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;


public class InterviewContinuePage extends EgonetPage {

	private Model message;
	private Long studyId;
	private String studyName;
	private PropertyModel studyNameModel;
	private Label lblCurrentStudy;
	private List<Interview> interviews;
	private ListView interviewView;
	private Form form;
	
	public InterviewContinuePage() {
		super("Continue an Interview");
		build();
	}
	
	private void build() {	
		form = new Form("form");
		form.setOutputMarkupId(true);
		add(form);
		
		studyId = null;
		studyName = null;
	
		ListView lvStudyNames = new ListView ("studies", new PropertyModel(this,"studies")) 		
	       {
			protected void populateItem(final ListItem item) {
				final Study study = (Study)item.getModelObject();
				if ( studyName==null ) {
					studyId = study.getId();
				    setStudyName(study.getName());
				    setInterviews ( Interviews.getInterviewsForStudy(studyId));
				}
				Link studyLink = new AjaxFallbackLink("studyLink", new Model(study.getId()))
				{
					public void onClick(AjaxRequestTarget target) {
						studyId = (Long)getModelObject();
						Study study = Studies.getStudy(studyId);
						setStudyName(study.getName());
						setInterviews ( Interviews.getInterviewsForStudy(studyId));
						target.addComponent(form);
					}
				};
				studyLink.add(new Label("studyName", study.getName()));
				item.add(studyLink);
			}
		};
		form.add(lvStudyNames);
		studyNameModel = new PropertyModel(this, "studyName");
		
		lblCurrentStudy = new Label("currentStudyName", studyNameModel);
		lblCurrentStudy.setOutputMarkupId(true);
		form.add(lblCurrentStudy);
		message = new Model("");
		add(new Label("message", message));
		
		// now create the list of interviews on the right side
	    interviewView = new ListView("interviews", new PropertyModel(this, "interviews"))
	        {
				protected void populateItem(ListItem item)
	            {
					final Interview interview = (Interview) item.getModelObject();
					Link interviewLink;
					Long id;
					String egoName;
					ArrayList<InterviewLink> links;
					
					id = interview.getId();
					egoName = Interviews.getEgoNameForInterview(id);
					
					interviewLink = new Link("interviewLink", new Model(id))
	                {
						public void onClick()
	                   	{
							Long id = (Long)getModelObject();
							EgonetPage comeFrom = InterviewingEgoPage.askNext(id, null, null);
							setResponsePage(
									InterviewingEgoPage.askNextUnanswered(id,null,comeFrom));
	                   	}
					};

					interviewLink.add(new Label("name", egoName));
					item.add(interviewLink);
					links = Lists.newArrayList(Interviewing.getAnsweredPagesForInterview(interview.getId()));
					if ( links.isEmpty())
					    item.add(new Label("lastQuestion", "(none)"));
					else
						item.add(new Label("lastQuestion", links.get(links.size()-1).toString()));
				}
			};
			interviewView.setOutputMarkupId(true);
			form.add(interviewView);
	}
	
	//======================================================================
	// functions below are specific to the list of existing interviews
	// =====================================================================
	
	public void setStudyName ( String studyName) {
		this.studyName = (studyName==null) ? "" : studyName;
	}
	public String getStudyName() {
		if ( studyName==null)
			studyName = "";
		return(studyName);
	}
	
	protected Page onInterviewClick(Interview interview) {
		return null;
	}
	
	protected Pair<Class<?>,PageParameters> getInterviewBookmark(Interview interview) {
		return null;
	}

	public List<Study> getStudies() {
		return ( Studies.getStudies());
	}

	public void setInterviews( List<Interview> interviews ) {
		this.interviews = interviews;
	}
	public List<Interview> getInterviews() {
	    return(interviews);
	}	
}
