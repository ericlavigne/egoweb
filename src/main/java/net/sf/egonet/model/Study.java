package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
//import java.util.HashSet;
//import java.util.Set;
import java.util.Set;

public class Study extends Entity
{
	private String name;
	private Boolean active;
	private String alterPrompt;
	private List<Alter> alters;
	private List<Ego> egos;
	private List<Interview> interviews;
	private Set<Question> questions = new HashSet<Question>();

	public Study()
	{
		this("");
	}

	public Study(String name) {
		this.name = name;
		this.active = true;
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
	public void setQuestions(Set<Question>  val) { this.questions   = val; }

	public Set<Question> getQuestions()   { return questions;        }
	public String        getAlterPrompt() { return this.alterPrompt; }
	public String        getName()        { return name;             }

	public List<Question> getQuestionList() {
		return new ArrayList<Question>(getQuestions());
	}
	
	public List<Question> getQuestionList(Question.QuestionType type) {
		ArrayList<Question> results = new ArrayList<Question>();
		for(Question question : getQuestionList()) {
			if(question.getType().equals(type)) {
				results.add(question);
			}
		}
		return results;
	}

	public Study addQuestion(Question question) {
		question.setStudy(this);
		questions.add(question);
		return this;
	}
}
