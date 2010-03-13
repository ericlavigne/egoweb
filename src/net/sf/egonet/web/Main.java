package net.sf.egonet.web;

import java.io.IOException;
import java.net.URL;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketServlet;

import net.sf.egonet.controller.EgonetMonitor;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.web.page.IndexPage;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.mortbay.jetty.Server;

//import org.mortbay.jetty.handler.ResourceHandler;

import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

import org.mortbay.resource.Resource;

public class Main extends WebApplication
{
	private static SessionFactory dbSessionFactory;
    private static Class<?>       homePage;
    private static EgonetMonitor  egonetMonitor;
    private static boolean        usingDBSessionFactoryManager;

	public Main()
	{
        if (this.homePage == null)
        {
            this.homePage = IndexPage.class;
        }
        if (!usingDBSessionFactoryManager) { // See setUsingDBSessionFactoryManager()
        	try {
        		dbSessionFactory = new Configuration().configure().buildSessionFactory();
        	} catch (Throwable ex) {
        		System.err.println("Caught an exception while trying to initialize hibernate session factory: "+ex);
        	}
        }
	}

	protected void onDestroy()
    {
		dbSessionFactory.close();
	}

	public static SessionFactory getDBSessionFactory()
    {
		return dbSessionFactory;
	}

    /**
     * Call this method when Egonet must switch context among an arbitrary
     * number of studies while maintaining each study's database connectivity.
     * See, for example, method useSessionFactory() in class
     * DBSessionFactoryManager, which is used by the InterviewController class.
     * 
     * @param dbSessionFactory SessionFactory instance associated with a study
     */
    public static void setDBSessionFactory( SessionFactory dbSessionFactory )
    {
        Main.dbSessionFactory = dbSessionFactory;
    }
	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<?> getHomePage()
    {
		return homePage;
	}

    /**
     * This method allows a client to specify an alternate home page.  It must
     * be called before the constructor for this class is called.
     *
     * @param homePage the HTML page that will be displayed when app starts up
     */
    public static void setHomePage( Class<?> homePage )
    {
        Main.homePage = homePage;
    }

	protected String applicationClassName = "net.sf.egonet.web.Main";
	
	public static Server createAndConfigureServer() {
		return createAndConfigureServer("net.sf.egonet.web.Main",8080,true);
	}
	
	public static Server createAndConfigureServer(String applicationClassName, Integer port, Boolean localOnly) {

        Server server = new Server(port); // TODO: Restrict access to localhost: 0.0.0.0 -> 192.168.1.1?

		/*
		Context staticContext = new Context(server,"/static",0);
		staticContext.setHandler(new ResourceHandler()); // maybe extend resource handler to pull from classpath
		staticContext.setResourceBase("./static/");
		staticContext.setContextPath("/static");
		*/

		Context embeddedContext = new Context(server,"/static",0);
		ServletHolder embeddedHolder = new ServletHolder(new StaticContentFromClasspathServlet());
		embeddedContext.addServlet(embeddedHolder, "/*");

		Context context = new Context(server, "/", Context.SESSIONS);
		if(localOnly) {
			context.setVirtualHosts(new String[]{"127.0.0.1"});
		}
		ServletHolder servletHolder = new ServletHolder(new WicketServlet());
		servletHolder.setInitParameter("applicationClassName",
				applicationClassName);
		servletHolder.setInitOrder(1);
		context.addServlet(servletHolder, "/*");
		
		return server;
	}
	
	// Mostly copied from http://www.codecommit.com/blog/java/so-long-wtp-embedded-jetty-for-me
	public static void main(String[] args) throws Exception {

		Server server = createAndConfigureServer();
		
		server.start();
		DB.migrate();
		server.join();
	}

	public static class StaticContentFromClasspathServlet extends DefaultServlet
    {
		public Resource getResource(String pathInContext) {
			try {
				URL url = getResourceFromClasspath(pathInContext);
				return Resource.newResource(url);
			} catch(IOException ex) {
				System.err.println("Caught an IOException: "+ex);
			}
			return null;
		}

		public static URL getResourceFromClasspath(String name) {
			if (name == null || name.length()==0) {
				return null;
            }

			return StaticContentFromClasspathServlet.class.getResource(name);
		}
	}

    /**
     * Call this method when database session factories for more than one
     * study must be maintained.  Do not call this method when traditional
     * Egonet behavior is desired (or call it with an argument of 'false').
     *
     * @param usingManager whether or not multiple studies must be managed in a single Egonet session
     */
    public static void setUsingDBSessionFactoryManager( boolean usingManager )
    {
        usingDBSessionFactoryManager = usingManager;
    }

    /**
     * Call this method when there is a single EgonetMonitor implementor that
     * wants to be notified by this class of certain events.
     *
     * Note: If this kind of event notification is generally useful, the more
     *       robust event listener implementation should be used.
     *
     * @param egonetMonitor an instance of EgonetMonitor
     */
    public static void registerEgonetMonitor( EgonetMonitor egonetMonitor )
    {
        Main.egonetMonitor = egonetMonitor;
    }

    /**
     * Call this method to indicate that the user has performed some action,
     * such as answering a question or navigating to another screen.
     */
    public static void userActivityOccurred()
    {
        if (egonetMonitor != null)
        {
            egonetMonitor.userActivityOccurred();
        }
    }

    /**
     * Call this method to indicate that the interview has ended the current
     * interview.
     */
    public static void interviewHasEnded()
    {
        if (egonetMonitor != null)
        {
            egonetMonitor.interviewHasEnded();
        }
    }
}

