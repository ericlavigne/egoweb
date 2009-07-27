package net.sf.egonet.network;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

public class Network
{
	private Set<Node> nodes;
	private Set<Edge> edges;

    public Network(Set<Node> nodes, Set<Edge> edges)
    {
		ensureEdgeNodesAreIn(nodes, edges);
		this.nodes = nodes;
        this.edges = edges;
    }

	private static void ensureEdgeNodesAreIn(Set<Node> nodes, Set<Edge> edges)
	{
		// all edges should have nodes that are in nodes
		for (Edge edge : edges)
		{
			Preconditions.checkArgument(nodes.contains(edge.getNode1()));
			Preconditions.checkArgument(nodes.contains(edge.getNode2()));
		}
	}

    public Set<Node> getNodes() { return Collections.unmodifiableSet(this.nodes); }
    public Set<Edge> getEdges() { return Collections.unmodifiableSet(this.edges); }

	// ----------------------------------------

	public interface Node
	{
		String toString();
	}

	public interface Edge
	{
		Node getNode1();
		Node getNode2();
		String toString();
	}
}
