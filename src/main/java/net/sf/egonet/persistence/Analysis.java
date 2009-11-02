package net.sf.egonet.persistence;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.hibernate.Session;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Study;
import net.sf.egonet.network.Network;
import net.sf.egonet.network.NetworkService;
import net.sf.egonet.persistence.Expressions.EvaluationContext;
import net.sf.functionalj.tuple.PairUni;
import net.sf.functionalj.tuple.TripleUni;

public class Analysis {
	
	public static BufferedImage getImageForInterview(final Interview interview, final Expression connection) {
		return new DB.Action<BufferedImage>() {
			public BufferedImage get() {
				return getImageForInterview(session, interview, connection);
			}
		}.execute();
	}
	
	public static BufferedImage getImageForInterview(Session session, Interview interview, Expression connection) {
		return NetworkService.createImage(getNetworkForInterview(session,interview,connection));
	}
	

	public static class ImageResourceStream implements IResourceStream {

		private byte[] imagedata;
		
		public ImageResourceStream(BufferedImage image) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
			try {
				encoder.encode(image);
				imagedata = os.toByteArray();
			} catch(Exception ex) {
				throw new RuntimeException("Failed to create image resource stream",ex);
			}
		}
		public void close() throws IOException {
		}
		public String getContentType() {
			return "image/jpeg";
		}
		public InputStream getInputStream()
				throws ResourceStreamNotFoundException {
			return new ByteArrayInputStream(imagedata);
		}
		public Locale getLocale() {
			return null;
		}
		public long length() {
			return imagedata.length;
		}
		public void setLocale(Locale arg0) {
		}
		public Time lastModifiedTime() {
			return Time.now();
		}
	}
	
	
	public static Network getNetworkForInterview(Session session, Interview interview, Expression connection) {
		EvaluationContext context = Expressions.getContext(session, interview);
		Set<AlterNode> alterNodes = Sets.newHashSet();
		for(Alter alter : Alters.getForInterview(session, interview.getId())) {
			alterNodes.add(new AlterNode(alter));
		}
		Set<Network.Edge> edges = Sets.newHashSet();
		for(AlterNode node1 : alterNodes) {
			for(AlterNode node2 : alterNodes) {
				Alter alter1 = node1.getAlter(), alter2 = node2.getAlter();
				ArrayList<Alter> alters = Lists.newArrayList(alter1,alter2);
				if(alter1.getId() < alter2.getId() && Expressions.evaluate(connection, alters, context)) {
					edges.add(new AlterEdge(node1,node2));
				}
			}
		}
		Set<Network.Node> nodes = Sets.newHashSet();
		for(AlterNode node : alterNodes) {
			nodes.add(node);
		}
		return new Network(nodes,edges);
	}
	
	public static class AlterNode implements Network.Node {
		private Alter alter;
		public AlterNode(Alter alter) {
			this.alter = alter;
		}
		public Alter getAlter() {
			return alter;
		}
		public String toString() {
			return alter.getName();
		}
		public boolean equals(Object object) {
			return object instanceof AlterNode && 
			((AlterNode) object).alter.getId().equals(alter.getId());
		}
		public int hashCode() {
			return alter.getId().hashCode();
		}
	}
	
	public static class AlterEdge implements Network.Edge {

		private AlterNode alter1, alter2;
		
		public AlterEdge(AlterNode alter1, AlterNode alter2) {
			this.alter1 = alter1;
			this.alter2 = alter2;
		}
		public AlterNode getNode1() {
			return alter1;
		}
		public AlterNode getNode2() {
			return alter2;
		}
		public String toString() {
			return alter1.toString()+"-"+alter2.toString();
		}
		public boolean equals(Object object) {
			if(object instanceof AlterEdge) {
				AlterEdge edge = (AlterEdge) object;
				return (alter1.equals(edge.getNode1()) && alter2.equals(edge.getNode2())) ||
					(alter2.equals(edge.getNode2()) && alter1.equals(edge.getNode1()));
			}
			return false;
		}
		public int hashCode() {
			return alter1.hashCode()/2 + alter2.hashCode()/2;
		}
	}
	
	public static String getRawDataCSVForStudy(final Study study) {
		return new DB.Action<String>() {
			public String get() {
				return getRawDataCSVForStudy(session, study);
			}
		}.execute();
	}
	
	public static String getRawDataCSVForStudy(Session session, Study study) {
		try {
			StringWriter stringWriter = new StringWriter();
			CSVWriter writer = new CSVWriter(stringWriter);
			List<Interview> interviews = Interviews.getInterviewsForStudy(session, study.getId());

			List<Question> egoIdQuestions = 
				Questions.getQuestionsForStudy(session, study.getId(), Question.QuestionType.EGO_ID);
			List<Question> egoQuestions = 
				Questions.getQuestionsForStudy(session, study.getId(), Question.QuestionType.EGO);
			List<Question> alterQuestions = 
				Questions.getQuestionsForStudy(session, study.getId(), Question.QuestionType.ALTER);
			
			// TODO: collection of options so I don't have to re-fetch for each interview
			
			List<String> header = Lists.newArrayList();
			header.add("Interview number");
			for(Question question : egoIdQuestions) {
				header.add(question.getTitle());
			}
			for(Question question : egoQuestions) {
				header.add(question.getTitle());
			}
			header.add("Alter number");
			header.add("Alter name");
			for(Question question : alterQuestions) {
				header.add(question.getTitle());
			}
			writer.writeNext(header.toArray(new String[]{}));
			
			for(Integer interviewIndex = 1; interviewIndex < interviews.size()+1; interviewIndex++) {
				Interview interview = interviews.get(interviewIndex-1);
				EvaluationContext context = Expressions.getContext(session, interview);
				List<Alter> alters = Alters.getForInterview(interview.getId());
				for(Integer alterIndex = 1; alterIndex < alters.size()+1; alterIndex++) {
					Alter alter = alters.get(alterIndex-1);
					List<String> output = Lists.newArrayList();
					output.add(interviewIndex.toString());
					for(Question question : egoIdQuestions) {
						output.add(showAnswer(session,question,context.qidToEgoAnswer.get(question.getId())));
					}
					for(Question question : egoQuestions) {
						output.add(showAnswer(session,question,context.qidToEgoAnswer.get(question.getId())));
					}
					output.add(alterIndex.toString());
					output.add(alter.getName());
					for(Question question : alterQuestions) {
						output.add(showAnswer(session,question,
								context.qidAidToAlterAnswer.get(
										new PairUni<Long>(question.getId(),alter.getId()))));
					}
					writer.writeNext(output.toArray(new String[]{}));
				}
			}
			
			writer.writeNext(new String[]{}); // blank line between tables

			List<Question> alterPairQuestions = 
				Questions.getQuestionsForStudy(session, study.getId(), Question.QuestionType.ALTER_PAIR);
			

			header = Lists.newArrayList();
			header.add("Interview number");
			for(Question question : egoIdQuestions) {
				header.add(question.getTitle());
			}
			header.add("Alter 1 number");
			header.add("Alter 1 name");
			header.add("Alter 2 number");
			header.add("Alter 2 name");
			for(Question question : alterPairQuestions) {
				header.add(question.getTitle());
			}
			writer.writeNext(header.toArray(new String[]{}));
			
			for(Integer interviewIndex = 1; interviewIndex < interviews.size()+1; interviewIndex++) {
				Interview interview = interviews.get(interviewIndex-1);
				EvaluationContext context = Expressions.getContext(session, interview);
				List<Alter> alters = Alters.getForInterview(interview.getId());
				for(Integer alter1Index = 1; alter1Index < alters.size()+1; alter1Index++) {
					Alter alter1 = alters.get(alter1Index-1);
					for(Integer alter2Index = alter1Index+1; alter2Index < alters.size()+1; alter2Index++) {
						Alter alter2 = alters.get(alter2Index-1);
						List<String> output = Lists.newArrayList();
						output.add(interviewIndex.toString());
						for(Question question : egoIdQuestions) {
							output.add(showAnswer(session,question,context.qidToEgoAnswer.get(question.getId())));
						}
						output.add(alter1Index.toString());
						output.add(alter1.getName());
						output.add(alter2Index.toString());
						output.add(alter2.getName());
						for(Question question : alterPairQuestions) {
							output.add(showAnswer(session,question,
									context.qidA1idA2idToAlterPairAnswer.get(
											new TripleUni<Long>(question.getId(),alter1.getId(),alter2.getId()))));
						}
						writer.writeNext(output.toArray(new String[]{}));
					}
				}
			}
			
			writer.close();
			return stringWriter.toString();
		} catch(Exception ex) {
			throw new RuntimeException("Unable to output raw data csv for study "+study.getName(),ex);
		}
	}
	
	private static String showAnswer(Session session, Question question, Answer answer) {
		if(answer == null) {
			return null;
		}
		if(question.getAnswerType().equals(Answer.AnswerType.NUMERICAL) ||
				question.getAnswerType().equals(Answer.AnswerType.TEXTUAL))
		{
			return answer.getValue();
		}
		if(question.getAnswerType().equals(Answer.AnswerType.SELECTION) ||
				question.getAnswerType().equals(Answer.AnswerType.MULTIPLE_SELECTION))
		{
			String value = answer.getValue();
			if(value == null || value.isEmpty()) {
				return "";
			}
			List<QuestionOption> options = Options.getOptionsForQuestion(session, question.getId());
			List<String> selectedOptions = Lists.newArrayList();
			for(String optionIdString : value.split(",")) {
				Long optionId = Long.parseLong(optionIdString);
				for(QuestionOption option : options) {
					if(option.getId().equals(optionId)) {
						selectedOptions.add(option.getValue());
					}
				}
			}
			return Joiner.on("; ").join(selectedOptions);
		}
		throw new RuntimeException("Unable to answer for answer type: "+question.getAnswerType());
	}
}
