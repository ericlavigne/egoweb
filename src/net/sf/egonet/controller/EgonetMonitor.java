/*
 * EgonetMonitor.java
 *
 * Created on March 4, 2010, 10:00 AM
 */

package net.sf.egonet.controller;

/**
 * An EgonetMonitor receives notifications related to Egonet actions and
 * activities.
 * 
 * @author Matt Futterman
 */
public interface EgonetMonitor
{
    /**
     * Indicates that a user has performed an Egonet action.
     */
    public void userActivityOccurred();

    /**
     * Indicates that an interviewer has finished an interview.
     */
    public void interviewHasEnded();
}
