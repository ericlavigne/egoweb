/*
 * StartInterviewPage.java
 *
 * Created on March 4, 2010, 10:00 AM
 */

package net.sf.egonet.controller;

import net.sf.egonet.web.page.EgonetPage;
import net.sf.egonet.web.page.InterviewingEgoPage;

/**
 * The InterviewController uses this page to start or reenter an interview.
 * See Main.setHomePage().
 *
 * @author Matt Futterman
 */
public class StartInterviewPage extends EgonetPage
{
    /**
     * This page is used as the entry point when starting or resuming an
     * interview.  See Main.setHomePage();
     */
    public StartInterviewPage()
    {
        if (shouldStartInterview())
        {
            long interviewID = InterviewController.getInstance().getInterviewID();
            setResponsePage( InterviewingEgoPage.askNextUnanswered( interviewID, null, null ) );
        }
        else
        {
            // XXX To-do: Display a page that tells the user to go back to the
            // XXX        client (e.g. CASES) software.
        }
    }

    private boolean shouldStartInterview()
    {
        boolean shouldStart = true;
        // XXX To-do: Handle the case where an interview has been completed.
        return shouldStart;
    }
}
