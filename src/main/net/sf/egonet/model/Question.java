package net.sf.egonet.model;

public class Question extends Entity
{
	public static enum QuestionType  { EGO, ALTER, ALTER_PAIR };

	private String prompt;
	private Answer.AnswerType answerType;

	private QuestionType type;
	private boolean isRequired;

	public Question(String prompt, Answer.AnswerType answerType, QuestionType type, boolean isRequired)
	{
		this.prompt     = prompt;
		this.answerType = answerType;
		this.type       = type;
		this.isRequired = isRequired;
	}

	public String            getPrompt()     { return prompt;     }
	public Answer.AnswerType getAnswerType() { return answerType; }
	public QuestionType      getType()       { return type;       }
	public boolean           isRequired()    { return isRequired; }

	/** Whether the question concerns the interviewee
	  */
	public boolean isAboutEgo() { return getType() == QuestionType.EGO; }

	/** Whether the question concerns the interviewee's alter
	  */
	public boolean isAboutAlter() { return getType() == QuestionType.ALTER; }

	/** Whether the question describes the relationship between two of the interviewee's alters
	  */
	public boolean isAboutRelationship() { return getType() == QuestionType.ALTER_PAIR; }

	public boolean needsTextualResponse()        { return getAnswerType() == Answer.AnswerType.TEXTUAL;            }
	public boolean needsNumericalResponse()      { return getAnswerType() == Answer.AnswerType.NUMERICAL;          }
	public boolean needsSelectionResponse()      { return getAnswerType() == Answer.AnswerType.SELECTION;          }
	public boolean needsMultiSelectionResponse() { return getAnswerType() == Answer.AnswerType.MULTIPLE_SELECTION; }
}
