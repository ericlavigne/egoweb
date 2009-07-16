package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.List;
//import java.util.HashSet;
//import java.util.Set;

public class Study extends Entity
{
	private String name;
	private Boolean active;
	private String alterPrompt;
	private List<Alter> alters;
	private List<Ego> egos;
	private List<Interview> interviews;
	private List<Question> questions;

	public Study()
	{
		this("");
	}

	public Study(String name) {
		this.name = name;
		this.active = true;
		this.questions = new ArrayList<Question>();
		/*
		this.setQuestions(new HashSet<Question>());
		this.sections = new ArrayList<Section>();
		sections.add(new Section("Ego Questions"        ,Section.Subject.EGO));
		sections.add(new Section("Alter Prompt"         ,Section.Subject.ALTER_PROMPT));
		sections.add(new Section("Alter Questions"      ,Section.Subject.ALTER));
		sections.add(new Section("Alter Pair Questions" ,Section.Subject.ALTER_PAIR));
		*/
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

	public void setAlterPrompt(String       val) { this.alterPrompt = val; }
	public void setName(String              val) { this.name        = val; }
	public void setQuestions(List<Question> val) { this.questions   = val; }

	public List<Question> getQuestions()   { return questions;        }
	public String         getAlterPrompt() { return this.alterPrompt; }
	public String         getName()        { return name;             }

	/*
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

	public void addEgo(String name, Answer.AnswerType answerType)
	{
		egoFields.add(new Ego(name, answerType));
	}

	public List<Question> getQuestionList() {
		return new ArrayList<Question>(getQuestions());
	}

	public void addAlter(String name, Answer.AnswerType answerType)
	{
		alterFields.add(new Alter(name, answerType));
	}
	*/
}
