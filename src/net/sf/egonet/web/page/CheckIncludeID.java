package net.sf.egonet.web.page;

import java.io.Serializable;
import java.util.List;

/**
 * In a number of cases we'll want to select which interviews to include
 * in analysis or a report or something similar.  Arrays or lists
 * of this class can be used to transfer what interviews to include between
 * the GUI pages/panels and the analysis/export
 * patterned after the innerclass CheckableWrapper in
 * CheckboxesPanel.java
 *
 */
public class CheckIncludeID implements Serializable {

	private Long id;
	private Boolean selected;
	private Boolean completed;

	public CheckIncludeID(Long id, Boolean completed) {
		this.id = id;
		selected = true;
		this.completed = completed;
	}
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
	public Boolean getSelected() {
		return selected;
	}
	public Long getId() {
		return id;
	}
		
	public static boolean useThisID (List<CheckIncludeID> checkIncludeIDList, Long idToCheck) {
		for ( CheckIncludeID checkID : checkIncludeIDList ) {
			if ( checkID.id.equals(idToCheck)) {
				System.out.println ( "Interview " + idToCheck + " " + checkID.selected);
				return(checkID.selected);
			}
		}
		System.out.println ( "Interview number " + idToCheck + "fell thru");
		return(true);
	}
	
	public String toString() {
		return ( "ID=" + id + " Completed=" + completed + " Selected=" + selected);
	}
}
