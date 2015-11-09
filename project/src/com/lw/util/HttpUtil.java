package com.lw.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpUtil {

	
	public static String readString(String url) throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(url);
		HttpClient client = new DefaultHttpClient();
		HttpResponse resp = client.execute(request);
		InputStream in = resp.getEntity().getContent();
		int len = 0;
		byte buffer[] = new byte[8*1024];
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		while((len=in.read(buffer)) != -1) {
			bo.write(buffer, 0, len);
		}
		String result = bo.toString("utf-8");
		in.close();
		return result;
	}
}
