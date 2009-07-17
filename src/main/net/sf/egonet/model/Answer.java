package net.sf.egonet.model;

public class Answer extends Entity
{
	protected enum AnswerType { TEXTUAL, NUMERICAL, SELECTION, MULTIPLE_SELECTION };

	private AnswerType answerType;
	private Question question;

	public Answer(Question question)
	{
		this.answerType = question.getAnswerType();
		this.question = question;
	}

	public AnswerType getType() { return answerType; }

	public boolean isTextual()        { return getType() == AnswerType.TEXTUAL;            }
	public boolean isNumerical()      { return getType() == AnswerType.NUMERICAL;          }
	public boolean isSelection()      { return getType() == AnswerType.SELECTION;          }
	public boolean isMultiSelection() { return getType() == AnswerType.MULTIPLE_SELECTION; }
}
