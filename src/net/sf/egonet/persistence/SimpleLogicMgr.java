package net.sf.egonet.persistence;

import java.util.ArrayList;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.Question.QuestionType;

public class SimpleLogicMgr {

	/**
	 * VERY redimentary expression tree creator
	 * the strExpression list is of the type created by parseComparisonList
	 * @param strExpression
	 * @return
	 */
	public static ExpressionNode createSimpleExpression ( ArrayList<String> strExpression) {
		ExpressionNode mainNode;
		ExpressionNode leftNode;
		ExpressionNode rightNode;
		
		if ( strExpression.size() != 3 ) {
			System.out.println ("SimpleLogicMgr can only handle 3 nodes right now.");
			return(null);
		}
		leftNode = new ExpressionNode ( strExpression.get(0));
		mainNode = new ExpressionNode ( strExpression.get(1));
		rightNode = new ExpressionNode ( strExpression.get(2));
		mainNode.setLeftChild(leftNode);
		mainNode.setRightChild(rightNode);
		System.out.println ( "Expression = " + mainNode.toString(false));
		System.out.println ( "Expression = " + mainNode.toString(true));	
		return(mainNode);
	}
	
	public static ExpressionNode createSimpleExpression ( String strExpression ) {
		 ArrayList<String> expressionList;
		 
		 expressionList = parseComparisonList ( strExpression );
		 return ( createSimpleExpression(expressionList));
	}
	
	public static int createSimpleExpressionAndEvaluate ( String strExpression,
			Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters) {
		
		ExpressionNode expressionNode = createSimpleExpression ( strExpression);
		return ( expressionNode.evaluate(interviewId, iType, studyId, listOfAlters));
	}


	/**
	 * creates an arrayList of strings from a larger input string
	 * in anticipation of (rather simple) conditional calculation.
	 * For our purposes the calculations will only involve variables
	 * separated by the comparison operators 
	 * >  >=  <  <=  ==  !=
	 * @param strInput  string of the form Q1+Q2-4
	 * @return Arraylist of the form "q1","+","Q2","-","4"
	 */
	public static ArrayList<String> parseComparisonList ( String strInput ) {
	
	ArrayList<String> theList = new ArrayList<String>();
	String strNextComparisonOp;
	int iWordEnd;
	int iNextCompareOp;
	    

	iWordEnd = 0;
	while ( strInput.length()>0  &&  iWordEnd != Integer.MAX_VALUE) {
	    iWordEnd = ExpressionNode.indexOfNextComparisonOp ( strInput );
	    if ( iWordEnd != Integer.MAX_VALUE) {
	    	if ( iWordEnd>1 )
	    		theList.add(strInput.substring(0,iWordEnd).trim());
	    	strInput = strInput.substring(iWordEnd);
	    	iNextCompareOp = ExpressionNode.typeOfNextCompareOp(strInput);
	    	if ( iNextCompareOp<0 || iNextCompareOp>=6 ) {
	    		System.out.println ("Something bad-wrong in parseComparisonList " + iNextCompareOp);
	    		return(theList);
	    	}
	    	strNextComparisonOp = ExpressionNode.CompareOpStr[iNextCompareOp];
	    	theList.add(strNextComparisonOp);
	    	strInput = strInput.substring(strNextComparisonOp.length());
	    	strInput = strInput.trim();
	    }
	}
	if ( strInput.length()>0)
		theList.add(strInput);
	
	System.out.println ( "parseComparisionList");
	for ( String str:theList ) {
		System.out.println(str);
	}
	
	return(theList);
	}	
}
