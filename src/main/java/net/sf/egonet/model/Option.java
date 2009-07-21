package net.sf.egonet.model;

/**
 * Value object representing an item of a drop-down, listbox, or similar widget
 */
public class Option extends Entity
{
	private Long questionId;
	private String name;

	public Option(Long questionId, String name)
	{
		this.questionId = questionId;
		this.name = name;
	}

	public Long getQuestionId() {
		return questionId;
	}
	
	public String getName()
	{
		return name;
	}

	// For Hibernate use only
	protected Option() {
		
	}
	// For Hibernate use only
	protected void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}
	// For Hibernate use only
	protected void setName(String name)
	{
		this.name = name;
	}
}
