package net.sf.egonet.web.page;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.persistence.Analysis;
import net.sf.egonet.persistence.Interviews;
import net.sf.egonet.web.component.NetworkImage;

public class NetworkVisualizationPage extends EgonetPage {
	public NetworkVisualizationPage(Interview interview, Expression connectionReason) {
		
		super(Interviews.getEgoNameForInterview(interview.getId()));
		
		add(new NetworkImage<Alter>("networkImage", 
				Analysis.getNetworkForInterview(interview, connectionReason)));
	}
}
