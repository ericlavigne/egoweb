package net.sf.egonet.model;

public class Answer extends Entity
{
	public enum AnswerType { TEXTUAL, NUMERICAL, SELECTION, MULTIPLE_SELECTION };

	private Question question;

	public Answer(Question question)
	{
		this.question = question;
	}

	public AnswerType getType() { return this.question.getAnswerType(); }

	public boolean isTextual()        { return getType() == AnswerType.TEXTUAL;            }
	public boolean isNumerical()      { return getType() == AnswerType.NUMERICAL;          }
	public boolean isSelection()      { return getType() == AnswerType.SELECTION;          }
	public boolean isMultiSelection() { return getType() == AnswerType.MULTIPLE_SELECTION; }
}
