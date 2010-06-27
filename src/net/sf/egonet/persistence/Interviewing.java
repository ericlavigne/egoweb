package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.persistence.Expressions.EvaluationContext;
import net.sf.egonet.web.page.InterviewingAlterPage;
import net.sf.egonet.web.page.InterviewingAlterPairPage;
import net.sf.egonet.web.page.InterviewingNetworkPage;
import net.sf.egonet.web.panel.InterviewNavigationPanel;
import net.sf.egonet.web.panel.InterviewNavigationPanel.InterviewLink;
import net.sf.functionalj.tuple.Pair;
import net.sf.functionalj.tuple.PairUni;
import net.sf.functionalj.tuple.Triple;
import net.sf.functionalj.tuple.TripleUni;

import org.hibernate.Session;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Interviewing {

	public static Interview findOrCreateMatchingInterviewForStudy(
			final Long studyId, final List<Answer> egoIdAnswers)
	{
		return DB.withTx(new Function<Session,Interview>(){
			public Interview apply(Session session) {
				Multimap<Long,Answer> answersByInterview = ArrayListMultimap.create();
				for(Answer answer : Answers.getAnswersForStudy(session,studyId,QuestionType.EGO_ID)) {
					answersByInterview.put(answer.getInterviewId(), answer);
				}
				for(Long interviewId : answersByInterview.keySet()) {
					Collection<Answer> interviewAnswers = answersByInterview.get(interviewId);
					if(answersMatch(egoIdAnswers,interviewAnswers)) {
						return Interviews.getInterview(session, interviewId);
					}
				}
				// If reach this point without finding a match, time to start a new interview.
				Interview interview = new Interview(Studies.getStudy(studyId));
				Long interviewId = (Long) session.save(interview);
				interview.setId(interviewId);
				List<Question> egoIdQuestions = 
					Questions.getQuestionsForStudy(session, studyId, QuestionType.EGO_ID);
				for(Question question : egoIdQuestions) {
					for(Answer answer : egoIdAnswers) {
						if(answer.getQuestionId().equals(question.getId())) {
							DB.save(new Answer(interview,question,answer.getValue()));
						}
					}
				}
				return interview;
			}
			private Boolean answersMatch(Collection<Answer> ego1Answers, Collection<Answer> ego2Answers) {
                if (ego1Answers.isEmpty() && !ego2Answers.isEmpty())
                {
                    return false;
                }
                if (!ego1Answers.isEmpty() && ego2Answers.isEmpty())
                {
                    return false;
                }
				for(Answer ego1Answer : ego1Answers) {
					for(Answer ego2Answer : ego2Answers) {
						if(ego1Answer.getQuestionId().equals(ego2Answer.getQuestionId()) &&
								! ego1Answer.getValue().equals(ego2Answer.getValue())) {
							return false;
						}
					}
				}
				return true;
			}
		});
	}

	public static Question nextEgoQuestionForInterview(
			final Long interviewId, final Question current, 
			final Boolean forward, final Boolean unansweredOnly) 
	{
		return DB.withTx(new Function<Session,Question>() {
			public Question apply(Session session) {
				return nextEgoQuestionForInterview(session,interviewId,current,forward,unansweredOnly);
			}
		});
	}
	
	public static Question nextEgoQuestionForInterview(
			Session session, Long interviewId, Question current, Boolean forward, Boolean unansweredOnly)
	{
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.EGO);
		if(! forward) {
			Collections.reverse(questions);
		}
		List<Answer> answers = 
			Answers.getAnswersForInterview(session, interviewId, QuestionType.EGO);
		Boolean passedCurrent = current == null;
		EvaluationContext context = Expressions.getContext(session, interview);
		for(Question question : questions) {
			Boolean foundAnswer = false;
			for(Answer answer : answers) {
				if(answer.getQuestionId().equals(question.getId())) {
					foundAnswer = true;
				}
			}
			if(unansweredOnly && foundAnswer) {
				// Looking for unanswered. This one is answered. Not the question we're looking for.
			} else if(passedCurrent) { 
				Long reasonId = question.getAnswerReasonExpressionId();
				Boolean shouldAnswer = 
					reasonId == null || 
					Expressions.evaluateAsBool(
							context.eidToExpression.get(reasonId), 
							new ArrayList<Alter>(), 
							context);

				if(shouldAnswer) {
					return question;
				}
			}
			if(current != null && question.getId().equals(current.getId())) {
				passedCurrent = true;
			}
		}
		return null;
	}

	public static List<Question> answeredEgoQuestionsForInterview(
			Session session, EvaluationContext context, Long interviewId) 
			{
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.EGO);
		List<Question> answeredQuestions = Lists.newArrayList();
		for(Question question : questions) {
			Long reasonId = question.getAnswerReasonExpressionId();
			Boolean shouldAnswer = 
				reasonId == null || 
				Expressions.evaluateAsBool(
						context.eidToExpression.get(reasonId), 
						new ArrayList<Alter>(), 
						context);
			if(shouldAnswer && context.qidToEgoAnswer.get(question.getId()) != null) {
				answeredQuestions.add(question);
			}
		}
		return answeredQuestions;
	}
	
	public static InterviewingAlterPage.Subject nextAlterPageForInterview(final Long interviewId, 
			final InterviewingAlterPage.Subject currentPage, final Boolean forward, final Boolean unansweredOnly)
	{
		return new DB.Action<InterviewingAlterPage.Subject>() {
			public InterviewingAlterPage.Subject get() {
				return nextAlterPageForInterview(
						session, interviewId, currentPage, 
						forward, unansweredOnly);
			}
		}.execute();
	}
	public static InterviewingAlterPage.Subject nextAlterPageForInterview(Session session, Long interviewId, 
			InterviewingAlterPage.Subject currentPage, Boolean forward, Boolean unansweredOnly)
	{
		InterviewingAlterPage.Subject previousPage = currentPage;
		EvaluationContext context = 
			Expressions.getContext(session, Interviews.getInterview(session, interviewId));
		TreeSet<InterviewingAlterPage.Subject> pages =
			alterPagesForInterview(session, interviewId);
		while(true) {
			InterviewingAlterPage.Subject nextPage;
			if(pages.isEmpty()) {
				return null;
			}
			if(previousPage == null) {
				nextPage = 
					forward ? pages.first() : pages.last();
			} else {
				nextPage =
					forward ? pages.higher(previousPage) : pages.lower(previousPage);
			}
			if(nextPage == null) {
				return null;
			}
			if(! unansweredOnly) {
				return nextPage;
			}
			for(Alter alter : nextPage.alters) {
				Boolean answered = 
					context.qidAidToAlterAnswer.containsKey(
							new PairUni<Long>(nextPage.question.getId(),alter.getId()));
				if(! answered) {
					return nextPage;
				}
			}
			previousPage = nextPage;
		}
	}
	
	public static InterviewingAlterPairPage.Subject nextAlterPairPageForInterview(
			final Long interviewId, final InterviewingAlterPairPage.Subject currentPage, 
			final Boolean forward, final Boolean unansweredOnly)
	{
		return new DB.Action<InterviewingAlterPairPage.Subject>() {
			public InterviewingAlterPairPage.Subject get() {
				return nextAlterPairPageForInterview(
						session, interviewId, currentPage, 
						forward, unansweredOnly);
			}
		}.execute();
	}
	
	public static InterviewingAlterPairPage.Subject nextAlterPairPageForInterview(Session session, 
			Long interviewId, InterviewingAlterPairPage.Subject currentPage, 
			Boolean forward, Boolean unansweredOnly)
	{
		InterviewingAlterPairPage.Subject previousPage = currentPage;
		EvaluationContext context = 
			Expressions.getContext(session, Interviews.getInterview(session, interviewId));
		TreeSet<InterviewingAlterPairPage.Subject> pages =
			alterPairPagesForInterview(session, interviewId);
		while(true) {
			InterviewingAlterPairPage.Subject nextPage;
			if(pages.isEmpty()) {
				return null;
			}
			if(previousPage == null) {
				nextPage = 
					forward ? pages.first() : pages.last();
			} else {
				nextPage =
					forward ? pages.higher(previousPage) : pages.lower(previousPage);
			}
			if(nextPage == null) {
				return null;
			}
			if(! unansweredOnly) {
				return nextPage;
			}
			for(Alter secondAlter : nextPage.secondAlters) {
				Boolean answered = 
					context.qidA1idA2idToAlterPairAnswer.containsKey(
							new TripleUni<Long>(
									nextPage.question.getId(),
									nextPage.firstAlter.getId(),
									secondAlter.getId()));
				if(! answered) {
					return nextPage;
				}
			}
			previousPage = nextPage;
		}
	}
	
	public static Question nextNetworkQuestionForInterview(
			final Long interviewId, final Question current, 
			final Boolean forward, final Boolean unansweredOnly) 
	{
		return DB.withTx(new Function<Session,Question>() {
			public Question apply(Session session) {
				return nextNetworkQuestionForInterview(session,interviewId,current,forward,unansweredOnly);
			}
		});
	}
	
	public static Question nextNetworkQuestionForInterview(
			Session session, Long interviewId, Question current, Boolean forward, Boolean unansweredOnly)
	{
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.NETWORK);
		if(! forward) {
			Collections.reverse(questions);
		}
		List<Answer> answers = 
			Answers.getAnswersForInterview(session, interviewId, QuestionType.NETWORK);
		Boolean passedCurrent = current == null;
		EvaluationContext context = Expressions.getContext(session, interview);
		for(Question question : questions) {
			Boolean foundAnswer = false;
			for(Answer answer : answers) {
				if(answer.getQuestionId().equals(question.getId())) {
					foundAnswer = true;
				}
			}
			if(unansweredOnly && foundAnswer) {
				// Looking for unanswered. This one is answered. Not the question we're looking for.
			} else if(passedCurrent) { 
				Long reasonId = question.getAnswerReasonExpressionId();
				Boolean shouldAnswer = 
					reasonId == null || 
					Expressions.evaluateAsBool(
							context.eidToExpression.get(reasonId), 
							new ArrayList<Alter>(), 
							context);
				if(shouldAnswer) {
					return question;
				}
			}
			if(current != null && question.getId().equals(current.getId())) {
				passedCurrent = true;
			}
		}
		return null;
	}

	public static List<Question> answeredNetworkQuestionsForInterview(
			Session session, EvaluationContext context, Long interviewId) 
			{
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.NETWORK);
		List<Question> answeredQuestions = Lists.newArrayList();
		for(Question question : questions) {
			Long reasonId = question.getAnswerReasonExpressionId();
			Boolean shouldAnswer = 
				reasonId == null || 
				Expressions.evaluateAsBool(
						context.eidToExpression.get(reasonId), 
						new ArrayList<Alter>(), 
						context);
			if(shouldAnswer && context.qidToNetworkAnswer.get(question.getId()) != null) {
				answeredQuestions.add(question);
			}
		}
		return answeredQuestions;
	}

	// Note: sections only relevant for alter and alter pair questions... for now...
	private static Map<Long,TreeSet<Question>> 
	createQuestionIdToSectionMap(Collection<Question> questions) 
	{
		Map<Long,TreeSet<Question>> questionIdToSection = Maps.newTreeMap();
		TreeSet<Question> section = null;
		Question previousQuestion = null;
		for(Question question : new TreeSet<Question>(questions)) {
			if(section == null || // reasons to start a new section
					question.hasPreface() ||
					question.getAskingStyleList() || 
					previousQuestion.getAskingStyleList()) 
			{
				section = new TreeSet<Question>();
			}
			previousQuestion = question;
			section.add(question);
			questionIdToSection.put(question.getId(), section);
		}
		return questionIdToSection;
	}

	public static String getPrefaceBetweenQuestions(final Question early, final Question late) {
		return new DB.Action<String>() {
			public String get() {
				return getPrefaceBetweenQuestions(session,early,late);
			}
		}.execute();
	}
	public static String getPrefaceBetweenQuestions(
			Session session, Question early, Question late) 
	{
		if(late == null) {
			return null;
		}
		Set<Question> section = 
			createQuestionIdToSectionMap(
					Questions.getQuestionsForStudy(late.getStudyId(), late.getType()))
			.get(late.getId());
		if(early != null && section.contains(early)) {
			return null;
		}
		for(Question question : section) {
			if(question.hasPreface()) {
				return question.getPreface();
			}
		}
		return null;
	}
	
	public static ArrayList<InterviewingAlterPage.Subject> 
	answeredAlterPagesForInterview(Session session, EvaluationContext context, Long interviewId)
	{
		ArrayList<InterviewingAlterPage.Subject> results = Lists.newArrayList();
		for(InterviewingAlterPage.Subject subject : alterPagesForInterview(session,interviewId)) {
			Boolean answered = false;
			for(Alter alter : subject.alters) {
				if(context.qidAidToAlterAnswer.containsKey(
						new PairUni<Long>(
								subject.question.getId(),
								alter.getId())))
				{
					answered = true;
				}
			}
			if(answered) {
				results.add(subject);
			}
		}
		return results;
	}
	
	private static TreeSet<InterviewingAlterPage.Subject> 
	alterPagesForInterview(Session session, Long interviewId)
	{
		ArrayList<Pair<Question,ArrayList<Alter>>> questionAlterListPairs =
			alterQuestionsForInterview(session,interviewId);
		Interview interview = Interviews.getInterview(interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.ALTER);
		Map<Long,TreeSet<Question>> qIdToSection = 
			createQuestionIdToSectionMap(new TreeSet<Question>(questions));
		TreeSet<InterviewingAlterPage.Subject> results = Sets.newTreeSet();
		for(Pair<Question,ArrayList<Alter>> questionAndAlters : questionAlterListPairs) {
			Question question = questionAndAlters.getFirst();
			ArrayList<Alter> alters = questionAndAlters.getSecond();
			if(question.getAskingStyleList()) {
				InterviewingAlterPage.Subject subject = new InterviewingAlterPage.Subject();
				subject.alters = alters;
				subject.interviewId = interviewId;
				subject.question = question;
				subject.sectionQuestions = qIdToSection.get(question.getId());
				results.add(subject);
			} else {
				for(Alter alter : alters) {
					InterviewingAlterPage.Subject subject = new InterviewingAlterPage.Subject();
					subject.alters = Lists.newArrayList(alter);
					subject.interviewId = interviewId;
					subject.question = question;
					subject.sectionQuestions = qIdToSection.get(question.getId());
					results.add(subject);
				}
			}
		}
		return results;
	}
	
	public static ArrayList<InterviewingAlterPairPage.Subject> 
	answeredAlterPairPagesForInterview(Session session, EvaluationContext context, Long interviewId)
	{
		ArrayList<InterviewingAlterPairPage.Subject> results = Lists.newArrayList();
		for(InterviewingAlterPairPage.Subject subject : alterPairPagesForInterview(session,interviewId)) {
			Boolean answered = false;
			for(Alter secondAlter : subject.secondAlters) {
				if(context.qidA1idA2idToAlterPairAnswer.containsKey(
						new TripleUni<Long>(
								subject.question.getId(),
								subject.firstAlter.getId(),
								secondAlter.getId())))
				{
					answered = true;
				}
			}
			if(answered) {
				results.add(subject);
			}
		}
		return results;
	}
	
	
	private static TreeSet<InterviewingAlterPairPage.Subject> 
	alterPairPagesForInterview(Session session, Long interviewId)
	{
		ArrayList<Triple<Question,Alter,ArrayList<Alter>>> questionAlterSecondAltersList =
			alterPairQuestionFirstAlterAndSecondAlters(session, interviewId);
		Interview interview = Interviews.getInterview(interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.ALTER_PAIR);
		Map<Long,TreeSet<Question>> qIdToSection = 
			createQuestionIdToSectionMap(new TreeSet<Question>(questions));
		TreeSet<InterviewingAlterPairPage.Subject> results = Sets.newTreeSet();
		for(Triple<Question,Alter,ArrayList<Alter>> questionAlterSecondAlters : questionAlterSecondAltersList) 
		{
			Question question = questionAlterSecondAlters.getFirst();
			Alter firstAlter = questionAlterSecondAlters.getSecond();
			ArrayList<Alter> secondAlters = questionAlterSecondAlters.getThird();
			if(question.getAskingStyleList()) {
				InterviewingAlterPairPage.Subject subject = new InterviewingAlterPairPage.Subject();
				subject.interviewId = interviewId;
				subject.question = question;
				subject.firstAlter = firstAlter;
				subject.secondAlters = secondAlters;
				subject.sectionQuestions = qIdToSection.get(question.getId());
				results.add(subject);
			} else {
				for(Alter secondAlter : secondAlters) {
					InterviewingAlterPairPage.Subject subject = new InterviewingAlterPairPage.Subject();
					subject.interviewId = interviewId;
					subject.question = question;
					subject.firstAlter = firstAlter;
					subject.secondAlters = Lists.newArrayList(secondAlter);
					subject.sectionQuestions = qIdToSection.get(question.getId());
					results.add(subject);
				}
			}
		}
		return results;
	}
	 
	/**
	 * alterQuestionsForInterview is response for getting the set of
	 * questions to ask in the alter section.  Here is an example that
	 * explains how the questions are grouped:
	 *   Q1 non list question with preface text
	 *   Q2 non list question (no preface text)
	 *   Q3 non list question with preface text
	 *   Q4 non list question (no preface text)
	 *   Q5 list question
	 *   Q6 non list question
	 *   Q7 non list question
	 *   Q8 list question
	 *  
	 *   It should ask:
	 *  
	 *   1. Q1 for alter 1, Q2 for alter 1, Q1 for alter 2, Q2 for alter 2 and so on for each alter
	 *   2. Q3 for alter 1, Q4 for alter 1, Q3 for alter 2, Q4 for alter 2 and so on for each alter
	 *   3. Q5 in list format
	 *   4. Q6 for alter 1, Q7 for alter 1, Q6 for alter 2, Q7 for alter 2 and so on for each alter
	 *   5. Q8 in list format
	 */
	
	private static ArrayList<Pair<Question,ArrayList<Alter>>> alterQuestionsForInterview(
			Session session, Long interviewId) 
	{
	
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.ALTER);
		Multimap<Long,Answer> questionIdToAnswers = ArrayListMultimap.create();
		for(Answer answer : Answers.getAnswersForInterview(session, interviewId, QuestionType.ALTER)) {
			questionIdToAnswers.put(answer.getQuestionId(), answer);
		}
		List<Alter> alters = Alters.getForInterview(session, interviewId);
		EvaluationContext context = Expressions.getContext(session, interview);
		ArrayList<Pair<Question,ArrayList<Alter>>> results = Lists.newArrayList();
		for(Question question : questions) {
			Set<Long> answeredAlterIds = Sets.newHashSet();
			for(Answer answer : questionIdToAnswers.get(question.getId())) {
				answeredAlterIds.add(answer.getAlterId1());
			}
			ArrayList<Alter> resultAlters = Lists.newArrayList();
			for(Alter alter : alters) {
				Long reasonId = question.getAnswerReasonExpressionId();
				// Boolean shouldAnswer =
				// 	reasonId == null ||
				// 		Expressions.evaluateAsBool(
				// 				context.eidToExpression.get(reasonId), 
				// 				Lists.newArrayList(alter), 
				// 				context);
				Boolean shouldAnswer;
				
				if ( reasonId==null ) {
					shouldAnswer = true;
				} else {
					shouldAnswer = Expressions.evaluateAsBool(
							 				context.eidToExpression.get(reasonId), 
							 				Lists.newArrayList(alter), 
							 				context);
				}

				
				if(shouldAnswer) {
					resultAlters.add(alter);
				}
			}
			if(! resultAlters.isEmpty()) {
				results.add(new Pair<Question,ArrayList<Alter>>(question,resultAlters));
			}
		}
		return results;
	}
	
	private static ArrayList<Triple<Question,Alter,ArrayList<Alter>>>
	alterPairQuestionFirstAlterAndSecondAlters(Session session, Long interviewId) {
		ArrayList<Triple<Question,Alter,ArrayList<Alter>>> results = Lists.newArrayList();
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.ALTER_PAIR);
		List<Alter> alters = Alters.getForInterview(session, interviewId);
		EvaluationContext context = Expressions.getContext(session, interview);
		for(Question question : questions) {
			for(Alter alter1 : alters) {
				ArrayList<Alter> secondAlters = Lists.newArrayList();
				for(Alter alter2 : alters) {
					if( ( !question.getSymmetric() && alter1.getId()!=alter2.getId()) ||  
							alter1.getId() < alter2.getId()) {
						Long reasonId = question.getAnswerReasonExpressionId();
						Boolean shouldAnswer =
							reasonId == null ||
							Expressions.evaluateAsBool(
									context.eidToExpression.get(reasonId), 
									Lists.newArrayList(alter1,alter2), 
									context);
												
						if( shouldAnswer ) {
							secondAlters.add(alter2);
						}
					}
				}
				if(! secondAlters.isEmpty()) {
					results.add(new Triple<Question,Alter,ArrayList<Alter>>(
							question,alter1,secondAlters));
				}
			}
		}
		return results;
	}

	public static List<InterviewNavigationPanel.InterviewLink> 
	getAnsweredPagesForInterview(final Long interviewId) {
		return new DB.Action<List<InterviewNavigationPanel.InterviewLink>>() {
			public List<InterviewNavigationPanel.InterviewLink> get() {
				return getAnsweredPagesForInterview(session, interviewId);
			}
		}.execute();
	}
	public static List<InterviewNavigationPanel.InterviewLink> 
	getAnsweredPagesForInterview(Session session, Long interviewId) {
		Interview interview = Interviews.getInterview(session, interviewId);
		EvaluationContext context = Expressions.getContext(session, interview);
		List<InterviewLink> links = Lists.newArrayList();
		for(Question egoQuestion : answeredEgoQuestionsForInterview(session,context,interviewId)) {
			links.add(new InterviewNavigationPanel.EgoLink(interviewId,egoQuestion));
		}
		Integer alters = Alters.getForInterview(session,interviewId).size();
		if(alters > 0) {
			links.add(new InterviewNavigationPanel.AlterPromptLink(interviewId,alters));
		}
		for(InterviewingAlterPage.Subject alterSubject : 
			answeredAlterPagesForInterview(session,context,interviewId)) 
		{
			links.add(new InterviewNavigationPanel.AlterLink(alterSubject));
		}
		for(InterviewingAlterPairPage.Subject alterPairSubject : 
			answeredAlterPairPagesForInterview(session,context,interviewId)) 
		{
			links.add(new InterviewNavigationPanel.AlterPairLink(alterPairSubject));
		}
		for(Question networkQuestion : answeredNetworkQuestionsForInterview(session,context,interviewId)) {
			links.add(new InterviewNavigationPanel.NetworkLink(interviewId,networkQuestion));
		}
		return links;
	}
	
	/**
	 * simple convenience function for alterQuestionsForInterview
	 * reverses a list of questions
	 * @param questionList list to be reverse
	 * @return the input lists data but in reverse order
	 */
	private static List<Question> reverse ( List<Question> questionList) {
		int ix;
		ArrayList<Question> returnList = new ArrayList<Question>(questionList.size());
		
		for ( ix=questionList.size()-1 ; ix>=0 ; --ix ) 
			returnList.add(questionList.get(ix));
		return(returnList);
	}
	
	/**
	 * simple convenience function for alterQuestionsForInterview
	 * starting from the start on the left, remove all questions up to 
	 * and including prevQuestion (if present)
	 * @param questionList list to remove items from
	 * @param prevQuestion question we will trim up to 
	 * @return the input list possibly shortened
	 */
	private static List<Question> trimLeading ( List<Question> questionList, Question prevQuestion ) {
		int ix;
		int questionIndex = -1;
		Question removed;
		
		if ( prevQuestion==null )
			return(questionList);
		questionIndex = questionList.indexOf(prevQuestion);
		if ( questionIndex<1 )
			return(questionList);
		
		for ( ix=questionIndex-1 ; ix>=0 ; --ix ) {
			removed = questionList.remove(ix);
			System.out.println ("Skipping " + removed);
		}
		return(questionList);
	}
	
	/**
	 * this version of alterQuestionsForInterview will return just ONE question,
	 * the next questions to ask
	 * @param session database object
	 * @param interviewId IDs this interview
	 * @param eContext EvaluationContext that will check for answered questions
	 * @param prevQuestion the question just asked
	 * @param forward if true, advancing 
	 * @param unansweredOnly if true, only want unanswered questions
	 * @return a ArrayList with just one question
	 */
	private static ArrayList<Pair<Question,ArrayList<Alter>>> alterQuestionsForInterview(
			Session session, Long interviewId, EvaluationContext eContext,
			Question prevQuestion, Boolean forward, Boolean unansweredOnly) 
	{
		long onFunctionEntry = System.currentTimeMillis();
		long onFunctionExit;
		long onSkipLogicEntry;
		long onUseIfEntry;
		long timeInSetup = 0;
		long timeInFunction = 0;
		long timeInSkipLogic = 0;
		long timeInUseIf = 0;
		int  skipLogicCount = 0;
		int  useIfCount = 0;
		
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.ALTER);
		List<Alter> alters = Alters.getForInterview(session, interviewId);
		EvaluationContext context = Expressions.getContext(session, interview);
		ArrayList<Pair<Question,ArrayList<Alter>>> results = Lists.newArrayList();
		
		System.out.println ( "========================");
		System.out.println ("Moving " + (forward ? "forward" : "backward") + "  looking for " + 
				(unansweredOnly ? "UNanswered only" : "answered OR unanswered"));
		
		// to make things simpler, reverse the order of the list if we
		// are going backward
		if ( !forward ) {
			questions = reverse(questions);
		}
		// also to help keep things simple, get rid of questions we
		// know we have absolutely no use for.
		// that is, all questions up to and including the previous one.
		// ( if we are going backwards we don't want questions IN FRONT
		// of the previous one, but the reverse took care of that
		if ( prevQuestion!=null) {
			System.out.println ("prevquestion=" + prevQuestion.getTitle());
			questions = trimLeading(questions, prevQuestion);
		} else {
			System.out.println ("prevquestion=null");
		}
		
		timeInSetup = System.currentTimeMillis() - onFunctionEntry;
		for(Question question : questions) {
			System.out.println ( "examining " + question.getTitle());
			ArrayList<Alter> resultAlters = Lists.newArrayList();
			Long reasonId = question.getAnswerReasonExpressionId();
			Boolean answered = true;
			Boolean shouldAnswerSkipLogic = true;
			Boolean shouldAnswerUseIf = true;
			// if we are looking for unanswered only and any one
			// alter has answered the question we are not interested
			// in the question
	//		if ( unansweredOnly ) {
	//			for ( Alter alter : alters ) {
	//				if ( !context.qidAidToAlterAnswer.containsKey(
	//						new PairUni<Long>(question.getId(),alter.getId())))
	//					answered = false;
	//			}
	//			if ( answered )
	//				System.out.println ( "Skipping, looking for unanswered and this has an answer");
	//		}
			
			if ( true /* !unansweredOnly || !answered */ ) {
			    for(Alter alter : alters) {
			    	int iEvaluate;
			    	shouldAnswerSkipLogic = shouldAnswerUseIf = true;
			    	ArrayList<Alter> singleAlterList = Lists.newArrayList(alter);
			    	
			    	if ( reasonId!=null ) {
			    		++skipLogicCount;
			    		onSkipLogicEntry = System.currentTimeMillis();
			    		shouldAnswerSkipLogic = Expressions.evaluateAsBool(
							 				context.eidToExpression.get(reasonId), 
							 				singleAlterList, 
							 				context);
			    		System.out.println ( "skip logic says to " + (shouldAnswerSkipLogic ? "ask  " : "SKIP " ) + question.getTitle() + " with " + alter.getName());
			    		timeInSkipLogic += System.currentTimeMillis() - onSkipLogicEntry;
			    	} // end if skip logic reasonID != null
			    
			    	if ( shouldAnswerSkipLogic ) {
			    		String strUseIf = question.getUseIfExpression();
			    		if ( strUseIf!=null && strUseIf.trim().length()>0 ) {
			    			++useIfCount;
			    			onUseIfEntry = System.currentTimeMillis();
			    			strUseIf = strUseIf.trim();
			    			strUseIf = question.answerCountInsertion(strUseIf, interviewId);
			    			strUseIf = question.questionContainsAnswerInsertion(strUseIf, interviewId, singleAlterList);		
			    			strUseIf = question.calculationInsertion(strUseIf, interviewId, singleAlterList);
			    			strUseIf = question.variableInsertion(strUseIf, interviewId, singleAlterList);
			    			iEvaluate = SimpleLogicMgr.createSimpleExpressionAndEvaluate (
			    				strUseIf, interviewId, 
			    				question.getType(), question.getStudyId(), singleAlterList);
			    			if ( SimpleLogicMgr.hasError()) {
			    				System.out.println ("USE IF error in " + question.getTitle());
			    				System.out.println ("USE IF =" + question.getUseIfExpression());
			    			}
			    			if (iEvaluate==0)
			    				shouldAnswerUseIf = false;
			    			System.out.println ( "use-if says to " + (shouldAnswerUseIf ? "ask  " : "SKIP " ) + question.getTitle() + " with " + alter.getName());
			    			timeInUseIf += System.currentTimeMillis() - onUseIfEntry;
			    		}  // end if strUseIf != null
			    	} // end if shouldAnswerSkipLogic
			    if(shouldAnswerSkipLogic && shouldAnswerUseIf) {
			    	System.out.println ( " adding " + alter.getName() + " to list ");
			    	resultAlters.add(alter);
			    } // end of for ( Alter alters 
			    if( !resultAlters.isEmpty()) {
			    	results.add(new Pair<Question,ArrayList<Alter>>(question,resultAlters));	
			    } // end of !resultAlters.isEmpty()
			} // end off for ( Alter alters... loop
		} // end  if ( !unansweredOnly || !answered )
			
		if ( false /* !results.isEmpty() */ ) {
			onFunctionExit = System.currentTimeMillis();
			timeInFunction = onFunctionExit - onFunctionEntry;
			System.out.println ( "Time in NEW alterQuestionsForInterview=" + timeInFunction);
			System.out.println ( "Time in setup =" + timeInSetup);
			if ( skipLogicCount>0 )
			    System.out.println ( "Time in skip-logic=" + timeInSkipLogic + " (" + skipLogicCount + ") @ " + timeInSkipLogic/skipLogicCount);
			if ( useIfCount>0 )
				System.out.println ( "Time in use-if=" + timeInUseIf + " (" + useIfCount + ") @ " + timeInUseIf/useIfCount);
			System.out.println ("returning " + results.size());
			return results;  
			}
		}
		// if we get to this point results is empty, no questions were found
		onFunctionExit = System.currentTimeMillis();
		timeInFunction = onFunctionExit - onFunctionEntry;
		System.out.println ( "Exit from bottom Time in NEW alterQuestionsForInterview=" + timeInFunction);
		System.out.println ( "Time in setup =" + timeInSetup);
		if ( skipLogicCount>0 )
		    System.out.println ( "Time in skip-logic=" + timeInSkipLogic + " (" + skipLogicCount + ") @ " + timeInSkipLogic/skipLogicCount);
		if ( useIfCount>0 )
			System.out.println ( "Time in use-if=" + timeInUseIf + " (" + useIfCount + ") @ " + timeInUseIf/useIfCount);
		System.out.println ("returning " + results.size());
		return results;
	}
	
	/**
	 * duplicate of the existing alterPagesForInterview above but
	 * has the four additional parameters to pass to the new version
	 * of alterQuestionsForInterview
	 * @param session
	 * @param interviewId
	 * @param eContext
	 * @param prevQuestion
	 * @param forward
	 * @param unansweredOnly
	 * @return
	 */
	private static TreeSet<InterviewingAlterPage.Subject> 
	alterPagesForInterview(Session session, Long interviewId,
			EvaluationContext eContext,
			Question prevQuestion, Boolean forward, Boolean unansweredOnly) 
	{
		ArrayList<Pair<Question,ArrayList<Alter>>> questionAlterListPairs =
			alterQuestionsForInterview(session,interviewId,
			eContext, prevQuestion, forward, unansweredOnly );
		Interview interview = Interviews.getInterview(interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.ALTER);
		Map<Long,TreeSet<Question>> qIdToSection = 
			createQuestionIdToSectionMap(new TreeSet<Question>(questions));
		TreeSet<InterviewingAlterPage.Subject> results = Sets.newTreeSet();
		for(Pair<Question,ArrayList<Alter>> questionAndAlters : questionAlterListPairs) {
			Question question = questionAndAlters.getFirst();
			ArrayList<Alter> alters = questionAndAlters.getSecond();
			if(question.getAskingStyleList()) {
				InterviewingAlterPage.Subject subject = new InterviewingAlterPage.Subject();
				subject.alters = alters;
				subject.interviewId = interviewId;
				subject.question = question;
				subject.sectionQuestions = qIdToSection.get(question.getId());
				results.add(subject);
			} else {
				for(Alter alter : alters) {
					InterviewingAlterPage.Subject subject = new InterviewingAlterPage.Subject();
					subject.alters = Lists.newArrayList(alter);
					subject.interviewId = interviewId;
					subject.question = question;
					subject.sectionQuestions = qIdToSection.get(question.getId());
					results.add(subject);
				}
			}
		}
		return results;
	}
	
	/**
	 * new version of nextAlterPageForInterview
	 * @param interviewId this interview
	 * @param currentPage 'subject' if this is null we are 'entering' the alter section,
	 *                     and will want the first or the last question
	 * @param forward  if false user pressed the << button, searching backwards
	 * @param unansweredOnly  if true looking for unanswered questions only
	 * @return
	 */
	public static InterviewingAlterPage.Subject nextAlterPageForInterviewNEW(final Long interviewId, 
			final InterviewingAlterPage.Subject currentPage, final Boolean forward, final Boolean unansweredOnly)
	{
		return new DB.Action<InterviewingAlterPage.Subject>() {
			public InterviewingAlterPage.Subject get() {
				return nextAlterPageForInterviewNEW(
						session, interviewId, currentPage, 
						forward, unansweredOnly);
			}
		}.execute();
	}
	
	/**
	 * new version of nextAlterPageForInterview
	 * @param session  the database 
	 * @param interviewId this interview
	 * @param currentPage 'subject' if this is null we are 'entering' the alter section,
	 *                     and will want the first or the last question
	 * @param forward if true searching forward to next question
	 * @param unansweredOnly if true only want unanswered questions
	 * @return
	 */                                       
	public static InterviewingAlterPage.Subject nextAlterPageForInterviewNEW(Session session, Long interviewId, 
			InterviewingAlterPage.Subject currentPage, Boolean forward, Boolean unansweredOnly)
	{
		InterviewingAlterPage.Subject previousPage = currentPage;
		Question previousQuestion = (currentPage==null) ? null : currentPage.question;
		EvaluationContext context = 
			Expressions.getContext(session, Interviews.getInterview(session, interviewId));
		TreeSet<InterviewingAlterPage.Subject> pages =
			alterPagesForInterview(session, interviewId, context, previousQuestion, forward, unansweredOnly);
		
		while(true) {
			InterviewingAlterPage.Subject nextPage;
			if(pages.isEmpty()) {
				return null;
			}
			if(previousPage == null) {
				nextPage = 
					forward ? pages.first() : pages.last();
			} else {
				nextPage =
					forward ? pages.higher(previousPage) : pages.lower(previousPage);
			}
			if(nextPage == null) {
				return null;
			}
			if(! unansweredOnly) {
				return nextPage;
			}
			for(Alter alter : nextPage.alters) {
				Boolean answered = 
					context.qidAidToAlterAnswer.containsKey(
							new PairUni<Long>(nextPage.question.getId(),alter.getId()));
				if(! answered) {
					return nextPage;
				}
			}
			previousPage = nextPage;
		}
	}
		
}
