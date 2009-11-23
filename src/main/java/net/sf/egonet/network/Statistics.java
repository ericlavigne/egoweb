package net.sf.egonet.network;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Statistics<N> {
	Network<N> network;
	public Statistics(Network<N> network) {
		this.network = network;
	}
	
	/*
	 * Density is the number of connections in the actual
	 * network divided by the number of possible connections
	 * for networks with that number of nodes.
	 */
	public Double density() {
		if(density == null) {
			int nodes = network.getNodes().size();
			int edges = network.getEdges().size();
			int possibleEdges = 0;
			for(int i = 0; i < nodes; i++) { // possibleEdges = additorial(nodes) = (nodes-1)+(nodes-2)+...+1+0
				possibleEdges += i;
			}
			density = possibleEdges < 1 ? 0.0 : ((double) edges) / possibleEdges;
		}
		return density;
	}
	private Double density;
	
	/*
	 * Degree centrality is the number of direct connections
	 * to a node divided by the number of possible direct
	 * connections to a node in a network of that size.
	 */
	public Double degree(N node) {
		Integer nodes = network.getNodes().size();
		return nodes < 2 ? 0.0 : network.connections(node).size() * 1.0 / (nodes-1);
	}
	
	/*
	 * For fully connected network, closeness is the reciprocal 
	 * of the average distance to other nodes. For disconnected
	 * networks, it is the closeness within a component multiplied
	 * by the portion of other nodes that are in that component.
	 */
	public Double closeness(N node) {
		if(! nodeToCloseness.containsKey(node)) {
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
			nodeToCloseness.put(node, reachable / (averageDistance * (nodes.size()-1)));
		}
		return nodeToCloseness.get(node);
	}
	private Map<N,Double> nodeToCloseness = Maps.newHashMap();
	
	/*
	 * Sum over pairs of nodes a,b (such that none of a,b,n are equal)
	 * of the number of shortest paths from a to b that pass through
	 * n divided by the total number of shortest paths from a to b.
	 * Disconnected networks are addressed by choosing that 0/0 => 0.
	 */
	public Double betweenness(N node) {
		if(! nodeToBetweenness.containsKey(node)) {
			List<N> nodes = Lists.newArrayList(network.getNodes());
			Double result = 0.0;
			for(Integer i = 0; i < nodes.size(); i++) {
				N node1 = nodes.get(i);
				for(Integer j = i+1; j < nodes.size(); j++) {
					N node2 = nodes.get(j);
					if(! (node.equals(node1) || node.equals(node2))) {
						result += portionOfShortestPathsBetweenAandBthroughN(node1, node2, node);
					}
				}
			}
			nodeToBetweenness.put(node, result);
		}
		return nodeToBetweenness.get(node);
	}
	private Map<N,Double> nodeToBetweenness = Maps.newHashMap();
	private Double portionOfShortestPathsBetweenAandBthroughN(N a, N b, N n) {
		Integer totalDistance = network.distance(a, b);
		Integer distance1 = network.distance(a, n);
		Integer distance2 = network.distance(b, n);
		if(totalDistance == null || distance1 == null || ! totalDistance.equals(distance1+distance2)) {
			return 0.0;
		}
		Integer totalPaths = numberOfShortestPaths(a,b);
		Integer inclusivePaths = numberOfShortestPaths(a,n)*numberOfShortestPaths(b,n);
		return inclusivePaths * 1.0 / totalPaths;
	}
	private Integer numberOfShortestPaths(N a, N b) {
		Integer distance = network.distance(a,b);
		if(distance == null) {
			return 0;
		}
		if(distance < 1) {
			return 1;
		}
		Integer paths = 0;
		for(N n : network.connections(a)) {
			if(network.distance(n, b) < distance) {
				paths += numberOfShortestPaths(n,b);
			}
		}
		return paths;
	}
	
	/*
	 * The eigenvector centrality of a node is proportional to the sum
	 * of the eigenvector centralities of its neighbors. I compute
	 * the eigenvector centrality iteratively, using closeness as an
	 * initial guess.
	 */
	public Double eigenvectorCentrality(N n) {
		if(eigenvectorCentralities == null) {
			Integer tries = (network.getNodes().size()+5)*(network.getNodes().size()+5);
			Map<N,Double> guess = initialEigenvectorGuess();
			while(true) {
				Map<N,Double> nextGuess = nextEigenvectorGuess(guess);
				if(change(guess,nextGuess) < tinyNum || tries < 0) {
					eigenvectorCentralities = nextGuess;
					return eigenvectorCentrality(n);
				}
				guess = nextGuess;
				tries--;
			}
		}
		return eigenvectorCentralities.get(n) < Math.sqrt(tinyNum) ? 0.0 : eigenvectorCentralities.get(n);
	}
	private Map<N,Double> eigenvectorCentralities = null;
	private Map<N,Double> nextEigenvectorGuess(Map<N,Double> guess) {
		Map<N,Double> results = Maps.newHashMap();
		for(N node : guess.keySet()) {
			Double result = 0.0;
			for(N neighbor : network.connections(node)) {
				result += guess.get(neighbor);
			}
			results.put(node, result);
		}
		return normalize(results);
	}
	private Double change(Map<N,Double> vec1, Map<N,Double> vec2) {
		Double total = 0.0;
		for(N node : vec1.keySet()) {
			total += Math.abs(vec1.get(node) - vec2.get(node));
		}
		return total;
	}
	private Double tinyNum = 0.0000001;
	private Map<N,Double> normalize(Map<N,Double> vec) {
		Double magnitudeSquared = 0.0;
		for(Double component : vec.values()) {
			magnitudeSquared += component * component;
		}
		Double magnitude = Math.sqrt(magnitudeSquared);
		Double factor = 1 / (magnitude < tinyNum ? tinyNum : magnitude);
		Map<N,Double> normalized = Maps.newHashMap();
		for(N node : vec.keySet()) {
			normalized.put(node, vec.get(node)*factor);
		}
		return normalized;
	}
	private Map<N,Double> initialEigenvectorGuess() {
		Map<N,Double> guess = Maps.newHashMap();
		for(N node : network.getNodes()) {
			guess.put(node, closeness(node));
		}
		return guess;
	}
}
