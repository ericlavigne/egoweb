package net.sf.egonet.model;

public class Study extends Entity
{
	private String name;
	private Boolean active;
	private String alterPrompt;

	public Study()
	{
		this("");
	}

	public Study(String name) {
		this.name = name;
		this.active = true;
	}

	public String toString() {
		return "<Study | id = "+getId()
			+", name = "+getName()
			+", active="+isActive()
			+", alterPrompt="+getAlterPrompt()+">";
	}

	public Boolean isActive()
	{
		return active;
	}

	public void setActive(Boolean active)
	{
		if (active != null)
		{
			this.active = active;
		}
	}

	public void setAlterPrompt(String       val) { this.alterPrompt = val; }
	public void setName(String              val) { this.name        = val; }

	public String        getAlterPrompt() { return this.alterPrompt; }
	public String        getName()        { return name;             }

}
