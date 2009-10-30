package net.sf.egonet.web.component;

import org.apache.wicket.model.IModel;

public class TextArea extends org.apache.wicket.markup.html.form.TextArea {

	public TextArea(String id) {
		super(id);
	}

	public TextArea(String id, IModel model) {
		super(id, model);
	}

	public String getText() {
		return unescapeText(getModelObjectAsString());
	}
	
	public static String unescapeText(String escapedText) {
		return escapedText == null ? "" :
			escapedText
			.replaceAll("&lt;", "<")
			.replaceAll("&gt;", ">")
			.replaceAll("&amp;", "&")
			.replaceAll("&quot;", "\"");
	}
}
