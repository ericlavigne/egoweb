package net.sf.egonet.model;

/**
 * Value object representing an item of a drop-down, listbox, or similar widget
 */
public class Option implements java.io.Serializable
{
	private String name;

	public Option(String name)
	{
		this.name = name;
	}

//	public void setName(String name)
//	{
//		this.name = name;
//	}

	public String getName()
	{
		return name;
	}
}
