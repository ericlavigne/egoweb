package net.sf.egonet.model;

public class Field extends Entity
{
	private Answer.AnswerType answerType;
	private String            name;

	public Field(String name, Answer.AnswerType answerType)
	{
		this.name       = name;
		this.answerType = answerType;
	}

	public Answer.AnswerType getAnswerType() { return answerType; }
	public String            getName()       { return name;       }
}
