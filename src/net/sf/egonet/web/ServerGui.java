package net.sf.egonet.web;

import java.awt.Desktop;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;

import javax.swing.JFrame;

import net.sf.egonet.persistence.DB;

import org.mortbay.jetty.Server;

public class ServerGui extends JFrame {

	private Server server;
	
	public ServerGui() throws Exception {
		super("Server started at http://127.0.0.1:8080 - Close this window to shutdown the server.");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		server = Main.createAndConfigureServer();
		server.start();
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				try {
					server.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		DB.migrate();
		Desktop.getDesktop().browse(new URI("http://127.0.0.1:8080"));
		
		setSize(800,150);
		setVisible(true);
	}
	
	public static void main(String[] args) throws Exception {
		new ServerGui();
	}
}
