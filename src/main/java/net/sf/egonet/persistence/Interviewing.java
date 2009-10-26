package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;
import net.sf.egonet.persistence.Expressions.EvaluationContext;
import net.sf.functionalj.tuple.Pair;
import net.sf.functionalj.tuple.PairUni;
import net.sf.functionalj.tuple.TripleUni;

import org.hibernate.Session;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
					Expressions.evaluate(
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

	public static Pair<Question,ArrayList<Alter>> nextAlterQuestionForInterview(
			final Long interviewId, 
			final Question currentQuestion, final Alter currentAlter, 
			final Boolean forward, final Boolean unansweredOnly, final Integer maxAlters)
	{
		return new DB.Action<Pair<Question,ArrayList<Alter>>>() {
			public Pair<Question, ArrayList<Alter>> get() {
				return nextAlterQuestionForInterview(
						session, interviewId, currentQuestion, currentAlter, 
						forward, unansweredOnly, maxAlters);
			}
		}.execute();
	}
	
	public static Pair<Question,ArrayList<Alter>> nextAlterQuestionForInterview(
			Session session, Long interviewId, Question currentQuestion, Alter currentAlter, 
			Boolean forward, Boolean unansweredOnly, Integer maxAlters)
	{
		ArrayList<Pair<Question,ArrayList<Alter>>> questions =
			alterQuestionsForInterview(session,interviewId,forward);
		Boolean passedCurrent = currentQuestion == null; // No current question? Then pretend already passed it.
		Interview interview = Interviews.getInterview(interviewId);
		EvaluationContext context = Expressions.getContext(session, interview);
		for(Pair<Question,ArrayList<Alter>> questionAlters : questions) {
			Question question = questionAlters.getFirst();
			ArrayList<Alter> results = Lists.newArrayList();
			for(Alter alter : questionAlters.getSecond()) {
				Boolean answered = 
					context.qidAidToAlterAnswer.containsKey(
							new PairUni<Long>(question.getId(),alter.getId()));
				if(passedCurrent && (maxAlters == null || results.size() < maxAlters) &&
						! (unansweredOnly && answered)) {
					results.add(alter);
				}
				if(currentQuestion != null && 
						currentQuestion.getId().equals(question.getId()) && 
						currentAlter.getId().equals(alter.getId())) 
				{
					passedCurrent = true;
				}
			}
			if(! results.isEmpty()) {
				if(! forward) { // Want to return alters in normal order.
					Collections.reverse(results);
				}
				return new Pair<Question,ArrayList<Alter>>(question,results);
			}
		}
		return null;
	}

	public static ArrayList<Pair<Question,ArrayList<Alter>>> 
	alterQuestionsForInterview(Session session, Long interviewId)
	{
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.ALTER);
		List<Alter> alters = Alters.getForInterview(session, interviewId);
		ArrayList<Pair<Question,ArrayList<Alter>>> results = Lists.newArrayList();
		for(Question question : questions) {
			results.add(new Pair<Question,ArrayList<Alter>>(
					question, new ArrayList<Alter>(alters)));
		}
		return results;
	}
	
	private static ArrayList<Pair<Question,ArrayList<Alter>>>
	afterCurrent(Question currentQuestion, Alter currentAlter,
			ArrayList<Pair<Question,ArrayList<Alter>>> questionsAndAlters)
	{
		if(currentQuestion == null) {
			return questionsAndAlters;
		}
		Boolean passedCurrent = false;
		ArrayList<Pair<Question,ArrayList<Alter>>> results = Lists.newArrayList();
		for(Pair<Question,ArrayList<Alter>> questionAndAlters : questionsAndAlters) {
			Question question = questionAndAlters.getFirst();
			ArrayList<Alter> alters = Lists.newArrayList();
			for(Alter alter : questionAndAlters.getSecond()) {
				if(passedCurrent) {
					alters.add(alter);
				}
				if(question.getId().equals(currentQuestion.getId()) && 
						alter.getId().equals(currentAlter.getId()))
				{
					passedCurrent = true;
				}
			}
			if(! alters.isEmpty()) {
				results.add(new Pair<Question,ArrayList<Alter>>(question,alters));
			}
		}
		return results;
	}
	
	private static ArrayList<Pair<Question,ArrayList<Alter>>>
	unansweredOnly(
			ArrayList<Pair<Question,ArrayList<Alter>>> questionsAndAlters,
			Map<PairUni<Long>,Answer> questionAndAlterToAnswer)
	{
		ArrayList<Pair<Question,ArrayList<Alter>>> results = Lists.newArrayList();
		for(Pair<Question,ArrayList<Alter>> questionAndAlters : questionsAndAlters) {
			ArrayList<Alter> alters = Lists.newArrayList();
			for(Alter alter : questionAndAlters.getSecond()) {
				if(questionAndAlterToAnswer.containsKey(
						new PairUni<Long>(
								questionAndAlters.getFirst().getId(),
								alter.getId()))) 
				{
					alters.add(alter);
				}
			}
			if(! alters.isEmpty()) {
				results.add(new Pair<Question,ArrayList<Alter>>(
						questionAndAlters.getFirst(),
						alters));
			}
		}
		return results;
	}
	
	private static ArrayList<Pair<Question,ArrayList<Alter>>>
	reverseQuestionsAndAlters(ArrayList<Pair<Question,ArrayList<Alter>>> questionsAndAlters) 
	{
		ArrayList<Pair<Question,ArrayList<Alter>>> results = Lists.newArrayList();
		for(Pair<Question,ArrayList<Alter>> questionAndAlters : 
			Iterables.reverse(questionsAndAlters)) 
		{
			results.add(new Pair<Question,ArrayList<Alter>>(
					questionAndAlters.getFirst(),
					Lists.newArrayList(Iterables.reverse(questionAndAlters.getSecond()))));
		}
		return results;
	}
	
	public static ArrayList<Pair<Question,ArrayList<Alter>>> alterQuestionsForInterview(
			Session session, Long interviewId, Boolean forward) 
	{
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.ALTER);
		Multimap<Long,Answer> questionIdToAnswers = ArrayListMultimap.create();
		for(Answer answer : Answers.getAnswersForInterview(session, interviewId, QuestionType.ALTER)) {
			questionIdToAnswers.put(answer.getQuestionId(), answer);
		}
		List<Alter> alters = Alters.getForInterview(session, interviewId);
		if(! forward) {
			Collections.reverse(questions);
			Collections.reverse(alters);
		}
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
				Boolean shouldAnswer =
					reasonId == null ||
						Expressions.evaluate(
								context.eidToExpression.get(reasonId), 
								Lists.newArrayList(alter), 
								context);
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
	
	public static Pair<Question,ArrayList<PairUni<Alter>>> nextAlterPairQuestionForInterview(
			final Long interviewId, 
			final Question currentQuestion, final PairUni<Alter> currentAlterPair, 
			final Boolean forward, final Boolean unansweredOnly, final Integer maxAlterPairs)
	{
		return new DB.Action<Pair<Question,ArrayList<PairUni<Alter>>>>() {
			public Pair<Question, ArrayList<PairUni<Alter>>> get() {
				return nextAlterPairQuestionForInterview(
						session, interviewId, currentQuestion, currentAlterPair, 
						forward, unansweredOnly, maxAlterPairs);
			}
		}.execute();
	}
	
	public static Pair<Question,ArrayList<PairUni<Alter>>> nextAlterPairQuestionForInterview(
			Session session, Long interviewId, Question currentQuestion, PairUni<Alter> currentAlterPair, 
			Boolean forward, Boolean unansweredOnly, Integer maxAlterPairs)
	{
		ArrayList<Pair<Question,ArrayList<PairUni<Alter>>>> questions =
			alterPairQuestionsForInterview(session,interviewId,forward);
		Boolean passedCurrent = currentQuestion == null; // No current question? Then pretend already passed it.
		Interview interview = Interviews.getInterview(interviewId);
		EvaluationContext context = Expressions.getContext(session, interview);
		for(Pair<Question,ArrayList<PairUni<Alter>>> questionAlters : questions) {
			Question question = questionAlters.getFirst();
			ArrayList<PairUni<Alter>> results = Lists.newArrayList();
			for(PairUni<Alter> alters : questionAlters.getSecond()) {
				if(passedCurrent && (maxAlterPairs == null || results.size() < maxAlterPairs)) {
					Boolean answered = context.qidA1idA2idToAlterPairAnswer.containsKey(
							new TripleUni<Long>(
									question.getId(),
									alters.getFirst().getId(),
									alters.getSecond().getId()));
					if(unansweredOnly && answered) {
						// Answered, but we only want unanswered. Skip it.
					} else {
						results.add(alters);
					}
				}
				if(currentQuestion != null && 
						currentQuestion.getId().equals(question.getId()) && 
						currentAlterPair.getFirst().getId().equals(alters.getFirst().getId()) &&
						currentAlterPair.getSecond().getId().equals(alters.getSecond().getId()) )
				{
					passedCurrent = true;
				}
			}
			if(! results.isEmpty()) {
				if(! forward) { // Want to return alter pairs in normal order.
					Collections.reverse(results);
				}
				return new Pair<Question,ArrayList<PairUni<Alter>>>(question,results);
			}
		}
		return null;
	}
	
	public static ArrayList<Pair<Question,ArrayList<PairUni<Alter>>>> alterPairQuestionsForInterview(
			Session session, Long interviewId, Boolean forward) 
	{
		ArrayList<Pair<Question,ArrayList<PairUni<Alter>>>> results = Lists.newArrayList();
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.ALTER_PAIR);
		List<Alter> alters = Alters.getForInterview(session, interviewId);
		if(! forward) {
			Collections.reverse(questions);
		}
		EvaluationContext context = Expressions.getContext(session, interview);
		for(Question question : questions) {
			ArrayList<PairUni<Alter>> resultPairs = Lists.newArrayList();
			for(Alter alter1 : alters) {
				for(Alter alter2 : alters) {
					if(alter1.getId() < alter2.getId()) { // To avoid repeats, convention that lower ID is first
						Long reasonId = question.getAnswerReasonExpressionId();
						Boolean shouldAnswer =
							reasonId == null ||
								Expressions.evaluate(
										context.eidToExpression.get(reasonId), 
										Lists.newArrayList(alter1,alter2), 
										context);
						if(shouldAnswer) {
							resultPairs.add(new PairUni<Alter>(alter1,alter2));
						}
					}
				}
			}
			if(! resultPairs.isEmpty()) {
				if(! forward) {
					Collections.reverse(resultPairs);
				}
				results.add(new Pair<Question,ArrayList<PairUni<Alter>>>(question,resultPairs));
			}
		}
		return results;
	}
}
