package net.sf.egonet.web.panel;

import net.sf.egonet.web.component.TextField;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class MultipleSelectionLimitsPanel extends Panel {

	private Integer minCheckableBoxes;
	private Integer maxCheckableBoxes;
	private TextField textMinCheckableBoxes;
	private TextField textMaxCheckableBoxes;
	
	public MultipleSelectionLimitsPanel (String id) {
		super(id);
		minCheckableBoxes = new Integer(1);
		maxCheckableBoxes = new Integer(100);
		build();
	}
	
	/**
	 * builds the panel
	 * creates the form an all input widgets
	 */
	
	private void build() {		
		Form form = new Form("form");
		textMinCheckableBoxes = new TextField("minMultipleSelectionEntry", new PropertyModel(this, "minCheckableBoxes"), Integer.class);
		form.add(textMinCheckableBoxes); 
		
		textMaxCheckableBoxes = new TextField("maxMultipleSelectionEntry", new PropertyModel(this, "maxCheckableBoxes"), Integer.class);
		form.add(textMaxCheckableBoxes);
		add(form);
	}
	
	/**
	 * getters & setters 
	 */
	
	public void setMinCheckableBoxes (Integer minCheckableBoxes) {
		this.minCheckableBoxes = (minCheckableBoxes==null)?new Integer(1):minCheckableBoxes;
	}
	public Integer getMinCheckableBoxes() { 
		return( minCheckableBoxes );
	}
	public void setMaxCheckableBoxes (Integer maxCheckableBoxes) {
		this.maxCheckableBoxes = (maxCheckableBoxes==null)?new Integer(1):maxCheckableBoxes;
	}
	public Integer getMaxCheckableBoxes() { 
		return( maxCheckableBoxes );
	}
}
