package net.sf.egonet.web.page;

import java.awt.Color;
import java.util.ArrayList;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.collect.Lists;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.network.NetworkService;
import net.sf.egonet.persistence.Analysis;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.web.component.NetworkImage;
import net.sf.egonet.web.panel.SingleSelectionPanel;

public class NetworkVisualizationPage extends EgonetPage {
	
	private NetworkImage<Alter> networkImage;
	private Panel primaryPanel;
	
	private void replacePrimary(Panel newPanel) {
		primaryPanel.replaceWith(newPanel);
		primaryPanel = newPanel;
	}
	
	public NetworkVisualizationPage(Interview interview, Expression connectionReason) {
		
		super(Interviews.getEgoNameForInterview(interview.getId()));
		
		networkImage = new NetworkImage<Alter>("networkImage", 
				Analysis.getNetworkForInterview(interview, connectionReason));
		add(networkImage);
		
		primaryPanel = new EmptyPanel("primaryPanel");
		add(primaryPanel);

		add(new Link("layoutLink") {
			public void onClick() {
				ArrayList<NetworkService.LayoutOption> options = 
					Lists.newArrayList(NetworkService.LayoutOption.values());
				replacePrimary(
						new SingleSelectionPanel<NetworkService.LayoutOption>("primaryPanel",
								"Layout",options) 
						{
							public void action(NetworkService.LayoutOption option) {
								networkImage.setLayout(option);
								networkImage.refresh();
							}
						});
			}
		});
		
		add(new Link("backgroundLink") {
			public void onClick() {
				final ArrayList<Color> colors = 
					Lists.newArrayList(Color.WHITE,Color.RED,Color.ORANGE,
							Color.GREEN,Color.BLUE,Color.CYAN,Color.MAGENTA);
				final ArrayList<String> colorNames =
					Lists.newArrayList("White","Red","Orange",
							"Green","Blue","Cyan","Magenta");
				replacePrimary(
						new SingleSelectionPanel<Color>("primaryPanel",
								"Background",colors) 
						{
							public void action(Color option) {
								networkImage.setBackground(option);
								networkImage.refresh();
							}
							public String show(Color color) {
								for(int i = 0; i < colors.size(); i++) {
									if(color.equals(colors.get(i))) {
										return colorNames.get(i);
									}
								}
								return "Unknown";
							}
						});
			}
		});
	}
}
