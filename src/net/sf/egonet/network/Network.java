package net.sf.egonet.network;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.functionalj.tuple.PairUni;

public class Network<N>
{
	private Set<N> nodes;
	private Set<PairUni<N>> edges;
	private Map<N,Set<N>> nodeToConnections;
    private Map<N,Map<N,Integer>> distanceBetweenNodes;

    public Network(Set<N> nodes, Set<PairUni<N>> edges)
    {
		ensureEdgeNodesAreIn(nodes, edges);
		this.nodes = Collections.unmodifiableSet(nodes);
        this.edges = Collections.unmodifiableSet(edges);
        
        Multimap<N,N> directConnections = ArrayListMultimap.create();
        for(PairUni<N> edge : edges) {
        	directConnections.put(edge.getFirst(), edge.getSecond());
        	directConnections.put(edge.getSecond(), edge.getFirst());
        }
        this.nodeToConnections = Maps.newHashMap();
        for(N node : nodes) {
        	this.nodeToConnections.put(node, Collections.unmodifiableSet(new HashSet<N>(directConnections.get(node))));
        }

        distanceBetweenNodes = Maps.newHashMap();
        for(N startingPoint : nodes) {
        	Map<N,Integer> distanceToNode = Maps.newHashMap();
        	Set<N> frontier = Sets.newHashSet();
        	frontier.add(startingPoint);
        	Integer distance = 0;
        	while(! frontier.isEmpty()) {
        		Set<N> newFrontier = Sets.newHashSet();
        		for(N frontierNode : frontier) {
        			if(! distanceToNode.containsKey(frontierNode)) {
        				distanceToNode.put(frontierNode, distance);
        				for(N newFrontierNode : connections(frontierNode)) {
        					if(! distanceToNode.containsKey(newFrontierNode)) {
        						newFrontier.add(newFrontierNode);
        					}
        				}
        			}
        		}
        		frontier = newFrontier;
        		distance++;
        	}
        	distanceBetweenNodes.put(startingPoint, distanceToNode);
        }
    }

	private void ensureEdgeNodesAreIn(Set<N> nodes, Set<PairUni<N>> edges)
	{
		// all edges should have nodes that are in nodes
		for (PairUni<N> edge : edges)
		{
			Preconditions.checkArgument(nodes.contains(edge.getFirst()));
			Preconditions.checkArgument(nodes.contains(edge.getSecond()));
		}
	}

    public Set<N> getNodes() { return this.nodes; }
    public Set<PairUni<N>> getEdges() { return this.edges; }
    
    public Set<N> connections(N node) {
    	return this.nodeToConnections.get(node);
    }
    
    // Returns null if there is no connection.
    public Integer distance(N node1, N node2) {
    	return distanceBetweenNodes.get(node1).get(node2);
    }
}
