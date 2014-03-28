package de.hsbremen.mds.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;

import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/*
 * Stellt notwendige Methoden für die Kommunikation mit dem Server bereit
 */
public class ServerClientConnector {

	private MainActivity main;
	private String serverIp;
	private static final String PROTOKOLL_HTTP = "http://";
	private static final String PORT_HTTP = ":8080";
	private static final String PROTOKOLL_WS = "ws://";
	private static final String PORT_WS = ":8887";
	
	
	public ServerClientConnector(MainActivity main, String serverIp) {
		this.main = main;
		this.serverIp = serverIp;
	}

	public String objectToJsonString(Object obj) {

		Gson gson = new GsonBuilder().create();

		System.out.println(gson.toJson(obj));

		return gson.toJson(obj);

	}

	public Object jsonStringToObject(String json) {

		Gson gson = new GsonBuilder().create();

		// TODO: für getClass() muss anscheinend die Klasse
		// in die das Objekt umgewandelt wird angegeben werden
		return gson.fromJson(json, getClass());
	}

	private InputStream httpGet(String path) {
		InputStream content = null;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(PROTOKOLL_HTTP + serverIp + path);
			
			HttpResponse response = httpclient.execute(httpGet);
			
			// Debugnachricht
			Log.i("debug_Client", response.getStatusLine().toString());
			content = response.getEntity().getContent();
			httpGet.abort();
		} catch (Exception e) {
			Log.e("debug_Client", "Network exception", e);
		}

		return content;
	}

	public String httpGetString(String url) {

		InputStream instream = this.httpGet(url);

		// Stream in Json String umwandeln:
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				instream));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "n");
			}
			instream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String result = sb.toString();
		Log.i("debug_Client", result);

		return result;
	}

	private void httpPost(String json, String path) {

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(PROTOKOLL_HTTP + serverIp + PORT_HTTP + path);

		List<NameValuePair> postlist = new ArrayList<NameValuePair>();
		postlist.add(new BasicNameValuePair("player", json));

		try {
			post.setEntity(new UrlEncodedFormEntity(postlist));
		} catch (UnsupportedEncodingException e) {
			main.toastShow(e.toString(), Toast.LENGTH_LONG);
		}

		try {
			HttpResponse response = client.execute(post);
		} catch (ClientProtocolException e) {
			main.toastShow(e.toString(), Toast.LENGTH_LONG);
		} catch (IOException e) {
			main.toastShow(e.toString(), Toast.LENGTH_LONG);
		}
	}

	public void createSocket(String clientname) {
		Draft d = new Draft_17();
		
		String serverlocation = PROTOKOLL_WS + serverIp + PORT_WS;
		SocketClient sc;

		URI uri = URI.create(serverlocation + "/runCase?case=" + 1 + "&agent="
				+ clientname);
		sc = new SocketClient(d, uri, main);

		Thread t = new Thread(sc);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
