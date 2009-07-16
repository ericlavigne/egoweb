package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Study extends Entity
{
	private String name;
	private Boolean active;
	private Set<Question> questions;
	
	private ArrayList<Section> sections; // TODO: get rid of sections and delete Section.class
	
	public Study()
	{
		this("");
	}

	public Study(String name) {
		this.name = name;
		this.active = true;
		this.setQuestions(new HashSet<Question>());
		this.sections = new ArrayList<Section>();
		sections.add(new Section("Ego Questions"        ,Section.Subject.EGO));
		sections.add(new Section("Alter Prompt"         ,Section.Subject.ALTER_PROMPT));
		sections.add(new Section("Alter Questions"      ,Section.Subject.ALTER));
		sections.add(new Section("Alter Pair Questions" ,Section.Subject.ALTER_PAIR));
	}
	
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

	public ArrayList<Section> getSections()
	{
		return sections;
	}

	public void addSection(String name, Section.Subject subject)
	{
		sections.add(new Section(name, subject));
	}

	public void setQuestions(Set<Question> questions) {
		this.questions = questions;
	}

	public Set<Question> getQuestions() {
		return questions;
	}

	public Study addQuestion(Question question) {
		question.setStudy(this);
		questions.add(question);
		return this;
	}
}
