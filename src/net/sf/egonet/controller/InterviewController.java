/*
 * InterviewController.java
 *
 * Created on March 4, 2010, 10:00 AM
 */

package net.sf.egonet.controller;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.EventListenerList;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Study;
import net.sf.egonet.persistence.Answers;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.persistence.Options;
import net.sf.egonet.persistence.Questions;
import net.sf.egonet.persistence.Studies;
import net.sf.egonet.web.Main;
import org.mortbay.jetty.Server;

/**
 * InterviewController is a Singleton class that allows third party clients to
 * control the operation of the Egonet software.  In this model, clients see
 * the Egonet software as a library with a set of methods it can call in order
 * to control Egonet's execution.  This class is the provider of these methods.
 *
 * More specifically, while Egonet provides several different functions,
 * including study authoring, data analysis and data import and export, this
 * class focuses only on Egonet's interviewing capability.  It allows client
 * software to control the way Egonet provides its interviewing functionality
 * to interviewers.  Using InterviewController, client software directs Egonet
 * to start its server and to keep the server session alive while an arbitrary
 * number of studies are sequentially selected for execution.  During the
 * execution of a study, an arbitrary number of interviews are sequentially
 * selected for execution.  Once the client software has finished with all
 * studies and their interviews, it instructs Egonet to stop its server.  This
 * concept is illustrated in the diagram below, with each indented portion of
 * the diagram indicating an inner loop that can be instructed by the client
 * software to execute an arbitrary number of times.
 *
 * startServer()
 *      beginStudy()
 *          beginInterview()
 *          endInterview()
 *      endStudy()
 * stopServer()
 *
 * InterviewController also provides a method for client software to retrieve
 * responses to any of the questions defined in the active study.  In addition,
 * InterviewController will, if the client software requests it, notify the
 * client when it detects user activity and when an interview has been ended.
 *
 * All of these control functions are performed by the client software without
 * any user intervention.  The interviewer is presented with the interview
 * starting page displayed in his/her browser for the interview and study
 * selected by the client software.  When the interviewer is finished with the
 * interview, control is automatically transfered back to the client software.
 * When the interviewer has finished with the client software session, the
 * client software instructs Egonet to shut down.
 *
 * When InterviewController is not used, Egonet operates in its traditional
 * manner.
 * 
 * @author Matt Futterman
 */
public class InterviewController implements EgonetMonitor
{
    private static InterviewController     interviewController;
    private static DBSessionFactoryManager dbSessionFactoryManager;
    private static EventListenerList       listenerList;
	private static Server                  server;
    private static long                    dbStudyID;
    private static long                    dbInterviewID;
    private static String                  studyName;
    private static String                  clientCaseID;
    private static Question                caseIDQuestion;
    private static Question                completionStatusFlagQuestion;
    private static Question                everTouchedStatusFlagQuestion;
    private static List<Question>          questionList;
    private static Map<String,String>      preloadMap;

    private boolean ensureIntrinsicFlagsExist;
    private boolean completionStatusFlagEnabled;
    private boolean everTouchedStatusFlagEnabled;
    private boolean preloadsHaveBeenLoaded;

    private enum AdminQuestions
    {
        caseID( 0 ), completed( 1 ), everTouched( 2 );

        private final int position;

        private AdminQuestions( int position )
        {
            this.position = position;
        }

        private int getPosition()
        {
            return position;
        }
    }

    /**
     * This constructor creates the Singleton instance of this class.
     */
	private InterviewController()
    {
        dbSessionFactoryManager = DBSessionFactoryManager.getInstance();
        listenerList = new EventListenerList();
        Main.setUsingDBSessionFactoryManager( true );
        Main.setHomePage( StartInterviewPage.class );
        Main.registerEgonetMonitor( this );
		server = Main.createAndConfigureServer();
    }

    /**
     * This method returns the singleton instance of InterviewController.  The
     * instance will be created the first time this method is called.
     */
    public static InterviewController getInstance()
    {
        if (interviewController == null)
        {
            interviewController = new InterviewController();
        }
        return interviewController;
    }

    /**
     * Returns whether or not the use of an instrinsic completion status flag
     * is enabled.  This behavior is specified on a per-study basis.
     */
    public boolean isCompletionStatusFlagEnabled( boolean enable )
    {
        return completionStatusFlagEnabled;
    }

    /**
     * Returns whether or not the use of an instrinsic everTouched status flag
     * is enabled.  This behavior is specified on a per-study basis.
     */
    public boolean isEverTouchedStatusFlagEnabled( boolean enable )
    {
        return everTouchedStatusFlagEnabled;
    }

    /**
     * Adds the given InterviewControllerListener to the InterviewController's
     * list of listeners.
     */
    public void addInterviewControllerListener( InterviewControllerListener l )
    {
        listenerList.add( InterviewControllerListener.class, l );
    }

    /**
     * Removes the given InterviewControllerListener from the InterviewController's
     * list of listeners.
     */
    public void removeInterviewControllerListener( InterviewControllerListener l )
    {
        listenerList.remove( InterviewControllerListener.class, l );
    }

    /**
     * Returns whether or not the Egonet server is running.
     */
    public boolean isServerRunning()
    {
        return server.isRunning();
    }

    /**
     * Returns whether or not an Egonet study is active.
     */
    public boolean isStudyActive()
    {
        return dbStudyID != 0;
    }

    private void setStudyInactive()
    {
        dbStudyID = 0;
        studyName = null;
        questionList = null;
    }

    /**
     * Returns whether or not an Egonet interview is active.
     */
    public boolean isInterviewActive()
    {
        return dbInterviewID != 0;
    }

    private void setInterviewInactive()
    {
        dbInterviewID = 0;
    }

    /**
     * Starts the Egonet server if it is not already running.
     *
     * Throws an IllegalStateException if the Egonet server is already running.
     * 
     * @throws Exception
     */
    public void startServer() throws Exception
    {
        if (server.isRunning())
        {
            throw new IllegalStateException( "The Egonet server is already running." );
        }
		server.start();
    }

    /**
     * Stops the Egonet server if there is no active interview and no active
     * study.
     *
     * Throws an IllegalStateException if an interview is active or if a study
     * is active.
     *
     * @throws Exception
     */
    public void stopServer() throws Exception
    {
        if (isInterviewActive())
        {
            throw new IllegalStateException( "Cannot stop Egonet server. An interview is active." );
        }
        if (isStudyActive())
        {
            throw new IllegalStateException( "Cannot stop Egonet server. A study is active. Call endStudy()." );
        }
        server.stop();
    }

    /**
     * Begins an Egonet study named 'studyName' if there is no active interview
     * and no active study and the server is running.  Uses 'dbConfigFilePath'
     * to retrieve the configuration file for the database associated with the
     * study.
     *
     * Throws an IllegalStateException if an interview or study is active or
     * if the server is not running.
     *
     * Throws an Exception if no Egonet study exists for 'studyName'.
     *
     * @param studyName name of an Egonet study
     * @param dbConfigFilePath configuration file for database associated with study
     * @throws Exception
     */
    public void beginStudy( String studyName, String dbConfigFilePath ) throws Exception
    {
        beginStudy( studyName, dbConfigFilePath, false );
    }

    /**
     * Begins an Egonet study named 'studyName' if there is no active interview
     * and no active study and the server is running.  Uses 'dbConfigFilePath'
     * to retrieve the configuration file for the database associated with the
     * study.
     *
     * Throws an IllegalStateException if an interview or study is active or
     * if the server is not running.
     *
     * Throws an Exception if no Egonet study exists for 'studyName'.
     *
     * @param studyName name of an Egonet study
     * @param dbConfigFilePath configuration file for database associated with study
     * @param ensureIntrinsicFlagsExist whether or not the intrinsic flags must exist
     * @throws Exception
     */
    public void beginStudy( String studyName, String dbConfigFilePath,
                            boolean ensureIntrinsicFlagsExist ) throws Exception
    {
        if (isInterviewActive())
        {
            throw new IllegalStateException( "An interview is active. Call endInterview()." );
        }
        if (isStudyActive())
        {
            throw new IllegalStateException( "A study is already active. Call endStudy()." );
        }
        if (!server.isRunning())
        {
            throw new IllegalStateException( "The Egonet server is not running. Call startServer()." );
        }
        this.ensureIntrinsicFlagsExist = ensureIntrinsicFlagsExist;
        dbSessionFactoryManager.useSessionFactory( studyName, dbConfigFilePath );
        Study study = null;
        List<Study> studyList = Studies.getStudies();
        Iterator<Study> studies = studyList.iterator();
        while( studies.hasNext() )
        {
            Study thisStudy = studies.next();
            if (thisStudy.getName().equals( studyName ))
            {
                study = thisStudy;
                dbStudyID = thisStudy.getId();
                break;
            }
        }
        if (study == null)
        {
            stopServer();
            throw new Exception( "Unable to find study '" + studyName + "'." );
        }
        questionList = Questions.getQuestionsForStudy( dbStudyID, null );
        this.studyName = studyName;
    }

    /**
     * Ends (makes inactive) the currently active Egonet study.
     *
     * Throws an IllegalStateException if an interview is active or if no study
     * is active.
     * 
     * @throws Exception
     */
    public void endStudy() throws Exception
    {
        if (isInterviewActive())
        {
            throw new IllegalStateException( "An interview is active. Call endInterview()." );
        }
        if (isStudyActive())
        {
            setStudyInactive();
        }
        else
        {
            throw new IllegalStateException( "No study is currently active. Call beginStudy()." );
        }
    }

    /**
     * Begins an Egonet interview that is, or will be, associated with the given
     * client's case ID.  The client's case ID is stored as the interview's
     * response for the question denoted as being of type 'EGO_ID'.  An
     * interview is determined to be already in existance if the study database
     * contains an interview for which that interview's EGO_ID question
     * response matches 'clientCaseID'.  Otherwise, a new interview is created
     * with its EGO_ID question response containing the value of 'clientCaseID'.
     *
     * Implementation note: This method sets the state for a subsequent call to
     * method getInterviewID(), which is called from the StartInterviewPage
     * class at the point where the interview's HTML entry point is launched in
     * the user's browser.  Specifically, if this method finds an existing
     * interview for 'clientCaseID', class variable 'dbInterviewID' will
     * have a non-zero value, otherwise it will be zero, indicating that a new
     * interview must be created.  See method getInterviewID() in this class.
     *
     * Throws an IllegalStateException if an interview is already active, if no
     * study is active, or if the server is not running.
     *
     * @param clientCaseID client case ID (to be) associated with Egonet interview
     *
     * @throws Exception
     */
    public void beginInterview( String clientCaseID ) throws Exception
    {
        beginInterview( clientCaseID, null );
    }

    /**
     * Begins an Egonet interview that is, or will be, associated with the given
     * client's case ID.  The client's case ID is stored as the interview's
     * response for the question denoted as being of type 'EGO_ID'.  An
     * interview is determined to be already in existance if the study database
     * contains an interview for which that interview's EGO_ID question
     * response matches 'clientCaseID'.  Otherwise, a new interview is created
     * with its EGO_ID question response containing the value of 'clientCaseID'.
     *
     * Implementation note: This method sets the state for a subsequent call to
     * method getInterviewID(), which is called from the StartInterviewPage
     * class at the point where the interview's HTML entry point is launched in
     * the user's browser.  Specifically, if this method finds an existing
     * interview for 'clientCaseID', class variable 'dbInterviewID' will
     * have a non-zero value, otherwise it will be zero, indicating that a new
     * interview must be created.  See method getInterviewID() in this class.
     *
     * Throws an IllegalStateException if an interview is already active, if no
     * study is active, or if the server is not running.
     *
     * @param clientCaseID client case ID (to be) associated with Egonet interview
     * @param preloadMap a Map with keys that are Egoweb question names and values
     *        that are responses for the associated questions
     *
     * @throws Exception
     */
    public void beginInterview( String clientCaseID, Map<String,String> preloadMap ) throws Exception
    {
        if (isInterviewActive())
        {
            throw new IllegalStateException( "An interview is already active. Call endInterview()." );
        }
        if (!isStudyActive())
        {
            throw new IllegalStateException( "No study is currently active. Call beginStudy()." );
        }
        if (!server.isRunning())
        {
            throw new IllegalStateException( "The Egonet server is not running. Call startServer()." );
        }
        this.clientCaseID = clientCaseID;
        this.preloadMap = preloadMap;
        preloadsHaveBeenLoaded = false;
        loadAdminQuestions();
        long questionID = caseIDQuestion.getId();
        List<Answer> answerList = Answers.getAnswersForQuestion( new Long( questionID ) );
        Iterator<Answer> answers = answerList.iterator();
        while ( answers.hasNext() )
        {
            Answer thisAnswer = answers.next();
            String value = thisAnswer.getValue();
            if (value != null)
            {
                if (value.equals( clientCaseID ))
                {
                    // The interview already exists.
                    dbInterviewID = thisAnswer.getInterviewId();
                    break;
                }
            }
        }
        Desktop.getDesktop().browse( new URI( "http://127.0.0.1:8080" ) );
    }

    private void loadPreloadQuestions( Map<String,String> preloadMap ) throws Exception
    {
        StringBuffer errorBuf = new StringBuffer();
        Set<Map.Entry<String,String>> entrySet = preloadMap.entrySet();
        for ( Map.Entry<String,String> entry : entrySet )
        {
            String varName = entry.getKey();
            String value = entry.getValue();
            Question question = Questions.getQuestionUsingTitleAndTypeAndStudy( varName,
                                                                                Question.QuestionType.EGO_ID,
                                                                                dbStudyID );
            if (question == null)
            {
                String s = "Question '" + varName + "' not found in Egoweb database.";
                errorBuf.append( "\n" );
                errorBuf.append( s );
            }
            else
            {
                try
                {
                    Answers.setAnswerForInterviewAndQuestion( dbInterviewID, question,
                                                              value, "", Answer.SkipReason.NONE );
                }
                catch ( Exception e )
                {
                    String s = "Could not store response for question '" + varName + "' in Egoweb database.";
                    errorBuf.append( "\n" );
                    errorBuf.append( s );
                }
            }
        }
        if (errorBuf.length() != 0)
        {
            throw new RuntimeException( errorBuf.toString() );
        }
    }

    private void loadAdminQuestions() throws Exception
    {
        List<Question> localQuestionList = Questions.getQuestionsForStudy( dbStudyID, Question.QuestionType.EGO_ID );
        try
        {
            caseIDQuestion = localQuestionList.get( AdminQuestions.caseID.getPosition() );
        }
        catch( Exception e )
        {
            String s = "Egoweb 'case ID' question not found.";
            throw new RuntimeException( s, e );
        }
        StringBuffer errorBuf = new StringBuffer();
        try
        {
            completionStatusFlagQuestion = localQuestionList.get( AdminQuestions.completed.getPosition() );
            completionStatusFlagEnabled = true;
        }
        catch( Exception e )
        {
            if (ensureIntrinsicFlagsExist)
            {
                String s = "Egoweb 'completion' status flag question not found.";
                errorBuf.append( "\n" );
                errorBuf.append( s );
            }
        }
        try
        {
            everTouchedStatusFlagQuestion = localQuestionList.get( AdminQuestions.everTouched.getPosition() );
            everTouchedStatusFlagEnabled = true;
        }
        catch( Exception e )
        {
            if (ensureIntrinsicFlagsExist)
            {
                String s = "Egoweb 'everTouched' status flag question not found.";
                errorBuf.append( "\n" );
                errorBuf.append( s );
            }
        }
        if (errorBuf.length() != 0)
        {
            throw new RuntimeException( errorBuf.toString() );
        }
    }

    /**
     * Ends (makes inactive) the currently active Egonet interview.
     * Note that in most cases this method will be called internally when
     * Egonet detects that the user has completed an interview.  However it
     * may also be called by the client when (for instance) a period of
     * inactivity has elapsed or there is some other reason that requires that
     * the interview be terminated programmatically.
     *
     * Throws an IllegalStateException if no study or interview is active.
     *
     * @throws Exception
     */
    public void endInterview() throws Exception
    {
        if (!isStudyActive())
        {
            throw new IllegalStateException( "No study is currently active. Call beginStudy()." );
        }
        if (!isInterviewActive())
        {
            throw new IllegalStateException( "No interview is currently active. Call beginInterview()." );
        }
        endInterview_work();
    }

    private void endInterview_work()
    {
        String caseID = clientCaseID;
        clientCaseID = null;
        fireInterviewHasEnded( studyName, caseID );
        setInterviewInactive();
    }

    /**
     * This method is called internally by Egonet (specifically, from class
     * StartInterviewPage) at the time where an interview's first page must be
     * displayed in the user's browser.  The value of class variable 
     * 'dbInterviewID' is used either to retrieve an existing interview (see the
     * comments for method beginInterview() in this class), or to
     * contain the database interview ID for a newly-created interview.  This
     * method returns the database ID for the interview in either case.
     *
     * @return the database ID for the active interview
     */
    long getInterviewID()
    {
        Interview interview = null;
        try
        {
            interview = Interviews.getInterview( dbInterviewID );
        }
        catch ( RuntimeException e )
        {
        }
        if (interview == null)
        {
            ArrayList<Answer> answers = new ArrayList<Answer>();
            interview = Interviewing.findOrCreateMatchingInterviewForStudy( dbStudyID, answers );
            dbInterviewID = interview.getId();
            Answers.setAnswerForInterviewAndQuestion( dbInterviewID, caseIDQuestion,
                                                      clientCaseID, "", Answer.SkipReason.NONE );
        }
        return dbInterviewID;
    }

    /**
     * This method returns a response for 'questionName' (i.e. an Egonet
     * question "title") for the currently active interview.  If there is no
     * response for 'questionName' an empty String is returned.
     *
     * Throws an IllegalStateException if there is no active study.
     * Throws an Exception if 'questionName' is not a recognized question title
     * in the active Egonet study.
     * 
     * @param questionName Egonet question title
     * @return response for 'questionName' or empty String if no response exists
     * @throws Exception
     */
    public String getQuestionResponse( String questionName ) throws Exception
    {
        if (questionList == null)
        {
            throw new IllegalStateException( "Question list has not been populated. Call beginStudy()." );
        }
        Question question = null;
        Iterator<Question> questions = questionList.iterator();
        while ( questions.hasNext() )
        {
            Question thisQuestion = questions.next();
            if (thisQuestion.getTitle().equals( questionName ))
            {
                question = thisQuestion;
                break;
            }
        }
        if (question == null)
        {
            throw new Exception( "Question named '" + questionName + "' not " +
                                 "found in study '" + studyName + "'." );
        }
        String response = "";
        Answer answer = Answers.getAnswerForInterviewAndQuestion( dbInterviewID, question );
        if (answer != null) // CASE-3225
        {
            response = answer.getValue();
            if (question.getAnswerType() == Answer.AnswerType.SELECTION
                || question.getAnswerType() == Answer.AnswerType.MULTIPLE_SELECTION) // CASE-3215
            {
                String selectionResponse = null;
// CASE-3243    long answerID = answer.getId().longValue();
                List<QuestionOption> optionList = Options.getOptionsForQuestion( question.getId() );
                for ( QuestionOption option : optionList )
                {
                    long optionID = option.getId().longValue();
                    String optionIDString = new Long( optionID ).toString(); // CASE-3243
// CASE-3243        if (optionID == answerID)
                    if (optionIDString.equals( response )) // CASE-3243
                    {
                        selectionResponse = option.getValue();
                        break;
                    }
                }
                if (selectionResponse == null)
                {
                    String s = "No selection value found for answer to question '" +
                               question.getTitle() + "'.";
                    selectionResponse = ""; // CASE-3238
                }
                response = selectionResponse;
            }
        }
        return response;
    }

    private void fireInterviewActivityOccurred( String studyName, String caseID )
    {
        InterviewControllerEvent e = new InterviewControllerEvent( this, studyName, caseID );

	    // Guaranteed to return a non-null array.
    	Object[] listeners = listenerList.getListenerList();

	    // Process the listeners last to first, notifying those that are
        // interested in this event.
	    for (int i = listeners.length-2; i >= 0; i -= 2)
	    {
    	    if (listeners[i] == InterviewControllerListener.class)
    	    {
                if (listeners[i+1] instanceof InterviewControllerListener)
                {
                    InterviewControllerListener l;
                    l = (InterviewControllerListener)listeners[i+1];
                    l.interviewActivityOccurred( e );
                }
	        }
    	}
    }

    private void fireInterviewHasEnded( String studyName, String caseID )
    {
        InterviewControllerEvent e = new InterviewControllerEvent( this, studyName, caseID );

	    // Guaranteed to return a non-null array.
    	Object[] listeners = listenerList.getListenerList();

	    // Process the listeners last to first, notifying those that are
        // interested in this event.
	    for (int i = listeners.length-2; i >= 0; i -= 2)
	    {
    	    if (listeners[i] == InterviewControllerListener.class)
    	    {
                if (listeners[i+1] instanceof InterviewControllerListener)
                {
                    InterviewControllerListener l;
                    l = (InterviewControllerListener)listeners[i+1];
                    l.interviewHasEnded( e );
                }
	        }
    	}
    }

    /**
     * Indicates that a user has performed an Egonet action.
     */
    public void userActivityOccurred()
    {
        if (everTouchedStatusFlagEnabled)
        {
            try
            {
                setIntrinsicFlag( everTouchedStatusFlagQuestion ); // CASE-3219
                if (!preloadsHaveBeenLoaded && preloadMap != null) // CASE-3228
                {
                    try
                    {
                        loadPreloadQuestions( preloadMap ); // CASE-3220
                    }
                    catch( Exception e )
                    {
                        throw new RuntimeException( e );
                    }
                    preloadsHaveBeenLoaded = true;
                }
            }
            catch( RuntimeException e )
            {
                e.toString(); // An exception occurs the very first time an interview is touched
            }
        }
        if (isInterviewActive())
        {
            fireInterviewActivityOccurred( studyName, clientCaseID );
        }
    }

    /**
     * Indicates that an interviewer has finished an interview.
     */
    public void interviewHasEnded()
    {
        if (completionStatusFlagEnabled)
        {
            setIntrinsicFlag( completionStatusFlagQuestion ); // CASE-3219
        }
        endInterview_work();
    }

    private void setIntrinsicFlag( Question question ) // CASE-3219
    {
        Answers.setAnswerForInterviewAndQuestion( dbInterviewID, question,
                                                  "1", "", Answer.SkipReason.NONE );
    }

// --------------------------------- main -----------------------------------

	public static void main( String[] args ) throws Exception
    {
        String testStudyName = "EgoWebSample";
//      String dbConfigFilePath = "C:/Studies/cases6.0/server1/ChrisTest1/_vn_/Egonet/_client_/hibernate.cfg.xml";
        String dbConfigFilePath = "C:/Studies/cases6.0/server1/EgoWebSample/_vn_/Egonet/_server_/hibernate.cfg.xml";
		InterviewController controller = InterviewController.getInstance();
        controller.startServer();
        controller.beginStudy( testStudyName, dbConfigFilePath, false );
        controller.beginInterview( "007", getTestPreloadsMap() );
//      String response = controller.getQuestionResponse( "ego_ques_1" );
//      response.toString();
//      controller.endInterview();
//      controller.stopServer();
	}

    private static Map<String,String> getTestPreloadsMap()
    {
        Map<String,String> preloads = new HashMap<String,String>();
        preloads.put( "site", "1" );
        preloads.put( "touched", "1" );
        return preloads;
    }
}
