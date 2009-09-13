package net.sf.egonet.web.page;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Alters;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Studies;

public class InterviewingAlterPromptPage extends EgonetPage {

	private Interview interview;
	private TextField addAlterField;
	
	public InterviewingAlterPromptPage(Long interviewId) {
		super(Studies.getStudyForInterview(interviewId).getName()+ " - Interviewing "
				+Interviews.getEgoNameForInterview(interviewId)
				+" (respondent #"+interviewId+")");
		this.interview = Interviews.getInterview(interviewId);
		build();
	}
	
	public List<Alter> getAlters() {
		return Alters.getForInterview(interview.getId());
	}
	
	public Integer getCurrentAlters() {
		return getAlters().size();
	}
	
	public Study getStudy() {
		return Studies.getStudy(interview.getStudyId());
	}
	
	private void build() {
		final Study study = getStudy();
		
		add(new Label("alterPrompt",study.getAlterPrompt())); 
		Form form = new Form("form") {
			public void onSubmit() {
				if(study.getMaxAlters() == null || getCurrentAlters() < study.getMaxAlters()) {
					DB.save(new Alter(interview,addAlterField.getModelObjectAsString()));
				}
			}
		};

		addAlterField = new TextField("addAlterField", new Model(""));
		addAlterField.setRequired(true);
		form.add(addAlterField);
		
		add(form);
		
		add(new Label("currentAlters", new PropertyModel(this,"currentAlters")));
		final Integer minAlters = study.getMinAlters();
		add(new Label("minAlters", minAlters == null ? "-" : minAlters+""));
		Integer maxAlters = study.getMaxAlters();
		add(new Label("maxAlters", maxAlters == null ? "-" : maxAlters+""));
		
		Form nextQuestionForm = new Form("nextQuestionForm");
		nextQuestionForm.add(new Button("nextQuestionButton") {
			public void onSubmit() {
				if(minAlters == null || ! (getCurrentAlters() < minAlters)) {
					setResponsePage(askNextUnanswered(interview.getId()));
				}
			}
		});
		add(nextQuestionForm);
		
		ListView alters = new ListView("alters", new PropertyModel(this,"alters"))
        {
			protected void populateItem(ListItem item) {
				final Alter alter = (Alter) item.getModelObject();

				item.add(new Label("alterName", alter.getName()));

				Link deleteLink = new Link("alterDelete")
                {
					public void onClick() {
						// DB.delete(alter); // TODO
					}
				};
				item.add(deleteLink);
			}
		};
		add(alters);
		
	}

	public static EgonetPage askNextUnanswered(Long interviewId) {
		Study study = Studies.getStudyForInterview(interviewId);
		Integer alters = Alters.getForInterview(interviewId).size();
		Boolean altersMeetRequirements = 
			(study.getMinAlters() == null || ! (alters < study.getMinAlters())) &&
			(study.getMaxAlters() == null || ! (alters > study.getMaxAlters()));
		if(! altersMeetRequirements) {
			return new InterviewingAlterPromptPage(interviewId);
		}
		if(alters < 1 && (study.getMaxAlters() == null || study.getMaxAlters() > 0)) {
			return new InterviewingAlterPromptPage(interviewId);
		}
		return InterviewingAlterPage.askNextUnanswered(interviewId);
	}
}
