package net.sf.egonet.web;

import net.sf.egonet.persistence.DB;

import org.mortbay.jetty.Server;

// Start both a web server and a Clojure prompt to allow 
// interactive evaluation of internal Egoweb components.

public class Prompt {
	public static void main(String[] args) throws Exception {
		Server server = null;
		try {
			server = Main.createAndConfigureServer();
			server.start();
			DB.migrate();
			clojure.main.main(new String[]{});
		} catch(Exception ex) {
			
		} finally {
			if(server != null) {
				server.stop();
			}
		}
	}
}
