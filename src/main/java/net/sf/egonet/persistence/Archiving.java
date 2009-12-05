package net.sf.egonet.persistence;

import java.io.StringWriter;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Answer.AnswerType;
import net.sf.egonet.model.Question.QuestionType;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.hibernate.Session;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

public class Archiving {

	public static String getStudyXML(final Study study) {
		return new DB.Action<String>() {
			public String get() {
				return getStudyXML(session, study,false);
			}
		}.execute();
	}
	public static String getRespondentDataXML(final Study study) {
		return new DB.Action<String>() {
			public String get() {
				return getStudyXML(session, study,true);
			}
		}.execute();
	}
	
	private static void addText(Element root, String elementName, String elementText) {
		root.addElement(elementName).addText(elementText == null ? "" : elementText);
	}
	
	public static String getStudyXML(Session session, Study study, Boolean includeInterviews) {
		try {
			Document document = DocumentHelper.createDocument();
			Element studyNode = document.addElement("study")
				.addAttribute("id", study.getId()+"")
				.addAttribute("name", study.getName())
				.addAttribute("key", study.getRandomKey()+"")
				.addAttribute("minAlters", study.getMinAlters()+"")
				.addAttribute("maxAlters", study.getMaxAlters()+"")
				.addAttribute("adjacencyExpressionId", study.getAdjacencyExpressionId()+"");
			addText(studyNode,"introduction",study.getIntroduction());
			addText(studyNode,"egoIdPrompt",study.getEgoIdPrompt());
			addText(studyNode,"alterPrompt",study.getAlterPrompt());
			studyNode.addElement("conclusion").addText(study.getConclusion());
			Element questionsNode = studyNode.addElement("questions");
			Multiset<QuestionType> questionsOfTypeSoFar = TreeMultiset.create();
			for(Question question : Questions.getQuestionsForStudy(session, study.getId(), null)) {
				Element questionNode = questionsNode.addElement("question")
					.addAttribute("id", question.getId()+"")
					.addAttribute("title", question.getTitle())
					.addAttribute("key", question.getRandomKey()+"")
					.addAttribute("answerType", question.getAnswerTypeDB())
					.addAttribute("subjectType", question.getTypeDB())
					.addAttribute("required", question.isRequired()+"")
					// in case ordering == null, I use the order they were pulled from the DB
					.addAttribute("ordering", questionsOfTypeSoFar.count(question.getType())+"")
					.addAttribute("answerReasonExpressionId", question.getAnswerReasonExpressionId()+"");
				questionsOfTypeSoFar.add(question.getType());
				addText(questionNode,"preface",question.getPreface());
				addText(questionNode,"prompt",question.getPrompt());
				addText(questionNode,"citation",question.getCitation());
				if(question.getAnswerType().equals(AnswerType.SELECTION) || 
						question.getAnswerType().equals(AnswerType.MULTIPLE_SELECTION))
				{
					Integer optionsSoFar = 0;
					for(QuestionOption option : Options.getOptionsForQuestion(session, question.getId())) {
						questionNode.addElement("option")
							.addAttribute("id", option.getId()+"")
							.addAttribute("name", option.getName())
							.addAttribute("key", option.getRandomKey()+"")
							.addAttribute("value", option.getValue())
							.addAttribute("ordering", optionsSoFar+"");
						optionsSoFar++; // would prefer to use ordering, but it might be null
					}
				}
			}
			Element expressionsNode = studyNode.addElement("expressions");
			for(Expression expression : Expressions.forStudy(session, study.getId())) {
				Element expressionNode = expressionsNode.addElement("expression")
					.addAttribute("id", expression.getId()+"")
					.addAttribute("name", expression.getName())
					.addAttribute("key", expression.getRandomKey()+"")
					.addAttribute("questionId", expression.getQuestionId()+"")
					.addAttribute("resultForUnanswered", expression.getResultForUnanswered()+"")
					.addAttribute("type", expression.getTypeDB()+"")
					.addAttribute("operator", expression.getOperatorDB()+"");
				addText(expressionNode,"value",expression.getValueDB());
			}
			if(includeInterviews) {
				Element interviewsNode = studyNode.addElement("interviews");
				for(Interview interview : Interviews.getInterviewsForStudy(session, study.getId())) {
					Element interviewNode = interviewsNode.addElement("interview")
						.addAttribute("id", interview.getId()+"")
						.addAttribute("key", interview.getRandomKey()+"");
					Element altersNode = interviewNode.addElement("alters");
					for(Alter alter : Alters.getForInterview(session, interview.getId())) {
						altersNode.addElement("alter")
							.addAttribute("id", alter.getId()+"")
							.addAttribute("name", alter.getName())
							.addAttribute("key", alter.getRandomKey()+"");
					}
					Element answersNode = interviewNode.addElement("answers");
					for(Answer answer : Answers.getAnswersForInterview(session, interview.getId())) {
						Element answerNode = answersNode.addElement("answer")
							.addAttribute("id", answer.getId()+"")
							.addAttribute("key", answer.getRandomKey()+"")
							.addAttribute("questionId", answer.getQuestionId()+"")
							.addAttribute("questionType", answer.getQuestionTypeDB())
							.addAttribute("answerType", answer.getAnswerTypeDB())
							.addAttribute("alterId1", answer.getAlterId1()+"")
							.addAttribute("alterId2", answer.getAlterId2()+"");
						addText(answerNode,"value",answer.getValue());
					}
				}
			}
			
			StringWriter stringWriter = new StringWriter();
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setNewlines(true);
			format.setTrimText(false);
			format.setLineSeparator("\r\n");
			new XMLWriter(stringWriter,format).write(document);
			return stringWriter.toString();
		} catch(Exception ex) {
			throw new RuntimeException("Failed to get XML for study "+study, ex);
		}
	}
}
