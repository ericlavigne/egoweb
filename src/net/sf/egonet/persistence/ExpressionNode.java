package net.sf.egonet.persistence;

import java.util.ArrayList;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;

public class ExpressionNode {

	public static enum NodeType { MATH, COMPARISON, VAR, LITERAL };
	public static enum MathOp {ADD, SUB, MUL, DIV};
	public static enum CompareOp { LESS_EQ, GREATER_EQ, LESS, GREATER, EQUALS, NOT_EQUALS };
	public static final String CompareOpStr[] = 
    {"<=", ">=", "<", ">", "==", "!="};


	private NodeType nodeType;
	private MathOp mathOp;
	private CompareOp compareOp;
	
	private int intResult; // boolean nodes use this with 0 or 1
	private int leftValue;
	private int rightValue;
	private String  varName; // ( or literal in string format )
	private ExpressionNode leftChild;
	private ExpressionNode rightChild;

	
	public ExpressionNode ( CompareOp compareOp) {
		nodeType = NodeType.COMPARISON;
		this.compareOp = compareOp;
		varName = CompareOpStr[this.compareOp.ordinal()];
		leftChild = null;
		rightChild = null;
	}
	
	public ExpressionNode ( String str ) {
		int iCompIndex;
		
		nodeType = NodeType.LITERAL;
		try {
			intResult = Integer.parseInt(str);
		} catch ( NumberFormatException nfe ) {
			iCompIndex = compareOpStrIndex(str);
			if ( iCompIndex<0 ) {
				nodeType = NodeType.VAR;
			} else {
				nodeType = NodeType.COMPARISON;
				switch ( iCompIndex ) {
				case 0: compareOp = CompareOp.LESS_EQ; 	break;
				case 1: compareOp = CompareOp.GREATER_EQ; break;
				case 2: compareOp = CompareOp.LESS; 	break;
				case 3: compareOp = CompareOp.GREATER; 	break;
				case 4: compareOp = CompareOp.EQUALS; 	break;
				case 5: compareOp = CompareOp.NOT_EQUALS; break;
				}
			}
		}
		varName = str;
		leftChild = null;
		rightChild = null;
	}
	
	public void setLeftChild ( ExpressionNode node) {
		leftChild = node;
	}
	
	public void setRightChild ( ExpressionNode node) {
		rightChild = node;
	}
	
	public String toString(boolean bShowVarNames) {
		String str;
		
		str = (leftChild==null) ? "" : leftChild.toString(bShowVarNames);
		if ( bShowVarNames || nodeType==NodeType.MATH || nodeType==NodeType.COMPARISON )
			str += varName;
		else
			str += " " + intResult + " ";
		if ( rightChild!=null )
			str += rightChild.toString(bShowVarNames);
		return(str);
	}
	
	public String toString() {
		return(toString(true));
	}
	
	public int evaluate( Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters) {
		String strQuestionVariable;
		
		leftValue = rightValue = 0;
		if ( leftChild!=null )
			leftValue = leftChild.evaluate(	interviewId, iType, studyId, listOfAlters);
		if ( rightChild!=null )
			rightValue = rightChild.evaluate( interviewId, iType, studyId, listOfAlters);
		switch ( nodeType ) {
		case MATH: // TODO !!!
			 break;
		case COMPARISON:
			 switch ( compareOp ) {
			 case LESS_EQ: 		intResult = (leftValue<=rightValue)?1:0; break;
			 case GREATER_EQ:	intResult = (leftValue>=rightValue)?1:0; break;
			 case LESS:			intResult = (leftValue<rightValue)?1:0; break;
			 case GREATER:		intResult = (leftValue>rightValue)?1:0; break;
			 case EQUALS:		intResult = (leftValue==rightValue)?1:0; break;
			 case NOT_EQUALS:	intResult = (leftValue!=rightValue)?1:0; break; 
			 }
			 break;
		case VAR:
			 strQuestionVariable = TextInsertionUtil.answerToQuestion ( varName, 
						interviewId, iType, studyId, listOfAlters );
			 if ( strQuestionVariable==null || strQuestionVariable.length()==0 ) {
				 System.out.println ( "In ExpressionNode.evaluate strQuestionVariable not found");
				 intResult = 0;
				 break;
			 }
			 try {
				 intResult = Integer.parseInt(strQuestionVariable);
			 } catch ( NumberFormatException nfe ) {
				 System.out.println ( "In ExpressionNode.evaluate strQuestionVariable was NOT an integer");
				 System.out.println ( "strQuestionVariable =" + strQuestionVariable);
				 intResult = 0;
				 break; 
			 }
			 break;
		case LITERAL: // intResult is already set
			 break;
		}
		return(intResult);
	}
	
	/**
	 * scans a string to find the index of the next comparison operator
	 * within it  - "<", "<=", ">", ">=", "==", "!=".
	 * Does some special logic in to give precedence to <= and >=,
	 * returns MAX_VALUE if NONE of them are found.
	 * This function is ad-hoc to parseComparisionList
	 * @param str string to examine
	 * @return index of next comparison string
	 */
	public static int indexOfNextComparisonOp ( String str ) {
		int indexes[];
		int ix;
		int index = Integer.MAX_VALUE;
		
		indexes = new int[ExpressionNode.CompareOpStr.length];
		for ( ix=0 ; ix<CompareOpStr.length ; ++ix ) {
			indexes[ix] = str.indexOf(CompareOpStr[ix]);
			if ( indexes[ix]<0 )
				indexes[ix] = Integer.MAX_VALUE;
		}
		
		if ( indexes[CompareOp.LESS.ordinal()] == indexes[CompareOp.LESS_EQ.ordinal()])
			indexes[CompareOp.LESS.ordinal()] = Integer.MAX_VALUE;
		if ( indexes[CompareOp.GREATER.ordinal()] == indexes[CompareOp.GREATER_EQ.ordinal()])
			indexes[CompareOp.GREATER.ordinal()] = Integer.MAX_VALUE;
		
		for ( ix=0 ; ix<CompareOpStr.length ; ++ix ) {
			if ( indexes[ix]<index )
				index = indexes[ix];
		}
		return(index);
	}
	
	/**
	 * compareOpStrIndex
	 * @param str
	 * @return
	 */
	public static int compareOpStrIndex (String str) {
		int ix;
		
		for ( ix=0 ; ix<CompareOpStr.length ; ++ix ) {
			if ( str.equals(CompareOpStr[ix]))
				return(ix);
		}
		return(-1);
	}
	
	/**
	 * typeOfNextCompareOp
	 * @param str
	 * @return
	 */
	public static int typeOfNextCompareOp (String str) {
		int ix;
		
		for ( ix=0 ; ix<CompareOpStr.length ; ++ix ) {
			if ( str.startsWith(CompareOpStr[ix]))
				return(ix);
		}
		return(-1);
	}	
}


