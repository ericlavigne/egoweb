package net.sf.egonet.network;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import net.sf.egonet.network.Network.Node;
import net.sf.egonet.network.Network.Edge;

public class NetworkService
{
    public static BufferedImage createImage(Network network)
    {
        final int width  = 1000;
        final int height = 1000;
        final Color nodeColor = Color.GREEN;
        final boolean labelVertices = true;
        final boolean labelEdges = false;
		final Point center = new Point(0,0);
//        float dash[] = {10.0f};
//        final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        final Stroke edgeStroke = new BasicStroke();

        //--------------------

        Graph<Node,Edge> g = graphFromNetwork(network);

        Dimension d = new Dimension(width, height);

        Layout<Node,Edge> layout = new FRLayout<Node,Edge>(g);
        layout.setSize(d);

        VisualizationImageServer<Node,Edge> vv = new VisualizationImageServer<Node,Edge>(layout, d);
        vv.getRenderContext().setVertexFillPaintTransformer(newVertexPainter(nodeColor));
        vv.getRenderContext().setEdgeStrokeTransformer(newEdgeStrokeTransformer(edgeStroke));
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
        if (labelVertices) vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Node>());
        if (labelEdges)    vv.getRenderContext().setEdgeLabelTransformer(  new ToStringLabeller<Edge>());

        return (BufferedImage) vv.getImage(center, d);
    }

	// ----------------------------------------

    private static Transformer<Node,Paint> newVertexPainter(final Color c)
    {
        return
            new Transformer<Node,Paint>()
		   	{
                public Paint transform(Node n) { return c; }
            };
    }

    private static Transformer<Edge,Stroke> newEdgeStrokeTransformer(final Stroke edgeStroke)
    {
        return
            new Transformer<Edge, Stroke>()
		   	{
                public Stroke transform(Edge e) { return edgeStroke; }
            };
    }

    private static Graph<Node,Edge> graphFromNetwork(Network network)
    {
        Graph<Node,Edge> g = new UndirectedSparseGraph<Node,Edge>();

        for (Node n : network.getNodes()) g.addVertex(n);
        for (Edge e : network.getEdges()) g.addEdge(e, e.getNode1(), e.getNode2());

        return g;
    }
}
