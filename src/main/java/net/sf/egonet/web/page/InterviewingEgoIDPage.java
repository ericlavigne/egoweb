package net.sf.egonet.web.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
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
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Questions;

public class InterviewingEgoIDPage extends EgonetPage {
	
	public ArrayList<QuestionWrapper> questions;
	private Model message;
	private ListView questionsView;
	private Long studyId;
	
	public InterviewingEgoIDPage(Study study) {
		super(study.getName());
		this.studyId = study.getId();
		
		questions = Lists.newArrayList(Lists.transform(
			Questions.getQuestionsForStudy(studyId, QuestionType.EGO_ID),
			new Function<Question,QuestionWrapper>() {
				public QuestionWrapper apply(Question question) {
					return new QuestionWrapper(question);
				}
			}));
		
		Form form = new Form("form") 
        {
			@Override
			public void onSubmit()
            {
				String newMessage = "Submitted data:";
				List<Answer> answers = Lists.newArrayList();
				for(QuestionWrapper question : questions) {
					newMessage += " "+question.getAnswer();
					answers.add(new Answer(question.getQuestion(),question.getAnswer()));
				}
				Interview interview = Interviewing.findOrCreateMatchingInterviewForStudy(studyId, answers);
				newMessage += " interview id is "+interview.getId();
				message.setObject(newMessage);
				
				setResponsePage(new InterviewingEgoPage(interview.getId()));
            }
        };
		
		questionsView = new ListView("questions", questions)
        {
			protected void populateItem(ListItem item)
            {
				QuestionWrapper wrapper = (QuestionWrapper) item.getModelObject();
				item.add(new Label("questionPrompt", new PropertyModel(wrapper,"prompt")));
				item.add(new TextField("answerField", new PropertyModel(wrapper,"answer")));
            }
        };
        questionsView.setReuseItems(true);
        form.add(questionsView);

		add(form);
		
		message = new Model("");
		
		add(new Label("message", message));
	}
	
	private static class QuestionWrapper implements Serializable {
		
		private Question question;
		private String answer;
		
		public QuestionWrapper(Question question) {
			this.question = question;
			this.answer = "";
		}
		
		public Question getQuestion() {
			return question;
		}
		
		public String getPrompt() {
			return question.getPrompt();
		}
		
		public String getAnswer() {
			return answer;
		}
		
		public void setAnswer(String answer) {
			this.answer = answer;
		}
	}
}
