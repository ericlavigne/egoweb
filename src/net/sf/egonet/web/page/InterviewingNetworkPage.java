package net.sf.egonet.web.page;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.commons.collections15.Transformer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.base.Joiner;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Answer.AnswerType;
import net.sf.egonet.persistence.Answers;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Alters;
import net.sf.egonet.persistence.Expressions;
import net.sf.egonet.persistence.Analysis;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.web.panel.AnswerFormFieldPanel;
import net.sf.egonet.web.panel.InterviewingPanel;
import net.sf.egonet.web.component.NetworkImage;
import net.sf.functionalj.tuple.PairUni;
import net.sf.egonet.network.Network;

import static net.sf.egonet.web.page.InterviewingQuestionIntroPage.possiblyReplaceNextQuestionPageWithPreface;

/*
 * A class to create an interview page that will display a network graph,
 * with nodes for the alters specified during the interview. The edges will
 * be created based on a condition specified when the question is created.
 * The network graph can be customized to modify parameters based on the
 * interviewee's response to other questions. These parameters are:
 * 
 *	node size (based on any ALTER question)
 *	node color (based on any ALTER question)
 *	node shape (based on any ALTER question)
 *	edge width (based on any ALTER_PAIR question)
 *	edge color (based on any ALTER_PAIR question)
 * 
 *	All parameters above are optional during question creation, but the 
 *	question author must specify an expression to define the edges between
 *	vertices.
 */
public class InterviewingNetworkPage extends InterviewingPage {
	
	private Long interviewId;
	private Question question;
	private InterviewingPanel interviewingPanel;
	private NetworkImage<Alter> networkImage;

	public InterviewingNetworkPage(Long interviewId, Question question) {
		super(interviewId);
		this.interviewId = interviewId;
		this.question = question;
		build();
	}

	private void build() {

		buildNetworkImage();
		Form form = new Form("form") {
			public void onSubmit() {
				List<String> pageFlags = interviewingPanel.pageFlags();
				List<AnswerFormFieldPanel> answerFields = interviewingPanel.getAnswerFields();
				boolean okayToContinue = 
					AnswerFormFieldPanel.okayToContinue(answerFields, pageFlags);
				boolean consistent = 
					AnswerFormFieldPanel.allConsistent(answerFields, pageFlags);
				for(AnswerFormFieldPanel field : interviewingPanel.getAnswerFields()) {
					if(okayToContinue) {
							Answers.setAnswerForInterviewAndQuestion(interviewId, question, 
									field.getAnswer(),field.getOtherText(),field.getSkipReason(pageFlags));
					} else if(consistent) {
						field.setNotification(
								field.answeredOrRefused(pageFlags) ?
										"" : "Unanswered");
					} else {
						field.setNotification(
								field.consistent(pageFlags) ?
										"" : field.inconsistencyReason(pageFlags));
					}
				}
				if(okayToContinue) {
					setResponsePage(
							askNextUnanswered(interviewId,question,
									new InterviewingNetworkPage(interviewId,question)));
				}
			}
		};
		
		AnswerFormFieldPanel field = AnswerFormFieldPanel.getInstance("question",question,interviewId);
		Answer answer = Answers.getAnswerForInterviewAndQuestion(interviewId, question);
		if(answer != null) {
			field = AnswerFormFieldPanel.getInstance("question",
					question,answer.getValue(),answer.getOtherSpecifyText(),answer.getSkipReason(),interviewId);
		}
		field.setAutoFocus();
		form.add(field);
		
		interviewingPanel = 
			new InterviewingPanel("interviewingPanel",question,Lists.newArrayList(field),interviewId);
		form.add(interviewingPanel);
		
		add(form);

		add(new Link("backwardLink") {
			public void onClick() {
				EgonetPage page = 
					askPrevious(interviewId,question,new InterviewingNetworkPage(interviewId,question));
				if(page != null) {
					setResponsePage(page);
				}
			}
		});
		Link forwardLink = new Link("forwardLink") {
			public void onClick() {
				EgonetPage page = 
					askNext(interviewId,question,new InterviewingNetworkPage(interviewId,question));
				if(page != null) {
					setResponsePage(page);
				}
			}
		};
		add(forwardLink);
		if(! AnswerFormFieldPanel.okayToContinue(
				interviewingPanel.getAnswerFields(),
				interviewingPanel.pageFlags())) 
		{
			forwardLink.setVisible(false);
		}

	}

	private void buildNetworkImage()
	{
		Interview interview = Interviews.getInterview(this.interviewId);
		List<Alter> interviewAlters = Alters.getForInterview(this.interviewId);

		/*
		 * Retrieve ID numbers for the questions used to set node/edge parameters,
		 * and for the Expression that defines whether edges are present between
		 * pairs of alters. For the parameter questions, retrieve the interviewee's
		 * answers to these ALTER/ALTER_PAIR questions and create an associative map
		 * matching each alter/alter-pair to the interviewee's answer about them
		 * from the relevant question.
		 */
		Long relExprId = question.getNetworkRelationshipExprId();
		Long nSizeQId = question.getNetworkNSizeQId();
		Long nShapeQId = question.getNetworkNShapeQId();
		Long nColorQId = question.getNetworkNColorQId();
		Long eSizeQId = question.getNetworkESizeQId();
		Long eColorQId = question.getNetworkEColorQId();

		Answer nSizeAnswer = null;
		Answer nShapeAnswer = null;
		Answer nColorAnswer = null;
		Answer eSizeAnswer = null;
		Answer eColorAnswer = null;

		Question nSizeQuestion = null;
		Question nShapeQuestion = null;
		Question nColorQuestion = null;
		Question eSizeQuestion = null;
		Question eColorQuestion = null;

		Map<Long, String> nSizeResponseMap = null;
		Map<Long, String> nShapeResponseMap = null;
		Map<Long, String> nColorResponseMap = null;
		Map<PairUni<Long>, String> eSizeResponseMap = null;
		Map<PairUni<Long>, String> eColorResponseMap = null;

		/*
		 * iterate through alters, asking about each for the node size questions, and each pair for the edge questions
		 */
		if (relExprId != null)
		{
			List<Question> allQuestions = Lists.newArrayList();
			if (nSizeQId != null)
			{
				nSizeQuestion = Questions.getQuestion(nSizeQId);
				nSizeResponseMap = Maps.newHashMap();
				allQuestions.add(nSizeQuestion);
			}
			if (nShapeQId != null)
			{
				nShapeQuestion = Questions.getQuestion(nShapeQId);
				nShapeResponseMap = Maps.newHashMap();
				allQuestions.add(nShapeQuestion);
			}
			if (nColorQId != null)
			{
				nColorQuestion = Questions.getQuestion(nColorQId);
				nColorResponseMap = Maps.newHashMap();
				allQuestions.add(nColorQuestion);
			}
			if (eSizeQId != null)
			{
				eSizeQuestion = Questions.getQuestion(eSizeQId);
				eSizeResponseMap = Maps.newHashMap();
				allQuestions.add(eSizeQuestion);
			}
			if (eColorQId != null)
			{
				eColorQuestion = Questions.getQuestion(eColorQId);
				eColorResponseMap = Maps.newHashMap();
				allQuestions.add(eColorQuestion);
			}

			Map<Long,String> optionIdToValue = Maps.newTreeMap();
			for(Question question : allQuestions) {
				if(question.getAnswerType().equals(Answer.AnswerType.SELECTION) || 
						question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION))
				{
					for(QuestionOption option : Options.getOptionsForQuestion(question.getId())) {
						optionIdToValue.put(option.getId(), option.getValue());
					}
				}
			}

			for (final Alter a1 : interviewAlters)
			{
				ArrayList<Alter> a1List = new ArrayList<Alter>(){{ add(a1); }};
				if (nSizeQuestion != null)
				{
					nSizeResponseMap.put(a1.getId(), showAnswer(optionIdToValue,
																nSizeQuestion,
																Answers.getAnswerForInterviewQuestionAlters(interview, nSizeQuestion, a1List)));

				}
				if (nShapeQuestion != null)
				{
					nShapeResponseMap.put(a1.getId(), showAnswer(optionIdToValue,
																nShapeQuestion,
																Answers.getAnswerForInterviewQuestionAlters(interview, nShapeQuestion, a1List)));
				}
				if (nColorQuestion != null)
				{
					nColorResponseMap.put(a1.getId(), showAnswer(optionIdToValue,
																nColorQuestion,
																Answers.getAnswerForInterviewQuestionAlters(interview, nColorQuestion, a1List)));
				}
				for (final Alter a2 : interviewAlters)
				{
					if (a2.getId() > a1.getId())
					{
						ArrayList<Alter> a1a2List = new ArrayList<Alter>() {{ add(a1); add(a2); }};
						if (eSizeQuestion != null)
						{
							eSizeResponseMap.put(new PairUni<Long>(a1.getId(), a2.getId()),  
												showAnswer(optionIdToValue,
															eSizeQuestion,
															Answers.getAnswerForInterviewQuestionAlters(interview, eSizeQuestion, a1a2List)));
						}
						if (eColorQuestion != null)
						{
							eColorResponseMap.put(new PairUni<Long>(a1.getId(), a2.getId()),  
												showAnswer(optionIdToValue,
															eColorQuestion,
															Answers.getAnswerForInterviewQuestionAlters(interview, eColorQuestion, a1a2List)));
						}
					}
				}
			}
		}
		else
		{
			/*
			 * ETHAN - This point should not be reached. No expression has been specified for edge-creation.
			 * TODO: Add exception/error warning code.
			 */
		}

		/*
		 * Retrieve a network with the interviewee's alters as the graph nodes, and edges
		 * as defined by the selected Expression.
		 */
		Network<Alter> questionNetwork = null;
		questionNetwork = Analysis.getNetworkForInterview(interview, Expressions.get(relExprId));

		networkImage = new NetworkImage<Alter>("networkImage", questionNetwork);

		/*
		 * Set node/edge transformers for the available parameters. If no question was selected for
		 * a parameter when the interview was first created, the transformer will be null here,
		 * and the parameter will not be varied during image-creation in NetworkService.java
		 */
		networkImage.setNodeColorizer(newAlterVtxPainter(questionNetwork, nColorResponseMap));
		/*
		 * Set the node shape and size. If the size question has an answer in the 
		 * NUMERICAL format, the transformer needs the question so that it can find
		 * the min/max value allowed for that answer.
		 */
		if (nSizeQuestion != null && nSizeQuestion.getAnswerType() == AnswerType.NUMERICAL)
			networkImage.setNodeShaper(newAlterVtxShaper(questionNetwork, nShapeResponseMap, nSizeResponseMap, nSizeQuestion));
		else
			networkImage.setNodeShaper(newAlterVtxShaper(questionNetwork, nShapeResponseMap, nSizeResponseMap));
			 
		networkImage.setEdgeColorizer(newAlterEdgePainter(questionNetwork, eColorResponseMap));
		networkImage.setEdgeSizer(newAlterEdgeSizer(questionNetwork, eSizeResponseMap));

		networkImage.setDimensions(1200,800);
			 
		add(networkImage);
	}

	/*
	 * Essentially the same method as the one by the same name in Analysis.java
	 * The Analysis.java method is private, and also can return "0" in a case where it fails
	 * to parse out an answer. "0" is part of a valid answer for most SELECTION or
	 * MULTIPLE_SELECTION questions, so we instead return null in the case where answer.getValue() == null
	 */
	private String showAnswer(Map<Long, String> optionIdToValue, Question question, Answer answer)
	{
		if(answer == null) {
			return null;
		}
		if(question.getAnswerType().equals(Answer.AnswerType.NUMERICAL) ||
				question.getAnswerType().equals(Answer.AnswerType.TEXTUAL) ||
				question.getAnswerType().equals(Answer.AnswerType.TEXTUAL_PP) ||
				question.getAnswerType().equals(Answer.AnswerType.DATE) ||
				question.getAnswerType().equals(Answer.AnswerType.TIME_SPAN))
				{
			return answer.getValue();
		}
		if(question.getAnswerType().equals(Answer.AnswerType.SELECTION) ||
				question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION))
		{
			String value = answer.getValue();
			if(value == null || value.isEmpty()) {
				return null;
			}
			List<String> selectedOptions = Lists.newArrayList();
			for(String optionIdString : value.split(",")) {
				Long optionId = Long.parseLong(optionIdString);
				String optionValue = optionIdToValue.get(optionId);
				if(optionValue != null) {
					selectedOptions.add(optionValue);
				}
			}
			return Joiner.on("; ").join(selectedOptions);
		}
		throw new RuntimeException("Unable to answer for answer type: "+question.getAnswerType());
	}

	//                    forward, unansweredOnly
	// askNextUnanswered: true,    true
	//           askNext: true,    false
	//       askPrevious: false,   false
	
	public static EgonetPage askNextUnanswered(
			Long interviewId,Question currentQuestion, EgonetPage comeFrom) 
	{
		Question nextNetworkQuestion = 
			Interviewing.nextNetworkQuestionForInterview(interviewId,currentQuestion,true,true);
		if(nextNetworkQuestion != null) {
			EgonetPage nextNetworkPage = new InterviewingNetworkPage(interviewId, nextNetworkQuestion);
			return possiblyReplaceNextQuestionPageWithPreface(
					interviewId,nextNetworkPage,currentQuestion,nextNetworkQuestion,
					comeFrom,nextNetworkPage);
		}
		return new InterviewingConclusionPage(interviewId);
	}
	public static EgonetPage askNext(Long interviewId,Question currentQuestion, EgonetPage comeFrom) 
	{
		Question nextNetworkQuestion = 
			Interviewing.nextNetworkQuestionForInterview(interviewId,currentQuestion,true,false);
		if(nextNetworkQuestion != null) {
			EgonetPage nextPage = new InterviewingNetworkPage(interviewId, nextNetworkQuestion);
			return possiblyReplaceNextQuestionPageWithPreface(
					interviewId,nextPage,currentQuestion,nextNetworkQuestion,
					comeFrom,nextPage);
		}
		return new InterviewingConclusionPage(interviewId);
	}
	public static EgonetPage askPrevious(Long interviewId,Question currentQuestion, EgonetPage comeFrom) {
		Question previousNetworkQuestion = 
			Interviewing.nextNetworkQuestionForInterview(interviewId,currentQuestion,false,false);
		EgonetPage previousPage = previousNetworkQuestion == null ? null :
			new InterviewingNetworkPage(interviewId, previousNetworkQuestion);
		return possiblyReplaceNextQuestionPageWithPreface(
				interviewId,previousPage,previousNetworkQuestion,currentQuestion,
				previousPage,comeFrom);
	}
	
	public String toString() {
		return question.getType()+" : "+question.getTitle();
	}

	/*
	 * Colors for the vertices/edges. Edge colors use essentially the same pallete
	 * as vertex colors, but are darker so the vertices are easier to see.
	 */
	private static Color[] vColors = new Color[] { new Color(117, 134, 144),
												   new Color(78, 205, 196),
												   new Color(199, 244, 100),
												   new Color(255, 107, 107),
												   new Color(220, 105, 135),
												   new Color(180, 105, 220),
												   new Color(210, 230, 255)
												};
	private static Color[] eColors = new Color[] { new Color(63, 74, 84),
												   new Color(59, 155, 147),
												   new Color(125, 152, 61),
												   new Color(145, 68, 128),
												   new Color(147, 57, 66),
												   new Color(55, 82, 162),
												};

	/*
	 * Given an alter, set that alter's vertex color depending on the interviewee's answer
	 * to the node-color question.
	 */
	private static Transformer<Alter, Paint> newAlterVtxPainter(final Network<Alter> network, final Map<Long, String> answers)
	{
		return
			new Transformer<Alter, Paint>()
			{
				public Paint transform(Alter a)
				{
					Color defaultColor = vColors[6];
					if (answers == null)
						return defaultColor;

					String ans = answers.get(a.getId());
					if (ans == null)
					{
						return defaultColor;
					}
					if (ans == null)
						return defaultColor;

					/*
					 * Vertex color order was modified here from their array position after testing
					 * with questions where not all colors were used. Thus, the slightly odd sequence
					 * of array indices.
					 */
					if (ans.contains("0"))
						return vColors[1];
					if (ans.contains("1"))
						return vColors[2];
					if (ans.contains("2"))
						return vColors[3];
					if (ans.contains("3"))
						return vColors[0];
					if (ans.contains("4"))
						return vColors[5];
					
					return defaultColor;
					
				}
			};
	}


	/*
	 * Predefined shapes that can be used for vertex rendering. We use
	 * 4-, 5-, and 6-sided regular polygons, as well as (the default) circle.
	 * Also available are a rounded rectangle and an oval, both wider than they
	 * are tall.
	 */
	private static int vtxPentagonSize = 14	;
	private static int c1 = (int)(0.3090 * vtxPentagonSize);
	private static int c2 = (int)(0.8090 * vtxPentagonSize);
	private static int s1 = (int)(0.9511 * vtxPentagonSize);
	private static int s2 = (int)(0.5878 * vtxPentagonSize);
	private static int[] xValsPentagon = { 0, s1, s2, -s2, -s1 };
	private static int[] yValsPentagon = { -vtxPentagonSize, -c1, c2, c2, -c1 };
	private static Polygon vertexPentagon = new Polygon(xValsPentagon, yValsPentagon, 5);

	private static int vtxHexagonSize = 13;
	private static int hexX = (int)(0.866 * vtxHexagonSize);
	private static int hexY = (int)(vtxHexagonSize / 2.0);
	private static int[] xValsHexagon = { -hexX, -hexX, 0, hexX, hexX, 0 };
	private static int[] yValsHexagon = { -hexY, hexY, vtxHexagonSize, hexY, -hexY, -vtxHexagonSize };
	private static Polygon vertexHexagon = new Polygon(xValsHexagon, yValsHexagon, 6);

	private static int vtxCircleRadius = 12;
	private static Ellipse2D vertexCircle = new Ellipse2D.Float(-vtxCircleRadius, -vtxCircleRadius, vtxCircleRadius * 2, vtxCircleRadius * 2);

	private static int vtxSquareHalfSize = 11;
	private static Rectangle2D vertexSquare = new Rectangle2D.Float(-vtxSquareHalfSize, -vtxSquareHalfSize, vtxSquareHalfSize * 2, vtxSquareHalfSize * 2);

	private static int vtxRoundRectHalfWidth = 14;
	private static int vtxRoundRectHalfHeight = 8;
	private static float vtxRoundRectArcWidth = 6;
	private static RoundRectangle2D vertexRoundRect = new RoundRectangle2D.Float(-vtxRoundRectHalfWidth, 
																				-vtxRoundRectHalfHeight,
																				vtxRoundRectHalfWidth * 2,
																				vtxRoundRectHalfHeight * 2,
																				vtxRoundRectArcWidth,
																				vtxRoundRectArcWidth);
																				
	private static int vtxOvalHalfWidth = 14;
	private static int vtxOvalHalfHeight = 8;
	private static Ellipse2D vertexOval = new Ellipse2D.Float(-vtxOvalHalfWidth, -vtxOvalHalfHeight, vtxOvalHalfWidth * 2, vtxOvalHalfHeight * 2);

	/*
	 * For questions with NUMERICAL answer type and a defined min/max value, we can scale the vertex size 
	 * based on where the interviewee's answer falls in the valid range. 
	 * 
	 * The scaling factor for the vertex will be:
	 *		[(answer - minAnswer) / (maxAnswer - minAnswer)] * (maxScale - minScale) + minScale
	 */
	private static float minSizeScale = 1.0f;
	private static float maxSizeScale = 4.0f;
	private static float vtxSizeScaleRange = maxSizeScale - minSizeScale;

	/*
	 * Given an alter, set that alter's vertex shape depending on the interviewee's answer
	 * to the node-color question. This transformer also provides the functionality for 
	 * varying the vertex size, based on the node-size question. (If only one of the two
	 * is used for a particular NETWORK question, this transformer will leave the other
	 * parameter unmodified from its default for all vertices.) 
	 * 
	 * The shapes defined above
	 * all occupy roughly the same amount of pixel space as defined, so that shape and size
	 * can both be used without causing perception problems. (If the pentagon were smaller
	 * the circle, a "large" pentagon wouldn't necessarily have the proper size relative
	 * to a "medium" circle.)
	 */

	private static Transformer<Alter, Shape> newAlterVtxShaper(final Network<Alter> network, 
															final Map<Long, String> shapeAnswers,
															final Map<Long, String> sizeAnswers)
	{
		return newAlterVtxShaper(network, shapeAnswers, sizeAnswers, null);
	}

	private static Transformer<Alter, Shape> newAlterVtxShaper(final Network<Alter> network, 
															final Map<Long, String> shapeAnswers,
															final Map<Long, String> sizeAnswers,
															final Question sizeQuestion)
	{
		return
			new Transformer<Alter, Shape>()
			{
				public Shape transform(Alter a)
				{
					Shape vtxShape = null;
					if (shapeAnswers != null)
					{
						String shapeAns = shapeAnswers.get(a.getId());
						if (shapeAns != null)
						{
							if (shapeAns.contains("0"))
								vtxShape = vertexSquare;
							else if (shapeAns.contains("1"))
								vtxShape = vertexPentagon;
							else if (shapeAns.contains("2"))
								vtxShape = vertexHexagon;
							else if (shapeAns.contains("3"))
								vtxShape = vertexRoundRect;
							else if (shapeAns.contains("4"))
								vtxShape = vertexOval;
						}
					}
					if (vtxShape == null)
						vtxShape = vertexCircle;
					
					if (sizeAnswers == null)
						return vtxShape;

					String sizeAns = sizeAnswers.get(a.getId());
					if (sizeQuestion != null && sizeQuestion.getAnswerType() == AnswerType.NUMERICAL)
					{
						try
						{
							Integer minAnswer = sizeQuestion.getMinLiteral();
							Integer maxAnswer = sizeQuestion.getMaxLiteral();

							float answer = Float.parseFloat(sizeAns);

							answer = (float)(answer > maxAnswer? maxAnswer : answer);
							answer = (float)(answer < minAnswer? minAnswer : answer);
							float scale = (answer - minAnswer) / (float)(maxAnswer - minAnswer) * vtxSizeScaleRange + minSizeScale;
							AffineTransform vtxResize = new AffineTransform();
							vtxResize.scale(scale, scale);
							return vtxResize.createTransformedShape(vtxShape);
						}
						catch (RuntimeException rtx)
						{
							return vtxShape;
						}

					}
					if (sizeAns != null)
					{
						AffineTransform vtxResize = new AffineTransform();
						if (sizeAns.contains("0"))
						{
							vtxResize.scale(1.35, 1.35);
							return vtxResize.createTransformedShape(vtxShape);
						}
						if (sizeAns.contains("1"))
						{
							vtxResize.scale(1.7, 1.7);
							return vtxResize.createTransformedShape(vtxShape);
						}
						if (sizeAns.contains("2"))
						{
							vtxResize.scale(2.4, 2.4);
							return vtxResize.createTransformedShape(vtxShape);
						}
						if (sizeAns.contains("3"))
						{
							vtxResize.scale(3.3, 3.3);
							return vtxResize.createTransformedShape(vtxShape);
						}
						if (sizeAns.contains("4"))
						{
							vtxResize.scale(4.3, 4.3);
							return vtxResize.createTransformedShape(vtxShape);
						}
						
					}
					return vtxShape;

					
				}
			};
	}

	/*
	 * Given a pair of alters, determine the interviewee's response to the ALTER_PAIR
	 * edge-color question (if applicable). Set the edge color based on that response.
	 */
	private static Transformer<PairUni<Alter>, Paint> newAlterEdgePainter(final Network<Alter> network, final Map<PairUni<Long>, String> answers)
	{
		return
			new Transformer<PairUni<Alter>, Paint>()
			{
				public Paint transform(PairUni<Alter> alters)
				{
					Long idA1 = alters.getFirst().getId();
					Long idA2 = alters.getSecond().getId();
					String ans = null;
					Color defaultColor = eColors[3];
					try
					{
						/*
						 * getNetworkForInterview creates a network with edges where 
						 * the first alter id listed is lower than the second
						 */
						if (idA1 < idA2)
							ans = answers.get(new PairUni<Long>(idA1, idA2));
						else
							ans = answers.get(new PairUni<Long>(idA2, idA1));

						if (ans == null)
							return defaultColor;
						
						if (ans == null)
							return defaultColor;

						if (ans.contains("0"))
							return eColors[1];
						if (ans.contains("1"))
							return eColors[2];
						if (ans.contains("2"))
							return eColors[3];
						if (ans.contains("3"))
							return eColors[4];
						if (ans.contains("4"))
							return eColors[5];
						
						return defaultColor;
					}
					catch (RuntimeException rtx)
					{
						return defaultColor;
					}

				}
			};

	}

	/*
	 * Given a pair of alters, determine the interviewee's response to the ALTER_PAIR
	 * edge-size question (if applicable). Set the edge width based on that response.
	 */

	private static Transformer<PairUni<Alter>, Stroke> newAlterEdgeSizer(final Network<Alter> network, final Map<PairUni<Long>, String> answers)
	{
		return
			new Transformer<PairUni<Alter>, Stroke>()
			{
				public Stroke transform(PairUni<Alter> alters)
				{
					Long idA1 = alters.getFirst().getId();
					Long idA2 = alters.getSecond().getId();
					String ans = null;

					BasicStroke defaultStroke = new BasicStroke(1.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
					try
					{
						/*
						 * getNetworkForInterview creates a network with edges where 
						 * the first alter id listed is lower than the second
						 */
						if (idA1 < idA2)
							ans = answers.get(new PairUni<Long>(idA1, idA2));
						else
							ans = answers.get(new PairUni<Long>(idA2, idA1));

						if (ans == null)
							return defaultStroke;
						
						if (ans == null)
							return defaultStroke;
				
						if (ans.contains("0"))
							return new BasicStroke(2.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
						if (ans.contains("1"))
							return new BasicStroke(4.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
						if (ans.contains("2"))
							return new BasicStroke(5.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
						if (ans.contains("3"))
							return new BasicStroke(7.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
						if (ans.contains("4"))
							return new BasicStroke(10.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
						
						return defaultStroke;
					}
					catch (RuntimeException rtx)
					{
						return defaultStroke;
					}

				}
			};

	}
}
