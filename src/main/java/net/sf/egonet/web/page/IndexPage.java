package net.sf.egonet.web.page;

import net.sf.egonet.persistence.DB;

import org.apache.wicket.markup.html.link.Link;

public class IndexPage extends EgonetPage
{
	public IndexPage()
	{
		DB.migrate();
		add(
	            new Link("authoringLink")
	            {
	                public void onClick()
	                {
	                    setResponsePage(new AuthoringPage());
	                }
	            }
	        );
		add(
	            new Link("interviewingLink")
	            {
	                public void onClick()
	                {
	                    setResponsePage(new InterviewingPage());
	                }
	            }
	        );
		add(
	            new Link("analysisLink")
	            {
	                public void onClick()
	                {
	                    // setResponsePage(new AnalysisPage());
	                }
	            }
	        );
	}
}
