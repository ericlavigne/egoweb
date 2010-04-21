package net.sf.egonet.model;

import net.sf.egonet.model.Question;

public class Interview extends Entity
{
	private Long studyId;
	private static Interview theInterview;
	
	public Interview(Study study) {
		this.studyId = study.getId();
		theInterview = this;
	}

	public Long getStudyId() { return this.studyId; }

	public Interview() { }
	public void setStudyId(Long val) { this.studyId = val; }
	
	/**
	 * this is for the benefit of the question class,
	 * which will need to have the interview for database
	 * queries for certain text replacement operations.
	 * There is only one interview at a time so we can
	 * treat it as a singleton
	 * @return a reference to the only interview. 
	 */
	public static Interview getTheInterview() {
		return(theInterview);
	}
}
