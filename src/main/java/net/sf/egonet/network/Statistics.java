package net.sf.egonet.network;

import java.util.Set;

public class Statistics<N> {
	Network<N> network;
	public Statistics(Network<N> network) {
		this.network = network;
	}
	
	/*
	 * For fully connected network, closeness is the reciprocal 
	 * of the average distance to other nodes. For disconnected
	 * networks, it is the closeness within a component multiplied
	 * by the portion of other nodes that are in that component.
	 */
	public Double closeness(N node) {
		Integer reachable = 0;
		Integer totalDistance = 0;
		Set<N> nodes = network.getNodes();
		for(N n : nodes) {
			Integer distance = network.distance(node, n);
			if(distance != null && distance > 0) {
				reachable++;
				totalDistance += distance;
			}
		}
		if(reachable < 1) {
			return 0.0;
		}
		Double averageDistance = totalDistance*1.0/reachable;
		return reachable / (averageDistance * (nodes.size()-1));
	}
}
