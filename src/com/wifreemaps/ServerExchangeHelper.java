package com.wifreemaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerExchangeHelper {
	
	
	public ServerExchangeHelper(){
	}
	
	public ArrayList<OpenNetwork> retrieveNetworksFromServer(String city){
		ArrayList<OpenNetwork> result = new ArrayList<OpenNetwork>();
		
		//connect to server
		//TODO: get and parse data, add to result
		
		
		return result;
	}
	
	
	public JSONObject prepareJSONnetworks(ArrayList<OpenNetwork> newNetworks){
		
		//connect to server, create json and send it
		
		
		JSONArray networksArray = new JSONArray();
		
		JSONObject networkN;

		for(OpenNetwork ntwk:newNetworks)
		{
			networkN = new JSONObject();
			
			try {
				networkN.put("bssid", ntwk.getBSSID());
				networkN.put("ssid",ntwk.getSSID());
				networkN.put("cityname", ntwk.getCityName());
				networkN.put("approxgps", ntwk.getLocation());
				networkN.put("frequency", ntwk.getFrequency());
				networkN.put("reachable", ntwk.isNetworkReachable());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			networksArray.put(networkN);
		}
		
		
		JSONObject networksObject = new JSONObject();
		
		try {
			networksObject.put("networks", networksArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 
		
		return networksObject;
	}

	
public JSONObject prepareJSONpoints(ArrayList<NetworkPoint> newPoints){
		
		//connect to server, create json and send it
		
		
		JSONArray pointsArray = new JSONArray();
		
		JSONObject pointN;

		for(NetworkPoint pnt:newPoints)
		{
			pointN = new JSONObject();
			
			try {
				pointN.put("pbssid", pnt.getBSSID());
				pointN.put("gpscoordinates",pnt.getLocationAsString());
				pointN.put("strength", pnt.getWifiStrength());
				pointN.put("accuracy", pnt.getGpsAccuracy());
				pointN.put("quality", pnt.getQuality());
				pointN.put("timestamp", pnt.getTimestamp());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pointsArray.put(pointN);
		}
		
		
		JSONObject pointsObject = new JSONObject();
		
		try {
			pointsObject.put("points", pointsArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 
		
		return pointsObject;
	}
	
	
	public static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }   
	
	
	

	
	

}
