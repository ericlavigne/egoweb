package net.sf.egonet.web.component;

import org.apache.wicket.model.IModel;
import org.apache.wicket.behavior.SimpleAttributeModifier;

public class TextField extends org.apache.wicket.markup.html.form.TextField {

	public TextField(String id) {
		super(id);
		// add( new SimpleAttributeModifier("onfocus","className=\"hiliteTextField\";"));
		// add( new SimpleAttributeModifier("onblur", "className=\"plainTextField\";"));
	}
	public TextField(String id, IModel model) {
		super(id, model);
		// add( new SimpleAttributeModifier("onfocus","className=\"hiliteTextField\";"));
		// add( new SimpleAttributeModifier("onblur", "className=\"plainTextField\";"));
	}
	public TextField(String id, Class<?> type) {
		super(id, type);
		// add( new SimpleAttributeModifier("onfocus","className=\"hiliteTextField\";"));
		// add( new SimpleAttributeModifier("onblur", "className=\"plainTextField\";"));
	}
	public TextField(String id, IModel model, Class<?> type) {
		super(id, model, type);
		// add( new SimpleAttributeModifier("onfocus","className=\"hiliteTextField\";"));
		// add( new SimpleAttributeModifier("onblur", "className=\"plainTextField\";"));
	}

	public String getText() {
		return TextArea.unescapeText(getModelObjectAsString());
	}
}
