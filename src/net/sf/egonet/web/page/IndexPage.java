package net.sf.egonet.web.page;

import org.apache.wicket.markup.html.link.Link;

public class IndexPage extends EgonetPage
{
	public IndexPage()
	{
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
	                    setResponsePage(new InterviewNewOrContinuePage());
	                }
	            }
	        );
		add(
	            new Link("analysisLink")
	            {
	                public void onClick()
	                {
	                    setResponsePage(new AnalysisPage());
	                }
	            }
	        );
		add(
	            new Link("importExportLink")
	            {
	                public void onClick()
	                {
	                    setResponsePage(new ImportExportPage());
	                }
	            }
	        );
	}
	
	public boolean isBookmarkable() {
		return false;
	}
}
