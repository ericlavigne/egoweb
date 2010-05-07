package net.sf.egonet.web.panel;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sf.egonet.web.component.FocusOnLoadBehavior;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;

public class CheckboxesPanel<T> extends Panel {

	/**
	 * private inner class extending CheckBox.
	 * This implements IOnChangeListener and in effect
	 * listens to itself do it can make some GUI fields
	 * visible when 'Other' is selected
	 * @author Kevin
	 *
	 */
	private static boolean otherSelected;
	
	private class CheckBoxPlus extends CheckBox {
		
		public CheckBoxPlus ( String id, PropertyModel propertyModel ) {
			super ( id, propertyModel );
			if ( otherSpecifyStyle )
			    otherSelected = isOtherSelected();
		}
		
		protected void onSelectionChanged(Object newSelection) {
			boolean otherNowSelected = false;
	
			otherNowSelected = isOtherSelected();
			if ( otherNowSelected != otherSelected ) {
				fireActionEvent (otherNowSelected, "OTHER SPECIFY" );
				otherSelected = otherNowSelected;
			}
		}
		
		protected boolean wantOnSelectionChangedNotifications() { return (otherSpecifyStyle);}	
		
		/**
		 * this checks the list of checkbox items, looking for one named 'Other'
		 * and, if found, returns is selected status.
		 * returns false if a checkbox names 'Other' is not located
		 * @return selection status of 'Other' checkbox
		 */
		private boolean isOtherSelected() {
			
			if ( !otherSpecifyStyle )
				return(false);
			for ( CheckableWrapper checkWrapper:items ) {
				System.out.println ( "CheckBoxesPanel name=" + checkWrapper.getName());
				if ( checkWrapper.getName().trim().startsWith("OTHER SPECIFY")) {
					System.out.println ( "isOtherSelected returning " + checkWrapper.getSelected());
					return(checkWrapper.getSelected());
				}
			}
			System.out.println ("isOtherSelected returning FALSE");
			return(false);
		}
	}
	/* end of private class CheckBoxPlus */
	/* ********************************* */
	
	protected String showItem(T item) {
		return item.toString();
	}
	
	private List<CheckableWrapper> items;
	private Boolean otherSpecifyStyle = false;
	private ArrayList<ActionListener> actionListeners;
	
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
		
		actionListeners = new ArrayList<ActionListener>();
		this.items = Lists.newArrayList();
		for(Integer i = 0; i < items.size(); i++) {
			T item = items.get(i);
			this.items.add(
					new CheckableWrapper(item)
					.setSelected(selected.contains(item))
					.setIndex(i));
		}
		build();
	}
	
	private Boolean autoFocus = false;
	
	private void build() {
		ListView checkboxes = new ListView("checkboxes",items) {
			protected void populateItem(ListItem item) {
				CheckableWrapper wrapper = (CheckableWrapper) item.getModelObject();
				String accessKey = (wrapper.getIndex()+1)+"";
				Boolean hasAccessKey = items.size() < 10;
				item.add(new Label("checkLabel",wrapper.getName()+
						(hasAccessKey ? " ("+accessKey+")" : "")));
				CheckBoxPlus checkBox = new CheckBoxPlus("checkField", new PropertyModel(wrapper, "selected"));
				if(autoFocus) {
					if(wrapper.getName() != null && items.get(0).getName() != null &&
							wrapper.getName().equals(items.get(0).getName()))
					{
						checkBox.add(new FocusOnLoadBehavior());
					}
				}
				if(hasAccessKey) {
					checkBox.add(new SimpleAttributeModifier("accessKey",accessKey));
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
		private Integer index;
		
		public CheckableWrapper(T item) {
			this.item = item;
			this.selected = false;
		}
		public CheckableWrapper setSelected(Boolean selected) {
			this.selected = selected;
			return this;
		}
		public CheckableWrapper setIndex(Integer index) {
			this.index = index;
			return this;
		}
		public Integer getIndex() {
			return index;
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
	
	/**
	 * the 'otherSpecifyStyle' indicates if we want a textfield to 
	 * appear when 'Other' is selected,  If this is try
	 * we will want to send actionEvents to the listeners
	 * @param otherSpecifyStyle
	 */
	public void setOtherSpecifyStyle(Boolean otherSpecifyStyle) {
		this.otherSpecifyStyle = (( otherSpecifyStyle==null)? false : otherSpecifyStyle );
	}
	public Boolean getOtherSpecifyStyle() {
		return(otherSpecifyStyle);
	}
	
	/**
	 * adds an ActionListener to the list of objects
	 * that want to be notified of events.
	 * In practice, the MultipleSelectionAnswerFormFieldPanel
	 * is the only outside object interested, and then only
	 * on whether the 'other' button is checked or unchecked, 
	 * and even that is ONLY if the question has the otherSpecifyType
	 * flag set.  This and the following functions are here to 
	 * maintain loose binding 
	 * @param aListener ActionListener to add
	 */
	public void addActionListener(ActionListener aListener) {
		if ( !actionListeners.contains(aListener))
			actionListeners.add(aListener);
	}
	
	/**
	 * simply removes an ActionListener from the list of
	 * ActionListeners
	 * @param aListener ActionListener to remove
	 */
	public void removeActionListener(ActionListener aListener) {
		actionListeners.remove(aListener);
	}
	
	/**
	 * fires an actionEvent to all listeners
	 * @param id 0 or 1 depending on whether a checkbox is selected
	 * @param strMessage name of the checkbox.
	 */
	public void fireActionEvent(boolean selected, String strMessage) {
		ActionEvent ae ;
		
		for ( ActionListener aListener:actionListeners ) {
			ae = new ActionEvent(this, (selected?1:0), strMessage);
			aListener.actionPerformed(ae);
		}
	}
	
	public boolean getOtherSelected() {
		return(otherSelected);
	}
}
