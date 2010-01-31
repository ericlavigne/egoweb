package net.sf.egonet.network;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
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

import org.apache.commons.collections15.Transformer;

import net.sf.functionalj.tuple.PairUni;

public class NetworkService
{
    public static <N> BufferedImage createImage(Network<N> network)
    {
        final int width  = 1200;
        final int height = 800;
        final Color nodeColor = Color.GREEN;
        final boolean labelVertices = true;
        final boolean labelEdges = false;
		final Point center = new Point(0,0);
//        float dash[] = {10.0f};
//        final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        final Stroke edgeStroke = new BasicStroke();

        //--------------------

        Graph<N,PairUni<N>> g = graphFromNetwork(network);

        Dimension d = new Dimension(width, height);

        Layout<N,PairUni<N>> layout = new FRLayout<N,PairUni<N>>(g);
        layout.setSize(d);

        VisualizationImageServer<N,PairUni<N>> vv = new VisualizationImageServer<N,PairUni<N>>(layout, d);
        vv.getRenderContext().setVertexFillPaintTransformer(newVertexPainter(network, nodeColor));
        vv.getRenderContext().setEdgeStrokeTransformer(newEdgeStrokeTransformer(network, edgeStroke));
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
        if (labelVertices) vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<N>());
        if (labelEdges)    vv.getRenderContext().setEdgeLabelTransformer(  new ToStringLabeller<PairUni<N>>());

        return (BufferedImage) vv.getImage(center, d);
    }

	// ----------------------------------------

    private static <N> Transformer<N,Paint> newVertexPainter(Network<N> network, final Color c)
    {
        return
            new Transformer<N,Paint>()
		   	{
                public Paint transform(N n) { return c; }
            };
    }

    private static <N> Transformer<PairUni<N>,Stroke> newEdgeStrokeTransformer(Network<N> network, final Stroke edgeStroke)
    {
        return
            new Transformer<PairUni<N>, Stroke>()
		   	{
                public Stroke transform(PairUni<N> e) { return edgeStroke; }
            };
    }

    private static <N> Graph<N,PairUni<N>> graphFromNetwork(Network<N> network)
    {
        Graph<N,PairUni<N>> g = new UndirectedSparseGraph<N,PairUni<N>>();

        for (N n : network.getNodes()) g.addVertex(n);
        for (PairUni<N> e : network.getEdges()) g.addEdge(e, e.getFirst(), e.getSecond());

        return g;
    }
}
