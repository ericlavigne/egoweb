package net.sf.egonet.model;


public class Interview extends Entity
{
	private Long studyId;
	private Boolean completed;
	
	public Interview(Study study) {
		this.studyId = study.getId();
		completed = new Boolean(false);
	}
	public Interview() { 
		completed = new Boolean(false);		
	}
	
	public Long getStudyId() { return this.studyId; }
	public void setStudyId(Long val) { this.studyId = val; }
	
	public void setCompleted(Boolean completed) {
		this.completed = (completed==null) ? new Boolean(false) : completed;
		// System.out.println ("setting interview " + getId() + " to " + this.completed);
	}
	public Boolean getCompleted() {
		return(completed);
	}
	
}
