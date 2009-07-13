package org.egonet.web.model;

import java.util.ArrayList;

public class Study implements java.io.Serializable {
	private Long id;
	private String name;
	private Boolean active;
	private ArrayList<Section> sections;
	
	public Study() {
		this("");
	}
	
	public Study(String name) {
		this.name = name;
		this.active = true;
		this.sections = new ArrayList<Section>();
		sections.add(new Section("Ego Questions",Section.Subject.EGO));
		sections.add(new Section("Alter Prompt",Section.Subject.ALTER_PROMPT));
		sections.add(new Section("Alter Questions",Section.Subject.ALTER));
		sections.add(new Section("Alter Pair Questions",Section.Subject.ALTER_PAIR));
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Boolean isActive() {
		return active;
	}
	
	public void setActive(Boolean active) {
		this.active = active;
	}
	
	public ArrayList<Section> getSections() {
		return sections;
	}
	
	public void addSection(String name, Section.Subject subject) {
		sections.add(new Section(name,subject));
	}
}


