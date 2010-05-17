package net.sf.egonet.web.panel;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sf.egonet.web.component.FocusOnLoadBehavior;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.Component;

import com.google.common.collect.Lists;

public class CheckboxesPanel<T> extends Panel {

	protected String showItem(T item) {
		return item.toString();
	}
	
	private List<CheckableWrapper> items;
	private Boolean otherSpecifyStyle;
	private Boolean otherSelected;
	private ArrayList<ActionListener> actionListeners;
	private ArrayList<Component> componentsToUpdate;
	private Form horizontalForm;
	private Form verticalForm;
	private boolean horizontalLayout = false;
	
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
		componentsToUpdate = new ArrayList<Component>();
		otherSpecifyStyle = false;
		otherSelected = false;
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
		horizontalForm = new Form ("horizontalForm");
		add(horizontalForm);
		
		ListView checkboxes = new ListView("checkboxes",items) {
			protected void populateItem(ListItem item) {
				CheckableWrapper wrapper = (CheckableWrapper) item.getModelObject();
				String accessKey = (wrapper.getIndex()+1)+"";
				Boolean hasAccessKey = items.size() < 10;
				item.add(new Label("checkLabel",
						(hasAccessKey ? wrapper.getAccessKey() : "") + wrapper.getName()));				
				AjaxCheckBox checkBox = new AjaxCheckBox("checkField", new PropertyModel(wrapper, "selected"))
				{
				 protected void onUpdate(AjaxRequestTarget target) {
						boolean otherNowSelected = false;
						
						otherNowSelected = isOtherSelected();
						if ( otherNowSelected != otherSelected ) {
							for ( Component component : componentsToUpdate) {
							 	target.addComponent(component);
							}			
							fireActionEvent (otherNowSelected, "OTHER SPECIFY" );
							otherSelected = otherNowSelected;
						}
				 }};
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
		horizontalForm.add(checkboxes);
		add(horizontalForm);
		
		verticalForm = new Form ("verticalForm");
		add(verticalForm);
		
		ListView checkboxesVertical = new ListView("checkboxesVertical",items) {
			protected void populateItem(ListItem item) {
				CheckableWrapper wrapper = (CheckableWrapper) item.getModelObject();
				String accessKey = (wrapper.getIndex()+1)+"";
				Boolean hasAccessKey = items.size() < 10;
				item.add(new Label("checkLabelVertical",
						(hasAccessKey ? wrapper.getAccessKey() : "") + wrapper.getName()));				
				AjaxCheckBox checkBox = new AjaxCheckBox("checkFieldVertical", new PropertyModel(wrapper, "selected"))
				{
				 protected void onUpdate(AjaxRequestTarget target) {
						boolean otherNowSelected = false;
						
						otherNowSelected = isOtherSelected();
						if ( otherNowSelected != otherSelected ) {
							for ( Component component : componentsToUpdate) {
							 	target.addComponent(component);
							}			
							fireActionEvent (otherNowSelected, "OTHER SPECIFY" );
							otherSelected = otherNowSelected;
						}
				 }};
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
		checkboxesVertical.setReuseItems(true);
		verticalForm.add(checkboxesVertical);
		add(verticalForm);
		
		if ( horizontalLayout )
			horizontalForm.setVisible(true);
		else
			verticalForm.setVisible(false);
	}
	
	public void setHorizontalLayout ( boolean horizontalLayout ) {
		this.horizontalLayout = horizontalLayout;
		horizontalForm.setVisible(horizontalLayout);
		verticalForm.setVisible(!horizontalLayout);
	}
	public boolean getHorizontalLayout() {
		return(horizontalLayout);
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
		/**
		 * Don't Know and Refuse should not have
		 * access keys, otherwise the access key is
		 * the index+1
		 * @return a string showing the integer access
		 * key for this Selection
		 */
		public String getAccessKey() {
			String name = getName().trim();
			int accessKey = getIndex()+1;
			
			if ( name.equals(AnswerFormFieldPanel.dontKnow) || name.equals(AnswerFormFieldPanel.refuse))
				return("");
			if ( accessKey>=10 )
				return("");
			return ( " (" + accessKey + ") ");
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
	 * similar to the list of actionlisteners, 
	 * keep a list of components to update.
	 * These will in reality only be 'Other/Specify' items
	 * in a multiple selection question with an OTHER SPECIFY item
	 * @param component
	 */
	public void addComponentToUpdate(Component component) {
		if ( !componentsToUpdate.contains(component))
			componentsToUpdate.add(component);
	}
	
	/**
	 * simply removes an component from the list of
	 * componentsToUpdate
	 * @param component Component to remove
	 */
	public void removeComponentToUpdate(Component component) {
		componentsToUpdate.remove(component);
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
			if ( checkWrapper.getName().trim().startsWith("OTHER SPECIFY")) {
				return(checkWrapper.getSelected());
			}
		}
		return(false);
	}
	
	public boolean getOtherSelected(boolean forceRecalcuation) {
		if ( forceRecalcuation )
			otherSelected = isOtherSelected();
		return(otherSelected);
	}
}
