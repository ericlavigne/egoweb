package net.sf.egonet.model;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;

import net.sf.egonet.persistence.DB;
import net.sf.egonet.persistence.Presets;
import net.sf.egonet.persistence.AnswerLists;

/**
 * this class is rather a cross between Presets in the
 * Persistence package and QuestionOption in the 
 * Model package
 * 
 * it will use static functions for testing, it 
 * may move to Persistence and make more use
 * of Hibernate and the database
 * might change name to AnswerLists to keep with naming
 * convention
 * @author Kevin
 *
 */

	/**
	 * TitleAndStudy is a convenience class for tracking
	 * lists of answers by title and study ID.
	 * Once this data is in the database much of the
	 * logic will likely be refactored to do filters
	 * using Hibernate SQL commands
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
	

public class AnswerListMgr extends Entity  {
	/**
	 * this will maintain two very similar data structures.
	 * answerListTree will be used during the creation and
	 * editting of lists of preset answers, 
	 * answerLists will be used when editting the questions 
	 * themselves
	 */
	private static Long studyId;
	private static TreeMap<TitleAndStudy,AnswerList> answerListTree;
	private static TreeMap<String,String[]> answerLists;
	private static boolean needsUpdate;
	


	/**
	 * simple loads all the answer lists for a specified study
	 * @param sId
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
		answerLists = new TreeMap<String,String[]>();
		
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
		
		answerLists = Presets.get();
		answerListTree = new TreeMap<TitleAndStudy,AnswerList>();
		listPresetTitles = new String[answerLists.size()];
		listPresetTitles = answerLists.keySet().toArray(listPresetTitles);
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
	
	public static String[] getOptionNames ( String strTitle, Long studyId ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
		
		answerList = answerListTree.get(titleAndStudy);
		if ( answerList==null ) {
			System.out.println ( titleAndStudy + " not found in AnswerListMgr.getOptionNames");
			return(new String[0]);
		}
		return(answerList.getListOptionNames());
	}
	
	public static ArrayList<String> getOptionNamesAsList (String strTitle, Long studyId ) {
		String[] optionNames = getOptionNames(strTitle,studyId);
		ArrayList<String> retList = new ArrayList<String>(optionNames.length);

		for ( String str:optionNames) {
			retList.add(str);
		}
		return(retList);
	}
	
	/**
	 * adds a new string to the list of option names
	 * @param strTitle title of the list of options we are dealing with
	 * @param studyId id of the study we are dealing with
	 * @param optionName the option name within the list we are dealing with
	 * @param newName the new value of the option name
	 * @return true if any changes take place
	 */
	public static boolean addOptionName ( String strTitle, Long studyId, String optionName ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
		ArrayList<String> strList;
			
		answerList = answerListTree.get(titleAndStudy);
		if ( answerList==null ) {
			System.out.println ( titleAndStudy + " not found in AnswerListMgr.addOptionName");
			return(false);
		}
		strList = answerList.getListOptionNamesAsList();
		if ( strList.contains(optionName))
			return(false);
		strList.add(optionName);
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
	public static boolean removeOptionName ( String strTitle, Long studyId, String optionName ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
		ArrayList<String> strList;
			
		answerList = answerListTree.get(titleAndStudy);
		if ( answerList==null ) {
			System.out.println ( titleAndStudy + " not found in AnswerListMgr.removeOptionName");
			return(false);
		}
		strList = answerList.getListOptionNamesAsList();
		if ( !strList.contains(optionName))
			return(false);
		strList.remove(optionName);
		answerList.setListOptionNamesFromList(strList);
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
			String optionName, String newName ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
		ArrayList<String> strList;
		int index;
		
		// special check, if the new name == the old name
		// don't need to do anything
		if ( optionName.equals(newName))
			return(false);
		answerList = answerListTree.get(titleAndStudy);
		if ( answerList==null ) {
			System.out.println ( titleAndStudy + " not found in AnswerListMgr.replaceOptionName");
			return(false);
		}
		strList = answerList.getListOptionNamesAsList();
		index = strList.indexOf(optionName);
		if (index<0)
			return(false);
		strList.remove(optionName);
		strList.add(index, newName);
		answerList.setListOptionNamesFromList(strList);
		needsUpdate = true;
		return(true);
	}
	
	/**
	 * used to allow the user to rearrange a list of option names.  They can move 
	 * a string up in the list
	 * @param strTitle title of the list of options we are dealing with
	 * @param studyId id of the study we are dealing with
	 * @param optionName the option name within the list we are dealing with
	 * @return true if any changes take place 
	 */
	public static boolean moveOptionNameUp ( String strTitle, Long studyId, String optionName ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
		ArrayList<String> strList;
		int index;
		
		answerList = answerListTree.get(titleAndStudy);
		if ( answerList==null ) {
			System.out.println ( titleAndStudy + " not found in AnswerListMgr.moveOptionNameUp");
			return(false);
		}
		strList = answerList.getListOptionNamesAsList();
		index = strList.indexOf(optionName);
		if (index<=0) // ( if it is zero index it can't move back )
			return(false);
		strList.remove(optionName);
		strList.add(index-1, optionName);
		answerList.setListOptionNamesFromList(strList);
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
	
	public static AnswerList getAnswerListFor ( String strTitle, Long studyId ) {
		TitleAndStudy titleAndStudy = new TitleAndStudy (strTitle, studyId );
		AnswerList answerList;
		
		answerList = answerListTree.get(titleAndStudy);
		return(answerList);	
	}
	
	/**
	 * now the non-editting functions, 
	 * for when this data is used to help construct questions
	 */
	
	public static TreeMap<String,String[]> get() {
		if (needsUpdate)
			update();
		return(answerLists);
	}
	
	
	public static boolean saveAllAnswerListsForStudy(Long studyId) {
		boolean bRetVal = true;
		String[] strTitles = getTitles ( studyId);
		AnswerList answerList;
		
		for ( String str:strTitles ) {
			answerList = getAnswerListFor(str, studyId);
			// System.out.println ( answerList);
			try {
			    DB.save(answerList);
			} catch ( org.hibernate.MappingException me ) {
				me.printStackTrace();
				bRetVal = false;
			//	return(false);
			} 
			//catch ( java.sql.SQLException sqle ) {
			//	sqle.printStackTrace();
			//}
		}
		return(bRetVal);
	}

}
