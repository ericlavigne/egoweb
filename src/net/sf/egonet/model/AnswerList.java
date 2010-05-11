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
	private NameAndValue[] listOptionNames;
	
	/**
	 * standard no-args constructor
	 */
	public AnswerList() {
		setActive(true);
		getRandomKey();
		listName = "";
		studyId = 0l;
		listOptionNames = new NameAndValue[0];
	}
		
	/**
	 * constructor
	 * @param listName the title given to this list
	 * @param studyId identifies the study this is associated with
	 * @param listOptionNames list of name/value pairs
	 */
	AnswerList ( String listName, Long studyId, NameAndValue[] listOptionNames) {
		setActive(true);
		getRandomKey();
		this.listName = listName;
		this.studyId = studyId;
		this.listOptionNames = listOptionNames;
	}
	
	/**
	 * constructor for use in GUI to create an AnswerList with
	 * a given name but not yet filled with data
	 * @param listName the name given this list
	 * @param studyId identifies the study it is associated with
	 */
	AnswerList ( String listName, Long studyId ) {
		setActive(true);
		getRandomKey();
		this.listName = listName;
		this.studyId = studyId;
		listOptionNames = new NameAndValue[0];	
	}
		
	/**
	 * we can't use the usual 'contains' function on an ArrayList of
	 * NameAndValues, as that uses ==.  This explicitly compares
	 * using equals() to find the index of a nameAndValue in the list.
	 * @param theList arrayList to examine
	 * @param nameAndValue name/value we are interested in
	 * @return index of nameAndValue in the array, -1 if not found
	 */
	private int indexOf ( ArrayList<NameAndValue> theList, NameAndValue nameAndValue ) {
		int ix;
		
		for ( ix=0 ; ix<theList.size() ; ++ix ) {
			if (theList.get(ix).equals(nameAndValue))
				return(ix);
		}
		return(-1);
	}
	
	/**
	 * removes a name/value pair from the list
	 * @param name human-readable string 
	 * @param value integer value associated with the name
	 * @return true if anything is removed, false otherwise
	 */
	public boolean removeNameAndValue ( String name, Integer value) {
		NameAndValue nameAndValue = new NameAndValue ( name, value);
		ArrayList<NameAndValue> theList;
		int index;
		
		theList = getListOptionNamesAsList();
		index = indexOf ( theList, nameAndValue);
		if ( index<0) {
			System.out.println ( "AnswerList.removeNameAndValue " + nameAndValue + "not found");
			return(false);
		}
		theList.remove(index);
		setListOptionNamesFromList(theList);
		return(true);
	}
		
	/**
	 * replaces one name/value pair in the list with another.
	 * this is generally used for correcting spelling mistakes
	 * or tweaking the integer values associated with the name
	 * @param oldName  name to replace
	 * @param oldValue integer value to replace
	 * @param newName  new name to use
	 * @param newValue new integer value to use
	 * @return true if the replacement takes place, false otherwise
	 */
	public boolean replaceNameAndValue ( String oldName, Integer oldValue,
			String newName, Integer newValue ) {
		NameAndValue oldNameAndValue = new NameAndValue ( oldName, oldValue);
		NameAndValue newNameAndValue = new NameAndValue ( newName, newValue);
		int ix;
		
		for ( ix=0 ; ix<listOptionNames.length ; ++ix ) {
			if ( listOptionNames[ix].equals(oldNameAndValue)) {
				listOptionNames[ix] = newNameAndValue;
				return(true);
			}	
		}
		return(true);
	}
		
	/**
	 * moves a name/value pair up one location in the list
	 * @param name string to move
	 * @param value integer value to move
	 * @return true if the name/value pair is found and moved, 
	 * false otherwise
	 */
	public boolean moveUp ( String name, Integer value) {
		NameAndValue nameAndValue = new NameAndValue ( name, value);
		int ix;
		int index = -1;
		
		for ( ix=0 ; ix<listOptionNames.length && index==-1 ; ++ix ) {
			if ( listOptionNames[ix].equals(nameAndValue))
					index = ix;
		}
		if ( index==-1 || index==0 )
			return(false);
		nameAndValue = listOptionNames[index-1];
		listOptionNames[index-1] = listOptionNames[index];
		listOptionNames[index] = nameAndValue;
		return(true);
	}
		
		
	/**
	 * getters & setters
	 * @param listName
	 */
	public void setListName ( String listName ) {
		this.listName = (listName==null)?"":listName;
	}
	public String getListName() { return listName;}
		
	public void setStudyId ( Long studyId ) {
		this.studyId = (studyId==null)?0L:studyId;
	}
	public Long getStudyId() { return studyId;}
	
	public String toString() {
		String strReturn = listName + " for study #" + studyId + "[";
		
		for ( NameAndValue nameAndValue : listOptionNames ) {
			strReturn += nameAndValue + ", ";
		}

		strReturn += "]";
		return(strReturn);
		}
	
	/**
	 * set / get the array of listOptionNames as an array
	 */
	public void setListOptionNames ( NameAndValue[] listOptionNames ) {
		this.listOptionNames = (listOptionNames==null)?new NameAndValue[0]:listOptionNames;
	}
	public NameAndValue[] getListOptionNames() { return listOptionNames;}	
	
	/**
	 * get / set the array of listOptionNames as an arrayList
	 */
	public ArrayList<NameAndValue> getListOptionNamesAsList() {
		return ( new ArrayList<NameAndValue>(Arrays.asList(listOptionNames)));	
	}
	public void setListOptionNamesFromList ( ArrayList<NameAndValue> listOptionNames ) {
		this.listOptionNames = new NameAndValue[listOptionNames.size()];
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
			listOptionNames = new NameAndValue[0];
			return;
		}
		
		strk = new StringTokenizer(listOptionNamesDB, ",");
		listOptionNames = new NameAndValue[strk.countTokens()];
		while ( strk.hasMoreTokens()) {
			listOptionNames[ix] = new NameAndValue(strk.nextToken());
			++ix;
		}
	}
	
	public String getListOptionNamesDB() {
		StringBuilder strb = new StringBuilder();
		int ix;
		
		for ( ix=0 ; ix<listOptionNames.length ; ++ix ) {
		 	strb.append(listOptionNames[ix]);
		 	if ( ix<listOptionNames.length-1)
		 		strb.append(',');
		}
		return ( strb.toString());
	}	
	
}
