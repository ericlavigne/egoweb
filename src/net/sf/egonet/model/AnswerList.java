package net.sf.egonet.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class AnswerList extends Entity implements Serializable {
	/**
	 * similar to the Presets class but editable
	 * and unique to a given study
	 * @author Kevin
	 *
	 */
	private String listName;
	private Long studyId;
	private String[] listOptionNames;
	
	AnswerList() {
		setActive(true);
		getRandomKey();
		listName = "";
		studyId = 0l;
		listOptionNames = new String[0];
	}
		
	AnswerList ( String listName, Long studyId, String[] listOptionNames) {
		setActive(true);
		getRandomKey();
		this.listName = listName;
		this.studyId = studyId;
		this.listOptionNames = listOptionNames;
	}
		
	AnswerList ( String listName, Long studyId ) {
		setActive(true);
		getRandomKey();
		this.listName = listName;
		this.studyId = studyId;
		this.listOptionNames = new String[0];	
	}
		
	public void setListName ( String listName ) {
		this.listName = (listName==null)?"":listName;
	}
	public String getListName() { return listName;}
		
	public void setStudyId ( Long studyId ) {
		this.studyId = (studyId==null)?0L:studyId;
	}
	public Long getStudyId() { return studyId;}
	
	/**
	 * set / get the array of listOptionNames as an array
	 */
	public void setListOptionNames ( String[] listOptionNames ) {
		this.listOptionNames = (listOptionNames==null)?new String[0]:listOptionNames;
	}
	public String[] getListOptionNames() { return listOptionNames;}	
	
	/**
	 * get / set the array of listOptionNames as an arrayList
	 */
	public ArrayList<String> getListOptionNamesAsList() {
		return ( new ArrayList<String>(Arrays.asList(listOptionNames)));	
	}
	public void setListOptionNamesFromList ( ArrayList<String> listOptionNames ) {
		this.listOptionNames = new String[listOptionNames.size()];
		this.listOptionNames = listOptionNames.toArray(this.listOptionNames);
	}
		
	/**
	 * get / set the array of listOptionNames as a comma-delimited string
	 * this is a convenience to make saving easier - the list of
	 * options becomes one string
	 */
	public void setListOptionNamesDB ( String listOptionNamesDB ) {
		StringTokenizer strk;
		int ix = 0;
		
		if ( listOptionNamesDB==null) {
			listOptionNames = new String[0];
			return;
		}
		
		strk = new StringTokenizer(listOptionNamesDB, ",");
		listOptionNames = new String[strk.countTokens()];
		while ( strk.hasMoreTokens()) {
			listOptionNames[ix] = strk.nextToken();
			++ix;
		}
	}
	public String getListOptionNamesDB() {
		StringBuilder strb = new StringBuilder();
		int ix;
		
		// System.out.println ("entered getListOptionNamesDB");
		for ( ix=0 ; ix<listOptionNames.length ; ++ix ) {
		 	strb.append(listOptionNames[ix]);
		 	if ( ix<listOptionNames.length-1)
		 		strb.append(',');
		}
		// System.out.println ( "exitting getListOptionNamesDB " + strb.toString());
		return ( strb.toString());
	}
	
	
	public String toString() {
		String strReturn = listName + " for study #" + studyId + "[";
		
		for ( String str:listOptionNames ) {
			strReturn += str + ", ";
		}
		strReturn += "]";
		return(strReturn);
		}
	
}
