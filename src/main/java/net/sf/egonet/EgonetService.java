package net.sf.egonet;

import java.util.List;
import java.util.Set;

import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Answer.AnswerType;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Option;
import net.sf.egonet.model.Study;

public class EgonetService
{
	public Study createStudy()
	{
		return null;
	}

	public void updateStudy(Study study)
	{
	}

	public void addEgoIdentifier(Study study, String name, AnswerType answerType)
	{
	}

	public void addEgoIdentifier(Study study, String name, AnswerType answerType, Set<Option> options)
	{
	}

	public void addEgoQuestion(Study study, String name, AnswerType answerType)
	{
	}

	public void addEgoQuestion(Study study, String name, AnswerType answerType, Set<Option> options)
	{
	}

	public void addAlterIdentifier(Study study, String name, AnswerType answerType)
	{
	}

	public void addAlterIdentifier(Study study, String name, AnswerType answerType, Set<Option> options)
	{
	}

	public void addAlterQuestion(Study study, String name, AnswerType answerType)
	{
	}

	public void addAlterQuestion(Study study, String name, AnswerType answerType, Set<Option> options)
	{
	}

	public Set<Option> getPresetOptionsByName(String name)
	{
		return null;
	}

	public List<Interview> findInterviewByStudy(Study study)
	{
		return null;
	}

	public List<Answer> findAnswersByInterview(Interview interview)
	{
		return null;
	}
}
