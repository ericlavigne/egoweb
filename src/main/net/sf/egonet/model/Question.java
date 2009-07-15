package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.List;

public class Question extends Entity 
{
	public static enum SubjectType {EGO_ID,EGO,ALTER_ID,ALTER,ALTER_PAIR}
	public static enum ResponseType {TEXT,NUMBER,CHOICE,MULTI_CHOICE}
	
	private String title; 
	private String prompt; 
	private String citation;
	private SubjectType subjectType;
	private ResponseType responseType;
	private List<Option> options; // empty if responseType is TEXT or NUMBER 
	
	public Question() {
		this.setTitle("First Name");
		this.setPrompt("What is your first name?");
		this.setCitation("");
		this.setSubjectType(SubjectType.EGO);
		this.setResponseType(ResponseType.TEXT);
		this.setOptions(new ArrayList<Option>());
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setCitation(String citation) {
		this.citation = citation;
	}

	public String getCitation() {
		return citation;
	}

	public void setSubjectType(SubjectType subjectType) {
		this.subjectType = subjectType;
	}

	public SubjectType getSubjectType() {
		return subjectType;
	}

	public void setSubjectTypeDB(String subjectTypeDB) {
		for(SubjectType type : SubjectType.values()) {
			if(type.name().equalsIgnoreCase(subjectTypeDB)) {
				this.setSubjectType(type);
			}
		}
	}

	public String getSubjectTypeDB() {
		return getSubjectType().name();
	}

	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}

	public ResponseType getResponseType() {
		return responseType;
	}

	public void setResponseTypeDB(String responseTypeDB) {
		for(ResponseType type : ResponseType.values()) {
			if(type.name().equalsIgnoreCase(responseTypeDB)) {
				this.setResponseType(type);
			}
		}
	}

	public String getResponseTypeDB() {
		return getResponseType().name();
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public List<Option> getOptions() {
		return options;
	}
}
