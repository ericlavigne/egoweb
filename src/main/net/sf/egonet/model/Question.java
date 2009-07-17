package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.List;
import net.sf.egonet.model.Answer.AnswerType;

public class Question extends Entity
{
	public static enum QuestionType { EGO_ID, EGO, ALTER_ID, ALTER, ALTER_PAIR };

	private String title;    // ??
	private String citation; // ??
	private String prompt;
	private AnswerType answerType;
	private QuestionType type;
	private boolean isRequired;
	private List<Option> options;

	public Question(String prompt, AnswerType answerType, QuestionType type, boolean isRequired)
	{
		this.prompt     = prompt;
		this.answerType = answerType;
		this.type       = type;
		this.isRequired = isRequired;
	}

	public List<Option> getOptions()      { return this.options;      }
	public AnswerType   getAnswerType()   { return this.answerType;   }
	public String       getCitation()     { return this.citation;     }
	public String       getPrompt()       { return this.prompt;       }
	public String       getTitle()        { return this.title;        }
	public QuestionType getType()         { return this.type;         }
	public boolean      isRequired()      { return this.isRequired;   }

	/**
	 * @return
	 *  if needsSelectionResponse returns a list of one Option,
	 *  if needsMultiSelectionResponse returns a list of Option
	 *  otherwise null
	 */
	public void setOptions(List<Option>  val) { this.options    = val; }
	public void setAnswerType(AnswerType val) { this.answerType = val; }
	public void setCitation(String       val) { this.citation   = val; }
	public void setPrompt(String         val) { this.prompt     = val; }
	public void setTitle(String          val) { this.title      = val; }
	public void setType(QuestionType     val) { this.type       = val; }
	public void setRequired(boolean      val) { this.isRequired = val; }

	/** Whether the question identifies the interviewee
	  */
	public boolean identifiesEgo() { return getType() == QuestionType.EGO_ID; }

	/** Whether the question identifies the interviewee's alter
	  */
	public boolean identifiesAlter() { return getType() == QuestionType.ALTER_ID; }

	/** Whether the question concerns the interviewee
	  */
	public boolean isAboutEgo() { return getType() == QuestionType.EGO; }

	/** Whether the question concerns the interviewee's alter
	  */
	public boolean isAboutAlter() { return getType() == QuestionType.ALTER; }

	/** Whether the question describes the relationship between two of the interviewee's alters
	  */
	public boolean isAboutRelationship() { return getType() == QuestionType.ALTER_PAIR; }

	public boolean needsTextualResponse()        { return getAnswerType() == AnswerType.TEXTUAL;            }
	public boolean needsNumericalResponse()      { return getAnswerType() == AnswerType.NUMERICAL;          }
	public boolean needsSelectionResponse()      { return getAnswerType() == AnswerType.SELECTION;          }
	public boolean needsMultiSelectionResponse() { return getAnswerType() == AnswerType.MULTIPLE_SELECTION; }
}
