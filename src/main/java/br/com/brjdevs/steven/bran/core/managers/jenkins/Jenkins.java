package br.com.brjdevs.steven.bran.core.managers.jenkins;

import br.com.brjdevs.steven.bran.Client;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.channels.FileChannel;

public class Jenkins {
	
	public Client client;
	
	public Jenkins(Client client) {
		this.client = client;
	}
	
	public void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		
		FileChannel source = null;
		FileChannel destination = null;
		
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}
	
	public void downloadFile(String saveDir) throws IOException {
		File f = new File(saveDir);
		if (!f.exists()) f.mkdirs();
		DefaultHttpClient client = new DefaultHttpClient();
		
		// Then provide the right credentials
		client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials(this.client.config.getJenkinsUser(), this.client.config.getJenkinsPass()));
		
		// Generate BASIC scheme object and stick it to the execution context
		BasicScheme basicAuth = new BasicScheme();
		BasicHttpContext context = new BasicHttpContext();
		context.setAttribute("preemptive-auth", basicAuth);
		
		// Add as the first (because of the zero) request interceptor
		// It will first intercept the request and preemptively initialize the authentication scheme if there is not
		client.addRequestInterceptor(new PreemptiveAuth(), 0);
		
		// You get request that will start the build
		HttpGet get = new HttpGet(this.client.config.getJenkinsLatestBuild() + this.client.config.getJenkinsToken());
		CloseableHttpResponse response = client.execute(get, context);
		
		if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
			String saveFilePath = saveDir + File.separator + "DiscordBot-1.0-SNAPSHOT.jar";
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				try (FileOutputStream outstream = new FileOutputStream(new File(saveFilePath))) {
					entity.writeTo(outstream);
					outstream.close();
				}
			}
			
		}
		response.close();
	}
	
}
