package net.sf.egonet.model;

import net.sf.egonet.model.Question.QuestionType;

public class Answer extends Entity
{
	public static enum AnswerType { TEXTUAL, NUMERICAL, SELECTION, MULTIPLE_SELECTION };
	public static enum SkipReason { NONE, REFUSE, DONT_KNOW };

	private Long questionId;
	private Long interviewId;
	private Long alterId1; // Null for EgoID and Ego questions.
	private Long alterId2; // Usually null - exception is alter pair questions.

	// Denormalizing for convenience and query performance - these fields can also be found in question
	private Long studyId;
	private QuestionType questionType;
	private AnswerType answerType;

	// Serialization of text, number, optionID, or (comma-separated) list of optionID
	// Or null to indicate the question was skipped.
	// Methods for getting the deserialized value will need to include the matching
	// question as an argument.
	protected String value;
	protected String valueOld;
	protected String otherSpecifyText; // for those questions of the Selection or Multiple Selection
	                            // that respond to 'other/specify' questions.
	private SkipReason skipReason;
	
	public Answer() {

	}

	public Answer(Question question, String answer) {
		if(! question.getType().equals(QuestionType.EGO_ID)) {
			throw new RuntimeException(
					"Constructor Answer(Question,String) can only be used for EGO_ID questions.");
		}
		this.setQuestionId(question.getId());
		this.setValue(answer);
		// TODO: Compare answer with answertype, possibly throwing exception.
		// TODO: Several constructors for different answer types: String, Integer, Option, List<Option>
		this.setStudyId(question.getStudyId());
		this.setQuestionType(question.getType());
		this.setAnswerType(question.getAnswerType());
	}

	public Answer(Interview interview, Question question, String answer)
	{
		this.setQuestionId(question.getId());
		this.setInterviewId(interview.getId());
		this.setValue(answer);
		// TODO: Compare answer with answertype, possibly throwing exception.
		// TODO: Several constructors for different answer types: String, Integer, Option, List<Option>
		this.setStudyId(question.getStudyId());
		this.setQuestionType(question.getType());
		this.setAnswerType(question.getAnswerType());
	}

	/**
	 * constructor with the 'other specify' text that can accompany
	 * Selection and MultipleSelection questions
	 * @param interview - identifies this interview
	 * @param question - the question the was answered
	 * @param answer - string representation of the answer 
	 * @param otherSpecText - text from 'Specify Other:' text box
	 */
	public Answer(Interview interview, Question question, String answer, String otherSpecText ) {
		this ( interview, question, answer);
		this.setOtherSpecifyText(otherSpecText);
	}
	
	// TODO: Extra constructors that include alter1 and alter2 parameters,
	// with check that question type allows them.

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}

	public Long getQuestionId() {
		return questionId;
	}

	public void setAlterId1(Long alterId1) {
		this.alterId1 = alterId1;
	}

	public Long getAlterId1() {
		return alterId1;
	}

	public void setAlterId2(Long alterId2) {
		this.alterId2 = alterId2;
	}

	public Long getAlterId2() {
		return alterId2;
	}

	public void setInterviewId(Long interviewId) {
		this.interviewId = interviewId;
	}

	public Long getInterviewId() {
		return interviewId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public Long getStudyId() {
		return studyId;
	}

	protected void setQuestionType(QuestionType questionType) {
		this.questionType = questionType;
	}

	public QuestionType getQuestionType() {
		return questionType;
	}

	protected void setAnswerType(AnswerType answerType) {
		this.answerType = answerType;
	}

	public AnswerType getAnswerType() {
		return answerType;
	}


	public String getAnswerTypeDB() { 
		AnswerType type = getAnswerType();
		return type == null ? null : type.name(); 
	}
	
	public String       getQuestionTypeDB()       { return Question.typeDB(getQuestionType());       }

	public void setAnswerTypeDB(String val) { 
		if(val != null && ! val.isEmpty()) {
			this.setAnswerType(AnswerType.valueOf(val)); 
		}
	}
	public void setQuestionTypeDB(String val) { 
		if(val != null && ! val.isEmpty()) {
			this.setQuestionType(QuestionType.valueOf(val)); 
		}
	}
	public void setSkipReasonDB(String val) { 
		if(val != null && ! val.isEmpty()) {
			this.setSkipReason(SkipReason.valueOf(val)); 
		}
	}

	public String getSkipReasonDB() { 
		return getSkipReason().name();
	}

	public void setSkipReason(SkipReason skipReason) {
		this.skipReason = skipReason;
	}

	public SkipReason getSkipReason() {
		return skipReason == null ? SkipReason.NONE : skipReason;
	}

	// ---------------------------------
	
	public void setValue(String value) {
		this.value = value;
		// TODO: Should need to pass the question as argument, so I can check if value has correct answer type
	}
	protected void setValueOld(String value) {
		this.valueOld = value;
	}
	public String getValue() {
		return migrateToText(this,"value");
		// TODO: Should have different methods depending on answer type, and provide deserialization
	}
	protected String getValueOld() {
		return null;
	}
	
	public void setOtherSpecifyText (String otherSpecifyText) {
		this.otherSpecifyText = (otherSpecifyText==null)?"":otherSpecifyText;
	}
	public String getOtherSpecifyText() {
		if ( otherSpecifyText==null )
			otherSpecifyText = "";
		return(otherSpecifyText);
	}
	
}
