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
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.commons.collections15.Transformer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Interview;
import net.sf.egonet.persistence.Answers;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Alters;
import net.sf.egonet.persistence.Expressions;
import net.sf.egonet.persistence.Analysis;
import net.sf.egonet.persistence.Questions;
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

		Map<Long, Answer> nSizeAnswerMap = null;
		Map<Long, Answer> nShapeAnswerMap = null;
		Map<Long, Answer> nColorAnswerMap = null;
		Map<PairUni<Long>, Answer> eSizeAnswerMap = null;
		Map<PairUni<Long>, Answer> eColorAnswerMap = null;

		/*
		 * iterate through alters, asking about each for the node size questions, and each pair for the edge questions
		 */
		if (relExprId != null)
		{
			if (nSizeQId != null)
			{
				nSizeQuestion = Questions.getQuestion(nSizeQId);
				nSizeAnswerMap = Maps.newHashMap();
			}
			if (nShapeQId != null)
			{
				nShapeQuestion = Questions.getQuestion(nShapeQId);
				nShapeAnswerMap = Maps.newHashMap();
			}
			if (nColorQId != null)
			{
				nColorQuestion = Questions.getQuestion(nColorQId);
				nColorAnswerMap = Maps.newHashMap();
			}
			if (eSizeQId != null)
			{
				eSizeQuestion = Questions.getQuestion(eSizeQId);
				eSizeAnswerMap = Maps.newHashMap();
			}
			if (eColorQId != null)
			{
				eColorQuestion = Questions.getQuestion(eColorQId);
				eColorAnswerMap = Maps.newHashMap();
			}

			for (final Alter a1 : interviewAlters)
			{
				ArrayList<Alter> a1List = new ArrayList<Alter>(){{ add(a1); }};
				if (nSizeQuestion != null)
				{
					nSizeAnswerMap.put(a1.getId(), Answers.getAnswerForInterviewQuestionAlters(interview, nSizeQuestion, a1List));
				}
				if (nShapeQuestion != null)
				{
					nShapeAnswerMap.put(a1.getId(), Answers.getAnswerForInterviewQuestionAlters(interview, nShapeQuestion, a1List));
				}
				if (nColorQuestion != null)
				{
					nColorAnswerMap.put(a1.getId(), Answers.getAnswerForInterviewQuestionAlters(interview, nColorQuestion, a1List));
				}
				for (final Alter a2 : interviewAlters)
				{
					if (a2.getId() > a1.getId())
					{
						ArrayList<Alter> a1a2List = new ArrayList<Alter>() {{ add(a1); add(a2); }};
						if (eSizeQuestion != null)
						{
							eSizeAnswerMap.put(new PairUni<Long>(a1.getId(), a2.getId()), 
								Answers.getAnswerForInterviewQuestionAlters(interview, eSizeQuestion, a1a2List));
						}
						if (eColorQuestion != null)
						{
							eColorAnswerMap.put(new PairUni<Long>(a1.getId(), a2.getId()), 
								Answers.getAnswerForInterviewQuestionAlters(interview, eColorQuestion, a1a2List));
						}
					}
				}
			}
		}
		else
		{
			/*
			 * This point should not be reached. No expression has been specified for edge-creation.
			 */
		}

		/*
		 * Retrieve a network with the interviewee's alters as the graph nodes, and edges
		 * as defined by the selected Expression.
		 */
		Network<Alter> questionNetwork = Analysis.getNetworkForInterview(interview, Expressions.get(relExprId));
		networkImage = new NetworkImage<Alter>("networkImage", questionNetwork);

		/*
		 * Set node/edge transformers for the available parameters. If no question was selected for
		 * a parameter when the interview was first created, the transformer will be null here,
		 * and the parameter will not be varied during image-creation in NetworkService.java
		 */
		networkImage.setNodeColorizer(newAlterVtxPainter(questionNetwork, nColorAnswerMap));
		networkImage.setNodeShaper(newAlterVtxShaper(questionNetwork, nShapeAnswerMap, nSizeAnswerMap));
		networkImage.setEdgeColorizer(newAlterEdgePainter(questionNetwork, eColorAnswerMap));
		networkImage.setEdgeSizer(newAlterEdgeSizer(questionNetwork, eSizeAnswerMap));
		networkImage.setDimensions(1200,800);
		add(networkImage);
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
												   new Color(220, 105, 135) };
	private static Color[] eColors = new Color[] { new Color(63, 74, 84),
												   new Color(59, 155, 147),
												   new Color(125, 152, 61),
												   new Color(145, 68, 128),
												   new Color(147, 57, 66) };

	/*
	 * Given an alter, set that alter's vertex color depending on the interviewee's answer
	 * to the node-color question.
	 */
	private static Transformer<Alter, Paint> newAlterVtxPainter(final Network<Alter> network, final Map<Long, Answer> answers)
	{
		return
			new Transformer<Alter, Paint>()
			{
				public Paint transform(Alter a)
				{
					if (answers == null)
						return vColors[4];

					Answer ans = answers.get(a.getId());
					if (ans == null)
					{
						return vColors[4];
					}
					String ansValue = ans.getValue();
					if (ansValue == null)
						return vColors[4];

					if (ansValue.contains("0"))
						return vColors[1];
					if (ansValue.contains("1"))
						return vColors[2];
					if (ansValue.contains("2"))
						return vColors[3];
					
					return vColors[4];
					
				}
			};
	}


	/*
	 * Predefined shapes that can be used for vertex rendering. We use
	 * 4-, 5-, and 6-sided regular polygons, as well as (the default) circle.
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
															final Map<Long, Answer> shapeAnswers,
															final Map<Long, Answer> sizeAnswers)
	{
		return
			new Transformer<Alter, Shape>()
			{
				public Shape transform(Alter a)
				{
					Shape vtxShape = null;
					if (shapeAnswers != null)
					{
						Answer shapeAns = shapeAnswers.get(a.getId());
						if (shapeAns != null)
						{
							String shapeAnsValue = shapeAns.getValue();
							if (shapeAnsValue != null)
							{
								if (shapeAnsValue.contains("0"))
									vtxShape = vertexSquare;
								else if (shapeAnsValue.contains("1"))
									vtxShape = vertexPentagon;
								else if (shapeAnsValue.contains("2"))
									vtxShape = vertexHexagon;
							}
						}
					}
					if (vtxShape == null)
						vtxShape = vertexCircle;

					if (sizeAnswers == null)
						return vtxShape;

					Answer sizeAns = sizeAnswers.get(a.getId());
					if (sizeAns != null)
					{
						String sizeAnsValue = sizeAns.getValue();
						if (sizeAnsValue != null)
						{
							AffineTransform vtxResize = new AffineTransform();
							if (sizeAnsValue.contains("0"))
							{
								vtxResize.scale(1.45, 1.45);
								return vtxResize.createTransformedShape(vtxShape);
							}
							if (sizeAnsValue.contains("1"))
							{
								vtxResize.scale(1.85, 1.85);
								return vtxResize.createTransformedShape(vtxShape);
							}
							if (sizeAnsValue.contains("2"))
							{
								vtxResize.scale(2.6, 2.6);
								return vtxResize.createTransformedShape(vtxShape);
							}
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
	private static Transformer<PairUni<Alter>, Paint> newAlterEdgePainter(final Network<Alter> network, final Map<PairUni<Long>, Answer> answers)
	{
		return
			new Transformer<PairUni<Alter>, Paint>()
			{
				public Paint transform(PairUni<Alter> alters)
				{
					Long idA1 = alters.getFirst().getId();
					Long idA2 = alters.getSecond().getId();
					Answer ans = null;
					/*
					 * getNetworkForInterview creates a network with edges where 
					 * the first alter id listed is lower than the second
					 */
					if (idA1 < idA2)
						ans = answers.get(new PairUni<Long>(idA1, idA2));
					else
						ans = answers.get(new PairUni<Long>(idA2, idA1));

					if (ans == null)
						return eColors[0];
					
					String ansValue = ans.getValue();
					if (ansValue == null)
						return eColors[0];

					if (ansValue.contains("0"))
						return eColors[1];
					if (ansValue.contains("1"))
						return eColors[2];
					if (ansValue.contains("2"))
						return eColors[3];
					
					return eColors[0];

				}
			};

	}

	/*
	 * Given a pair of alters, determine the interviewee's response to the ALTER_PAIR
	 * edge-size question (if applicable). Set the edge width based on that response.
	 */

	private static Transformer<PairUni<Alter>, Stroke> newAlterEdgeSizer(final Network<Alter> network, final Map<PairUni<Long>, Answer> answers)
	{
		return
			new Transformer<PairUni<Alter>, Stroke>()
			{
				public Stroke transform(PairUni<Alter> alters)
				{
					Long idA1 = alters.getFirst().getId();
					Long idA2 = alters.getSecond().getId();
					Answer ans = null;
					/*
					 * getNetworkForInterview creates a network with edges where 
					 * the first alter id listed is lower than the second
					 */
					if (idA1 < idA2)
						ans = answers.get(new PairUni<Long>(idA1, idA2));
					else
						ans = answers.get(new PairUni<Long>(idA2, idA1));

					if (ans == null)
						return new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
					
					String ansValue = ans.getValue();
					if (ansValue == null)
						return new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
			
					if (ansValue.contains("0"))
						return new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
					if (ansValue.contains("1"))
						return new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
					if (ansValue.contains("2"))
						return new BasicStroke(8.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
					
						return new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

				}
			};

	}
}
