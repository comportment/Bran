package br.com.brjdevs.bran.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtils {
	public static String read(String string) throws IOException {
		URL url = new URL(string);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:39.0) Gecko/20100101");
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder content = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			content.append(line).append("\n");
		}
		in.close();
		return content.toString();
		
	}
}
