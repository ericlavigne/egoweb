package net.sf.egonet.web.page;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.collections15.Transformer;

import org.apache.wicket.markup.html.link.Link;

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
import net.sf.egonet.web.panel.MapEditorPanel;
import net.sf.egonet.web.panel.PanelContainer;
import net.sf.egonet.web.panel.SingleSelectionPanel;
import net.sf.functionalj.tuple.PairUni;
import net.sf.functionalj.tuple.TripleUni;

public class NetworkVisualizationPage extends EgonetPage {
	
	private NetworkImage<Alter> networkImage;
	private PanelContainer primaryPanel;
	private PanelContainer secondaryPanel;
	private Expressions.EvaluationContext context;

	final ArrayList<Color> colors = 
		Lists.newArrayList(Color.RED,Color.ORANGE,
				Color.GREEN,Color.BLUE,Color.CYAN,
				Color.MAGENTA,Color.WHITE,Color.BLACK);
	final ArrayList<String> colorNames =
		Lists.newArrayList("Red","Orange",
				"Green","Blue","Cyan",
				"Magenta","White","Black");
	private String showColor(Color color) {
		if(color == null) {
			return " ";
		}
		for(int i = 0; i < colors.size(); i++) {
			if(color.equals(colors.get(i))) {
				return colorNames.get(i);
			}
		}
		return "Unknown";
	}
	
	private Interview interview;
	private Expression connectionReason;
	
	private NetworkImage<Alter> buildNetworkImage() {
		return new NetworkImage<Alter>("networkImage", 
				Analysis.getNetworkForInterview(interview, this.connectionReason));
	}
	
	public NetworkVisualizationPage(final Interview interview, Expression connectionReason) {
		
		super(Interviews.getEgoNameForInterview(interview.getId()));
		
		this.interview = interview;
		this.connectionReason = connectionReason;
		context = Expressions.getContext(interview);
		
		networkImage = buildNetworkImage();
		add(networkImage);
		
		primaryPanel = new PanelContainer("primaryPanel");
		add(primaryPanel);
		
		secondaryPanel = new PanelContainer("secondaryPanel");
		add(secondaryPanel);

		add(buildNetworkLayoutLink());
		add(buildNetworkBackgroundLink());
		add(buildNodeLabelLink());
		add(buildNodeColorLink());
		add(buildNodeSizeLink());
		add(buildNodeShapeLink());
		add(buildEdgeColorLink());
		add(buildEdgeSizeLink());
		add(buildAdjacencyReasonLink());
	}

	// XXX: Section network layout
	
	private NetworkService.LayoutOption layoutOption = null;
	
	private Link buildNetworkLayoutLink() {
		return new Link("layoutLink") {
			public void onClick() {
				ArrayList<NetworkService.LayoutOption> options = 
					Lists.newArrayList(NetworkService.LayoutOption.values());
				primaryPanel.changePanel(
						new SingleSelectionPanel<NetworkService.LayoutOption>("panel",
								"Layout",options) 
						{
							public void action(NetworkService.LayoutOption option) {
								layoutOption = option;
								networkImage.setLayoutOption(option);
								networkImage.refresh();
							}
						});
				secondaryPanel.removePanel();
			}
		};
	}
	
	// XXX: Section network background
	
	private Color backgroundColor = Color.WHITE;
	
	private Link buildNetworkBackgroundLink() {
		return new Link("backgroundLink") {
			public void onClick() {
				primaryPanel.changePanel(
						new SingleSelectionPanel<Color>("panel",
								"Background",colors) 
						{
							public void action(Color option) {
								backgroundColor = option;
								networkImage.setBackground(backgroundColor);
								networkImage.refresh();
							}
							public String show(Color color) {
								return showColor(color);
							}
						});
				secondaryPanel.removePanel();
			}
		};
	}
	
	// XXX: Common helpers

	private ArrayList<Object> noneAndSelectionQuestionsOfQuestionType(QuestionType questionType) {
		ArrayList<Object> results = Lists.newArrayList();
		results.add("None");
		List<Question> questions =
			Questions.getQuestionsForStudy(interview.getStudyId(), questionType);
		for(Question question : questions) {
			if(question.getAnswerType().equals(Answer.AnswerType.SELECTION) ||
					question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION))
			{
				results.add(question);
			}
		}
		return results;
	}
	
	private ArrayList<Object> alterSelectionQuestionsAndNone() {
		return noneAndSelectionQuestionsOfQuestionType(QuestionType.ALTER);
	}
	
	private ArrayList<Object> alterPairSelectionQuestionsAndNone() {
		return noneAndSelectionQuestionsOfQuestionType(QuestionType.ALTER_PAIR);
	}
	
	// XXX: Section node label
	
	private AlterLabeller alterLabeller = null;
	
	private Link buildNodeLabelLink() {
		return new Link("nodeLabelLink") {
			public void onClick() {
				ArrayList<AlterLabeller> options = 
					Lists.newArrayList(new AlterNoneLabeller(), new AlterLabeller());
				List<Question> questions =
					Questions.getQuestionsForStudy(interview.getStudyId(), QuestionType.ALTER);
				for(Question question : questions) {
					options.add(new AlterQuestionLabeller(question));
				}
				primaryPanel.changePanel(
						new SingleSelectionPanel<AlterLabeller>("panel",
								"Alter Label",options) 
						{
							public void action(AlterLabeller option) {
								alterLabeller = option;
								networkImage.setNodeLabeller(alterLabeller);
								networkImage.refresh();
							}
						});
				secondaryPanel.removePanel();
			}
		};
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
					question.getAnswerType().equals(Answer.AnswerType.TEXTUAL_PP) ||
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
			return alter.getName();
		}
	}

	// XXX: Section node color
	
	private Question nodeColorQuestion;
	private TreeMap<Question,TreeMap<QuestionOption,Color>> nodeColorSelectionQuestionDetails;
	
	private Link buildNodeColorLink() {
		nodeColorSelectionQuestionDetails = Maps.newTreeMap();
		return new Link("nodeColorLink") {
			public void onClick() {
				nodeColorChangePrimary();
				nodeColorChangeSecondary();
			}
		};
	}
	
	private void nodeColorChangePrimary() {
		primaryPanel.changePanel(
				new SingleSelectionPanel<Object>("panel","Color node based on",alterSelectionQuestionsAndNone()) {
					public String show(Object object) {
						return object instanceof Question ? 
								((Question) object).getTitle() : object.toString();
					}
					public void action(Object option) {
						if(option.equals("None")) {
							nodeColorQuestion = null;
						} else if(option instanceof Question) {
							nodeColorQuestion = (Question) option;
						} else {
							throw new RuntimeException("Unrecognized colorizing option: "+option);
						}
						nodeColorChangeSecondary();
						nodeColorUpdate(true);
					}
				});
	}
	
	private void nodeColorChangeSecondary() {
		if(nodeColorQuestion == null) {
			secondaryPanel.removePanel();
		} else {
			if(!nodeColorSelectionQuestionDetails.containsKey(nodeColorQuestion)) {
				nodeColorSelectionQuestionDetails.put(nodeColorQuestion, 
						new TreeMap<QuestionOption,Color>());
			}
			secondaryPanel.changePanel(
					new MapEditorPanel<QuestionOption,Color>("panel",
							"Color for each "+nodeColorQuestion.getTitle()+" option",
							"Color for $$",
							nodeColorSelectionQuestionDetails.get(nodeColorQuestion),
							Options.getOptionsForQuestion(nodeColorQuestion.getId()),
							colors) 
					{
						protected String showValue(Color color) {
							return showColor(color);
						}
						protected void mapChanged() {
							nodeColorUpdate(true);
						}
					});
		}
	}
	
	private void nodeColorUpdate(Boolean shouldRefresh) {
		if(nodeColorQuestion == null) {
			networkImage.setNodeColorizer(new AlterColorizer());
		} else {
			networkImage.setNodeColorizer(
					new AlterQuestionColorizer(
							nodeColorQuestion, 
							nodeColorSelectionQuestionDetails
							.get(nodeColorQuestion)));
		}
		if(shouldRefresh) {
			networkImage.refresh();
		}
	}
	
	public class AlterColorizer implements Transformer<Alter,Paint>, Serializable {
		public Paint transform(Alter alter) {
			return Color.WHITE;
		}
		public String toString() {
			return "None";
		}
	}
	
	public class AlterQuestionColorizer extends AlterColorizer {
		private Question question;
		private TreeMap<QuestionOption,Color> configuration;
		public AlterQuestionColorizer(Question question, TreeMap<QuestionOption,Color> configuration) {
			this.question = question;
			this.configuration = configuration;
		}
		public Paint transform(Alter alter) {
			String answerValue =
				context.qidAidToAlterAnswer.get(
						new PairUni<Long>(question.getId(),alter.getId()))
				.getValue();
			if(question.getAnswerType().equals(Answer.AnswerType.SELECTION) ||
					question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION)) 
			{
				for(String optionIdString : Lists.newArrayList(answerValue.split(","))) {
					Color color =
						configuration.get(context.idToOption.get(Long.parseLong(optionIdString)));
					if(color != null) {
						return color;
					}
				}
			}
			return Color.WHITE;
		}
		public String toString() {
			return question.getTitle();
		}
		public ArrayList<QuestionOption> getConfigKeys() {
			return Options.getOptionsForQuestion(question.getId());
		}
	}
	
	// XXX: Section node size 
	
	final ArrayList<Integer> sizes = 
		Lists.newArrayList(10,20,30,40,50);
	final ArrayList<Integer> sizeNames =
		Lists.newArrayList(1,2,3,4,5);
	private String showSizeName(Integer sizeName) {
		return sizeName == null ? " " : sizeName+"";
	}
	private Integer sizeOfName(Integer sizeName) {
		return sizes.get(sizeName == null ? 0 : (sizeName-1));
	}

	private Question nodeSizeQuestion;
	private TreeMap<Question,TreeMap<QuestionOption,Integer>> nodeSizeSelectionQuestionDetails;
	
	private Link buildNodeSizeLink() {
		nodeSizeSelectionQuestionDetails = Maps.newTreeMap();
		return new Link("nodeSizeLink") {
			public void onClick() {
				nodeSizeChangePrimary();
				nodeSizeChangeSecondary();
			}
		};
	}


	private void nodeSizeChangePrimary() {
		primaryPanel.changePanel(
				new SingleSelectionPanel<Object>("panel","Size node based on",
						alterSelectionQuestionsAndNone()) {
					public String show(Object object) {
						return object instanceof Question ? 
								((Question) object).getTitle() : object.toString();
					}
					public void action(Object option) {
						if(option.equals("None")) {
							nodeSizeQuestion = null;
						} else if(option instanceof Question) {
							nodeSizeQuestion = (Question) option;
						} else {
							throw new RuntimeException("Unrecognized sizing option: "+option);
						}
						nodeSizeChangeSecondary();
						nodeShapeAndSizeUpdate(true);
					}
				});
	}
	
	private void nodeSizeChangeSecondary() {
		if(nodeSizeQuestion == null) {
			secondaryPanel.removePanel();
		} else {
			if(!nodeSizeSelectionQuestionDetails.containsKey(nodeSizeQuestion)) {
				nodeSizeSelectionQuestionDetails.put(nodeSizeQuestion, 
						new TreeMap<QuestionOption,Integer>());
			}
			secondaryPanel.changePanel(
					new MapEditorPanel<QuestionOption,Integer>("panel",
							"Size for each "+nodeSizeQuestion.getTitle()+" option",
							"Size for $$",
							nodeSizeSelectionQuestionDetails.get(nodeSizeQuestion),
							Options.getOptionsForQuestion(nodeSizeQuestion.getId()),
							sizeNames) 
					{
						protected String showValue(Integer sizeName) {
							return showSizeName(sizeName);
						}
						protected void mapChanged() {
							nodeShapeAndSizeUpdate(true);
						}
					});
		}
	}
	
	// XXX: Section node shape 
	
	final ArrayList<Integer> shapeSides = 
		Lists.newArrayList(1,3,4,5,6);
	final ArrayList<String> shapeNames =
		Lists.newArrayList("Circle","Triangle","Square","Pentagon","Hexagon");
	private String showShapeName(Integer sides) {
		if(sides != null) {
			for(Integer i = 0; i < shapeSides.size(); i++) {
				if(shapeSides.get(i).equals(sides)) {
					return shapeNames.get(i);
				}
			}
		}
		return sides == null || sides < 3 ? "Circle" : (sides+"-gon");
	}

	private Question nodeShapeQuestion;
	private TreeMap<Question,TreeMap<QuestionOption,Integer>> nodeShapeSelectionQuestionDetails;

	private Link buildNodeShapeLink() {
		nodeShapeSelectionQuestionDetails = Maps.newTreeMap();
		return new Link("nodeShapeLink") {
			public void onClick() {
				nodeShapeChangePrimary();
				nodeShapeChangeSecondary();
			}
		};
	}


	private void nodeShapeChangePrimary() {
		primaryPanel.changePanel(
				new SingleSelectionPanel<Object>("panel","Shape node based on",
						alterSelectionQuestionsAndNone()) {
					public String show(Object object) {
						return object instanceof Question ? 
								((Question) object).getTitle() : object.toString();
					}
					public void action(Object option) {
						if(option.equals("None")) {
							nodeShapeQuestion = null;
						} else if(option instanceof Question) {
							nodeShapeQuestion = (Question) option;
						} else {
							throw new RuntimeException("Unrecognized shaping option: "+option);
						}
						nodeShapeChangeSecondary();
						nodeShapeAndSizeUpdate(true);
					}
				});
	}
	
	private void nodeShapeChangeSecondary() {
		if(nodeShapeQuestion == null) {
			secondaryPanel.removePanel();
		} else {
			if(!nodeShapeSelectionQuestionDetails.containsKey(nodeShapeQuestion)) {
				nodeShapeSelectionQuestionDetails.put(nodeShapeQuestion, 
						new TreeMap<QuestionOption,Integer>());
			}
			secondaryPanel.changePanel(
					new MapEditorPanel<QuestionOption,Integer>("panel",
							"Shape for each "+nodeShapeQuestion.getTitle()+" option",
							"Shape for $$",
							nodeShapeSelectionQuestionDetails.get(nodeShapeQuestion),
							Options.getOptionsForQuestion(nodeShapeQuestion.getId()),
							shapeSides) 
					{
						protected String showValue(Integer sides) {
							return showShapeName(sides);
						}
						protected void mapChanged() {
							nodeShapeAndSizeUpdate(true);
						}
					});
		}
	}
	
	private void nodeShapeAndSizeUpdate(Boolean shouldRefresh) {
		networkImage.setNodeShaper(new Transformer<Alter, Shape>() {
			public Shape transform(Alter alter) {
				return new RegularPolygon(
						nodeSides(alter), 
						nodeSize(alter));
			}
			private Integer nodeSides(Alter alter) {
				Integer sides = null;
				if(nodeShapeQuestion != null) {
					String answerValue =
						context.qidAidToAlterAnswer.get(
								new PairUni<Long>(nodeShapeQuestion.getId(),alter.getId()))
						.getValue();
					if(nodeShapeQuestion.getAnswerType().equals(Answer.AnswerType.SELECTION) ||
							nodeShapeQuestion.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION)) 
					{
						for(String optionIdString : Lists.newArrayList(answerValue.split(","))) {
							sides =
								nodeShapeSelectionQuestionDetails
								.get(nodeShapeQuestion)
								.get(context.idToOption.get(Long.parseLong(optionIdString)));
							if(sides != null) {
								return sides < 3 ? 20 : sides;
							}
						}
					}
				}
				return 20;
			}
			private Integer nodeSize(Alter alter) {
				if(nodeSizeQuestion != null) {
					String answerValue =
						context.qidAidToAlterAnswer.get(
								new PairUni<Long>(nodeSizeQuestion.getId(),alter.getId()))
						.getValue();
					if(nodeSizeQuestion.getAnswerType().equals(Answer.AnswerType.SELECTION) ||
							nodeSizeQuestion.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION)) 
					{
						for(String optionIdString : Lists.newArrayList(answerValue.split(","))) {
							Integer sizeName =
								nodeSizeSelectionQuestionDetails
								.get(nodeSizeQuestion)
								.get(context.idToOption.get(Long.parseLong(optionIdString)));
							if(sizeName != null) {
								return sizeOfName(sizeName);
							}
						}
					}
				}
				return sizeOfName(null);
			}
		});
		if(shouldRefresh) {
			networkImage.refresh();
		}
	}
	
	private static class RegularPolygon extends Polygon {
		public RegularPolygon(Integer sides, Integer size) {
			super(xcoords(sides,size),ycoords(sides,size),sides);
		}
		private static int[] xcoords(Integer sides, Integer size) {
			int[] result = new int[sides];
			for(Integer i = 0; i < sides; i++) {
				result[i] = (int) (Math.sin(2*(i+0.5)*Math.PI/sides)*size);
			}
			return result;
		}
		private static int[] ycoords(Integer sides, Integer size) {
			int[] result = new int[sides];
			for(Integer i = 0; i < sides; i++) {
				result[i] = (int) (Math.cos(2*(i+0.5)*Math.PI/sides)*size);
			}
			return result;
		}
	}
	
	// XXX: Section edge color
	
	private Question edgeColorQuestion;
	private TreeMap<Question,TreeMap<QuestionOption,Color>> edgeColorSelectionQuestionDetails;
	
	private Link buildEdgeColorLink() {
		edgeColorSelectionQuestionDetails = Maps.newTreeMap();
		return new Link("edgeColorLink") {
			public void onClick() {
				edgeColorChangePrimary();
				edgeColorChangeSecondary();
			}
		};
	}
	
	private void edgeColorChangePrimary() {
		primaryPanel.changePanel(
				new SingleSelectionPanel<Object>("panel","Color edge based on",alterPairSelectionQuestionsAndNone()) {
					public String show(Object object) {
						return object instanceof Question ? 
								((Question) object).getTitle() : object.toString();
					}
					public void action(Object option) {
						if(option.equals("None")) {
							edgeColorQuestion = null;
						} else if(option instanceof Question) {
							edgeColorQuestion = (Question) option;
						} else {
							throw new RuntimeException("Unrecognized edge colorizing option: "+option);
						}
						edgeColorChangeSecondary();
						edgeColorUpdate(true);
						networkImage.refresh();
					}
				});
	}
	
	private void edgeColorChangeSecondary() {
		if(edgeColorQuestion == null) {
			secondaryPanel.removePanel();
		} else {
			if(!edgeColorSelectionQuestionDetails.containsKey(edgeColorQuestion)) {
				edgeColorSelectionQuestionDetails.put(edgeColorQuestion, 
						new TreeMap<QuestionOption,Color>());
			}
			secondaryPanel.changePanel(
					new MapEditorPanel<QuestionOption,Color>("panel",
							"Color for each "+edgeColorQuestion.getTitle()+" option",
							"Color for $$",
							edgeColorSelectionQuestionDetails.get(edgeColorQuestion),
							Options.getOptionsForQuestion(edgeColorQuestion.getId()),
							colors) 
					{
						protected String showValue(Color color) {
							return showColor(color);
						}
						protected void mapChanged() {
							edgeColorUpdate(true);
						}
					});
		}
	}
	
	private void edgeColorUpdate(Boolean shouldRefresh) {
		if(edgeColorQuestion == null) {
			networkImage.setEdgeColorizer(new EdgeColorizer());
		} else {
			networkImage.setEdgeColorizer(
					new AlterPairQuestionColorizer(
							edgeColorQuestion, 
							edgeColorSelectionQuestionDetails
							.get(edgeColorQuestion)));
		}
		if(shouldRefresh) {
			networkImage.refresh();
		}
	}
	
	public class EdgeColorizer implements Transformer<PairUni<Alter>,Paint>, Serializable {
		public Paint transform(PairUni<Alter> alterPair) {
			return Color.BLACK;
		}
		public String toString() {
			return "None";
		}
	}
	
	public class AlterPairQuestionColorizer extends EdgeColorizer {
		private Question question;
		private TreeMap<QuestionOption,Color> configuration;
		public AlterPairQuestionColorizer(Question question, TreeMap<QuestionOption,Color> configuration) {
			this.question = question;
			this.configuration = configuration;
		}
		public Paint transform(PairUni<Alter> alterPair) {
			String answerValue =
				context.qidA1idA2idToAlterPairAnswer.get(
						new TripleUni<Long>(question.getId(),
								alterPair.getFirst().getId(),
								alterPair.getSecond().getId()))
				.getValue();
			if(question.getAnswerType().equals(Answer.AnswerType.SELECTION) ||
					question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION)) 
			{
				for(String optionIdString : Lists.newArrayList(answerValue.split(","))) {
					Color color =
						configuration.get(context.idToOption.get(Long.parseLong(optionIdString)));
					if(color != null) {
						return color;
					}
				}
			}
			return Color.BLACK;
		}
		public String toString() {
			return question.getTitle();
		}
		public ArrayList<QuestionOption> getConfigKeys() {
			return Options.getOptionsForQuestion(question.getId());
		}
	}
	
	// XXX: Section edge size
	
	private Question edgeSizeQuestion;
	private TreeMap<Question,TreeMap<QuestionOption,Integer>> edgeSizeSelectionQuestionDetails;
	
	private Link buildEdgeSizeLink() {
		edgeSizeSelectionQuestionDetails = Maps.newTreeMap();
		return new Link("edgeSizeLink") {
			public void onClick() {
				edgeSizeChangePrimary();
				edgeSizeChangeSecondary();
			}
		};
	}
	
	private void edgeSizeChangePrimary() {
		primaryPanel.changePanel(
				new SingleSelectionPanel<Object>("panel","Size edge based on",alterPairSelectionQuestionsAndNone()) {
					public String show(Object object) {
						return object instanceof Question ? 
								((Question) object).getTitle() : object.toString();
					}
					public void action(Object option) {
						if(option.equals("None")) {
							edgeSizeQuestion = null;
						} else if(option instanceof Question) {
							edgeSizeQuestion = (Question) option;
						} else {
							throw new RuntimeException("Unrecognized edge Sizing option: "+option);
						}
						edgeSizeChangeSecondary();
						edgeSizeUpdate(true);
					}
				});
	}
	
	private void edgeSizeChangeSecondary() {
		if(edgeSizeQuestion == null) {
			secondaryPanel.removePanel();
		} else {
			if(!edgeSizeSelectionQuestionDetails.containsKey(edgeSizeQuestion)) {
				edgeSizeSelectionQuestionDetails.put(edgeSizeQuestion, 
						new TreeMap<QuestionOption,Integer>());
			}
			secondaryPanel.changePanel(
					new MapEditorPanel<QuestionOption,Integer>("panel",
							"Size for each "+edgeSizeQuestion.getTitle()+" option",
							"Size for $$",
							edgeSizeSelectionQuestionDetails.get(edgeSizeQuestion),
							Options.getOptionsForQuestion(edgeSizeQuestion.getId()),
							sizeNames) 
					{
						protected String showValue(Integer size) {
							return size == null ? "" : size+"";
						}
						protected void mapChanged() {
							edgeSizeUpdate(true);
						}
					});
		}
	}
	
	private void edgeSizeUpdate(Boolean shouldRefresh) {
		if(edgeSizeQuestion == null) {
			networkImage.setEdgeSizer(new EdgeSizer());
		} else {
			networkImage.setEdgeSizer(
					new AlterPairQuestionSizer(
							edgeSizeQuestion, 
							edgeSizeSelectionQuestionDetails
							.get(edgeSizeQuestion)));
		}
		if(shouldRefresh) {
			networkImage.refresh();
		}
	}
	
	// TODO: might need to use alters to decide if there should even be an edge
	private Stroke strokeForAlterPairAndSize(PairUni<Alter> alters, Integer size) {
		return new BasicStroke(1.3f * size, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
	}
	
	public class EdgeSizer implements Transformer<PairUni<Alter>,Stroke>, Serializable {
		public Stroke transform(PairUni<Alter> alterPair) {
			return strokeForAlterPairAndSize(alterPair,1);
		}
		public String toString() {
			return "None";
		}
	}
	
	public class AlterPairQuestionSizer extends EdgeSizer {
		private Question question;
		private TreeMap<QuestionOption,Integer> configuration;
		public AlterPairQuestionSizer(Question question, TreeMap<QuestionOption,Integer> configuration) {
			this.question = question;
			this.configuration = configuration;
		}
		public Stroke transform(PairUni<Alter> alterPair) {
			String answerValue =
				context.qidA1idA2idToAlterPairAnswer.get(
						new TripleUni<Long>(question.getId(),
								alterPair.getFirst().getId(),
								alterPair.getSecond().getId()))
				.getValue();
			Integer size = 1;
			if(question.getAnswerType().equals(Answer.AnswerType.SELECTION) ||
					question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION)) 
			{
				for(String optionIdString : Lists.newArrayList(answerValue.split(","))) {
					Integer newSize =
						configuration.get(context.idToOption.get(Long.parseLong(optionIdString)));
					if(newSize != null && newSize > size) {
						size = newSize;
					}
				}
			}
			return strokeForAlterPairAndSize(alterPair,size);
		}
		public String toString() {
			return question.getTitle();
		}
		public ArrayList<QuestionOption> getConfigKeys() {
			return Options.getOptionsForQuestion(question.getId());
		}
	}
	
	// XXX: Section adjacency reason
	
	private Link buildAdjacencyReasonLink() {
		return new Link("adjacencyReasonLink") {
			public void onClick() {
				ArrayList<Expression> options = Lists.newArrayList(
						Expressions.forStudy(interview.getStudyId()));
				primaryPanel.changePanel(
						new SingleSelectionPanel<Expression>("panel",
								"Adjacency Reason",options) 
						{
							public void action(Expression option) {
								connectionReason = option;
								NetworkImage<Alter> newNetworkImage = buildNetworkImage();
								networkImage.replaceWith(newNetworkImage);
								networkImage = newNetworkImage;
								
								if(layoutOption != null) {
									networkImage.setLayoutOption(layoutOption);
								}
								if(backgroundColor != null) {
									networkImage.setBackground(backgroundColor);
								}
								if(alterLabeller != null) {
									networkImage.setNodeLabeller(alterLabeller);
								}
								nodeColorUpdate(false);
								nodeShapeAndSizeUpdate(false);
								edgeColorUpdate(false);
								edgeSizeUpdate(false);
								
								networkImage.refresh();
							}
						});
				secondaryPanel.removePanel();
			}
		};
	}
}
