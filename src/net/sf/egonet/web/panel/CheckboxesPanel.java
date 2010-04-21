package net.sf.egonet.web.panel;

import java.io.Serializable;
import java.util.List;

import net.sf.egonet.web.component.FocusOnLoadBehavior;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;

public class CheckboxesPanel<T> extends Panel {

	protected String showItem(T item) {
		return item.toString();
	}
	
	private List<CheckableWrapper> items;
	
	public List<T> getSelected() {
		List<T> result = Lists.newArrayList();
		for(CheckableWrapper wrapper : items) {
			if(wrapper.getSelected()) {
				result.add(wrapper.getItem());
			}
		}
		return result;
	}
	
	public CheckboxesPanel(String id, List<T> items, List<T> selected) {
		super(id);
		
		this.items = Lists.newArrayList();
		for(T item : items) {
			this.items.add(new CheckableWrapper(item).setSelected(selected.contains(item)));
		}
		build();
	}
	
	private Boolean autoFocus = false;
	
	private void build() {
		ListView checkboxes = new ListView("checkboxes",items) {
			protected void populateItem(ListItem item) {
				CheckableWrapper wrapper = (CheckableWrapper) item.getModelObject();
				item.add(new Label("checkLabel",wrapper.getName()));
				CheckBox checkBox = new CheckBox("checkField", new PropertyModel(wrapper, "selected"));
				if(autoFocus) {
					if(wrapper.getName() != null && items.get(0).getName() != null &&
							wrapper.getName().equals(items.get(0).getName()))
					{
						checkBox.add(new FocusOnLoadBehavior());
					}
				}
				item.add(checkBox);
			}
		};
		checkboxes.setReuseItems(true);
		add(checkboxes);
	}
	
	public class CheckableWrapper implements Serializable {
		private T item;
		private Boolean selected;
		
		public CheckableWrapper(T item) {
			this.item = item;
			this.selected = false;
		}
		public CheckableWrapper setSelected(Boolean selected) {
			this.selected = selected;
			return this;
		}
		public Boolean getSelected() {
			return selected;
		}
		public T getItem() {
			return item;
		}
		public String getName() {
			return showItem(item);
		}
	}
	
	public void setAutoFocus() {
		this.autoFocus = true;
		
	}

}
