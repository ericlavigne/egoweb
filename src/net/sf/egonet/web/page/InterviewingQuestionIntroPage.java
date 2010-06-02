package net.sf.egonet.web.page;

import java.util.ArrayList;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.SimpleLogicMgr;
import net.sf.egonet.web.component.FocusOnLoadBehavior;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;

public class InterviewingQuestionIntroPage extends InterviewingPage {

	private EgonetPage previousPage, nextPage;
	
	private String text;
	
	public InterviewingQuestionIntroPage(
			Long interviewId, String text, EgonetPage previous, EgonetPage next, Question question) 
	{
		super(interviewId);
		Link forwardLink;
		
        this.previousPage = previous;
        this.nextPage = next;
       
        // perform variable insertion on preface
        // passing the null value for the alters indicates
        // we only want answers from ego and ego_id sections,
        if ( question!=null ) {
        	// text = question.answerCountInsertion(text, interviewId);
        	text = question.dateDataInsertion(text, interviewId, (ArrayList<Alter>)null);
        	text = question.calculationInsertion(text, interviewId, (ArrayList<Alter>)null);
        	text = question.variableInsertion(text, interviewId, (ArrayList<Alter>)null);
        	text = question.conditionalTextInsertion(text, interviewId, (ArrayList<Alter>)null);
			if ( SimpleLogicMgr.hasError()) {
				System.out.println ("USE IF error in " + question.getTitle());
			}
        }
        
        this.text = text;
		setQuestionId("Question: " + question.getTitle());
        add(new MultiLineLabel("text", this.text).setEscapeModelStrings(false));

		add(new Link("backwardLink") {
			public void onClick() {
				if(previousPage != null) {
					setResponsePage(previousPage);
				}
			}
		});
		
		forwardLink = new Link("forwardLink") {
			public void onClick() {
				if(nextPage != null) {
					setResponsePage(nextPage);
				}
			}
		};
		forwardLink.add(new FocusOnLoadBehavior());
		add(forwardLink);
		
	}

	public String toString() {
		return text.length() < 23 ? text : text.substring(0, 20)+"...";
	}
	
	public static EgonetPage possiblyReplaceNextQuestionPageWithPreface(
			Long interviewId, EgonetPage proposedNextPage,
			Question earlyQuestion, Question lateQuestion,
			EgonetPage earlyPage, EgonetPage latePage)
	{
		String preface =
			Interviewing.getPrefaceBetweenQuestions(earlyQuestion, lateQuestion);
		return preface == null ? proposedNextPage : 
			new InterviewingQuestionIntroPage(interviewId,preface,earlyPage,latePage,
					((lateQuestion==null)?earlyQuestion:lateQuestion));
				//	((earlyQuestion!=null)?earlyQuestion:lateQuestion));
	}
}
