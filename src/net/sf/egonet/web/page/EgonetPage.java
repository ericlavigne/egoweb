package net.sf.egonet.web.page;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import net.sf.egonet.persistence.Analysis;
import net.sf.egonet.web.Main;

import org.apache.wicket.Application;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.StringResourceStream;

public class EgonetPage extends WebPage
{
	private String title;

	public EgonetPage() {
		this("Egoweb");
	}

	public EgonetPage(String title)
	{
		this.title = title;

		add(new Label("headTitle",getTitle()));
		add(new Label("inlineTitle",getTitle()));

        Application application = getApplication();
        if (application instanceof Main)
        {
            ((Main)application).userActivityOccurred();
        }
	}

	public String getTitle() {
		return title;
	}
	

	protected void downloadImage(String name, BufferedImage image) {
		ResourceStreamRequestTarget target =
			new ResourceStreamRequestTarget(
					new Analysis.ImageResourceStream(image));
		target.setFileName(name);
		RequestCycle.get().setRequestTarget(target);
	}
	
	protected void downloadText(String name, String mimeType, CharSequence contents) {
		// See example on p231 of Wicket in Action
		StringResourceStream stream = new StringResourceStream(contents, mimeType);
		stream.setCharset(Charset.forName("UTF-8")); // Without this, Chinese characters are converted to question marks.
		ResourceStreamRequestTarget target =
			new ResourceStreamRequestTarget(stream);
		target.setFileName(name);
		RequestCycle.get().setRequestTarget(target);
	}
	
	protected static String uploadText(FileUploadField field) throws IOException {
		FileUpload upload = field.getFileUpload();
		if(upload != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(upload.getInputStream()));
			StringBuffer buffer = new StringBuffer();
			String line = null;
			while((line = reader.readLine()) != null) {
				buffer.append(line+"\n");
			}
			return buffer.toString();
		}
		return null;
	}
}
