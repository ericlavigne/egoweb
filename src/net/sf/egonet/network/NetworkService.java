package net.sf.egonet.network;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
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
import java.awt.Shape;

import org.apache.commons.collections15.Transformer;

import net.sf.functionalj.tuple.PairUni;


public class NetworkService
{
	public static enum LayoutOption {KK,FR,Circle,ISOM}
	
    public static <N> BufferedImage createImage(Network<N> network, 
    		LayoutOption layoutOption, Color backgroundColor, 
    		Transformer<N,String> nodeLabeller)
    {
		return createImage(network, layoutOption, backgroundColor, nodeLabeller, null, null, null, null);
    }

	public static <N> BufferedImage createImage(Network<N> network, 
		LayoutOption layoutOption, Color backgroundColor, 
		Transformer<N,String> nodeLabeller,
		Transformer<N,Paint> nodeColorizer,
		Transformer<N,Shape> nodeShaper,
		Transformer<PairUni<N>,Stroke> edgeSizer,
		Transformer<PairUni<N>,Paint> edgeColorizer)
	{
		int width = 1600;
		int height = 800;
		return createImage(network, 
						layoutOption,
						backgroundColor,
						width,
						height,
						nodeLabeller,
						nodeColorizer,
						nodeShaper,
						edgeSizer,
						edgeColorizer);
	}

	public static <N> Layout<N, PairUni<N>> createLayout(Network<N> network, LayoutOption layoutOption)
	{
        Graph<N,PairUni<N>> g = graphFromNetwork(network);

		Layout<N,PairUni<N>> layout = null;
        if(layoutOption == null || layoutOption.equals(LayoutOption.FR)) {
        	layout = new FRLayout<N,PairUni<N>>(g);
        } else if(layoutOption.equals(LayoutOption.KK)) {
        	layout = new KKLayout<N,PairUni<N>>(g);
        } else if(layoutOption.equals(LayoutOption.Circle)) {
        	layout = new CircleLayout<N,PairUni<N>>(g);
        } else if(layoutOption.equals(LayoutOption.ISOM)) {
        	layout = new ISOMLayout<N,PairUni<N>>(g);
        }
		return layout;

	}

	public static <N> BufferedImage createImage(Network<N> network, 
    		LayoutOption layoutOption, Color backgroundColor, 
			int width,
			int height,
    		Transformer<N,String> nodeLabeller,
			Transformer<N,Paint> nodeColorizer,
			Transformer<N,Shape> nodeShaper,
			Transformer<PairUni<N>,Stroke> edgeSizer,
			Transformer<PairUni<N>,Paint> edgeColorizer)
	{
		Layout<N, PairUni<N>> layout = createLayout(network, layoutOption);
		return createImage(network, layout, backgroundColor, width, height,
			nodeLabeller, nodeColorizer, nodeShaper, edgeSizer, edgeColorizer);
	}

	public static <N> BufferedImage createImage(Network<N> network, 
    		Layout<N, PairUni<N>> layout, Color backgroundColor, 
			int width,
			int height,
    		Transformer<N,String> nodeLabeller,
			Transformer<N,Paint> nodeColorizer,
			Transformer<N,Shape> nodeShaper,
			Transformer<PairUni<N>,Stroke> edgeSizer,
			Transformer<PairUni<N>,Paint> edgeColorizer)
	{
        final Color nodeColor = Color.GREEN;
        final boolean labelVertices = true;
        final boolean labelEdges = false;
		final Point center = new Point(0,0);
        final Stroke edgeStroke = new BasicStroke();

        //--------------------

        Dimension d = new Dimension(width, height);

        layout.setSize(d);

        VisualizationImageServer<N,PairUni<N>> vv = new VisualizationImageServer<N,PairUni<N>>(layout, d);
        vv.getRenderContext().setVertexFillPaintTransformer(newVertexPainter(network, nodeColor));
        vv.setBackground(backgroundColor == null ? Color.WHITE : backgroundColor);
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.SE);
        if (labelVertices) {
        	vv.getRenderContext().setVertexLabelTransformer(
        			nodeLabeller == null ? new ToStringLabeller<N>() : nodeLabeller);
        }
        if (labelEdges) {
        	vv.getRenderContext().setEdgeLabelTransformer(  new ToStringLabeller<PairUni<N>>());
        }
		if (nodeColorizer != null)
		{
			vv.getRenderContext().setVertexFillPaintTransformer(nodeColorizer);
		}
		if (nodeShaper != null)
		{
			vv.getRenderContext().setVertexShapeTransformer(nodeShaper);
		}
		if (edgeSizer != null)
		{
			vv.getRenderContext().setEdgeStrokeTransformer(edgeSizer);
		}
		else
		{
			vv.getRenderContext().setEdgeStrokeTransformer(newEdgeStrokeTransformer(network, edgeStroke));		
		}
		if (edgeColorizer != null)
		{
			vv.getRenderContext().setEdgeDrawPaintTransformer(edgeColorizer);
		}

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
