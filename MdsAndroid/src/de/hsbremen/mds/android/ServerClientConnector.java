package de.hsbremen.mds.android;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ServerClientConnector {

	public String objectToJsonString(Object obj) {

		Gson gson = new GsonBuilder().create();

		System.out.println(gson.toJson(obj));
		
		return gson.toJson(obj);

	}

}
