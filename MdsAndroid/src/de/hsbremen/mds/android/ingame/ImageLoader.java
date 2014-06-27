package de.hsbremen.mds.android.ingame;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Log;

public class ImageLoader {
	
    public Bitmap getBitmapFromURL(String src){
    	
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    	
    	InputStream input = getInputStreamFromUrl(src);
    	
    	Bitmap myBitmap = BitmapFactory.decodeStream(input);
    	
    	return myBitmap;
	}
    
	public static InputStream getInputStreamFromUrl(String url) {
		  InputStream content = null;
		  try {
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpResponse response = httpclient.execute(new HttpGet(url));
		    content = response.getEntity().getContent();
		  } catch (Exception e) {
		    Log.e("[GET REQUEST]", "Network exception", e);
		  }
		    return content;
		}

}
