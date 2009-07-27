package net.sf.egonet.network;

import java.util.Set;

import net.sf.egonet.network.Network.Node;
import net.sf.egonet.network.Network.Edge;

public class NetworkFactory
{
    public static Network newNetwork(Set<Node> nodes, Set<Edge> edges)
    {
        return new Network(nodes, edges);
    }
}
