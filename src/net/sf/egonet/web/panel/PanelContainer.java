package net.sf.egonet.web.panel;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

public class PanelContainer extends Panel {

	private Panel panel;
	private Boolean empty;

	public PanelContainer(String id) {
		super(id);
		build();
	}
	public PanelContainer(String id, Panel panel) {
		super(id);
		this.panel = panel;
		build();
	}
	private void build() {
		if(panel == null) {
			panel = new EmptyPanel("panel");
			empty = true;
		} else {
			empty = false;
		}
		add(panel);
	}
	public boolean isEmpty() {
		return empty;
	}
	public void removePanel() {
		changePanel(new EmptyPanel("panel"));
		empty = true;
	}
	public void changePanel(Panel panel) {
		this.panel.replaceWith(panel);
		this.panel = panel;
		empty = false;
	}
}
