/*
 * InterviewControllerEvent.java
 *
 * Created on March 4, 2010, 10:00 AM
 */

package net.sf.egonet.controller;

import java.util.EventObject;

/**
 * An InterviewControllerEvent is triggered whenever an InterviewController 
 * determines that an event of interest to InterviewControllerListeners has
 * occurred.
 *
 *
 * @author Matt Futterman
 */
public class InterviewControllerEvent extends EventObject
{
    private final String studyName;
    private final String caseID;

    /**
     * Constructs a InterviewControllerEvent with the given source, study name
     * and case ID.
     */
    InterviewControllerEvent( InterviewController source,
                              String studyName, String caseID )
    {
        super( source );
        this.studyName = studyName;
        this.caseID = caseID;
    }

    /**
     * Returns the name of the study associated with this InterviewControllerEvent.
     *
     * @return
     */
    public String getStudyName()
    {
        return studyName;
    }

    /**
     * Returns the case ID associated with this InterviewControllerEvent.
     *
     * @return
     */
    public String getCaseID()
    {
        return caseID;
    }
}
