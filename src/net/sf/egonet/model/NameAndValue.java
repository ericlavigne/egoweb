package net.sf.egonet.model;

import java.io.Serializable;

/**
 * this class is rather an in-between between preSets
 * and QuestionOption.
 * This is used when editting reusable sets of answers
 * to Selection and Multiple Selection questions.
 * Each set will have a name and an array of string names, 
 * and each name will have an integer value associated
 * with it.
 * @author Kevin
 *
 */

public	class NameAndValue implements Serializable {
		private String name;
		private Integer value;
		
		public NameAndValue() {
			name = new String("");
			value = new Integer(0);
		}
		
		public NameAndValue( String name, Integer value ) {
			this.name = name;
			this.value = value;
		}
		
		public NameAndValue( String name, int value ) {
			this.name = name;
			this.value = new Integer(value);
		}
		
		/**
		 * this constructor assume the String nameEqualsValue
		 * will be of the form name=<Integer value>
		 * @param nameEqualsValue name AND value, separated by equals sign
		 */
		public NameAndValue ( String nameEqualsValue ) {
			int index;
			String strValue;
			
			index = nameEqualsValue.indexOf('=');
			if ( index<0 ) {
				name = nameEqualsValue.trim();
				value = new Integer(0);
			} else {
				name = nameEqualsValue.substring(0,index).trim();
				strValue = nameEqualsValue.substring(index+1).trim();
				try {
					value = new Integer(strValue);
				} catch ( NumberFormatException nfe) {
					value = new Integer(0);
				}
			}
			
		}
		public void    setName ( String name ) { this.name=(name==null) ? "" : name; }
		public String  getName() { return(name);}
		public void    setValue ( Integer value ) { this.value=(value==null) ? new Integer(0) : value; }
		public Integer getValue() { return(value);}
		
		public String toString() {
			return ( name + "=" + value );
		}
		
		public boolean equals ( NameAndValue that ) {
			if ( name.equals(that.name)  &&  value.equals(that.value))
				return(true);
			return(false);
		}
		/**
		 * given an array of strings, constructs an array of NameAndValue
		 * assigning default value values
		 * @param strInput array of strings ( from preSets ) 
		 * @return array of NameAndValue
		 */
		public static NameAndValue[] createArray ( String[] strInput ) {
			NameAndValue[] retArray = new NameAndValue[strInput.length];
			int ix = 0;
			int iValue;
			
			for ( String string : strInput ) {
				if ( string.equalsIgnoreCase("Yes")) {
					iValue = 1;
				} else if ( string.equalsIgnoreCase("No")) {
					iValue = 0;
				} else {
					iValue = ix+1;
				}
				retArray[ix] = new NameAndValue(string, iValue);
				++ix;
			}
			return(retArray);
		}	
	}

