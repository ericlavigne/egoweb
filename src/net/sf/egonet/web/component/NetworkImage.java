package net.sf.egonet.web.component;

import java.awt.Color;
import java.awt.image.BufferedImage;

import net.sf.egonet.network.Network;
import net.sf.egonet.network.NetworkService;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.util.io.ByteArrayOutputStream;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class NetworkImage<N> extends Image {

	private Network<N> network;
	private NetworkService.LayoutOption layoutOption;
	private Color background;
	
	public NetworkImage(String id, Network<N> network) {
		super(id);
		this.network = network;
		this.layoutOption = null;
		this.background = null;
		refresh();
	}
	
	public void setLayout(NetworkService.LayoutOption layoutOption) {
		this.layoutOption = layoutOption;
	}
	
	public void setBackground(Color color) {
		this.background = color;
	}
	
	public void refresh() {
		setImageResource(new DynamicImageResource() {
			protected byte[] getImageData() {
				return getJPEGFromBufferedImage(
						NetworkService.createImage(
								NetworkImage.this.network, 
								layoutOption, background));
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
