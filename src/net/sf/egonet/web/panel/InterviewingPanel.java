package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.List;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.SimpleLogicMgr;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.form.Form;

import com.google.common.collect.Lists;

public class InterviewingPanel extends Panel {

	private Question question;
	private ArrayList<AnswerFormFieldPanel> answerFields;
	private CheckboxesPanel<String> refDKCheck;
	private Long interviewId;
	
	private final String 
		dontKnow = AnswerFormFieldPanel.dontKnow,
		refuse = AnswerFormFieldPanel.refuse,
		none = AnswerFormFieldPanel.none;
	
	public InterviewingPanel(String id, 
			Question question, ArrayList<AnswerFormFieldPanel> answerFields, Long interviewId) 
	{
		super(id);
		this.question = question;
		this.answerFields = answerFields;
		this.interviewId = interviewId;
		build();
	}

	/**
	 * IF this IS an alter question, the type IS MULTIPLE_SELECTION 
	 * AND there is more than one answer Field 
	 * THEN we want to use the action buttons to (optionally)
	 * set Don't Know, Refuse or the 'All' selection
	 */
	private void build() {
		String strPrompt;
		String strSkipReason;
		Label lblBtnAll;
		Form pageButtonsForm = new Form("pageButtonsForm");
		boolean withDKRefActionButtons = false;
		
		if ( question.getType() == Question.QuestionType.ALTER &&
			 question.getAnswerType() == Answer.AnswerType.MULTIPLE_SELECTION ) {
			if ( answerFields.size() > 1)
				withDKRefActionButtons = true;
		}
		setOutputMarkupId(true);
		setOutputMarkupPlaceholderTag(true);
		
		ArrayList<Alter> altersInPrompt = Lists.newArrayList();
		if(answerFields.size() < 2 && ! answerFields.isEmpty()) {
			altersInPrompt = answerFields.get(0).getAlters();
		}
		if(answerFields.size() > 1 && answerFields.get(0).getAlters().size() > 1) {
			altersInPrompt = Lists.newArrayList(answerFields.get(0).getAlters().get(0));
		}
		
		strPrompt = question.individualizePrompt(altersInPrompt);
		if ( interviewId==null ) {
			strPrompt = question.escapeTextInsertionTags(strPrompt);
		} else {
		    strPrompt = question.answerCountInsertion(strPrompt, interviewId);
		    strPrompt = question.questionContainsAnswerInsertion(strPrompt, interviewId, altersInPrompt);
		    strPrompt = question.dateDataInsertion(strPrompt, interviewId, altersInPrompt);
		    strPrompt = question.calculationInsertion(strPrompt, interviewId, altersInPrompt);
		    strPrompt = question.variableInsertion(strPrompt,interviewId, altersInPrompt);
		    strPrompt = question.conditionalTextInsertion(strPrompt, interviewId, altersInPrompt);
		}
		
		if ( SimpleLogicMgr.hasError()) {
			System.out.println ("USE IF error in " + question.getTitle());
		}
		add(new MultiLineLabel("prompt", strPrompt).setEscapeModelStrings(false));
		
		ListView questionsView = new ListView("questions",answerFields) {
			protected void populateItem(ListItem item) {
				AnswerFormFieldPanel wrapper = (AnswerFormFieldPanel) item.getModelObject();
				item.add(wrapper);
				ArrayList<Alter> alters = wrapper.getAlters();
				Alter lastAlter = alters.isEmpty() ? null : alters.get(alters.size()-1);
				item.add(new Label("alter",
						(answerFields.size() < 2 && ! question.getAskingStyleList()) 
						|| lastAlter == null ? 
								"" : lastAlter.getName()));
			}
		};
		questionsView.setReuseItems(true);
		add(questionsView);
		
		ArrayList<String> allOptions = Lists.newArrayList();
		ArrayList<String> selectedOptions = Lists.newArrayList();
		strSkipReason = AnswerFormFieldPanel.getSkipReasonForListOfAlters(answerFields);
		if ( strSkipReason.equals(dontKnow))  {
			selectedOptions.add(dontKnow);
		} else if ( strSkipReason.equals(refuse)) {
			selectedOptions.add(refuse);
		} else if ( strSkipReason.equals(none)) {
			selectedOptions.add(none);
		}
		if(question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION) &&
		   question.getNoneButton()) {
			allOptions.add(none);
		}
		
		if( !withDKRefActionButtons  && 
			 question.getType() != Question.QuestionType.EGO_ID &&  
			 answerFields.size() > 1 ) {
			     allOptions.addAll(Lists.newArrayList(dontKnow,refuse));
		 }
		
		refDKCheck = new CheckboxesPanel<String>("refDKCheck",allOptions,selectedOptions);
		add(refDKCheck);
	
		
// ==========================================================================
// we may want buttons instead of checkboxs for Don't know and Refuse
		final String strAllOption = question.getAllOptionString();
		
		AjaxFallbackLink btnAll = new AjaxFallbackLink("btnAll") {
			public void onClick(AjaxRequestTarget target) {
				AnswerFormFieldPanel.forceSelectionIfNone(answerFields, strAllOption, 
						question.getMaxCheckableBoxes() );
				target.addComponent(this);
				for ( AnswerFormFieldPanel panel : answerFields) {
					target.addComponent(panel);
				}
			}
		};
		btnAll.add(new Label("btnAllLabel", "(Set all to " + strAllOption + ") "));
		pageButtonsForm.add(btnAll);
		
		AjaxFallbackLink btnDontKnow = new AjaxFallbackLink("btnDontKnow") {
			public void onClick(AjaxRequestTarget target) {
				AnswerFormFieldPanel.forceSelectionIfNone(answerFields, dontKnow, 1);
				target.addComponent(this);
				for ( AnswerFormFieldPanel panel : answerFields) {
					target.addComponent(panel);
				}
			}
		};
		pageButtonsForm.add(btnDontKnow);

		AjaxFallbackLink btnRefuse = new AjaxFallbackLink("btnRefuse") {
			public void onClick(AjaxRequestTarget target) {
				AnswerFormFieldPanel.forceSelectionIfNone(answerFields, refuse,1 );
				target.addComponent(this);
				for ( AnswerFormFieldPanel panel : answerFields) {
					target.addComponent(panel);
				}
			}
		};
		pageButtonsForm.add(btnRefuse);
		
		add(pageButtonsForm);
		// the Don't know & refuse buttons are ONLY for list-of-alters
		// with Multiple_Selection type questions AND list-of-alters
		if ( withDKRefActionButtons ) {
			if ( !question.getAllButton()) {
				btnAll.setEnabled(false);
				btnAll.setVisible(false);
			}
			if ( !question.getPageLevelDontKnowButton()) {
				btnDontKnow.setEnabled(false);
				btnDontKnow.setVisible(false);
			}
			if (  !question.getPageLevelRefuseButton()) {
				btnRefuse.setEnabled(false);
				btnRefuse.setVisible(false);
			}
		} else {
			btnAll.setEnabled(false);
			btnDontKnow.setEnabled(false);
			btnRefuse.setEnabled(false);
			btnAll.setVisible(false);
			btnDontKnow.setVisible(false);
			btnRefuse.setVisible(false);
		} 
	}
	

	
	public List<String> pageFlags() {
		return refDKCheck.getSelected();
	}
	
	public ArrayList<AnswerFormFieldPanel> getAnswerFields() {
		return this.answerFields;
	}
	
	public static InterviewingPanel createExample(String id, Question question) {
		ArrayList<AnswerFormFieldPanel> panels = Lists.newArrayList();
		Interview interview = new Interview();
		Alter oneAlter = new Alter(interview,"Jacob",1);
		List<String> names = 
			Lists.newArrayList("Emma","Michael","Ethan","Isabella","Joshua");
		for(int i = 0; i < (question.getAskingStyleList() ? 5 : 1); i++) {
			ArrayList<Alter> alters = Lists.newArrayList();
			Alter otherAlter = new Alter(interview,names.get(i),i+2);
			if(question.getType().equals(Question.QuestionType.ALTER)) {
				alters.add(otherAlter);
			}
			if(question.getType().equals(Question.QuestionType.ALTER_PAIR)) {
				alters.add(oneAlter);
				alters.add(otherAlter);
			}
			panels.add(AnswerFormFieldPanel.getInstance("question", question, alters, interview.getId()));
		}
		return new InterviewingPanel(id,question,panels, interview.getId());
	}
}
