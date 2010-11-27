package net.sf.egonet.web.component;

import java.awt.Color;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.Paint;
import java.awt.Stroke;

import net.sf.egonet.network.Network;
import net.sf.egonet.network.NetworkService;
import net.sf.functionalj.tuple.PairUni;

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
	
	public void setLayout(NetworkService.LayoutOption layoutOption) {
		this.layoutOption = layoutOption;
		dirty = true;
	}
	
	public void setBackground(Color color) {
		this.background = color;
		dirty = true;
	}

	/*
	 * Transformers to modify graph-rendering, allowing per-node variation
	 * of label/color/shape/size, and per-edge variation of color/width.
	 */
	public void setNodeLabeller(Transformer<N,String> nodeLabeller) {
		this.nodeLabeller = nodeLabeller;
		dirty = true;
	}
	public void setNodeColorizer(Transformer<N,Paint> nodeColorizer) {
		this.nodeColorizer = nodeColorizer;
		dirty = true;
	}
	public void setNodeShaper(Transformer<N,Shape> nodeShaper) {
		this.nodeShaper = nodeShaper;
		dirty = true;
	}
	public void setEdgeColorizer(Transformer<PairUni<N>,Paint> edgeColorizer) {
		this.edgeColorizer = edgeColorizer;
		dirty = true;
	}
	public void setEdgeSizer(Transformer<PairUni<N>,Stroke> edgeSizer) {
		this.edgeSizer = edgeSizer;
		dirty = true;
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
			dirty = true;
		}
	}
	
	private Boolean dirty = true;
	
	public void refresh() {
		if(dirty) {
			setImageResource(new DynamicImageResource() {
				protected byte[] getImageData() {
					return getJPEGFromBufferedImage(
							NetworkService.createImage(
									NetworkImage.this.network, 
									layoutOption, 
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
			dirty = false;
		}
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
