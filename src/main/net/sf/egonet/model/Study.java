package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.List;

public class Study extends Entity
{
	private String name;
	private Boolean active;
	private List<Question> questions;
	
	private ArrayList<Section> sections; // TODO: get rid of sections and delete Section.class
	
	public Study()
	{
		this("");
	}

	public Study(String name) {
		this.name = name;
		this.active = true;
		this.setQuestions(new ArrayList<Question>());
		this.sections = new ArrayList<Section>();
		sections.add(new Section("Ego Questions"        ,Section.Subject.EGO));
		sections.add(new Section("Alter Prompt"         ,Section.Subject.ALTER_PROMPT));
		sections.add(new Section("Alter Questions"      ,Section.Subject.ALTER));
		sections.add(new Section("Alter Pair Questions" ,Section.Subject.ALTER_PAIR));
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

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public List<Question> getQuestions() {
		return questions;
	}
}
