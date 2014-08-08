package com.wifreemaps;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class OpenNetwork {
	private String BSSID;
	private String SSID;
	private int wifiFrequency,isLive;
	
	private LatLng GPSLocation;
	private float GPSAccuracy;
	private float wifiStrength;
	
	
	
	public OpenNetwork(){
		
	}
	
	public OpenNetwork(String bssid, String ssid, int freq, LatLng coordinates, float accuracy, float signalStrength, int isLive)
	{
		super();
		this.BSSID=bssid;
		this.SSID=ssid;
		this.wifiFrequency=freq;
		this.GPSLocation = coordinates;
		this.GPSAccuracy = accuracy;
		this.wifiStrength = signalStrength;
		this.isLive = isLive;
	}
	
	
	
	
	
	
	public String getBSSID()
	{
		return this.BSSID;
	}
	public String getSSID()
	{
		return this.SSID;
	}
	public int getWiFiFrequency(){
		return this.wifiFrequency;
	}
	public String getLocations()
	{
		String latLan = this.GPSLocation.latitude +";"+this.GPSLocation.longitude;
		return latLan;
	}
	public float getLng(){
		return (float)this.GPSLocation.longitude;
	}
	public float getLat(){
		return (float)this.GPSLocation.latitude;
	}
	public LatLng getLocationAsCoordinate(){
		return this.GPSLocation;
	}
	public float getGPSaccuracy()
	{
		return this.GPSAccuracy;
	}
	public float getWiFiStrengths()
	{
		return this.wifiStrength;
	}
	
	public void setBSSID(String bssid)
	{
		this.BSSID=bssid;
	}
	public void setSSID(String ssid)
	{
		this.SSID= ssid;
	}
	public void setWiFiFrequency(int freq)
	{
		this.wifiFrequency = freq;
	}
	public void setGPSCoordinates(String latLng)
	{
		double lat,lng;
		try {
			lat=Double.parseDouble(latLng.split(";")[0]);
			lng=Double.parseDouble(latLng.split(";")[1]);
			LatLng gpsLocation= new LatLng(lat,lng);
			this.GPSLocation=gpsLocation;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setGPSaccuracy(float accuracy)
	{
		this.GPSAccuracy = accuracy;
	}
	public void setWiFiStrength(float signalStrength)
	{
		this.wifiStrength = signalStrength;
	}
	
	public String toString() {
        return "Network [BSSID=" + BSSID + ", SSID=" + SSID + ", Coordinates=" + GPSLocation + ", Accuracy=" + GPSAccuracy + ", WiFiStrength=" + wifiStrength 
                + "]";
    }

	public int getIsLive() {
		return this.isLive;
		
	}
	
}
