package net.sf.egonet.web;

import org.mortbay.jetty.Server;

import net.sf.egonet.persistence.DB;
import net.sf.egonet.web.page.WebSurveyIndexPage;

public class WebSurveyMain extends Main {
	@Override
	public Class<?> getHomePage()
    {
		return WebSurveyIndexPage.class;
	}
	
	public static void main(String[] args) throws Exception {

		Server server = createAndConfigureServer("net.sf.egonet.web.WebSurveyMain",8639);
		
		server.start();
		DB.migrate();
		server.join();
	}
}
