package net.sf.egonet.persistence;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.hibernate.Session;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Expression;
import net.sf.egonet.model.Interview;
import net.sf.egonet.network.Network;
import net.sf.egonet.network.NetworkService;
import net.sf.egonet.persistence.Expressions.EvaluationContext;

public class Analysis {
	
	public static BufferedImage getImageForInterview(final Interview interview, final Expression connection) {
		return new DB.Action<BufferedImage>() {
			public BufferedImage get() {
				return getImageForInterview(session, interview, connection);
			}
		}.execute();
	}
	
	public static BufferedImage getImageForInterview(Session session, Interview interview, Expression connection) {
		return NetworkService.createImage(getNetworkForInterview(session,interview,connection));
	}
	

	public static class ImageResourceStream implements IResourceStream {

		private byte[] imagedata;
		
		public ImageResourceStream(BufferedImage image) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
			try {
				encoder.encode(image);
				imagedata = os.toByteArray();
			} catch(Exception ex) {
				throw new RuntimeException("Failed to create image resource stream",ex);
			}
		}
		public void close() throws IOException {
		}
		public String getContentType() {
			return "image/jpeg";
		}
		public InputStream getInputStream()
				throws ResourceStreamNotFoundException {
			return new ByteArrayInputStream(imagedata);
		}
		public Locale getLocale() {
			return null;
		}
		public long length() {
			return imagedata.length;
		}
		public void setLocale(Locale arg0) {
		}
		public Time lastModifiedTime() {
			return Time.now();
		}
	}
	
	
	public static Network getNetworkForInterview(Session session, Interview interview, Expression connection) {
		EvaluationContext context = Expressions.getContext(session, interview);
		Set<AlterNode> alterNodes = Sets.newHashSet();
		for(Alter alter : Alters.getForInterview(session, interview.getId())) {
			alterNodes.add(new AlterNode(alter));
		}
		Set<Network.Edge> edges = Sets.newHashSet();
		for(AlterNode node1 : alterNodes) {
			for(AlterNode node2 : alterNodes) {
				Alter alter1 = node1.getAlter(), alter2 = node2.getAlter();
				ArrayList<Alter> alters = Lists.newArrayList(alter1,alter2);
				if(alter1.getId() < alter2.getId() && Expressions.evaluate(connection, alters, context)) {
					edges.add(new AlterEdge(node1,node2));
				}
			}
		}
		Set<Network.Node> nodes = Sets.newHashSet();
		for(AlterNode node : alterNodes) {
			nodes.add(node);
		}
		return new Network(nodes,edges);
	}
	
	public static class AlterNode implements Network.Node {
		private Alter alter;
		public AlterNode(Alter alter) {
			this.alter = alter;
		}
		public Alter getAlter() {
			return alter;
		}
		public String toString() {
			return alter.getName();
		}
		public boolean equals(Object object) {
			return object instanceof AlterNode && 
			((AlterNode) object).alter.getId().equals(alter.getId());
		}
		public int hashCode() {
			return alter.getId().hashCode();
		}
	}
	
	public static class AlterEdge implements Network.Edge {

		private AlterNode alter1, alter2;
		
		public AlterEdge(AlterNode alter1, AlterNode alter2) {
			this.alter1 = alter1;
			this.alter2 = alter2;
		}
		public AlterNode getNode1() {
			return alter1;
		}
		public AlterNode getNode2() {
			return alter2;
		}
		public String toString() {
			return alter1.toString()+"-"+alter2.toString();
		}
		public boolean equals(Object object) {
			if(object instanceof AlterEdge) {
				AlterEdge edge = (AlterEdge) object;
				return (alter1.equals(edge.getNode1()) && alter2.equals(edge.getNode2())) ||
					(alter2.equals(edge.getNode2()) && alter1.equals(edge.getNode1()));
			}
			return false;
		}
		public int hashCode() {
			return alter1.hashCode()/2 + alter2.hashCode()/2;
		}
	}
}
