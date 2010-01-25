package net.sf.egonet.model;

public class Study extends Entity
{
	private String name;

	protected String introduction;
	protected String introductionOld;
	protected String egoIdPrompt;
	protected String egoIdPromptOld;
	protected String alterPrompt;
	protected String alterPromptOld;
	protected String conclusion;
	protected String conclusionOld;
	
	private Integer minAlters;
	private Integer maxAlters;
	
	private Long adjacencyExpressionId;

	private String valueRefusal;
	private String valueDontKnow;
	private String valueLogicalSkip;
	private String valueNotYetAnswered;

	public Study()
	{
		this("");
	}

	public Study(String name) {
		this.name = name;
		setEgoIdPrompt("Please identify yourself to start or continue an interview.");
		setAlterPrompt("Please list some people that you know.");
		setConclusion("Thank you for completing this interview.");
		this.minAlters = 0;
	}

	public String toString() {
		return getName();
	}
	
	public String mediumString() {
		return "["+getId()+" - "+getName()+" - alters("+getMinAlters()+","+getMaxAlters()+") ]";
	}

	// Below here is just getters and setters.
	
	public void setName(String val) { 
		this.name        = val; 
	}

	public String getName() { 
		return name;
	}

	public void setMinAlters(Integer minAlters) {
		this.minAlters = minAlters;
	}

	public Integer getMinAlters() {
		return minAlters;
	}

	public void setMaxAlters(Integer maxAlters) {
		this.maxAlters = maxAlters;
	}

	public Integer getMaxAlters() {
		return maxAlters;
	}

	public void setAdjacencyExpressionId(Long adjacencyExpressionId) {
		this.adjacencyExpressionId = adjacencyExpressionId;
	}

	public Long getAdjacencyExpressionId() {
		return adjacencyExpressionId;
	}

	// --------------------------------------
	
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	public void setIntroductionOld(String introduction) {
		this.introductionOld = introduction;
	}
	public String getIntroduction() {
		return migrateToText(this,"introduction");
	}
	public String getIntroductionOld() {
		return null;
	}

	public void setEgoIdPrompt(String egoIdPrompt) {
		this.egoIdPrompt = egoIdPrompt;
	}
	public void setEgoIdPromptOld(String egoIdPrompt) {
		this.egoIdPromptOld = egoIdPrompt;
	}
	public String getEgoIdPrompt() {
		return migrateToText(this,"egoIdPrompt");
	}
	public String getEgoIdPromptOld() {
		return null;
	}

	public void setAlterPrompt(String alterPrompt) {
		this.alterPrompt = alterPrompt;
	}
	public void setAlterPromptOld(String alterPrompt) {
		this.alterPromptOld = alterPrompt;
	}
	public String getAlterPrompt() {
		return migrateToText(this,"alterPrompt");
	}
	public String getAlterPromptOld() {
		return null;
	}

	public void setConclusion(String conclusion) {
		this.conclusion = conclusion;
	}
	public void setConclusionOld(String conclusion) {
		this.conclusionOld = conclusion;
	}
	public String getConclusion() {
		return migrateToText(this,"conclusion");
	}
	public String getConclusionOld() {
		return null;
	}

	public void setValueRefusal(String valueRefusal) {
		if(valueRefusal != null && ! valueRefusal.isEmpty()) {
			this.valueRefusal = valueRefusal;
		}
	}

	public String getValueRefusal() {
		return valueRefusal == null || valueRefusal.isEmpty() ?
				"-1" : valueRefusal;
	}

	public void setValueDontKnow(String valueDontKnow) {
		if(valueDontKnow != null && ! valueDontKnow.isEmpty()) {
			this.valueDontKnow = valueDontKnow;
		}
	}

	public String getValueDontKnow() {
		return valueDontKnow == null || valueDontKnow.isEmpty() ?
				"-2" : valueDontKnow;
	}

	public void setValueLogicalSkip(String valueLogicalSkip) {
		if(valueLogicalSkip != null && ! valueLogicalSkip.isEmpty()) {
			this.valueLogicalSkip = valueLogicalSkip;
		}
	}

	public String getValueLogicalSkip() {
		return valueLogicalSkip == null || valueLogicalSkip.isEmpty() ?
				"-3" : valueLogicalSkip;
	}

	public void setValueNotYetAnswered(String valueNotYetAnswered) {
		if(valueNotYetAnswered != null && ! valueNotYetAnswered.isEmpty()) {
			this.valueNotYetAnswered = valueNotYetAnswered;
		}
	}

	public String getValueNotYetAnswered() {
		return valueNotYetAnswered == null || valueNotYetAnswered.isEmpty() ?
				"-4" : valueNotYetAnswered;
	}
}
