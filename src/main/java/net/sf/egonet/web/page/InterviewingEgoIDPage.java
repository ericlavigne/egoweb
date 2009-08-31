package net.sf.egonet.web.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;

public class InterviewingEgoIDPage extends EgonetPage {
	
	public ArrayList<AnswerFormFieldPanel> questions;
	private Model message;
	private ListView questionsView;
	private Long studyId;
	
	public InterviewingEgoIDPage(Study study) {
		super(study.getName());
		this.studyId = study.getId();
		
		questions = Lists.newArrayList(Lists.transform(
			Questions.getQuestionsForStudy(studyId, QuestionType.EGO_ID),
			new Function<Question,AnswerFormFieldPanel>() {
				public AnswerFormFieldPanel apply(Question question) {
					return AnswerFormFieldPanel.getInstance("question",question);
				}
			}));
		
		Form form = new Form("form") 
        {
			@Override
			public void onSubmit()
            {
				String newMessage = "Submitted data:";
				List<Answer> answers = Lists.newArrayList();
				for(AnswerFormFieldPanel question : questions) {
					newMessage += " "+question.getAnswer();
					answers.add(new Answer(question.getQuestion(),question.getAnswer()));
				}
				Interview interview = Interviewing.findOrCreateMatchingInterviewForStudy(studyId, answers);
				newMessage += " interview id is "+interview.getId();
				message.setObject(newMessage);
				
				setResponsePage(InterviewingEgoPage.askNextUnanswered(interview.getId()));
            }
        };
		
		questionsView = new ListView("questions", questions)
        {
			protected void populateItem(ListItem item)
            {
				AnswerFormFieldPanel wrapper = (AnswerFormFieldPanel) item.getModelObject();
				item.add(wrapper);
            }
        };
        questionsView.setReuseItems(true);
        form.add(questionsView);

		add(form);
		
		message = new Model("");
		
		add(new Label("message", message));
	}
}
