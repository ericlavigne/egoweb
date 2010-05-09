package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;

import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Presets;
import net.sf.egonet.persistence.AnswerLists;

	/**
	 * TitleAndStudy is a convenience class for tracking
	 * lists of answers by title and study ID.
	 * Once this data is in the database much of the
	 * logic will likely be refactored to do filters
	 * using Hibernate SQL commands
	 * TODO : this could be replaced by the more general
	 * purpose NameAndValue class.
	 */
	class TitleAndStudy implements Comparable<TitleAndStudy> {
		String title;
		Long studyId;
		
		TitleAndStudy ( String title, Long studyId ) {
			this.title = title;
			this.studyId = studyId;
		}
		
		public void setTitle ( String title ) {
			this.title = title;
		}
		public String getTitle() { return(title);}
		
		public void setStudyId( Long studyId ) {
			this.studyId = studyId;
		}
		public Long getStudyId() { return(studyId);}
		
		public boolean isForStudyId ( Long studyId) {
			return (this.studyId.equals(studyId));
		}
		
		public String toString() { 
			return ( title + " " + studyId);
		}
		
		public int compareTo( TitleAndStudy that ) {
			int iRetval;
			
			iRetval = title.compareTo(that.title);
			if ( iRetval==0)
				iRetval = studyId.compareTo(that.studyId);
			return(iRetval);
		}
	}
	

/* ******************************************************** */
/*            main public class, AnswerListMgr              */
/* ******************************************************** */
	
public class AnswerListMgr extends Entity  {
	
	
//	private class AnswerListIterator implements Iterator<AnswerList> {
//		String[] strTitles;
//		int index;
//		
//		/**
//		 * inner class Iterator so the outside world can step
//		 * through all the AnswerLists with ease
//		 * @param sID identifies the study to work with
//		 */
//		AnswerListIterator( Long sID) {
//			strTitles = getTitles ( sID);
//			index = 0;
//		}
//		
//		public boolean hasNext() {
//			return(index<strTitles.length);
//		}
//		
//		public AnswerList next() {
//			AnswerList retList = answerListTree.get(strTitles[index]);
//			++index;
//			return(retList);
//		}
//		
//		public void remove() {} // not implemented	
//		}	
//	
	

	
	/**
	 * this will maintain two very similar data structures.
	 * answerListTree will be used during the creation and
	 * editting of lists of preset answers, 
	 * answerLists will be used when editting the questions 
	 * themselves
	 */
	private static Long studyId;
	private static TreeMap<TitleAndStudy,AnswerList> answerListTree;
	private static TreeMap<String,NameAndValue[]> answerLists;
	private static TreeMap<String,String[]> originalPresets;
	private static boolean needsUpdate;
	


	/**
	 * simple loads all the answer lists for a specified study
	 * @param sId identifyes the study
	 * @return
	 */
     public static boolean loadAnswerListsForStudy ( Long sId) {
    	 boolean bListsLoaded = true;
    	 List<AnswerList> listAnswerLists = null;
  		 TitleAndStudy titleAndStudy;
  	
    	 studyId = sId;
    	 
    	 try { 
    		 listAnswerLists = AnswerLists.getAnswerListsUsingStudy(studyId);
    	 } catch ( Exception e) {
    		 System.out.println ( "exception thrown from Answerists.getAnswerListsUsingStudy");
    		 e.printStackTrace();
    		 bListsLoaded = false;
    	 }
    
    	 if ( listAnswerLists==null || !bListsLoaded || listAnswerLists.isEmpty()) {
    		 System.out.println ("Initializing using initFromPresets");
    		 initFromPresets();
    		 return(true);
    	 }
    	 
		answerListTree = new TreeMap<TitleAndStudy,AnswerList>();
		answerLists = new TreeMap<String,NameAndValue[]>();
		
		for ( AnswerList answerList : listAnswerLists ) {
			titleAndStudy = new TitleAndStudy ( answerList.getListName() , studyId);
			answerListTree.put(titleAndStudy, answerList);
		}
		update();
		needsUpdate = false;
    	return(bListsLoaded);
     }
     

	/**
	 * when a study is first started it will have no lists of
	 * preset Answer Options, this will create an initial list
	 */
	private static void initFromPresets() {
		String[] listPresetTitles;
		TitleAndStudy titleAndStudy;
		AnswerList answerList;
		
		originalPresets = Presets.get();
		listPresetTitles = new String[originalPresets.size()];
		listPresetTitles = originalPresets.keySet().toArray(listPresetTitles);
		
		// from the originalPresets, consisting of arrays of strings
		// keyed by a string name, create answerLists which will have
		// arrays of (strings+Integers) keyed by a string name, the
		// integers getting default values
		answerLists = new TreeMap<String,NameAndValue[]>();
		for ( String strTitle : listPresetTitles ) {
			answerLists.put(strTitle, NameAndValue.createArray(originalPresets.get(strTitle)));
		}
		
		
		answerListTree = new TreeMap<TitleAndStudy,AnswerList>();
		for ( String strTitle : listPresetTitles ) {
			answerList = new AnswerList ( strTitle, studyId, answerLists.get(strTitle));
			titleAndStudy = new TitleAndStudy (strTitle, studyId );
			answerListTree.put(titleAndStudy, answerList);
		}
		needsUpdate = false;
	}
	
	/**
	 * this first set of functions deal with allowing the outside world
	 * to edit this data.  
	 * First, functions that deal with the names of the lists available
	 */
	
	/**
	 * returns a list of the titles ( names assigned to lists of preset
	 * answers for Selection and Multiple Selection questions )
	 * @param studyId id of the study we are dealing with
	 * @return Array of string for use in the GUI
	 */
	public static String[] getTitles ( Long studyId) {
		ArrayList<String> workArray;
		String[] retArray;
		
		workArray = getTitlesAsList(studyId);
		retArray = new String[workArray.size()];
		retArray = workArray.toArray(retArray);
		return(retArray);
	}
	
	/**
	 * returns the list of titles as an array list
	 * @param studyId identifies the study
	 * @return ArrayList of titles ( names of preset answer groups )
	 */
	public static ArrayList<String> getTitlesAsList ( Long studyId) {
		ArrayList<String> workArray = new ArrayList<String>();
		
		for ( TitleAndStudy tas : answerListTree.keySet()) {
			if ( tas.isForStudyId(studyId)) 
				workArray.add(tas.getTitle());
		}	
		return(workArray);
	}
	/**
	 * simply returns the index of title/studyId within
	 * the answerTreeList
	 * @param strTitle title we are looking for
	 * @param studyId id of study we are dealing with
	 * @return
	 */
	private static int indexOf ( String strTitle, Long studyId ) {
		int index = 0;
		
		for ( TitleAndStudy tas : answerListTree.keySet()) {
			if ( tas.isForStudyId(studyId)) {
				if (tas.getTitle().equals(strTitle))
					return(index);
				++index;
			}
		}
		return(index);
	}
	
	/**
	 * removes a title from the data structure
	 * @param strTitle title of the list to remove
	 * @param studyId id of the study we are dealing with
	 * @return true if the title is removed false if it is not in the list
	 */
	public static int removeTitle ( String strTitle, Long studyId ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerListToRemove;
		int index = -1;
		
		if ( answerListTree.containsKey(titleAndStudy)) {
		    index = indexOf ( strTitle, studyId);
		    answerListToRemove = answerListTree.get(titleAndStudy);
		    answerListToRemove.setActive(false);
		    DB.save(answerListToRemove); // needed to save the active=false flag
			answerListTree.remove(titleAndStudy);
			needsUpdate = true;
		}
		return(index);
	}
	
	/**
	 * adds a title ( and hence a new, empty AnswerList )  to 
	 * the data structure
	 * @param strTitle title of the new list
	 * @param studyId id of the study we are dealing with
	 * @return true if the title is a new one and it is added, 
	 * false otherwise
	 */
	public static boolean addTitle ( String strTitle, Long studyId ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
		
		if ( answerListTree.containsKey(titleAndStudy)) {
			return(false);
		}
		answerList = new AnswerList ( strTitle, studyId);
		answerListTree.put(titleAndStudy, answerList);
		needsUpdate = true;
		return(true);
	}

	/**
	 * now the functions that deal with adding/removing option names
	 * from specific lists
	 */
    /**
     * returns all the option names (pre-created lists of answers for Selection
     * and Multiple Selection questions) for a given title and studyId
     */
	
	public static NameAndValue[] getOptionNames ( String strTitle, Long studyId ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
		
		answerList = answerListTree.get(titleAndStudy);
		if ( answerList==null ) {
			System.out.println ( titleAndStudy + " not found in AnswerListMgr.getOptionNames");
			return(new NameAndValue[0]);
		}
		return(answerList.getListOptionNames());
	}
	
	/**
	 * returns the list of name/value pairs as an Arraylist
	 * @param strTitle the name of the group
	 * @param studyId identifies the study we are interested in
	 * @return arraylist of name/value answer pairs
	 */
	public static ArrayList<NameAndValue> getOptionNamesAsList (String strTitle, Long studyId ) {
		NameAndValue[] optionNames = getOptionNames(strTitle,studyId);
		ArrayList<NameAndValue> retList = new ArrayList<NameAndValue>(optionNames.length);

		for ( NameAndValue str:optionNames) {
			retList.add(str);
		}
		return(retList);
	}
	
	/**
	 * adds a new string to the list of option names
	 * @param strTitle title of the list of options we are dealing with
	 * @param studyId id of the study we are dealing with
	 * @param optionName the option name within the list we are dealing with
	 * @param optionValue the value we want the option to have
	 * @return true if any changes take place
	 */
	public static boolean addOptionName ( String strTitle, Long studyId, 
			String optionName, Integer optionValue ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
		ArrayList<NameAndValue> strList;
			
		answerList = answerListTree.get(titleAndStudy);
		if ( answerList==null ) {
			System.out.println ( titleAndStudy + " not found in AnswerListMgr.addOptionName");
			return(false);
		}
		strList = answerList.getListOptionNamesAsList();
		if ( strList.contains(optionName))
			return(false);
		strList.add(new NameAndValue(optionName, optionValue));
		answerList.setListOptionNamesFromList(strList);
		needsUpdate = true;
		return(true);
	}
	
	/**
	 * lets the user remove an option name from the list
	 * @param strTitle title of the list of options we are dealing with
	 * @param studyId id of the study we are dealing with
	 * @param optionName the option name within the list we are dealing with
	 * @param newName the new value of the option name
	 * @return true if any changes take place
	 */
	public static boolean removeOptionName ( String strTitle, Long studyId, 
			String optionName, Integer optionValue ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
			
		answerList = answerListTree.get(titleAndStudy);
		if ( answerList==null ) {
			System.out.println ( titleAndStudy + " not found in AnswerListMgr.removeOptionName");
			return(false);
		}
		answerList.removeNameAndValue(optionName, optionValue);
		needsUpdate = true;
		return(true);
	}
	
	/**
	 * lets the user change an option name
	 * @param strTitle title of the list of options we are dealing with
	 * @param studyId id of the study we are dealing with
	 * @param optionName the option name within the list we are dealing with
	 * @param newName the new value of the option name
	 * @return true if any changes take place
	 */
	public static boolean replaceOptionName ( String strTitle, Long studyId, 
			String optionName, Integer optionValue, String newName, Integer newValue ) {
		boolean retVal = false;
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
		
		// special check, if the new name == the old name
		// don't need to do anything
		if ( optionName.equals(newName) && optionValue.equals(newValue))
			return(retVal);
		answerList = answerListTree.get(titleAndStudy);
		if ( answerList==null ) {
			System.out.println ( titleAndStudy + " not found in AnswerListMgr.replaceOptionName");
			return(retVal);
		}
		retVal = answerList.replaceNameAndValue(optionName, optionValue, newName, newValue);
		needsUpdate = true;
		return(retVal);
	}
	
	/**
	 * used to allow the user to rearrange a list of option names.  They can move 
	 * a string up in the list
	 * @param strTitle title of the list of options we are dealing with
	 * @param studyId id of the study we are dealing with
	 * @param optionName the option name within the list we are dealing with
	 * @return true if any changes take place 
	 */
	public static boolean moveOptionNameUp ( String strTitle, Long studyId, 
			String optionName, Integer optionValue ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;

		answerList = answerListTree.get(titleAndStudy);
		if ( answerList==null ) {
			System.out.println ( titleAndStudy + " not found in AnswerListMgr.moveOptionNameUp");
			return(false);
		}
		answerList.moveUp( optionName, optionValue);
		needsUpdate = true;
		return(true);
	}	
	
	/**
	 * takes care of creating the usable TreeMap 
	 */
	private static void update() {
		AnswerList answerList;
		
		answerLists.clear();
		for ( TitleAndStudy tas : answerListTree.keySet()) {
			answerList = answerListTree.get(tas);
			answerLists.put(tas.getTitle(), answerList.getListOptionNames());
		}
	}
	
	/**
	 * now the non-editting functions, 
	 * for when this data is used to help construct questions
	 * this is used in the editquestion page as a drop-in
	 * replacement for the old presets.
	 */
	
	public static TreeMap<String,NameAndValue[]> get() {
		if (needsUpdate)
			update();
		return(answerLists);
	}
	
	/** 
	 * save all the preset answer lists data for this study
	 * @param studyId identifies the study
	 * @return true if all goes well, false if things go bad-wrong
	 */
	public static boolean saveAllAnswerListsForStudy(Long studyId) {
		boolean bRetVal = true;
		TitleAndStudy titleAndStudy;
		String[] strTitles = getTitles ( studyId);
		AnswerList answerList;
		
		for ( String str:strTitles ) {
			titleAndStudy = new TitleAndStudy (str, studyId );
			answerList = answerListTree.get(titleAndStudy);;
			try {
			    DB.save(answerList);
			} catch ( org.hibernate.MappingException me ) {
				me.printStackTrace();
				bRetVal = false;
			} 
		}
		return(bRetVal);
	}

	/**
	 * returns the contents of the answerListTree as an Array
	 * so they can be processed sequentially quickly.
	 * Used in the Archiving to XML section
	 * @param sID identifies the study to save
	 * @return Array of all answerLists
	 */
	
	public static AnswerList[] getAnswerLists(Long sID) {
		AnswerList[] returnArray;
		
		loadAnswerListsForStudy (sID);
		returnArray = new AnswerList[answerListTree.size()];
		returnArray = answerListTree.values().toArray(returnArray);
		return(returnArray);
	}
	
	/**
	 * outside world can use this to deal with answerLists without
	 * dealing with the internals of how things are stored.
	 * 
	 * @param sID study ID we are dealing with
	 * @return an iterator
	 */
//	public static Iterator<AnswerList> getIterator(Long sID) {
//		
//		return ( new AnswerListMgr().new AnswerListIterator(sID));
//	}
}
