package net.sf.egonet.web.page;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections15.Transformer;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.network.NetworkService;
import net.sf.egonet.persistence.Analysis;
import net.sf.egonet.persistence.Expressions;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.web.component.NetworkImage;
import net.sf.egonet.web.panel.SingleSelectionPanel;
import net.sf.functionalj.tuple.PairUni;

public class NetworkVisualizationPage extends EgonetPage {
	
	private NetworkImage<Alter> networkImage;
	private Panel primaryPanel;
	private Expressions.EvaluationContext context;
	
	private void replacePrimary(Panel newPanel) {
		primaryPanel.replaceWith(newPanel);
		primaryPanel = newPanel;
	}
	
	public NetworkVisualizationPage(final Interview interview, Expression connectionReason) {
		
		super(Interviews.getEgoNameForInterview(interview.getId()));
		
		context = Expressions.getContext(interview);
		
		networkImage = new NetworkImage<Alter>("networkImage", 
				Analysis.getNetworkForInterview(interview, connectionReason));
		add(networkImage);
		
		primaryPanel = new EmptyPanel("primaryPanel");
		add(primaryPanel);

		add(new Link("layoutLink") {
			public void onClick() {
				ArrayList<NetworkService.LayoutOption> options = 
					Lists.newArrayList(NetworkService.LayoutOption.values());
				replacePrimary(
						new SingleSelectionPanel<NetworkService.LayoutOption>("primaryPanel",
								"Layout",options) 
						{
							public void action(NetworkService.LayoutOption option) {
								networkImage.setLayout(option);
								networkImage.refresh();
							}
						});
			}
		});

		add(new Link("backgroundLink") {
			public void onClick() {
				final ArrayList<Color> colors = 
					Lists.newArrayList(Color.WHITE,Color.RED,Color.ORANGE,
							Color.GREEN,Color.BLUE,Color.CYAN,Color.MAGENTA);
				final ArrayList<String> colorNames =
					Lists.newArrayList("White","Red","Orange",
							"Green","Blue","Cyan","Magenta");
				replacePrimary(
						new SingleSelectionPanel<Color>("primaryPanel",
								"Background",colors) 
						{
							public void action(Color option) {
								networkImage.setBackground(option);
								networkImage.refresh();
							}
							public String show(Color color) {
								for(int i = 0; i < colors.size(); i++) {
									if(color.equals(colors.get(i))) {
										return colorNames.get(i);
									}
								}
								return "Unknown";
							}
						});
			}
		});
		add(new Link("nodeLabelLink") {
			public void onClick() {
				ArrayList<AlterLabeller> options = 
					Lists.newArrayList(new AlterNoneLabeller(), new AlterLabeller());
				List<Question> questions =
					Questions.getQuestionsForStudy(interview.getStudyId(), QuestionType.ALTER);
				for(Question question : questions) {
					options.add(new AlterQuestionLabeller(question));
				}
				replacePrimary(
						new SingleSelectionPanel<AlterLabeller>("primaryPanel",
								"Alter Label",options) 
						{
							public void action(AlterLabeller option) {
								networkImage.setNodeLabeller(option);
								networkImage.refresh();
							}
						});
			}
		});
	}

	public class AlterLabeller implements Transformer<Alter,String>, Serializable {
		public String transform(Alter alter) {
			return alter.getName();
		}
		public String toString() {
			return "Name";
		}
	}
	public class AlterNoneLabeller extends AlterLabeller {
		public String transform(Alter alter) {
			return "";
		}
		public String toString() {
			return "None";
		}
	}
	public class AlterQuestionLabeller extends AlterLabeller {
		private Question question;
		private HashMap<Long,QuestionOption> idToOption;
		public AlterQuestionLabeller(Question question) {
			this.question = question;
			idToOption = Maps.newHashMap();
			for(QuestionOption option : Options.getOptionsForQuestion(question.getId())) {
				idToOption.put(option.getId(), option);
			}
		}
		public String transform(Alter alter) {
			Answer answer = 
				context.qidAidToAlterAnswer.get(
						new PairUni<Long>(question.getId(),alter.getId()));
			if(answer == null || answer.getValue() == null || answer.getValue().isEmpty()) {
				return "";
			} else if(question.getAnswerType().equals(Answer.AnswerType.TEXTUAL) ||
					question.getAnswerType().equals(Answer.AnswerType.NUMERICAL)) 
			{
				return answer.getValue();
			} else {
				String result = "";
				for(String optionId : answer.getValue().split(",")) {
					try {
						result += 
							(result.isEmpty() ? "" : ", ")+
							idToOption.get(Long.parseLong(optionId)).getName();
					} catch(Exception ex) {
						
					}
				}
				return result;
			}
		}
		public String toString() {
			return question.getTitle();
		}
	}
	public class AlterExpressionLabeller extends AlterLabeller {
		public String transform(Alter alter) {
			return alter.getName(); // TODO: constructor with expression, and use it
		}
	}
}
