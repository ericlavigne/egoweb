package net.sf.egonet.web.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

public class WebSurveyIndexPage extends WebPage {
	
	private Model password;
	
	public WebSurveyIndexPage() {
		
		add(new Link("interviewingLink") {
			public void onClick() {
				setResponsePage(new InterviewingPage());
			}
		});
		
		password = new Model("");
		Form form = new Form("form") {
			public void onSubmit() {
				if(((String) password.getObject()).equals("wdheuto")) {
					setResponsePage(new IndexPage());
				}
			}
		};
		TextField passField = new TextField("passField",password);
		passField.setRequired(true);
		form.add(passField);
		add(form);
	}
}
