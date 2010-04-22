package net.sf.egonet.model;


public class Interview extends Entity
{
	private Long studyId;

	public Interview(Study study) {
		this.studyId = study.getId();
	}

	public Long getStudyId() { return this.studyId; }

	public Interview() { }
	public void setStudyId(Long val) { this.studyId = val; }
	
}
