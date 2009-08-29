package net.sf.egonet.model;

public class Study extends Entity
{
	private String name;
	
	private String introduction;
	private String egoIdPrompt;
	private String alterPrompt;
	private String conclusion;
	
	private Integer minAlters;
	private Integer maxAlters;
	
	private Long adjacencyExpressionId;

	protected Study()
	{
		this("");
	}

	public Study(String name) {
		this.name = name;
	}

	public String toString() {
		return "<Study | id = "+getId()
			+", name = "+getName()+">";
	}

	// Below here is just getters and setters.
	
	public void setName(String val) { 
		this.name        = val; 
	}

	public String getName() { 
		return name;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setEgoIdPrompt(String egoIdPrompt) {
		this.egoIdPrompt = egoIdPrompt;
	}

	public String getEgoIdPrompt() {
		return egoIdPrompt;
	}

	public void setAlterPrompt(String alterPrompt) {
		this.alterPrompt = alterPrompt;
	}

	public String getAlterPrompt() {
		return alterPrompt;
	}

	public void setConclusion(String conclusion) {
		this.conclusion = conclusion;
	}

	public String getConclusion() {
		return conclusion;
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

}
