package net.sf.egonet.model;

public class Alter extends Entity
{
	private String name;
	private Long interviewId;

	public Alter(Interview interview, String name) {
		this.name = name;
		this.interviewId = interview.getId();
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return getName();
	}
	
	public Long getInterviewId() {
		return interviewId;
	}

	//////// Rest is Hibernate stuff

	protected Alter() {

	}

	protected void setName(String name) {
		this.name = name;
	}
	
	protected void setInterviewId(Long interviewId) {
		this.interviewId = interviewId;
	}
}
