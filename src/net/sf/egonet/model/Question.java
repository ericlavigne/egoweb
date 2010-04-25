package net.sf.egonet.model;

import java.util.ArrayList;

import net.sf.egonet.model.Answer.AnswerType;
import net.sf.egonet.persistence.TextInsertionUtil;
import net.sf.egonet.web.panel.NumericLimitsPanel.NumericLimitType;

public class Question extends OrderedEntity
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
	private Boolean askingStyleList;
	private Long answerReasonExpressionId; // Answer the question if this expression is null or true
	// variables that will be specific to numeric answers, 
	// and (optionally) limiting them to a range
	private NumericLimitType minLimitType;
	private Integer minLiteral;
	private String minPrevQues;
	private NumericLimitType maxLimitType;
	private Integer maxLiteral;
	private String maxPrevQues;	
	// similar variables to be used with MULTIPLE_SELECTION questions
	private Integer minCheckableBoxes;
	private Integer maxCheckableBoxes;
	
	public Question() {
		prompt = "";
		answerType = AnswerType.TEXTUAL;
		type = QuestionType.EGO;
		askingStyleList = false;
		
		minLimitType = NumericLimitType.NLT_NONE;
		minLiteral = 0;
		minPrevQues = "";
		maxLimitType = NumericLimitType.NLT_NONE;
		maxLiteral = 10000;
		maxPrevQues = "";
		
		minCheckableBoxes = 0;
		maxCheckableBoxes = 100;
	}

	public String toString() {
		return (title == null ? "Untitled question" : title)+" ("+type+")";
	}
	@Override
	public int compareTo(OrderedEntity entity) {
		if(entity instanceof Question) {
			Question question = (Question) entity;
			if(! getType().equals(question.getType())) {
				if(getType().ordinal() < question.getType().ordinal()) {
					return -1;
				}
				if(getType().ordinal() > question.getType().ordinal()) {
					return 1;
				}
			}
		}
		return super.compareTo(entity);
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
	public boolean hasPreface() {
		String preface = getPreface();
		return ! (preface == null || preface.isEmpty() || preface.matches("\\s*"));
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

	public void setAskingStyleList(Boolean askingStyleList) {
		if(askingStyleList != null) {
			this.askingStyleList = askingStyleList;
		}
	}

	public Boolean getAskingStyleList() {
		if(getType().equals(QuestionType.EGO)) {
			return false;
		}
		return askingStyleList == null ? false : askingStyleList;
	}
	
	/* ****************************************************************** */
	/* next set of functions deal with setting limits on NUMERICAL        */
	/* questions                                                          */
	/* ****************************************************************** */
	
	/**
	 * getters & setters for the numeric bounds
	 */
	
	public void setMinLimitType(NumericLimitType minLimitType) {
		this.minLimitType = (minLimitType==null)?NumericLimitType.NLT_NONE:minLimitType;
	}
	public NumericLimitType getMinLimitType() { return(minLimitType);}
	
	public void setMinLiteral ( Integer minLiteral ) {
		this.minLiteral = (minLiteral==null)?0:minLiteral;
	}
	public Integer getMinLiteral () { return(minLiteral);}
	
	public void setMinPrevQues ( String minPrevQues ) {
		this.minPrevQues = (minPrevQues==null)?"":minPrevQues;
	}
	public String getMinPrevQues() {return(minPrevQues);};
	
	public void setMaxLimitType(NumericLimitType maxLimitType) {
		this.maxLimitType = (maxLimitType==null)?NumericLimitType.NLT_NONE:maxLimitType;
	}
	public NumericLimitType getMaxLimitType() { return(maxLimitType);}
	
	public void setMaxLiteral ( Integer maxLiteral ) {
		this.maxLiteral = (maxLiteral==null)?10000:maxLiteral;
	}
	public Integer getMaxLiteral () { return(maxLiteral);}
	
	public void setMaxPrevQues ( String maxPrevQues ) {
		this.maxPrevQues = (maxPrevQues==null)?"":maxPrevQues;
	}
	public String getMaxPrevQues() {return(maxPrevQues);};
	
	/**
	 * getters & setters for min/max checkable checkboxes
	 * for MULTIPLE_SELECTION questions
	 */
	
	public void setMinCheckableBoxes (Integer minCheckableBoxes) {
		this.minCheckableBoxes = (minCheckableBoxes==null)?new Integer(0):minCheckableBoxes;
	}
	public Integer getMinCheckableBoxes() { 
		return( minCheckableBoxes );
	}
	public void setMaxCheckableBoxes (Integer maxCheckableBoxes) {
		this.maxCheckableBoxes = (maxCheckableBoxes==null)?new Integer(100):maxCheckableBoxes;
	}
	public Integer getMaxCheckableBoxes() { 
		return( maxCheckableBoxes );
	}
	
	/* ****************************************************************** */
	/* functions from this point down deal with substituting the answer   */
	/* from a previous question into a new questions prompt               */
	/* ****************************************************************** */
	
	/**
	 * non-static form of answerToQuestion below for the more common need
	 * to retrieve the answer to THIS question
	 */
	
	public String answerToQuestion ( Long interviewId, ArrayList<Alter> listOfAlters ) {
		return ( TextInsertionUtil.answerToQuestion(title, interviewId, type, studyId, listOfAlters));
	}
	
	/**
	 * numeric questions sometimes have their response bounded by answers to previous
	 * questions, the names held in minPrevQues and maxPrevQues.  This function will
	 * retrieve those answers
	 * @param alterId1 id of alter one ( may be null )
	 * @param alterId2 id of alter two ( may be null )
	 * @param getMaximum if true get maximum limit, if false get minimum limit
	 * @return answer to previous question
	 */
	
	public String answerToRangeLimitingQuestion ( Long interviewId, ArrayList<Alter> listOfAlters, boolean getMaximum ) {
		String strPreviousAnswer;
		
		if (getMaximum)
			strPreviousAnswer = TextInsertionUtil.answerToQuestion ( maxPrevQues, interviewId, type, studyId, listOfAlters);
		else
			strPreviousAnswer = TextInsertionUtil.answerToQuestion ( minPrevQues, interviewId, type, studyId, listOfAlters);
		// shouldn't happen, but check anyway
		// *might* want to assign low/high default values if not found, 
		// for now just assign 0
		if ( strPreviousAnswer==null )
			strPreviousAnswer = "0";
		return(strPreviousAnswer);	
	}
	
	/**
	 * a convenience function that turns around and called the TextInsertionUtil static function
	 * variableInsertion.  Most commonly we will want to do it on the 
	 * prompt of this question but in this case the prompt is an individualized 
	 * prompt that has had appropriate strings already substituted into place.
	 * This deals with variable insertion using tags of the format <VAR Q1 />
	 * @param strPrompt text of question
	 * @param alterId1 id of one person addressed.  may be null
	 * @param alterId2 id of second person addressed.  may be null
	 * @return a string with previous answers inserted into place
	 */
	public String variableInsertion ( String strPrompt, Long interviewId, ArrayList<Alter> listOfAlters ) {
		return ( TextInsertionUtil.variableInsertion(strPrompt, interviewId, type, studyId, listOfAlters ));
	}
	
	/**
	 * a convenience function that turns around and called the TextInsertionUtil static function
	 * calculationInsertion.  Most commonly we will want to do it on the 
	 * prompt of this question but in this case the prompt is an individualized 
	 * prompt that has had appropriate strings already substituted into place.
	 * This deals with variable insertion using tags of the format <CALC Q1+Q2 />
	 * @param strPrompt text of question
	 * @param interviewId uniquely identifies this interview
	 * @param listOfAlters - used in retrieving question answers
	 * @return a string with previous answers inserted into place
	 */
	public String calculationInsertion ( String strPrompt, Long interviewId, ArrayList<Alter> listOfAlters ) {
		return ( TextInsertionUtil.calculationInsertion(strPrompt, interviewId, type, studyId, listOfAlters ));
	}
	
	/**
	 * a convenience function that turns around and called the TextInsertionUtil static function
	 * calculationInsertion.  Most commonly we will want to do it on the 
	 * prompt of this question but in this case the prompt is an individualized 
	 * prompt that has had appropriate strings already substituted into place
	 * This deals with variable insertion using tags of the format <COUNT Q1 "answer"/>
	 * @param strPrompt text of question
	 * @param interviewId uniquely identifies this interview
	 * @param listOfAlters - a list of ALL alters in the study
	 * @return a string with previous answers inserted into place
	 */
	public String answerCountInsertion ( String strPrompt, Long interviewId ) {
		return ( TextInsertionUtil.answerCountInsertion(strPrompt, interviewId, studyId ));
	}
	
	/**
	 * a convenience function that turns around and called the TextInsertionUtil static function
	 * conditionalTextInsertion.  Most commonly we will want to do it on the 
	 * prompt of this question but in this case the prompt is an individualized 
	 * prompt that has had appropriate strings already substituted into place
	 * This deals with variable insertion using tags of the format <IF Q1>3  "more than three"/>
	 * @param strPrompt text of question
	 * @param alterId1 id of one person addressed.  may be null
	 * @param alterId2 id of second person addressed.  may be null
	 * @return a string with previous answers inserted into place
	 */
	public String conditionalTextInsertion ( String strPrompt, Long interviewId, ArrayList<Alter> listOfAlters ) {
		return ( TextInsertionUtil.conditionalTextInsertion(strPrompt, interviewId, type, studyId, listOfAlters ));
	}
}
