package net.sf.egonet.model;

public class Alter extends Entity
{
	private String name;

	public Alter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	//////// Rest is Hibernate stuff

	protected Alter() {

	}

	protected void setName(String name) {
		this.name = name;
	}
}
