package net.sf.egonet.persistence;

import java.io.StringWriter;

import net.sf.egonet.model.Question;
import net.sf.egonet.model.Study;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.hibernate.Session;

public class Archiving {

	public static String getStudyXML(final Study study) {
		return new DB.Action<String>() {
			public String get() {
				return getStudyXML(session, study,false);
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
			for(Question question : Questions.getQuestionsForStudy(session, study.getId(), null)) {
				org.dom4j.Element questionNode = questionsNode.addElement("question")
					.addAttribute("id", question.getId()+"")
					.addAttribute("title", question.getTitle())
					.addAttribute("key", question.getRandomKey()+"")
					.addAttribute("answerType", question.getAnswerTypeDB())
					.addAttribute("subjectType", question.getTypeDB())
					.addAttribute("required", question.isRequired()+"")
					.addAttribute("ordering", question.getOrdering()+"")
					.addAttribute("answerReasonExpressionId", question.getAnswerReasonExpressionId()+"");
				addText(questionNode,"preface",question.getPreface());
				addText(questionNode,"prompt",question.getPrompt());
				addText(questionNode,"citation",question.getCitation());
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
