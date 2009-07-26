package net.sf.egonet.model;

public class Answer extends Entity
{
	public static enum AnswerType { TEXTUAL, NUMERICAL, SELECTION, MULTIPLE_SELECTION };

	private Long questionId;
	private Long interviewId;
	private Long alterId1; // Null for EgoID and Ego questions.
	private Long alterId2; // Usually null - exception is alter pair questions.
	
	// Serialization of text, number, optionID, or (comma-separated) list of optionID
	// Or null to indicate the question was skipped.
	// Methods for getting the deserialized value will need to include the matching
	// question as an argument.
	private String value; 

	protected Answer() {
		
	}
	
	public Answer(Interview interview, Question question, String answer)
	{
		this.setQuestionId(question.getId());
		this.setInterviewId(interview.getId());
		this.setValue(answer); 
		// TODO: Compare answer with answertype, possibly throwing exception.
		// TODO: Several constructors for different answer types: String, Integer, Option, List<Option>
	}
	
	// TODO: Extra constructors that include alter1 and alter2 parameters,
	// with check that question type allows them.

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}

	public Long getQuestionId() {
		return questionId;
	}

	public void setValue(String value) {
		this.value = value;
		// TODO: Should need to pass the question as argument, so I can check if value has correct answer type
	}

	public String getValue() {
		return value;
		// TODO: Should have different methods depending on answer type, and provide deserialization
	}

	protected void setAlterId1(Long alterId1) {
		this.alterId1 = alterId1;
	}

	public Long getAlterId1() {
		return alterId1;
	}

	protected void setAlterId2(Long alterId2) {
		this.alterId2 = alterId2;
	}

	public Long getAlterId2() {
		return alterId2;
	}

	protected void setInterviewId(Long interviewId) {
		this.interviewId = interviewId;
	}

	public Long getInterviewId() {
		return interviewId;
	}
}
