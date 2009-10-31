package net.sf.egonet.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;

// This class is a slight modification of an example at
// http://cwiki.apache.org/WICKET/request-focus-on-a-specific-form-component.html
public class FocusOnLoadBehavior extends AbstractBehavior {
	private Component component;
	
	public void bind(Component component) {
		this.component = component;
		component.setOutputMarkupId(true);
	}
	
	public void renderHead(IHeaderResponse iHeaderResponse) {
		super.renderHead(iHeaderResponse);
		iHeaderResponse.renderOnLoadJavascript(
				"document.getElementById('" + component.getMarkupId() + "').focus()");
	}
	
	public boolean isTemporary() {
		// false means my onload behavior happens again if the component loads again
		return false;
	}
}
