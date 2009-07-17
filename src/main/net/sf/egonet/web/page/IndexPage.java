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
	}
}
