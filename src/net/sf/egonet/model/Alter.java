package net.sf.egonet.model;

public class Alter extends OrderedEntity
{
	private String name;
	private Long interviewId;

	public Alter(Interview interview, String name, Integer ordering) {
		this.name = name;
		this.interviewId = interview.getId();
		setOrdering(ordering);
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return getName();
	}
	
	public boolean equals(Object obj) {
		if(! (obj instanceof Alter)) {
			return false;
		}
		Alter alter = (Alter) obj;
		return (getName()+"").equals(alter.getName()+"") && super.equals(alter);
	}
	
	public Long getInterviewId() {
		return interviewId;
	}

	//////// Rest is Hibernate stuff

	public Alter() {

	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setInterviewId(Long interviewId) {
		this.interviewId = interviewId;
	}
}
