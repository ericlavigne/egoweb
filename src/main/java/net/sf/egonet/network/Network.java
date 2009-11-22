package net.sf.egonet.network;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Set;

import net.sf.functionalj.tuple.PairUni;

public class Network<N>
{
	private Set<N> nodes;
	private Set<PairUni<N>> edges;

    public Network(Set<N> nodes, Set<PairUni<N>> edges)
    {
		ensureEdgeNodesAreIn(nodes, edges);
		this.nodes = nodes;
        this.edges = edges;
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

    public Set<N> getNodes() { return Collections.unmodifiableSet(this.nodes); }
    public Set<PairUni<N>> getEdges() { return Collections.unmodifiableSet(this.edges); }
}
