package net.sf.egonet.web.panel;

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class MapEditorPanel<K,V> extends Panel {

	protected TreeMap<K,V> map;
	
	private PanelContainer pairEditorContainer;
	private String heading, subEditorHeading;
	private ArrayList<K> keys;
	private ArrayList<V> valueOptions;

	public MapEditorPanel(String id, String heading, String subEditorHeading, TreeMap<K,V> map,
			ArrayList<K> keys, ArrayList<V> valueOptions) 
	{
		super(id);
		this.map = map;
		this.heading = heading;
		this.subEditorHeading = subEditorHeading;
		this.keys = keys;
		this.valueOptions = valueOptions;
		build();
	}
	
	///////////////////////////////////////////////
	// Expect to override these three methods.   //
	///////////////////////////////////////////////
	protected String showKey(K key) {
		return key+"";
	}
	protected String showValue(V value) {
		return value == null ? "" : value+"";
	}
	protected void mapChanged() {
		
	}
	///////////////////////////////////////////////
	
	private void build() {

		add(new Label("heading",heading));
		
		add(new ListView("pairs", new PropertyModel(this,"keys"))
		{
			public void populateItem(ListItem item) {
				final K key = (K) item.getModelObject();
				Link pairEditLink = new Link("pairEditLink") {
					public void onClick() {
						editPair(key);
					}
				};
				pairEditLink.add(new Label("key",showKey(key)));
				pairEditLink.add(new Label("value",showValue(map.get(key))));
				item.add(pairEditLink);
			}
		});
		pairEditorContainer = new PanelContainer("pairEditorContainer");
		add(pairEditorContainer);
	}
	private void editPair(final K key) {
		pairEditorContainer.changePanel(
				new SingleSelectionPanel<V>("panel", 
						subEditorHeading.replaceAll("\\$\\$", showKey(key)), 
						valueOptions) {
			public void action(V newValue) {
				map.put(key, newValue);
				MapEditorPanel.this.mapChanged();
			}
			public String show(V value) {
				return showValue(value);
			}
		});
	}
	public ArrayList<K> getKeys() {
		return keys;
	}
}
