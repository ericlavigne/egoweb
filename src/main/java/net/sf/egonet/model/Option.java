package net.sf.egonet.model;

/**
 * Value object representing an item of a drop-down, listbox, or similar widget
 */
public class Option extends Entity
{
	private Long questionId;
	private String name;
	
	private Long ordering;

	public Option(Long questionId, String name)
	{
		this.questionId = questionId;
		this.name = name;
	}

	public Long   getQuestionId() { return questionId; }
	public String getName()       { return name;       }

	public Long getOrdering() { return ordering; }
	public void setOrdering(Long ordering) { this.ordering = ordering; }
	
	// For Hibernate use only -----------------

	protected Option() {}

	protected void setQuestionId(Long val) { this.questionId = val; }
	protected void setName(String val)     { this.name       = val; }
}
