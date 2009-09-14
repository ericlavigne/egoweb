package net.sf.egonet.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;

import org.hibernate.Session;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
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

	public static Question nextUnansweredEgoQuestionForInterview(final Long interviewId) {
		return DB.withTx(new Function<Session,Question>() {
			public Question apply(Session session) {
				return nextUnansweredEgoQuestionForInterview(session,interviewId);
			}
		});
	}
	
	public static Question nextUnansweredEgoQuestionForInterview(Session session, Long interviewId) {
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.EGO);
		List<Answer> answers = 
			Answers.getAnswersForInterview(session, interviewId, QuestionType.EGO);
		for(Question question : questions) {
			Boolean foundAnswer = false;
			for(Answer answer : answers) {
				if(answer.getQuestionId().equals(question.getId())) {
					foundAnswer = true;
				}
			}
			if(! foundAnswer) {
				return question;
			}
		}
		return null;
	}

	public static Question nextUnansweredAlterQuestionForInterview(final Long interviewId) {
		return DB.withTx(new Function<Session,Question>() {
			public Question apply(Session session) {
				return nextUnansweredAlterQuestionForInterview(session,interviewId);
			}
		});
	}
	
	public static Question nextUnansweredAlterQuestionForInterview(Session session, Long interviewId) {
		Interview interview = Interviews.getInterview(session, interviewId);
		List<Question> questions = 
			Questions.getQuestionsForStudy(session, interview.getStudyId(), QuestionType.ALTER);
		Map<Long,Question> idToQuestion = Maps.newHashMap();
		for(Question question : questions) {
			idToQuestion.put(question.getId(), question);
		}
		Multimap<Long,Answer> questionIdToAnswers = ArrayListMultimap.create();
		for(Answer answer : Answers.getAnswersForInterview(session, interviewId, QuestionType.ALTER)) {
			questionIdToAnswers.put(answer.getQuestionId(), answer);
		}
		List<Alter> alters = Alters.getForInterview(session, interviewId);
		for(Question question : questions) {
			Set<Long> answeredAlterIds = Sets.newHashSet();
			for(Answer answer : questionIdToAnswers.get(question.getId())) {
				answeredAlterIds.add(answer.getAlterId1());
			}
			for(Alter alter : alters) {
				if(! answeredAlterIds.contains(alter.getId())) {
					return question;
				}
			}
		}
		return null;
	}
}
