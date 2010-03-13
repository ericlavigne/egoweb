/*
 * InterviewControllerListener.java
 *
 * Created on March 4, 2010, 10:00 AM
 */

package net.sf.egonet.controller;

import java.util.EventListener;

/**
 * An InterviewControllerListener receives event notifications of interest
 * from the InterviewController.
 * 
 * @author Matt Futterman
 */
public interface InterviewControllerListener extends EventListener
{
    /**
     * Indicates that the InterviewController associated with the given
     * InterviewControllerEvent has detected that an interviewer has
     * performed an action on an interview.
     */
    public void interviewActivityOccurred( InterviewControllerEvent e );

    /**
     * Indicates that the InterviewController associated with the given
     * InterviewControllerEvent has detected that an interviewer has
     * finished an interview.
     */
    public void interviewHasEnded( InterviewControllerEvent e );
}
