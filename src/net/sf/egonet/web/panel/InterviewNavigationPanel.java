package net.sf.egonet.web.panel;

import java.io.Serializable;
import java.util.List;

import net.sf.egonet.model.Question;
import net.sf.egonet.persistence.Interviewing;
import net.sf.egonet.web.page.EgonetPage;
import net.sf.egonet.web.page.InterviewingAlterPage;
import net.sf.egonet.web.page.InterviewingAlterPairPage;
import net.sf.egonet.web.page.InterviewingAlterPromptPage;
import net.sf.egonet.web.page.InterviewingEgoPage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class InterviewNavigationPanel extends Panel {
	
	private Long interviewId;
	
	public InterviewNavigationPanel(String id, Long interviewId) {
		super(id);
		this.interviewId = interviewId;
		build();
	}
	
	public static interface InterviewLink extends Serializable {
		public EgonetPage getPage();
		public String toString();
	}

	public static class EgoLink implements InterviewLink {
		private Long interviewId;
		private Question question;
		private InterviewingEgoPage page;
		public EgoLink(Long interviewId, Question question) {
			this.interviewId = interviewId;
			this.question = question;
		}
		public EgonetPage getPage() {
			if(page == null) {
				page = new InterviewingEgoPage(interviewId,question);
			}
			return page;
		}
		public String toString() {
			return "Ego : "+question.getTitle();
		}
	}

	public static class AlterPromptLink implements InterviewLink {
		private Long interviewId;
		private Integer alters;
		private InterviewingAlterPromptPage page;
		public AlterPromptLink(Long interviewId, Integer alters) {
			this.interviewId = interviewId;
			this.alters = alters;
		}
		public EgonetPage getPage() {
			if(page == null) {
				page = new InterviewingAlterPromptPage(interviewId);
			}
			return page;
		}
		public String toString() {
			return "Alter Prompt ("+alters+")";
		}
	}
	public static class AlterLink implements InterviewLink {
		private InterviewingAlterPage.Subject subject;
		private InterviewingAlterPage page;
		public AlterLink(InterviewingAlterPage.Subject subject) {
			this.subject = subject;
		}
		public EgonetPage getPage() {
			if(page == null) {
				page = new InterviewingAlterPage(subject);
			}
			return page;
		}
		public String toString() {
			return "Alter : "+subject.question.getTitle()+
			(subject.alters.size() > 1 ? "" : " : "+subject.getAlter().getName());
		}
	}
	public static class AlterPairLink implements InterviewLink {
		private InterviewingAlterPairPage.Subject subject;
		private InterviewingAlterPairPage page;
		public AlterPairLink(InterviewingAlterPairPage.Subject subject) {
			this.subject = subject;
		}
		public EgonetPage getPage() {
			if(page == null) {
				page = new InterviewingAlterPairPage(subject);
			}
			return page;
		}
		public String toString() {
			return "Alter Pair : "+subject.question.getTitle()+" : "+subject.firstAlter+
			(subject.secondAlters.size() > 1 ? "" : " : "+subject.getSecondAlter().getName());
		}
	}
	
	public List<InterviewLink> getLinks() {
		return Interviewing.getAnsweredPagesForInterview(interviewId);
	}
	
	private void build() {
		add(new ListView("pages", new PropertyModel(this,"links")) {
			protected void populateItem(ListItem item) {
				final InterviewLink page = (InterviewLink) item.getModelObject();
				Link link = new Link("pageLink") {
					public void onClick() {
						setResponsePage(page.getPage());
					}
				};
				link.add(new Label("pageName",page.toString()));
				item.add(link);
			}
		});
	}
}
