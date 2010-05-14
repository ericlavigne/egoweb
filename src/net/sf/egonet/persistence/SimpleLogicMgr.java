package net.sf.egonet.persistence;

import java.util.ArrayList;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Question;
 
public class SimpleLogicMgr {

	static boolean error = false;
	
	/**
	 * VERY rudimentary expression tree creator
	 * the strExpression list is of the type created by parseComparisonList
	 * @param strExpression
	 * @return
	 */
	public static ExpressionNode createSimpleExpression ( ArrayList<String> strExpression) {
		int iExpressionSize;
		ExpressionNode mainNode = null;
		ExpressionNode leftNode;
		ExpressionNode rightNode;
		
		iExpressionSize = strExpression.size();
		
		if ( iExpressionSize != 3  &&  iExpressionSize != 1 ) {
			error = true;
			System.out.println ("ERROR - SimpleLogicMgr can only handle 1 OR 3 nodes right now not " + iExpressionSize);
			for ( String str:strExpression )
				System.out.println ( str);
			return(mainNode);
		}
		switch ( iExpressionSize ) {
		    case 1:
		    	 mainNode = new ExpressionNode ( strExpression.get(0));
		    	 // System.out.println ( "Expression = " + mainNode.toString(false));
		    	 // System.out.println ( "Expression = " + mainNode.toString(true));
		    	 break;
		    case 3:
		    	 leftNode = new ExpressionNode ( strExpression.get(0));
		    	 mainNode = new ExpressionNode ( strExpression.get(1));
		    	 rightNode = new ExpressionNode ( strExpression.get(2));
		    	 mainNode.setLeftChild(leftNode);
		    	 mainNode.setRightChild(rightNode);
		    	 // System.out.println ( "Expression = " + mainNode.toString(false));
		    	 // System.out.println ( "Expression = " + mainNode.toString(true));	
		    	 break;
			}
		return(mainNode);
	}
	
	/**
	 * parses a string into an arrayList of string tokens representing symbols
	 * in a simple conditional expression
	 * @param strExpression a string with a (very simple) conditional expression
	 * such as "Q1>=4"
	 * @return the trunk of a tree of ExpressionNode ready for evaluation
	 */
	public static ExpressionNode createSimpleExpression ( String strExpression ) {
		 ArrayList<String> expressionList;
		 
		 expressionList = parseComparisonList ( strExpression );
		 return ( createSimpleExpression(expressionList));
	}
	
	/**
	 * takes a string that is a simple conditional expression, parses it into an
	 * arrayList of strings, creates an ExpressionNode tree from those strings, 
	 * and evaluates it with using the current settings of any and all vars.
	 * @param strExpression the expression in string form such as "EGO_AGE>=18"
	 * @param interviewId - needed to look up answers
	 * @param iType - needed to look up answers
	 * @param studyId - needed to look up answers
	 * @param listOfAlters - needed to look up answers
	 * @return 1 if the expression is true, 0 if it is false
	 */
	public static int createSimpleExpressionAndEvaluate ( String strExpression,
			Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters) {
		int iEvaluate;
		
		ExpressionNode expressionNode = createSimpleExpression ( strExpression);
		if ( expressionNode==null ) {
			error = true;
			System.out.println ( "ERROR SimpleLogicMgr.createSimpleExpressionAndEvaluate returning 1");
			System.out.println ( "Input expression=" + strExpression );
			return(1);
		}
		iEvaluate =  expressionNode.evaluate(interviewId, iType, studyId, listOfAlters);
		if ( iEvaluate==Integer.MAX_VALUE ) {
			System.out.println ( "ERROR SimpleLogicMgr.createSimpleExpressionAndEvaluate returning 1");
			System.out.println ( "Problem in evaluating arithmetic expression");
			System.out.println ( "Input expression=" + strExpression );	
		}
		return (iEvaluate);
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
	    

	if ( strInput==null || strInput.length()==0 ) {
 		return(theList);
	}
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
	    		for ( String str:theList ) {
	    			System.out.println(str);
	    		}
	    		return(theList);
	    	}
	    	strNextComparisonOp = ExpressionNode.CompareOpStr[iNextCompareOp];
	    	theList.add(strNextComparisonOp);
	    	strInput = strInput.substring(strNextComparisonOp.length());
	    	strInput = strInput.trim();
	    }
	}
	if ( strInput.length()>0)
		theList.add(strInput.trim());
	
	// System.out.println ( "parseComparisionList");
	// for ( String str:theList ) {
	// 	System.out.println(str);
	// }
	
	return(theList);
	}	
	
	public static boolean hasError() {
		boolean bRetVal = error;
		
		error = false;
		return(bRetVal);
	}
}
