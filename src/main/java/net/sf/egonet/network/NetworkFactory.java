package net.sf.egonet.network;

import java.util.Set;

import net.sf.functionalj.tuple.PairUni;

public class NetworkFactory
{
    public static <N> Network<N> newNetwork(Set<N> nodes, Set<PairUni<N>> edges)
    {
        return new Network<N>(nodes, edges);
    }
}
