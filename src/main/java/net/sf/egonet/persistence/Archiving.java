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
	
	public static String getStudyXML(Session session, Study study, Boolean includeInterviews) {
		try {
			Document document = DocumentHelper.createDocument();
			Element studyNode = document.addElement("study")
				.addAttribute("id", study.getId()+"")
				.addAttribute("name", study.getName())
				.addAttribute("key", study.getRandomKey()+"");
			Element questionsNode = studyNode.addElement("questions");
			for(Question question : Questions.getQuestionsForStudy(session, study.getId(), null)) {
				org.dom4j.Element questionNode = questionsNode.addElement("question")
					.addAttribute("id", question.getId()+"")
					.addAttribute("key", question.getRandomKey()+"");
				questionNode.addElement("prompt").addText(question.getPrompt());
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
