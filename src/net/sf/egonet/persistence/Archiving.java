package net.sf.egonet.persistence;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.ArrayList;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Entity;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Study;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.model.AnswerList;
import net.sf.egonet.model.AnswerListMgr;
import net.sf.egonet.web.panel.NumericLimitsPanel.NumericLimitType;

import net.sf.functionalj.tuple.Pair;
import net.sf.functionalj.tuple.Triple;

import org.dom4j.Attribute;
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
		root.addElement(elementName).addText(replaceProblemCharacters(elementText));
	}
	
	private static void addAttribute(Element element, String attrKey, Object attrValue) {
		element.addAttribute(attrKey, replaceProblemCharacters(attrValue));
	}
	
	private static String replaceProblemCharacters(Object object) {
		String string = object == null ? "" : object.toString();
		// smart double-quotes
		string = string.replaceAll("[\\u201c\\u201d\\u201e\\u201f\\u2033\\u2036]", "\"");
		// smart single-quotes and apostrophes
		string = string.replaceAll("[\\u2018\\u2019\\u201a\\u201b\\u2032\\u2035]", "'");
		// unusual dash types
		string = string.replaceAll("[\\u2012\\u2013\\u2014\\u2015]","-");
		return string;
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
			
			Element answerListsNode = studyNode.addElement("answerLists");
			AnswerList[] answerListArray = AnswerListMgr.getAnswerLists(study.getId());
			for ( AnswerList answerList : answerListArray )
				addAnswerListNode(answerListsNode,answerList);
			
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

	public static Study loadStudyXML(final Study study, final String studyXML, final String name) {
		return new DB.Action<Study>() {
			public Study get() {
				return loadStudyXML(session, study,studyXML,name,true,false);
			}
		}.execute();
	}
	
	public static Study loadRespondentXML(final Study study, final String studyXML) {
		return new DB.Action<Study>() {
			public Study get() {
				return loadStudyXML(session, study,studyXML,null,false,true);
			}
		}.execute();
	}
	
	public static Study loadStudyXML(final Session session, 
			Study studyToUpdate, String studyXML, String newName,
			Boolean updateStudy, Boolean updateRespondentData)
	{
		try {
			Document document = new SAXReader().read(new StringReader(studyXML));
			Element studyElement = document.getRootElement();
			
			Study study = studyToUpdate;
			if(study == null) {
				study = new Study();
			} else {
				Long xmlKey = attrLong(studyElement,"key");
				if(xmlKey == null || ! xmlKey.equals(study.getRandomKey())) {
					throw new RuntimeException("Trying to import incompatible study.");
				}
			}
			DB.save(session, study);
			
			List<Element> expressionElements = studyElement.element("expressions").elements("expression");
			List<Expression> expressions = Expressions.forStudy(session, study.getId());
			Map<Long,Long> remoteToLocalExpressionId = createRemoteToLocalMap(
					expressions, expressionElements, updateStudy, updateStudy,
					fnCreateExpression(), fnDeleteExpression(session));
			
			if(updateStudy) {
				updateStudyFromNode(session,study,studyElement,remoteToLocalExpressionId,newName);
			}
			
			// older XML files might not have the answerLists section
			// in this case pass over the answerList updates, they will get
			// initialized from the presets
			List<Element> answerListElements;
			try {
			    answerListElements = studyElement.element("answerLists").elements("answerList");
			} catch ( java.lang.NullPointerException npe ) {
				answerListElements = null;
			}
			if ( answerListElements != null ) {
				for ( Element element : answerListElements ) {
					updateAnswerListFromNode(session, new AnswerList(), element, study.getId());
				}
			}
			
			List<Element> questionElements = studyElement.element("questions").elements("question");
			List<Question> questions = Questions.getQuestionsForStudy(session, study.getId(), null);
			Map<Long,Long> remoteToLocalQuestionId = createRemoteToLocalMap(
					questions, questionElements, updateStudy, updateStudy,
					fnCreateQuestion(), fnDeleteQuestion(session));
			
			Map<Long,Long> remoteToLocalOptionId = Maps.newTreeMap();

			Map<Long,Question> localIdToQuestion = indexById(questions);
			for(Element questionElement : questionElements) {
				Long remoteQuestionId = attrId(questionElement);
				Long localQuestionId = remoteToLocalQuestionId.get(remoteQuestionId);
				if(localQuestionId != null) {
					Question question = localIdToQuestion.get(localQuestionId);
					if(question == null) {
						String msg = "LocalQuestionId is "+localQuestionId+" but no local question? ";
						msg += "Remote to local map: ";
						for(Map.Entry<Long,Long> keyVal : remoteToLocalQuestionId.entrySet()) {
							msg += " <"+keyVal.getKey()+","+keyVal.getValue()+">, ";
						}
						msg += "Ids in local map: ";
						for(Long key : localIdToQuestion.keySet()) {
							msg += " "+key+", ";
						}
						throw new RuntimeException(msg);
					}
					if(updateStudy) {
						updateQuestionFromNode(session,question,questionElement,
								study.getId(),remoteToLocalExpressionId);
					}
					List<Element> optionElements = questionElement.elements("option");
					List<QuestionOption> optionEntities = Options.getOptionsForQuestion(session, question.getId());
					Map<Long,Long> optionIdMap = createRemoteToLocalMap(
							optionEntities,optionElements,updateStudy,updateStudy,
							fnCreateOption(),fnDeleteOption(session));
					remoteToLocalOptionId.putAll(optionIdMap);
					if(updateStudy) {
						Map<Long,QuestionOption> idToOptionEntity = indexById(optionEntities);
						for(Element optionElement : optionElements) {
							Long localId = remoteToLocalOptionId.get(attrId(optionElement));
							if(localId != null) {
								QuestionOption optionEntity = idToOptionEntity.get(localId);
								updateOptionFromNode(session,optionEntity,optionElement,
										study,question);
							}
						}
					}
				}
			}
			
			if(updateStudy) {
				Map<Long,Expression> localIdToExpression = indexById(expressions);
				for(Element expressionElement : expressionElements) {
					Long localExpressionId = remoteToLocalExpressionId.get(attrId(expressionElement));
					if(localExpressionId != null) {
						Expression expression = localIdToExpression.get(localExpressionId);
						updateExpressionFromNode(session,expression,expressionElement,
								study.getId(),remoteToLocalQuestionId,remoteToLocalOptionId,
								remoteToLocalExpressionId);
					}
				}
			}
			
			if(updateRespondentData) {
				// Import interviews
				List<Element> interviewElements = studyElement.element("interviews").elements("interview");
				List<Interview> interviews = Interviews.getInterviewsForStudy(session, study.getId());
				Map<Long,Long> remoteToLocalInterviewId = createRemoteToLocalMap(
						interviews, 
						interviewElements, true, false,
						fnCreateInterview(), null);
				Map<Long,Interview> localIdToInterview = indexById(interviews);
				for(Element interviewElement : interviewElements) {
					Long localInterviewId = remoteToLocalInterviewId.get(attrId(interviewElement));
					Interview interview = localIdToInterview.get(localInterviewId);
					updateInterviewFromNode(session,interview,interviewElement,study.getId());
					
					// Import alters
					List<Element> alterElements = interviewElement.element("alters").elements("alter");
					List<Alter> alterEntities = Alters.getForInterview(session, interview.getId());
					Map<Long,Long> remoteToLocalAlterId = createRemoteToLocalMap(
							alterEntities,
							alterElements, true, true,
							fnCreateAlter(), fnDeleteAlter(session));
					Map<Long,Alter> localIdToAlter = indexById(alterEntities);
					for(Integer i = 0; i < alterElements.size(); i++) {
						Element alterElement = alterElements.get(i);
						Long localAlterId = remoteToLocalAlterId.get(attrId(alterElement));
						Alter alter = localIdToAlter.get(localAlterId);
						alter.setOrdering(i);
						updateAlterFromNode(session,alter,alterElement,interview.getId());
					}
					
					// Import answers
					List<Element> answerElements = interviewElement.element("answers").elements("answer");
					List<Answer> answerEntities = Answers.getAnswersForInterview(session, interview.getId());
					Map<Long,Long> remoteToLocalAnswerId = createRemoteToLocalMap(
							answerEntities,
							answerElements, true, false,
							fnCreateAnswer(), null);
					Map<Long,Answer> localIdToAnswer = indexById(answerEntities);
					for(Element answerElement : answerElements) {
						Long localAnswerId = remoteToLocalAnswerId.get(attrId(answerElement));
						Answer answer = localIdToAnswer.get(localAnswerId);
						updateAnswerFromNode(session,answer,answerElement,
								study.getId(),interview.getId(),
								remoteToLocalQuestionId,remoteToLocalAlterId,remoteToLocalOptionId);
					}
				}
			}
			
			return study;
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
				entities.add(entity);
			}
			if(shouldDelete && ! keyToElement.containsKey(key)) {
				E entity = keyToEntity.remove(key);
				deleter.apply(entity);
				entities.remove(entity);
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
		Element studyNode = document.addElement("study");
		addAttribute(studyNode,"id", study.getId());
		addAttribute(studyNode,"name", study.getName());
		addAttribute(studyNode,"key", study.getRandomKey()+"");
		addAttribute(studyNode,"minAlters", study.getMinAlters()+"");
		addAttribute(studyNode,"maxAlters", study.getMaxAlters()+"");
		addAttribute(studyNode,"valueDontKnow", study.getValueDontKnow());
		addAttribute(studyNode,"valueLogicalSkip", study.getValueLogicalSkip());
		addAttribute(studyNode,"valueNotYetAnswered", study.getValueNotYetAnswered());
		addAttribute(studyNode,"valueRefusal", study.getValueRefusal());
		addAttribute(studyNode,"adjacencyExpressionId", study.getAdjacencyExpressionId());
		addText(studyNode,"introduction",study.getIntroduction());
		addText(studyNode,"egoIdPrompt",study.getEgoIdPrompt());
		addText(studyNode,"alterPrompt",study.getAlterPrompt());
		addText(studyNode,"conclusion",study.getConclusion());
		return studyNode;
	}
	
	private static void updateStudyFromNode(Session session, Study study, Element studyElement, 
			Map<Long,Long> remoteToLocalExpressionId, String newName) 
	{
		study.setName(newName == null || newName.isEmpty() ? 
				attrString(studyElement,"name") : newName);
		study.setRandomKey(attrLong(studyElement,"key"));
		study.setMinAlters(attrInt(studyElement,"minAlters"));
		study.setMaxAlters(attrInt(studyElement,"maxAlters"));
		study.setValueDontKnow(attrString(studyElement,"valueDontKnow"));
		study.setValueLogicalSkip(attrString(studyElement,"valueLogicalSkip"));
		study.setValueNotYetAnswered(attrString(studyElement,"valueNotYetAnswered"));
		study.setValueRefusal(attrString(studyElement,"valueRefusal"));
		Long remoteAdjacencyId = attrLong(studyElement,"adjacencyExpressionId");
		study.setAdjacencyExpressionId(
				remoteAdjacencyId == null ? null : 
					remoteToLocalExpressionId.get(remoteAdjacencyId));
		study.setIntroduction(attrText(studyElement,"introduction"));
		study.setEgoIdPrompt(attrText(studyElement,"egoIdPrompt"));
		study.setAlterPrompt(attrText(studyElement,"alterPrompt"));
		study.setConclusion(attrText(studyElement,"conclusion"));
		DB.save(session, study);
	}
	
	private static Element addQuestionNode(Element questionsNode, 
			Question question, List<QuestionOption> options, Integer ordering) 
	{
		Answer.AnswerType aType;
		
		Element questionNode = questionsNode.addElement("question");
		addAttribute(questionNode,"id", question.getId());
		addAttribute(questionNode,"title", question.getTitle());
		addAttribute(questionNode,"key", question.getRandomKey());
		addAttribute(questionNode,"answerType", question.getAnswerTypeDB());
		addAttribute(questionNode,"subjectType", question.getTypeDB());
		addAttribute(questionNode,"askingStyleList", question.getAskingStyleList());
			// in case ordering == null, I use the order they were pulled from the DB
		addAttribute(questionNode,"ordering", ordering);
		addAttribute(questionNode,"answerReasonExpressionId", question.getAnswerReasonExpressionId());
		// addAttribute(questionNode,"useIf", question.getUseIfExpression()); no longer used!
		addAttribute(questionNode,"otherSpecify", question.getOtherSpecify());
		addAttribute(questionNode,"noneButton", question.getNoneButton());
		addAttribute(questionNode,"allButton", question.getAllButton());
		addAttribute(questionNode,"pageLevelDontKnowButton", question.getPageLevelDontKnowButton());
		addAttribute(questionNode,"pageLevelRefuseButton", question.getPageLevelRefuseButton());
		addAttribute(questionNode,"dontKnowButton", question.getDontKnowButton());
		addAttribute(questionNode,"networkRelationshipExprId", question.getNetworkRelationshipExprId());
		addAttribute(questionNode,"networkNShapeQId", question.getNetworkNShapeQId());
		addAttribute(questionNode,"networkNColorQId", question.getNetworkNColorQId());
		addAttribute(questionNode,"networkNSizeQId", question.getNetworkNSizeQId());
		addAttribute(questionNode,"networkEColorQId", question.getNetworkEColorQId());
		addAttribute(questionNode,"networkESizeQId", question.getNetworkESizeQId());
		addAttribute(questionNode,"refuseButton", question.getRefuseButton());
		addAttribute(questionNode,"allOptionString", question.getAllOptionString());
		addAttribute(questionNode,"symmetric", question.getSymmetric());
		addAttribute(questionNode,"groupID", question.getGroupID());
		aType = question.getAnswerType();
		if (aType==Answer.AnswerType.NUMERICAL ) {
			addAttribute(questionNode,"minLimitType", question.getMinLimitTypeDB());
			addAttribute(questionNode,"minLiteral", question.getMinLiteral());
			addAttribute(questionNode,"minPrevQues", question.getMinPrevQues());
			addAttribute(questionNode,"maxLimitType", question.getMaxLimitTypeDB());
			addAttribute(questionNode,"maxLiteral", question.getMaxLiteral());
			addAttribute(questionNode,"maxPrevQues", question.getMaxPrevQues());	
		} else if (aType==Answer.AnswerType.MULTIPLE_SELECTION ) {
			addAttribute(questionNode,"minCheckableBoxes", question.getMinCheckableBoxes());
			addAttribute(questionNode,"maxCheckableBoxes", question.getMaxCheckableBoxes());
		} else if ( aType==Answer.AnswerType.DATE || aType==Answer.AnswerType.TIME_SPAN ) {
			addAttribute(questionNode,"timeUnits", question.getTimeUnits());
		}
		if ( aType==Answer.AnswerType.MULTIPLE_SELECTION || aType==Answer.AnswerType.SELECTION) {
		    addAttribute(questionNode,"withListRange", question.getWithListRange());
		    addAttribute(questionNode,"listRangeString", question.getListRangeString());
		    addAttribute(questionNode,"minListRange", question.getMinListRange());
		    addAttribute(questionNode,"maxListRange", question.getMaxListRange());
		}
		addText(questionNode,"preface",question.getPreface()); 
		addText(questionNode,"prompt",question.getPrompt());
		addText(questionNode,"citation",question.getCitation());
		for(Integer i = 0; i < options.size(); i++) {
			addOptionNode(questionNode,options.get(i),i);
		}
		return questionNode;
	}
	
	/**
	 * when importing older xml files, the data regarding the numeric checking
	 * and ranges might not be present.  If an exception is thrown we will just
	 * assign default values.
	 * @param session
	 * @param question
	 * @param node
	 * @param studyId
	 * @param remoteToLocalExpressionId
	 */
	private static void updateQuestionFromNode(Session session, Question question, Element node, 
			Long studyId, Map<Long,Long> remoteToLocalExpressionId) 
	{	
		Answer.AnswerType aType;
		
		question.setStudyId(studyId);
		question.setTitle(attrString(node,"title"));
		question.setAnswerTypeDB(attrString(node,"answerType"));
		question.setTypeDB(attrString(node,"subjectType"));
		question.setAskingStyleList(attrBool(node,"askingStyleList"));
		question.setOrdering(attrInt(node,"ordering"));
		question.setPreface(attrText(node,"preface"));
		question.setPrompt(attrText(node,"prompt"));
		question.setCitation(attrText(node,"citation"));
		// question.setUseIfExpression(attrString(node,"useIf")); No longer used!
		try {
		    question.setOtherSpecify(attrBool(node,"otherSpecify"));
		} catch ( java.lang.RuntimeException rte2 ) {
			question.setOtherSpecify(false);
		}
		
		try {
			question.setSymmetric(attrBool(node,"symmetric"));
		} catch ( java.lang.RuntimeException rte5 ) {
			question.setSymmetric(false);
		}
		
		try {
			question.setGroupID(attrString(node,"groupID"));
		} catch ( java.lang.RuntimeException rte6 ) {
			question.setGroupID("");
		}
		
		aType = question.getAnswerType();
		if ( aType==Answer.AnswerType.NUMERICAL ) {
			try {
			question.setMinLimitTypeDB(attrString(node,"minLimitType"));
			question.setMinLiteral(attrInt(node,"minLiteral"));
			question.setMinPrevQues(attrString(node,"minPrevQues"));
			question.setMaxLimitTypeDB(attrString(node,"maxLimitType"));
			question.setMaxLiteral(attrInt(node,"maxLiteral"));
			question.setMaxPrevQues(attrString(node,"maxPrevQues"));
			} catch ( java.lang.RuntimeException rte ) {
				// if just about anything went wrong, goto defaults
				question.setMinLimitType(NumericLimitType.NLT_NONE);
				question.setMinLiteral(0);
				question.setMinPrevQues("");
				question.setMaxLimitType(NumericLimitType.NLT_NONE);
				question.setMaxLiteral(1000);
				question.setMaxPrevQues("");
			}
		}
		
		if ( aType==Answer.AnswerType.MULTIPLE_SELECTION ) {
			try {
				question.setMinCheckableBoxes(attrInt(node,"minCheckableBoxes"));
				question.setMaxCheckableBoxes(attrInt(node,"maxCheckableBoxes"));
			} catch ( java.lang.RuntimeException rte2 ) {
			    // if anything went wrong, assign reasonable defaults
				question.setMinCheckableBoxes(0);
				question.setMaxCheckableBoxes(100);
			}
		}
		
		if ( aType==Answer.AnswerType.MULTIPLE_SELECTION || aType==Answer.AnswerType.SELECTION) {
			try {
				question.setOtherSpecify(attrBool(node,"otherSpecify"));
				question.setWithListRange(attrBool(node,"withListRange"));
				question.setListRangeString(attrString(node,"listRangeString"));
				question.setMinListRange(attrInt(node,"minListRange"));
				question.setMaxListRange(attrInt(node,"maxListRange"));
			} catch ( java.lang.RuntimeException rte3 ) {
				question.setOtherSpecify(false);
				question.setWithListRange(false);
				question.setListRangeString("");
				question.setMinListRange(0);
				question.setMaxListRange(100);
			}
		}
		
		try {
			question.setNoneButton(attrBool(node,"noneButton"));
			question.setAllButton(attrBool(node,"allButton"));
			question.setPageLevelDontKnowButton(attrBool(node,"pageLevelDontKnowButton"));
			question.setPageLevelRefuseButton (attrBool(node,"pageLevelRefuseButton"));
			question.setDontKnowButton(attrBool(node,"dontKnowButton"));
			question.setRefuseButton(attrBool(node,"refuseButton"));
			question.setAllOptionString(attrString(node,"allOptionString"));
		} catch ( java.lang.RuntimeException rte4 ) {
			question.setNoneButton(new Boolean(false));
			question.setAllButton(new Boolean(false));
			question.setPageLevelDontKnowButton(new Boolean(true));
			question.setPageLevelRefuseButton (new Boolean(true));
			question.setDontKnowButton(new Boolean(true));
			question.setRefuseButton(new Boolean(true));
			question.setAllOptionString(new String(""));
		}
		
		if ( aType==Answer.AnswerType.DATE || aType==Answer.AnswerType.TIME_SPAN ) {
			try {
				question.setTimeUnits(attrInt(node,"timeUnits"));
			} catch ( java.lang.RuntimeException rte4 ) {
				question.setTimeUnits(0xff);
			}
		}
		Long remoteReasonId = attrLong(node,"answerReasonExpressionId");
		question.setAnswerReasonExpressionId(
				remoteReasonId == null ? null : 
					remoteToLocalExpressionId.get(remoteReasonId));

		try
		{
			Long remoteNetworkRelationshipId = attrLong(node,"networkRelationshipExprId");
			question.setNetworkRelationshipExprId(
					remoteNetworkRelationshipId == null ? null : 
						remoteToLocalExpressionId.get(remoteNetworkRelationshipId));

			question.setNetworkNShapeQId(attrLong(node, "networkNShapeQId"));
			question.setNetworkNColorQId(attrLong(node, "networkNColorQId"));
			question.setNetworkNSizeQId(attrLong(node, "networkNSizeQId"));
			question.setNetworkEColorQId(attrLong(node, "networkEColorQId"));
			question.setNetworkESizeQId(attrLong(node, "networkESizeQId"));
		}
		catch (java.lang.RuntimeException rte)
		{
			question.setNetworkRelationshipExprId(null);	
			question.setNetworkNShapeQId(null);
			question.setNetworkNColorQId(null);
			question.setNetworkNSizeQId(null);
			question.setNetworkEColorQId(null);
			question.setNetworkESizeQId(null);
		}

		DB.save(session, question);
	}
	
	private static Element addOptionNode(Element questionNode, 
			QuestionOption option, Integer ordering) 
	{
		Element optionNode = questionNode.addElement("option");
		addAttribute(optionNode,"id", option.getId());
		addAttribute(optionNode,"name", option.getName());
		addAttribute(optionNode,"key", option.getRandomKey());
		addAttribute(optionNode,"value", option.getValue());
		addAttribute(optionNode,"ordering", ordering);
		return optionNode;
	}
	
	private static void updateOptionFromNode(Session session, QuestionOption option, Element node, 
			Study study, Question question) 
	{
		option.setQuestionId(question.getId());
		option.setName(attrString(node,"name"));
		option.setValue(attrString(node,"value"));
		option.setStudyId(study.getId());
		option.setOrdering(attrInt(node,"ordering"));
		DB.save(session, option);
	}
	
	private static Element addExpressionNode(Element parent, Expression expression) {
		Element expressionNode = parent.addElement("expression");
		addAttribute(expressionNode,"id", expression.getId());
		addAttribute(expressionNode,"name", expression.getName());
		addAttribute(expressionNode,"key", expression.getRandomKey());
		addAttribute(expressionNode,"questionId", expression.getQuestionId());
		addAttribute(expressionNode,"resultForUnanswered", expression.getResultForUnanswered());
		addAttribute(expressionNode,"type", expression.getTypeDB());
		addAttribute(expressionNode,"operator", expression.getOperatorDB());
		addText(expressionNode,"value",expression.getValueDB());
		return expressionNode;
	}
	
	private static void updateExpressionFromNode(Session session, Expression expression, Element node,
			Long studyId, Map<Long,Long> remoteToLocalQuestionId, Map<Long,Long> remoteToLocalOptionId,
			Map<Long,Long> remoteToLocalExpressionId)
	{
		expression.setStudyId(studyId);
		expression.setName(attrString(node,"name"));
		expression.setResultForUnanswered(attrBool(node,"resultForUnanswered"));
		expression.setTypeDB(attrString(node,"type"));
		expression.setOperatorDB(attrString(node,"operator"));
		
		// questionId
		Long remoteQuestionId = attrLong(node,"questionId");
		expression.setQuestionId(
				remoteQuestionId == null ? null : 
					remoteToLocalQuestionId.get(remoteQuestionId));
		
		// value (first set as normal, then convert IDs)
		expression.setValueDB(attrText(node,"value"));
		if(expression.getType().equals(Expression.Type.Selection) || 
				expression.getType().equals(Expression.Type.Compound))
		{
			List<Long> localValueIds = Lists.newArrayList();
			for(Long remoteValueId : (List<Long>) expression.getValue()) {
				Map<Long,Long> remoteToLocalValueId = 
					expression.getType().equals(Expression.Type.Compound) ?
							remoteToLocalExpressionId : remoteToLocalOptionId;
				Long localValueId = remoteToLocalValueId.get(remoteValueId);
				if(localValueId != null) {
					localValueIds.add(localValueId);
				}
			}
			expression.setValue(localValueIds);
		} else if(expression.getType().equals(Expression.Type.Comparison)) {
			Pair<Integer,Long> remoteNumberExpr = 
				(Pair<Integer,Long>) expression.getValue();
			expression.setValue(new Pair<Integer,Long>(
					remoteNumberExpr.getFirst(),
					remoteToLocalExpressionId.get(remoteNumberExpr.getSecond())));
		} else if(expression.getType().equals(Expression.Type.Counting)) {
			Triple<Integer,List<Long>,List<Long>> remoteNumberExprsQuests =
				(Triple<Integer,List<Long>,List<Long>>) expression.getValue();
			List<Long> localExprs = Lists.newArrayList();
			for(Long remoteExpr : remoteNumberExprsQuests.getSecond()) {
				Long localExpr = remoteToLocalExpressionId.get(remoteExpr);
				if(localExpr != null) {
					localExprs.add(localExpr);
				}
			}
			List<Long> localQuests = Lists.newArrayList();
			for(Long remoteQuest : remoteNumberExprsQuests.getThird()) {
				Long localQuest = remoteToLocalQuestionId.get(remoteQuest);
				if(localQuest != null) {
					localQuests.add(localQuest);
				}
			}
			expression.setValue(new Triple<Integer,List<Long>,List<Long>>(
					remoteNumberExprsQuests.getFirst(),
					localExprs, localQuests));
		}
		DB.save(session, expression);
	}
	
	private static Element addInterviewNode(Element parent, 
			Interview interview, List<Alter> alters, List<Answer> answers)
	{
		Element interviewNode = parent.addElement("interview");
		addAttribute(interviewNode,"id", interview.getId());
		addAttribute(interviewNode,"key", interview.getRandomKey());
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
	
	private static void updateInterviewFromNode(Session session, Interview interview, Element node, Long studyId) 
	{
		interview.setStudyId(studyId);
		DB.save(session, interview);
	}
	
	private static Element addAlterNode(Element parent, Alter alter) {
		Element alterNode = parent.addElement("alter");
		addAttribute(alterNode,"id", alter.getId());
		addAttribute(alterNode,"name", alter.getName());
		addAttribute(alterNode,"key", alter.getRandomKey()+"");
		return alterNode;
	}
	
	private static void updateAlterFromNode(Session session, Alter alter, Element node, Long interviewId) {
		alter.setInterviewId(interviewId);
		alter.setName(attrString(node,"name"));
		DB.save(session, alter);
	}
	
	private static Element addAnswerNode(Element parent, Answer answer) {
		Element answerNode = parent.addElement("answer");
		addAttribute(answerNode,"id", answer.getId());
		addAttribute(answerNode,"key", answer.getRandomKey());
		addAttribute(answerNode,"questionId", answer.getQuestionId());
		addAttribute(answerNode,"questionType", answer.getQuestionTypeDB());
		addAttribute(answerNode,"skipReason", answer.getSkipReasonDB());
		addAttribute(answerNode,"answerType", answer.getAnswerTypeDB());
		addAttribute(answerNode,"alterId1", answer.getAlterId1());
		addAttribute(answerNode,"alterId2", answer.getAlterId2());
		addAttribute(answerNode,"otherSpecifyText", answer.getOtherSpecifyText());
		addText(answerNode,"value",answer.getValue());
		return answerNode;
	}
	
	private static void updateAnswerFromNode(Session session, Answer answer, Element node, 
			Long studyId, Long interviewId, Map<Long,Long> remoteToLocalQuestionId, 
			Map<Long,Long> remoteToLocalAlterId, Map<Long,Long> remoteToLocalOptionId)
	{
		answer.setStudyId(studyId);
		answer.setInterviewId(interviewId);
		answer.setQuestionTypeDB(attrString(node,"questionType"));
		answer.setSkipReasonDB(attrString(node,"skipReason"));
		answer.setAnswerTypeDB(attrString(node,"answerType"));
		
		// questionId
		Long remoteQuestionId = attrLong(node,"questionId");
		answer.setQuestionId(
				remoteQuestionId == null ? null : 
					remoteToLocalQuestionId.get(remoteQuestionId));
		
		// alterId1
		Long remoteAlterId1 = attrLong(node,"alterId1");
		answer.setAlterId1(
				remoteAlterId1 == null ? null : 
					remoteToLocalAlterId.get(remoteAlterId1));
		
		// alterId2
		Long remoteAlterId2 = attrLong(node,"alterId2");
		answer.setAlterId2(
				remoteAlterId2 == null ? null : 
					remoteToLocalAlterId.get(remoteAlterId2));
		
		// value (requires translation - see Archiving.updateExpressionFromNode and MultipleSelectionAnswerFormFieldPanel)
		String answerString = attrText(node,"value");
		Answer.AnswerType answerType = answer.getAnswerType();
		if(answerType.equals(Answer.AnswerType.SELECTION) ||
				answerType.equals(Answer.AnswerType.MULTIPLE_SELECTION))
		{
			String optionIds = "";
			try {
				for(String optionRemoteIdString : answerString.split(",")) {
					Long optionRemoteId = Long.parseLong(optionRemoteIdString);
					Long optionLocalId = remoteToLocalOptionId.get(optionRemoteId);
					if(optionLocalId != null) {
						optionIds += (optionIds.isEmpty() ? "" : ",")+optionLocalId;
					}
				}
			} catch(Exception ex) {
				// Most likely failed to parse answer. Fall back to no existing answer.
			}
			answer.setValue(optionIds);
		} else {
			answer.setValue(answerString);
		}
		answer.setOtherSpecifyText(attrString(node,"otherSpecifyText"));
		DB.save(session, answer);
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
		if(element == null || name == null) {
			throw new RuntimeException(
					"Unable to determine "+name+" attribute for "+
					(element == null ? "null " : "")+"element.");
		}
		Attribute attribute = element.attribute(name);
		if(attribute == null) {
			String others = "";
			for(Object attrObj : element.attributes()) {
				Attribute attr = (Attribute) attrObj;
				others += ((others.isEmpty() ? "" : ", ") + "[" +
						(attr.getName()+" : "+attr.getValue()) + "]");
			}
			throw new RuntimeException("Element does not contain the requested attribute "+name+
					", but does contain: "+others);
		}
		String attr = attribute.getValue();
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
	
	private static Boolean attrBool(Element element, String name) {
		String str = attrString(element,name);
		if(str == null) {
			return null;
		}
		if(str.equalsIgnoreCase("true")) {
			return true;
		}
		if(str.equalsIgnoreCase("false")) {
			return false;
		}
		return null;
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
	private static Function<Alter,Object> fnDeleteAlter(final Session session) {
		return new Function<Alter,Object>() {
			public Object apply(Alter alter) {
				Alters.delete(session, alter);
				return null;
			}
		};
	}
	
	private static Element addAnswerListNode(Element parent, AnswerList answerList) {
		Element answerListNode = parent.addElement("answerList");
		addAttribute(answerListNode,"id", answerList.getId());
		addAttribute(answerListNode,"key", answerList.getRandomKey());
		addAttribute(answerListNode,"listName", answerList.getListName());
		addAttribute(answerListNode,"listOptionNames", answerList.getListOptionNamesDB());
		return answerListNode;
	}
	
	private static void updateAnswerListFromNode(Session session, AnswerList answerList, 
			Element node, Long studyId )
	{
		answerList.setStudyId(studyId);
		answerList.setListName(attrString(node,"listName"));
		answerList.setListOptionNamesDB(attrString(node,"listOptionNames"));
		DB.save(session, answerList);
	}
	
	/**
	 * this creates a plain text report, 
	 * but might be altered to XML format.
	 * This creates the report dealing with the Other/Specify types of questions 
	 * and their answers
	 * @param study to examine
	 * @return a huge string containing the report
	 */
	
	public static String getOtherSpecifyReport ( Study study ) {
		String cr = System.getProperty("line.separator");
		Question.QuestionType qType;
		Long[] ids;
		StringBuilder strb = new StringBuilder();
		List<Question> listOfQuestions;
		List<Answer> listOfAnswers;
		AnswerSorter answerSorter = new AnswerSorter();
		String otherSpecText;
		
		strb.append("Other/Specify text responses for " + study.getName() + cr);
		listOfQuestions = Questions.getQuestionsWithOtherSpecifyForStudy(study.getId());
		for ( Question.QuestionType questType : Question.QuestionType.values() ) {
			for ( Question question : listOfQuestions ) {
				qType = question.getType();
				if ( qType.equals(questType )) {
					answerSorter.newQuestion();
					listOfAnswers = Answers.getAnswersForQuestion(question.getId());
					if ( answerSorter.addData(listOfAnswers)) {
						strb.append(cr);
						strb.append( "       QUESTION: " + question.getTitle() + "  (" + qType.toString() + ")" + cr);
						strb.append( "QUESTION PROMPT: " + question.getPrompt() + cr);	
						ids = answerSorter.getIDs();
						for ( Long interviewId : ids) {
							strb.append ("         EGO ID: " + Interviews.getEgoNameForInterview(interviewId) + cr);
							listOfAnswers = answerSorter.getAnswersForID(interviewId);
							for ( Answer answer : listOfAnswers ) {
								otherSpecText = answer.getOtherSpecifyText();
								strb.append("   SPECIFY TEXT: " + otherSpecText + cr);
								switch ( qType ) {
								case ALTER:
									 strb.append( "       ALTER ID: " + answer.getAlterId1() + cr);
									 break;
								case ALTER_PAIR:
									 strb.append("     ALTER PAIR: " + answer.getAlterId1() + " , " + answer.getAlterId2() + cr);
									 break;
										 
								} // end case ( qType...
							} // end for ( Answer ...
						} // end for ( Long interviewId
					} // end if ( answerSorter.addData...
				} // end if ( qType.equals...
			} // end for question...
		}  // end for questType...
		return(strb.toString());
	}
}

/**
 * a simple convenience class to sort answers to a question
 * by the interview ID. Used exclusively by getOtherSpecifyReport
 *
 */
class AnswerSorter {
	private TreeMap <Long,ArrayList<Answer>> answersByEgoId;
	private boolean anyData;
	
	public AnswerSorter() {
		answersByEgoId = new TreeMap<Long, ArrayList<Answer>>();
		anyData = false;
	}
	
	public void newQuestion() {
		answersByEgoId.clear();
		anyData = false;
	}
	
	public boolean addData ( List<Answer> listOfAnswers ) {
		ArrayList<Answer> answerList;
		String otherSpecText;
		Long id;
		
		if ( listOfAnswers==null || listOfAnswers.isEmpty())
			return(anyData);
		for ( Answer answer : listOfAnswers ) {
			otherSpecText = answer.getOtherSpecifyText();
			if ( otherSpecText!=null && otherSpecText.length()>0 ) {
				id = answer.getInterviewId();
				if ( !answersByEgoId.containsKey(id)) {
					answerList = new ArrayList<Answer>();
					answersByEgoId.put(id, answerList);
				} else {
					answerList = answersByEgoId.get(id);
				}
				if ( answerList != null ) {
					answerList.add(answer);
					anyData = true;
				}
			}
		}
		return(anyData);
	}
	
	public boolean isAnyData() { return ( anyData);}
	
	public Long[] getIDs() {
		Set<Long> keySet = answersByEgoId.keySet();
		Long[] returnArray = new Long[keySet.size()];
	
		returnArray = keySet.toArray(returnArray);
		return(returnArray);
	}
	
	public ArrayList<Answer> getAnswersForID ( Long id ) {
		return ( answersByEgoId.get(id));
	}
}
