package net.sf.egonet.web.component;

import java.awt.Color;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.Paint;
import java.awt.Stroke;

import net.sf.egonet.network.Network;
import net.sf.egonet.network.NetworkService;
import net.sf.functionalj.tuple.PairUni;

import edu.uci.ics.jung.algorithms.layout.Layout;
import org.apache.commons.collections15.Transformer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.util.io.ByteArrayOutputStream;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class NetworkImage<N> extends Image {

	private Network<N> network;
	private NetworkService.LayoutOption layoutOption;
	private Color background;
	private Transformer<N,String> nodeLabeller;
	private Transformer<N,Paint> nodeColorizer;
	private Transformer<N,Shape> nodeShaper;
	private Transformer<PairUni<N>,Paint> edgeColorizer;
	private Transformer<PairUni<N>,Stroke> edgeSizer;
	private int imWidth;
	private int imHeight;
	
	public NetworkImage(String id, Network<N> network) {
		super(id);
		this.network = network;
		this.layoutOption = null;
		this.background = null;
		this.nodeLabeller = null;
		this.nodeColorizer = null;
		this.nodeShaper = null;
		this.edgeColorizer = null;
		this.edgeSizer = null;
		this.imWidth = 1600;
		this.imHeight = 800;
		refresh();
	}
	
	public void setLayoutOption(NetworkService.LayoutOption layoutOption) {
		this.layoutOption = layoutOption;
		this.graphLayout = NetworkService.createLayout(NetworkImage.this.network, layoutOption);
	}

	/*
	 * Functions to retrieve/set the layout to be used for the network. If a survey
	 * has multiple NETWORK questions that use the same adjacency expression and 
	 * node set, these functions can be used to make sure those questions have the
	 * same graph layout when rendered. The same method could be used to ensure
	 * that a single question shows the same layout each time the page is rendered,
	 * rather than redrawing the graph each time.
	 */
	public void setLayout(Layout<N, PairUni<N>> graphLayout) {
		this.graphLayout = graphLayout;
	}

	public Layout<N, PairUni<N>> getOrCreateLayout() {
		if (graphLayout == null)
		{
			graphLayout = NetworkService.createLayout(NetworkImage.this.network, layoutOption);		
		}
		return this.graphLayout;
	}


	public void setBackground(Color color) {
		this.background = color;
	}

	/*
	 * Transformers to modify graph-rendering, allowing per-node variation
	 * of label/color/shape/size, and per-edge variation of color/width.
	 */
	public void setNodeLabeller(Transformer<N,String> nodeLabeller) {
		this.nodeLabeller = nodeLabeller;
	}
	public void setNodeColorizer(Transformer<N,Paint> nodeColorizer) {
		this.nodeColorizer = nodeColorizer;
	}
	public void setNodeShaper(Transformer<N,Shape> nodeShaper) {
		this.nodeShaper = nodeShaper;
	}
	public void setEdgeColorizer(Transformer<PairUni<N>,Paint> edgeColorizer) {
		this.edgeColorizer = edgeColorizer;
	}
	public void setEdgeSizer(Transformer<PairUni<N>,Stroke> edgeSizer) {
		this.edgeSizer = edgeSizer;
	}

	/*
	 * Pixel dimensions of the rendered image.
	 */
	public void setDimensions(int width, int height)
	{
		if (width > 0 && height > 0)
		{
			this.imWidth = width;
			this.imHeight = height;
		}
	}
	
	public void refresh() {
		if (graphLayout == null)
		{
			graphLayout = NetworkService.createLayout(NetworkImage.this.network, layoutOption);
		}

		setImageResource(new DynamicImageResource() {
			protected byte[] getImageData() {
				return getJPEGFromBufferedImage(
						NetworkService.createImage(
								NetworkImage.this.network, 
								graphLayout, 
								background,
								imWidth,
								imHeight,
								nodeLabeller,
								nodeColorizer,
								nodeShaper,
								edgeSizer,
								edgeColorizer));
			}
		});
	}
	
	public static byte[] getJPEGFromBufferedImage(BufferedImage image) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
		try {
			encoder.encode(image);
			return os.toByteArray();
		} catch(Exception ex) {
			throw new RuntimeException("Failed to convert BufferedImage into JPEG",ex);
		}
	}
}
