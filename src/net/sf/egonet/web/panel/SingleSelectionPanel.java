package net.sf.egonet.web.panel;

import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public abstract class SingleSelectionPanel<N> extends Panel {
	
	private ArrayList<N> options;
	
	public ArrayList<N> getOptions() {
		return options;
	}
	
	public abstract void action(N option);
	
	public SingleSelectionPanel(String id, String title, ArrayList<N> options) {
		super(id);
		this.options = options;
		
		add(new Label("title",title));
		
		add(new ListView("options", new PropertyModel(this,"options"))
		{
			public void populateItem(ListItem item) {
				final N option = (N) item.getModelObject();
				Link optionLink = new Link("optionLink") {
					public void onClick() {
						action(option);
					}
				};
				optionLink.add(new Label("optionName",option.toString()));
				item.add(optionLink);
			}
		});
	}
}
