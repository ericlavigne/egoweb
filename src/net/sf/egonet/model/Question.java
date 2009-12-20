package net.sf.egonet.model;

import java.util.ArrayList;

import net.sf.egonet.model.Answer.AnswerType;

public class Question extends Entity
{
	public static enum QuestionType { EGO_ID, EGO, ALTER, ALTER_PAIR };

	private Long studyId;
	private String title;    // Gender
	protected String citation; // This question originally written by Dr. X from the Institute of Advanced Research.
	protected String citationOld;
	protected String prompt;   // Are you male or female?
	protected String promptOld;
	protected String preface;   // The following questions relate to...
	protected String prefaceOld;
	private AnswerType answerType;
	private QuestionType type;
	private boolean isRequired;
	private Long answerReasonExpressionId; // Answer the question if this expression is null or true
	private Integer ordering; // Just controls order in which questions are asked

	public Question(String prompt, AnswerType answerType, QuestionType type, boolean isRequired)
	{
		this.prompt     = prompt;
		this.answerType = answerType;
		this.type       = type;
		this.isRequired = isRequired;
	}

	public Question() {
		answerType = AnswerType.TEXTUAL;
		type = QuestionType.EGO;
	}

	public String toString() {
		return (title == null ? "Untitled question" : title)+" ("+type+")";
	}

	public String individualizePrompt(ArrayList<Alter> alters) {
		String questionText = getPrompt();
		String dd = "\\$\\$";
		String dd1 = dd+"1";
		String dd2 = dd+"2";
		String blank = "_____";
		String alter1Name = alters.size() > 0 ? alters.get(0).getName() : blank;
		String alter2Name = alters.size() > 1 ? alters.get(1).getName() : blank;
		if(getType().equals(QuestionType.ALTER)) {
			questionText = questionText.replaceAll(dd1, alter1Name);
			questionText = questionText.replaceAll(dd, alter1Name);
		}
		if(getType().equals(QuestionType.ALTER_PAIR)) {
			questionText = questionText.replaceAll(dd1, alter1Name);
			questionText = questionText.replaceAll(dd2, alter2Name);
			questionText = questionText.replaceFirst(dd, alter1Name);
			questionText = questionText.replaceFirst(dd, alter2Name);
		}
		return questionText;
	}
	
	public AnswerType   getAnswerType()   { return this.answerType;   }
	
	public String       getTitle()        { return this.title;        }
	public QuestionType getType()         { return this.type;         }
	public boolean      isRequired()      { return this.isRequired;   }
	public Long         getStudyId()      { return this.studyId;        }
	public String       getAnswerTypeDB() { return getAnswerType().name(); }
	public String       getTypeDB()       { return typeDB(getType());       }

	public static String typeDB(QuestionType type) { return type == null ? null : type.name(); }
	/**
	 * @return
	 *  if needsSelectionResponse returns a list of one Option,
	 *  if needsMultiSelectionResponse returns a list of Option
	 *  otherwise null
	 */
	public void setAnswerType(AnswerType val) { this.answerType = val; }
	public void setTitle(String          val) { this.title      = val; }
	public void setType(QuestionType     val) { this.type       = val; }
	public void setRequired(boolean      val) { this.isRequired = val; }
	public void setStudyId(Long          val) { this.studyId      = val; }

	public void setAnswerTypeDB(String val) { this.setAnswerType(AnswerType.valueOf(val)); }
	public void setTypeDB(String val) { this.setType(QuestionType.valueOf(val)); }

	/** Whether the question identifies the interviewee
	  */
	public boolean identifiesEgo() { return getType() == QuestionType.EGO_ID; }

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

	public void setAnswerReasonExpressionId(Long answerReasonExpressionId) {
		this.answerReasonExpressionId = answerReasonExpressionId;
	}

	public Long getAnswerReasonExpressionId() {
		return answerReasonExpressionId;
	}

	public void setOrdering(Integer ordering) {
		this.ordering = ordering;
	}

	public Integer getOrdering() {
		return ordering;
	}
	
	// ---------------------------------

	protected String getPromptOld() {
		return null;
	}
	public String getPrompt() {
		return migrateToText(this,"prompt");
	}
	public void setPrompt(String val) {
		this.prompt = val; 
	}
	protected void setPromptOld(String val) {
		this.promptOld  = val;
	}
	

	public void setPreface(String preface) {
		this.preface = preface;
	}
	public String getPreface() {
		return migrateToText(this,"preface");
	}
	protected void setPrefaceOld(String preface) {
		this.prefaceOld = preface;
	}
	protected String getPrefaceOld() {
		return null;
	}
	
	public String getCitation() { 
		return migrateToText(this,"citation");
	}
	public void setCitation(String val) {
		this.citation   = val;
	}
	protected String getCitationOld() { 
		return null;
	}
	protected void setCitationOld(String val) {
		this.citationOld = val;
	}
}
