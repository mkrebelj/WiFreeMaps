package com.wifreemaps;

import com.google.android.gms.maps.model.LatLng;

public class NetworkPoint {
	private String pbssid;
	private LatLng gpsLocation;
	private int index;
	private double wifiStrength,gpsAccuracy, quality;
	private long timeStamp;
	

	public NetworkPoint()
	{
		
	}
	
	
	public NetworkPoint(String bssid, LatLng location, double wifistrength, double gpsaccuracy, long timestamp)
	{
		this.pbssid = bssid;
		this.gpsLocation = location;
		this.wifiStrength = wifistrength;
		this.gpsAccuracy = gpsaccuracy;
		this.timeStamp = timestamp;
		this.quality = wifistrength / gpsaccuracy;
	}
	
	
	public NetworkPoint(String bssid, LatLng location,int index, double wifistrength, double gpsaccuracy, long timestamp)
	{
		this.pbssid = bssid;
		this.gpsLocation = location;
		this.index = index;
		this.wifiStrength = wifistrength;
		this.gpsAccuracy = gpsaccuracy;
		this.timeStamp = timestamp;
		this.quality = wifistrength / gpsaccuracy;
	}
	
	public String getBSSID()
	{
		return this.pbssid;
	}
	public LatLng getLocation()
	{
		return this.gpsLocation;
	}
	public String getLocationAsString()
	{
		String locAsString="";
		locAsString+=this.gpsLocation.latitude+";"+this.gpsLocation.longitude;
		return locAsString;
	}
	
	public int getIndex()
	{
		return this.index;
	}
	
	public double getWifiStrength()
	{
		return this.wifiStrength;
	}
	public double getGpsAccuracy()
	{
		return this.gpsAccuracy;
	}
	public double getQuality()
	{
		return this.quality;
	}
	public long getTimestamp()
	{
		return this.timeStamp;
	}
	
	
	public void setBSSID(String ssid)
	{
		this.pbssid=ssid;
	}
	public void setLocation(LatLng location)
	{
		this.gpsLocation=location;
	}
	
	public void setLocation(String locationAsString)
	{
		
		double lat=0.0, lng=0.0;
		
		try {
			lat=Double.parseDouble(locationAsString.split(";")[0]);
			lng=Double.parseDouble(locationAsString.split(";")[1]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			
		}
		
		LatLng loc=new LatLng(lat, lng);
		this.gpsLocation = loc;
	}
	
	public void setWifiStrength(double wifistrength)
	{
		this.wifiStrength = wifistrength;
		this.quality = this.wifiStrength / this.gpsAccuracy;
	}
	public void setGpsAccuracy(double gpsaccuracy)
	{
		this.gpsAccuracy=gpsaccuracy;
		this.quality = this.wifiStrength / this.gpsAccuracy;
	}
	public void setTimestamp(long timestamp)
	{
		this.timeStamp=timestamp;
	}
	
	@Override
	public String toString()
	{
		 return "point [pBSSID=" + pbssid + ", index= "+ index +", Location:"+ gpsLocation+ ", wifiSignalStrength= "+wifiStrength + ", gpsAccuracy="+ gpsAccuracy +  ", Quality ="+ quality +", timestamp=" +timeStamp +"]" ; 
	}

}
