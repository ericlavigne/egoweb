package net.sf.egonet.persistence;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Question.QuestionType;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
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
			Element studyNode = addStudyNode(document, study);
			Element questionsNode = studyNode.addElement("questions");
			Multiset<QuestionType> questionsOfTypeSoFar = TreeMultiset.create();
			for(Question question : Questions.getQuestionsForStudy(session, study.getId(), null)) {
				addQuestionNode(questionsNode,question, 
						Options.getOptionsForQuestion(session, question.getId()),
						questionsOfTypeSoFar.count(question.getType()));
				questionsOfTypeSoFar.add(question.getType());
			}
			Element expressionsNode = studyNode.addElement("expressions");
			for(Expression expression : Expressions.forStudy(session, study.getId())) {
				addExpressionNode(expressionsNode,expression);
			}
			if(includeInterviews) {
				Element interviewsNode = studyNode.addElement("interviews");
				for(Interview interview : Interviews.getInterviewsForStudy(session, study.getId())) {
					addInterviewNode(interviewsNode,interview,
							Alters.getForInterview(session, interview.getId()),
							Answers.getAnswersForInterview(session, interview.getId()));
				}
			}
			return formatXMLDocument(document);
		} catch(Exception ex) {
			throw new RuntimeException("Failed to get XML for study "+study, ex);
		}
	}
	
	public Study loadStudyXML(Session session, 
			Study studyToUpdate, String studyXML, 
			Boolean updateStudy, Boolean updateRespondentData)
	{
		try {
			Document document = new SAXReader().read(new StringReader(studyXML));
			Element studyElement = document.getRootElement();
			
			Study study = studyToUpdate;
			if(study == null) {
				study = new Study();
				DB.save(session, study);
			} else {
				Long xmlKey = attrLong(studyElement,"key");
				if(xmlKey == null || ! xmlKey.equals(study.getRandomKey())) {
					throw new RuntimeException("Trying to import incompatible study.");
				}
			}
			updateStudyFromNode(study,studyElement);
			Long studyId = study.getId();
			
			
			
			// TODO: Finish pulling data from XML
			return study;
		} catch(Exception ex) {
			throw new RuntimeException("Failed to load XML for study "+studyToUpdate, ex);
		}
	}
	
	private static Element addStudyNode(Document document, Study study) {
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
		addText(studyNode,"conclusion",study.getConclusion());
		return studyNode;
	}
	
	private static void updateStudyFromNode(Study study, Element studyElement) {
		study.setName(attrString(studyElement,"name"));
		study.setRandomKey(attrLong(studyElement,"key"));
		study.setMinAlters(attrInt(studyElement,"minAlters"));
		study.setMaxAlters(attrInt(studyElement,"maxAlters"));
		study.setAdjacencyExpressionId(attrLong(studyElement,"adjacencyExpressionId")); // XXX: remote -> local
		study.setIntroduction(attrText(studyElement,"introduction"));
		study.setEgoIdPrompt(attrText(studyElement,"egoIdPrompt"));
		study.setAlterPrompt(attrText(studyElement,"alterPrompt"));
		study.setConclusion(attrText(studyElement,"conclusion"));
	}
	
	private static String attrText(Element element, String name) {
		Element textElement = element.element(name);
		if(textElement == null) {
			return null;
		}
		return textElement.getText();
	}
	
	private static String attrString(Element element, String name) {
		String attr = element.attribute(name).getValue();
		return
			attr == null || attr.isEmpty() || attr.equals("null") ?
					null : attr;
	}

	private static Long attrLong(Element element, String name) {
		String str = attrString(element,name);
		return str == null ? null : Long.parseLong(str);
	}
	
	private static Integer attrInt(Element element, String name) {
		String str = attrString(element,name);
		return str == null ? null : Integer.parseInt(str);
	}
	
	private static Element addQuestionNode(Element questionsNode, 
			Question question, List<QuestionOption> options, Integer ordering) 
	{
		Element questionNode = questionsNode.addElement("question")
			.addAttribute("id", question.getId()+"")
			.addAttribute("title", question.getTitle())
			.addAttribute("key", question.getRandomKey()+"")
			.addAttribute("answerType", question.getAnswerTypeDB())
			.addAttribute("subjectType", question.getTypeDB())
			.addAttribute("required", question.isRequired()+"")
			// in case ordering == null, I use the order they were pulled from the DB
			.addAttribute("ordering", ordering+"")
			.addAttribute("answerReasonExpressionId", question.getAnswerReasonExpressionId()+"");
		addText(questionNode,"preface",question.getPreface());
		addText(questionNode,"prompt",question.getPrompt());
		addText(questionNode,"citation",question.getCitation());
		for(Integer i = 0; i < options.size(); i++) {
			addOptionNode(questionNode,options.get(i),i);
		}
		return questionNode;
	}
	
	private static Element addOptionNode(Element questionNode, 
			QuestionOption option, Integer ordering) 
	{
		return questionNode.addElement("option")
			.addAttribute("id", option.getId()+"")
			.addAttribute("name", option.getName())
			.addAttribute("key", option.getRandomKey()+"")
			.addAttribute("value", option.getValue())
			.addAttribute("ordering", ordering+"");
	}
	
	private static Element addExpressionNode(Element parent, Expression expression) {
		Element expressionNode = parent.addElement("expression")
			.addAttribute("id", expression.getId()+"")
			.addAttribute("name", expression.getName())
			.addAttribute("key", expression.getRandomKey()+"")
			.addAttribute("questionId", expression.getQuestionId()+"")
			.addAttribute("resultForUnanswered", expression.getResultForUnanswered()+"")
			.addAttribute("type", expression.getTypeDB()+"")
			.addAttribute("operator", expression.getOperatorDB()+"");
		addText(expressionNode,"value",expression.getValueDB());
		return expressionNode;
	}
	
	private static Element addInterviewNode(Element parent, 
			Interview interview, List<Alter> alters, List<Answer> answers)
	{
		Element interviewNode = parent.addElement("interview")
			.addAttribute("id", interview.getId()+"")
			.addAttribute("key", interview.getRandomKey()+"");
		Element altersNode = interviewNode.addElement("alters");
		for(Alter alter : alters) {
			addAlterNode(altersNode,alter);
		}
		Element answersNode = interviewNode.addElement("answers");
		for(Answer answer : answers) {
			addAnswerNode(answersNode,answer);
		}
		return interviewNode;
	}
	
	private static Element addAlterNode(Element parent, Alter alter) {
		return parent.addElement("alter")
			.addAttribute("id", alter.getId()+"")
			.addAttribute("name", alter.getName())
			.addAttribute("key", alter.getRandomKey()+"");
	}
	
	private static Element addAnswerNode(Element parent, Answer answer) {
		Element answerNode = parent.addElement("answer")
			.addAttribute("id", answer.getId()+"")
			.addAttribute("key", answer.getRandomKey()+"")
			.addAttribute("questionId", answer.getQuestionId()+"")
			.addAttribute("questionType", answer.getQuestionTypeDB())
			.addAttribute("answerType", answer.getAnswerTypeDB())
			.addAttribute("alterId1", answer.getAlterId1()+"")
			.addAttribute("alterId2", answer.getAlterId2()+"");
		addText(answerNode,"value",answer.getValue());
		return answerNode;
	}
	
	private static String formatXMLDocument(Document document) throws IOException {
		StringWriter stringWriter = new StringWriter();
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setNewlines(true);
		format.setTrimText(false);
		format.setLineSeparator("\r\n");
		new XMLWriter(stringWriter,format).write(document);
		return stringWriter.toString();
	}
}
