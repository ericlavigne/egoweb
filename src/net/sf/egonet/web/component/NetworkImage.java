package net.sf.egonet.web.component;

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
	
	public NetworkImage(String id, Network<N> network) {
		super(id);
		this.network = network;
		
		setImageResource(new DynamicImageResource() {
			protected byte[] getImageData() {
				return getJPEGFromBufferedImage(
						NetworkService.createImage(
								NetworkImage.this.network));
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
