package net.sf.egonet.persistence;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Entity;
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
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
	
	public static Study loadStudyXML(final Study study, final String studyXML) {
		return new DB.Action<Study>() {
			public Study get() {
				return loadStudyXML(session, study,studyXML,true,false);
			}
		}.execute();
	}
	
	public static Study loadStudyXML(final Session session, 
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
			
			List<Expression> expressions = Expressions.forStudy(session, study.getId());
			Map<Long,Expression> localIdToExpression = indexById(expressions);
			List<Element> expressionElements = studyElement.element("expressions").elements("expression");
			Map<Long,Long> remoteToLocalExpressionId = createRemoteToLocalMap(
					expressions, expressionElements, updateStudy, updateStudy,
					fnCreateExpression(), fnDeleteExpression(session));
			
			if(updateStudy) {
				updateStudyFromNode(study,studyElement,remoteToLocalExpressionId);
			}
			
			List<Question> questions = Questions.getQuestionsForStudy(session, study.getId(), null);
			Map<Long,Question> localIdToQuestion = indexById(questions);
			List<Element> questionElements = studyElement.element("questions").elements("question");
			Map<Long,Long> remoteToLocalQuestionId = createRemoteToLocalMap(
					questions, questionElements, updateStudy, updateStudy,
					fnCreateQuestion(), fnDeleteQuestion(session));
			
			List<QuestionOption> allOptionEntities = Lists.newArrayList();
			List<Element> allOptionElements = Lists.newArrayList();
			
			for(Element questionElement : questionElements) {
				Long remoteQuestionId = attrId(questionElement);
				Long localQuestionId = remoteToLocalQuestionId.get(remoteQuestionId);
				if(localQuestionId != null) {
					Question question = localIdToQuestion.get(localQuestionId);
					if(updateStudy) {
						// TODO: updateQuestionFromNode(question,questionElement,study.getId(),remoteToLocalExpressionId);
					}
					List<Element> optionElements = questionElement.elements("option");
					allOptionElements.addAll(optionElements);
					List<QuestionOption> optionEntities = Options.getOptionsForQuestion(session, question.getId());
					allOptionEntities.addAll(optionEntities);
				}
			}
			
			
			
			throw new RuntimeException("StudyId: "+study.getId()+", Study: "+study.mediumString());
			
			// TODO: Finish pulling data from XML
			//return study;
		} catch(Exception ex) {
			throw new RuntimeException("Failed to load XML for study "+studyToUpdate, ex);
		}
	}
	
	private static <E extends Entity> Map<Long,Long> 
	createRemoteToLocalMap(
			List<E> entities, List<Element> elements,
			Boolean shouldCreate, Boolean shouldDelete,
			Function<Object,E> creator, Function<E,Object> deleter)
	{
		// Index entities by key
		Set<Long> keys = Sets.newTreeSet();
		Map<Long,E> keyToEntity = Maps.newTreeMap();
		for(E entity : entities) {
			keyToEntity.put(entity.getRandomKey(), entity);
			keys.add(entity.getRandomKey());
		}
		Map<Long,Element> keyToElement = Maps.newTreeMap();
		for(Element element : elements) {
			Long key = attrLong(element,"key");
			keyToElement.put(key, element);
			keys.add(key);
		}
		
		// Create map from remote id to local id
		Map<Long,Long> remoteToLocalId = Maps.newTreeMap();
		for(Long key : keys) {
			if(shouldCreate && ! keyToEntity.containsKey(key)) {
				E entity = creator.apply(null);
				entity.setRandomKey(key);
				DB.save(entity);
				keyToEntity.put(key, entity);
			}
			if(shouldDelete && ! keyToElement.containsKey(key)) {
				deleter.apply(keyToEntity.remove(key));
			}
			Entity entity = keyToEntity.get(key);
			Element element = keyToElement.get(key);
			if(entity != null && element != null) {
				remoteToLocalId.put(attrId(element), entity.getId());
			}
		}
		
		return remoteToLocalId;
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
	
	private static void updateStudyFromNode(
			Study study, Element studyElement, Map<Long,Long> remoteToLocalExpressionId) 
	{
		study.setName(attrString(studyElement,"name"));
		study.setRandomKey(attrLong(studyElement,"key"));
		study.setMinAlters(attrInt(studyElement,"minAlters"));
		study.setMaxAlters(attrInt(studyElement,"maxAlters"));
		Long remoteAdjacencyId = attrLong(studyElement,"adjacencyExpressionId");
		study.setAdjacencyExpressionId(
				remoteAdjacencyId == null ? null : 
					remoteToLocalExpressionId.get(remoteAdjacencyId));
		study.setIntroduction(attrText(studyElement,"introduction"));
		study.setEgoIdPrompt(attrText(studyElement,"egoIdPrompt"));
		study.setAlterPrompt(attrText(studyElement,"alterPrompt"));
		study.setConclusion(attrText(studyElement,"conclusion"));
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
	
	private static <E extends Entity> Map<Long,E> indexById(List<E> entities) {
		Map<Long,E> idToEntity = Maps.newTreeMap();
		for(E entity : entities) {
			idToEntity.put(entity.getId(), entity);
		}
		return idToEntity;
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
	
	private static Long attrId(Element element) {
		return attrLong(element,"id");
	}
	
	private static Integer attrInt(Element element, String name) {
		String str = attrString(element,name);
		return str == null ? null : Integer.parseInt(str);
	}

	private static Function<Object,Question> fnCreateQuestion() {
		return new Function<Object,Question>() {
			public Question apply(Object arg0) {
				return new Question();
			}
		};
	}
	private static Function<Object,QuestionOption> fnCreateOption() {
		return new Function<Object,QuestionOption>() {
			public QuestionOption apply(Object arg0) {
				return new QuestionOption();
			}
		};
	}
	private static Function<Object,Expression> fnCreateExpression() {
		return new Function<Object,Expression>() {
			public Expression apply(Object arg0) {
				return new Expression();
			}
		};
	}
	private static Function<Object,Interview> fnCreateInterview() {
		return new Function<Object,Interview>() {
			public Interview apply(Object arg0) {
				return new Interview();
			}
		};
	}
	private static Function<Object,Alter> fnCreateAlter() {
		return new Function<Object,Alter>() {
			public Alter apply(Object arg0) {
				return new Alter();
			}
		};
	}
	private static Function<Object,Answer> fnCreateAnswer() {
		return new Function<Object,Answer>() {
			public Answer apply(Object arg0) {
				return new Answer();
			}
		};
	}

	private static Function<Question,Object> fnDeleteQuestion(final Session session) {
		return new Function<Question,Object>() {
			public Object apply(Question question) {
				Questions.delete(session, question);
				return null;
			}
		};
	}
	private static Function<QuestionOption,Object> fnDeleteOption(final Session session) {
		return new Function<QuestionOption,Object>() {
			public Object apply(QuestionOption option) {
				Options.delete(session, option);
				return null;
			}
		};
	}
	private static Function<Expression,Object> fnDeleteExpression(final Session session) {
		return new Function<Expression,Object>() {
			public Object apply(Expression expression) {
				Expressions.delete(session, expression);
				return null;
			}
		};
	}
	private static Function<Interview,Object> fnDeleteInterview(final Session session) {
		return new Function<Interview,Object>() {
			public Object apply(Interview interview) {
				// Deleting interviews should not happen as part of XML import.
				return null;
			}
		};
	}
	private static Function<Alter,Object> fnDeleteAlter(final Session session) {
		return new Function<Alter,Object>() {
			public Object apply(Alter alter) {
				Alters.delete(session, alter);
				return null;
			}
		};
	}
	private static Function<Answer,Object> fnDeleteAnswer(final Session session) {
		return new Function<Answer,Object>() {
			public Object apply(Answer answer) {
				// Only delete answers when the corresponding question or alter is deleted.
				return null;
			}
		};
	}
}
