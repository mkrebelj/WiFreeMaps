package com.wifreemaps;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;


import com.google.android.gms.maps.model.LatLng;

public class DataHandler {
	
	private MySQLiteHelper db;
	
	public DataHandler(MySQLiteHelper helper)
	{
		this.db = helper;
	}
	
	
	
	
	public void addSamplePointsToDatabase(Context context) {
		//only for debugging
		
		//add 10 networks to database
		String bssid,ssid,city;
		int freq,alive,newpoints;
		float startLat,startLng,latitude,longitude,accuracy,signal;
		
		LatLng approxCoordinates;
		LatLng ntwkPntGPS;
		
		OpenNetwork newNetwork;
		NetworkPoint newPoint;

		for(int i = 0; i < 5; i++)
		{
			//for each add from 5 to 15 new points
			bssid="bssid"+i+"sample";
			ssid="network"+i+"name";
			freq= i%3;//(int)(Math.random()*14);
			alive=( i%4==3 ? 0:1 );
			
			startLat =46.042931f +(float)(Math.random()*0.001);
			startLng =14.487516f +(float)(Math.random()*0.001);
			approxCoordinates = new LatLng(startLat, startLng);
			
			
			
			city ="fake ljubljana";
			
			Geocoder gcd = new Geocoder(context, Locale.getDefault());
			try {
				List<Address> addresses = gcd.getFromLocation(startLat, startLng, 1);
				if (addresses.size() > 0) 
				    city=addresses.get(0).getLocality();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			//signal -35dbm(and higher) = 100%; -95dbm = 1%, linear
			//accuracy is location in meters, 68% confidence that location is inside circle with r as accuracy: 
			//accuracy of 4 means that location is roughly inside 8 meter of diameter
			
			newNetwork=new OpenNetwork(bssid, ssid, city, freq, approxCoordinates, alive);
			//create network first
			db.addNetwork(newNetwork);
			
			newpoints=(int)(Math.random()*11+5);

			//create point
			

			for (int j = 0;j<newpoints;j++){

				latitude = startLat + (float)(Math.random()*0.0001);
				longitude = startLng +  +(float)(Math.random()*0.0001);
				ntwkPntGPS = new LatLng(latitude, longitude);
				accuracy = (int)(Math.random()*10+1);
				signal = (float) (Math.random());
				newPoint = new NetworkPoint(bssid, ntwkPntGPS, signal, accuracy, System.currentTimeMillis());
				db.addNetworkPoint(newPoint);
				
			}

		}



	}
	

}
