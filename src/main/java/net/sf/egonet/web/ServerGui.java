package net.sf.egonet.web;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.mortbay.jetty.Server;

public class ServerGui extends JFrame {

	private Server server;
	
	public ServerGui() throws Exception {
		super("Server started at http://localhost:8080 - Close this window to shutdown the server.");
		
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
		
		setSize(500,500);
		setVisible(true);
	}
	
	public static void main(String[] args) throws Exception {
		new ServerGui();
	}
}
