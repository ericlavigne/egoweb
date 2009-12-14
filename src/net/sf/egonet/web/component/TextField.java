package net.sf.egonet.web.component;

import org.apache.wicket.model.IModel;

public class TextField extends org.apache.wicket.markup.html.form.TextField {

	public TextField(String id) {
		super(id);
	}
	public TextField(String id, IModel model) {
		super(id, model);
	}
	public TextField(String id, Class<?> type) {
		super(id, type);
	}
	public TextField(String id, IModel model, Class<?> type) {
		super(id, model, type);
	}

	public String getText() {
		return TextArea.unescapeText(getModelObjectAsString());
	}
}
