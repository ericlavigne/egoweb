package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.List;

public class Study extends Entity
{
	private String name;
	private Boolean active;
	private String alterPrompt;
	private List<Alter> alters;
	private List<Ego> egos;
	private List<Interview> interviews;
	private List<Question> questions;

	public Study(String name) {
		this.name = name;
		this.active = true;
		this.questions = new ArrayList<Question>();
	}

	/*
	public Study()
	{
		this("");
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof Study))
		{
			return false;
		}
		return getName().equals(((Study) obj).getName());
	}

	public int hashCode()
	{
		return getName().hashCode();
	}
	*/

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Boolean isActive()
	{
		return active;
	}

	public void setActive(Boolean active)
	{
		if (active != null)
		{
			this.active = active;
		}
	}

	public void setAlterPrompt(String alterPrompt)
	{
		this.alterPrompt = alterPrompt;
	}

	public String getAlterPrompt()
	{
		return this.alterPrompt;
	}

	/*
	public void addEgo(String name, Answer.AnswerType answerType)
	{
		egoFields.add(new Ego(name, answerType));
	}

	public void addAlter(String name, Answer.AnswerType answerType)
	{
		alterFields.add(new Alter(name, answerType));
	}

	public void addQuestion(String prompt, Answer.AnswerType answerType, Question.QuestionType questionType, boolean isRequired)
	{
		questions.add(new Question(prompt, answerType, questionType, isRequired));
	}
	*/
}
