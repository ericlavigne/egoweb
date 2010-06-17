package net.sf.egonet.web.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;
import net.sf.functionalj.tuple.Pair;

public class InterviewingEgoIDPage extends EgonetPage {
	
	public ArrayList<AnswerFormFieldPanel> questions;
	private Model message;
	private ListView questionsView;
	private Long studyId;
	
	public InterviewingEgoIDPage(PageParameters parameters) {
		this(Studies.getStudy(Long.parseLong(parameters.getString("studyId"))));
	}
	
	public InterviewingEgoIDPage(Study study) {
		super(study.getName());
		this.studyId = study.getId();
		build();
	}
	
	/**
	 * third parameter to AnswerFormFieldPanel.getInstance() is the 
	 * interviewId.  In the EgoId page we're not concerned with this 
	 * and it's not used, so pass a zero
	 */
	private void build() {
		
		questions = Lists.newArrayList(Lists.transform(
			Questions.getQuestionsForStudy(studyId, QuestionType.EGO_ID),
			new Function<Question,AnswerFormFieldPanel>() {
				public AnswerFormFieldPanel apply(Question question) {
					return AnswerFormFieldPanel.getInstance("question",question, new Long(0));
				}
			}));
		
		Form form = new Form("form") 
        {
			@Override
			public void onSubmit()
            {
				boolean answeredAll = true;
				List<Answer> answers = Lists.newArrayList();
				// first, verify that any and all multiple selection questions
				// are answered fully
				boolean multipleSelectionsOkay = 
					AnswerFormFieldPanel.allRangeChecksOkay(questions);

				if ( !multipleSelectionsOkay ) {
					for(AnswerFormFieldPanel answerField : questions) {
						answerField.setNotification(answerField.getRangeCheckNotification());
					}
					return;
				}
				// if all multiple selection questions are okay, proceed
				for(AnswerFormFieldPanel question : questions) {
					if(question.answered()) {
						answers.add(new Answer(question.getQuestion(),question.getAnswer()));
						question.setNotification("");
					} else {
						answeredAll = false;
						question.setNotification("Unanswered");
					}
				}
				if(answeredAll && ! answers.isEmpty()) {
					Interview interview = 
						Interviewing.findOrCreateMatchingInterviewForStudy(studyId, answers);
				
					EgonetPage comeFrom = InterviewingEgoPage.askNext(interview.getId(), null, null);
					setResponsePage(
							InterviewingEgoPage.askNextUnanswered(interview.getId(),null,comeFrom));
				}
            }
        };
		
        form.add(new MultiLineLabel("prompt", Studies.getStudy(studyId).getEgoIdPrompt())
        		.setEscapeModelStrings(false));
        
		questionsView = new ListView("questions", questions)
        {
			protected void populateItem(ListItem item)
            {
				AnswerFormFieldPanel wrapper = (AnswerFormFieldPanel) item.getModelObject();
				if(wrapper.getQuestion().getId().equals(questions.get(0).getQuestion().getId())) {
					wrapper.setAutoFocus();
				}
				item.add(wrapper);
				item.add(new MultiLineLabel("questionPrompt", wrapper.getQuestion().getPrompt()));
            }
        };
        questionsView.setReuseItems(true);
        form.add(questionsView);

		add(form);
		
		message = new Model("");
		
		add(new Label("message", message));
		
		// now create the list of interviews on the right side
//	    ListView interviewView = new ListView("interviews", new PropertyModel(this, "interviews"))
//	        {
//				protected void populateItem(ListItem item)
//	            {
//					final Interview interview = (Interview) item.getModelObject();
//					Link interviewLink;
//					Long id;
//					String egoName;
//					
//					id = interview.getId();
//					egoName = Interviews.getEgoNameForInterview(id);
//					
//					interviewLink = new Link("interviewLink", new Model(id))
//	                {
//						public void onClick()
//	                   	{
//							Long id = (Long)getModelObject();
//							// System.out.println ( "     id=" + id);
//							EgonetPage comeFrom = InterviewingEgoPage.askNext(id, null, null);
//							setResponsePage(
//									InterviewingEgoPage.askNextUnanswered(id,null,comeFrom));
//	                   	}
//					};
//
//					interviewLink.add(new Label("name", egoName));
//					item.add(interviewLink);
//					
//				}
//			};
//			form.add(interviewView);
	}
	
	//======================================================================
	// functions below are specific to the list of existing interviews
	// =====================================================================
	
//	protected Page onInterviewClick(Interview interview) {
//		return null;
//	}
//	
//	protected Pair<Class<?>,PageParameters> getInterviewBookmark(Interview interview) {
//		return null;
//	}
//
//	public List<Interview> getInterviews()
//   {
//		return Interviews.getInterviewsForStudy(studyId);
//	}
}
