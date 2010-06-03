package net.sf.egonet.web.panel;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sf.egonet.web.component.FocusOnLoadBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;

import com.google.common.collect.Lists;

public class CheckboxesPanel<T> extends Panel {

	protected String showItem(T item) {
		String strItem = item.toString();
		return( strItem);
	}

	private static final String otherSpecify = "OTHER SPECIFY";
	private List<CheckableWrapper> items;
	private Boolean otherSpecifyStyle;
	private Boolean otherSelected;
	private ArrayList<ActionListener> actionListeners;
	private ArrayList<Component> componentsToUpdate;
	private Form horizontalForm;
	private Form verticalForm;
	private boolean horizontalLayout = false;
	private int maxStringLength = 100;
	
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
		if ( items.size()>20 ) {
			setMaxStringLength(12);
		} else {
			switch ( items.size()) {
			case 19: setMaxStringLength(13); break;
			case 18: setMaxStringLength(13); break;
			case 17: setMaxStringLength(14); break;
			case 16: setMaxStringLength(15); break;
			case 15: setMaxStringLength(16); break;
			case 14: setMaxStringLength(17); break;
			case 13: setMaxStringLength(18); break;
			case 12: setMaxStringLength(20); break;
			case 11: setMaxStringLength(22); break;
			default: setMaxStringLength(100); break;
			}
		}
			
		this.items = Lists.newArrayList();
		for(T item : items) {
			this.items.add(
					new CheckableWrapper(item)
					.setSelected(selected.contains(item)));
		}
		build();
	}

	private Boolean autoFocus = false;

	/**
	 * this will be used in horizontal layouts in the list-of-alters
	 * page to get more items on each line
	 * @param str the original checkbox label text
	 * @return a (possibly shortened) label
	 */
	
	private String truncate(String str) {
		if ( str.length() > maxStringLength )
			str = str.substring(0,maxStringLength);
		return(str);
	}
	
	/**
	 * the horizontal form of the checkboxes will depend on javascript
	 * functions being available in the file listofalters.js
	 */
	private void build() {
		horizontalForm = new Form ("horizontalForm");
		add(horizontalForm);
		
		ListView checkboxes = new ListView("checkboxes",items) {
			protected void populateItem(ListItem item) {
				CheckableWrapper wrapper = (CheckableWrapper) item.getModelObject();

				item.add(new Label("checkLabel", truncate(wrapper.getName())));				
				AjaxCheckBox checkBox = new AjaxCheckBox("checkField", new PropertyModel(wrapper, "selected"))
				{
				 protected void onUpdate(AjaxRequestTarget target) {
						boolean otherNowSelected = false;

						otherNowSelected = isOtherSelected();
						if ( otherNowSelected != otherSelected ) {
							for ( Component component : componentsToUpdate) {
							 	target.addComponent(component);
							}			
							fireActionEvent (otherNowSelected, otherSpecify );
							otherSelected = otherNowSelected;
						}
				 }};
				checkBox.add( new SimpleAttributeModifier("onfocus","doOnFocusHorz(this);"));
				checkBox.add( new SimpleAttributeModifier("onblur", "doOnBlur(this);"));
				checkBox.add( new SimpleAttributeModifier("onkeyup","doOnKeyUpHorz(event);")); 
					
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
		horizontalForm.add(checkboxes);
		add(horizontalForm);
	
		verticalForm = new Form ("verticalForm");
		add(verticalForm);
		
		ListView checkboxesVertical = new ListView("checkboxesVertical",items) {
			protected void populateItem(ListItem item) {
				CheckableWrapper wrapper = (CheckableWrapper) item.getModelObject();

				item.add(new Label("checkLabelVertical", wrapper.getName()));				
				AjaxCheckBox checkBox = new AjaxCheckBox("checkFieldVertical", new PropertyModel(wrapper, "selected"))
				{
				 protected void onUpdate(AjaxRequestTarget target) {
						boolean otherNowSelected = false;

						otherNowSelected = isOtherSelected();
						if ( otherNowSelected != otherSelected ) {
							for ( Component component : componentsToUpdate) {
							 	target.addComponent(component);
							}			
							fireActionEvent (otherNowSelected, otherSpecify );
							otherSelected = otherNowSelected;
						}
				 }};
				 
				// vertical lists of less than 10 items will have numeric hotkeys 
				if (items.size() >= 10  ) { 
				    checkBox.add( new SimpleAttributeModifier("onfocus","doOnFocusVert(this);"));
				    checkBox.add( new SimpleAttributeModifier("onblur", "doOnBlur(this);"));
				    checkBox.add( new SimpleAttributeModifier("onkeyup","doOnKeyUpVert(event);")); 
				}
				// this was an attempt to remove the 'hotkey' class from
				// Don't know and Refuse checkboxes.
				// it didn't work - the hotkey action was removed, but
				// the number in parenthesis remained in the label
				// if ( wrapper.getName().equals(AnswerFormFieldPanel.dontKnow) ||
				//       wrapper.getName().equals(AnswerFormFieldPanel.refuse)) {
				//  	checkBox.add( new SimpleAttributeModifier("class",""));
				// }
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

	/**
	 * the 'otherSpecifyStyle' indicates if we want a textfield to 
	 * appear when 'Other' is selected,  If this is trur
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
			if ( checkWrapper.getName().trim().startsWith(otherSpecify)) {
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

	public void setMaxStringLength(int maxStringLength) {
		this.maxStringLength = maxStringLength;
	}
	public int getMaxStringLength() { return(maxStringLength);}

	/**
	 * used in those cases of a list-of-alters screen and the
	 * user clicks one of the 'global' Don't know or Refuse buttons
	 * if nothing is selected in this group of checkboxes, search
	 * for one that matches the string and select it
	 * @param selection - string to look for.  generally 'Don't know' or 'Refuse'
	 * @return true if a new checkbox is selected
	 */
	public boolean forceSelectionIfNone(String selection) {
		// if anything is checked at all, 
		// we won't bother with this
		for ( CheckableWrapper checkWrapper : items ) {
			if ( checkWrapper.getSelected()) {
				System.out.println ("forceSelectionIfNone, item selected");
				return(false);
			}
		}
		// if everything is unchecked, search for an item
		// that matches the string and select it
		for ( CheckableWrapper checkWrapper : items ) {
			System.out.println ("examining " + checkWrapper.getName());
			if ( checkWrapper.getName().equalsIgnoreCase(selection)) {
				System.out.println ("SETTING " + checkWrapper.getName());
				checkWrapper.setSelected(true);
				return(true);
			}
		}
		return(false);
	}
}















