package com.wifreemaps;



import com.google.android.gms.maps.model.LatLng;

public class OpenNetwork {
	private String BSSID;
	private String SSID;
	private String cityName;
	private int wifiFrequency,isReachable; //isReachable: 0=no, 1=yes; 2=unknown
	
	private LatLng approxGPSLocation;
	
	
	
	public OpenNetwork(){
		
	}
	
	public OpenNetwork(String bssid, String ssid, String cityname,int freq, LatLng approxCoordinates, int reachable)
	{
		super();
		this.BSSID=bssid;
		this.SSID=ssid;
		this.cityName = cityname;
		this.wifiFrequency=freq;
		this.approxGPSLocation = approxCoordinates;
		this.isReachable = reachable;
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
	public String getLocation()
	{
		String latLan = this.approxGPSLocation.latitude +";"+this.approxGPSLocation.longitude;
		return latLan;
	}
	public float getLng(){
		return (float)this.approxGPSLocation.longitude;
	}
	public float getLat(){
		return (float)this.approxGPSLocation.latitude;
	}
	public LatLng getLocationAsCoordinate()
	{
		return this.approxGPSLocation;
	}
	public String getCityName()
	{
		return this.cityName;
	}
	
	public int getFrequency()
	{
		return this.wifiFrequency;
	}
	public int isNetworkReachable()
	{
		return this.isReachable;
	}
	
	public void setNetworkReachable(int state){
		this.isReachable=state;
	}
	
	public void setCityName(String cityname)
	{
		this.cityName = cityname;
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
			this.approxGPSLocation=gpsLocation;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setGPSCoordinates(LatLng coordinates)
	{
		this.approxGPSLocation=coordinates;
	}
	
	
	@Override
	public String toString() {
        return "Network [BSSID=" + BSSID + ", SSID=" + SSID + ", City=" + cityName +", Coordinates=" + approxGPSLocation + ", wwwReachable= "+ (isReachable == 0? "no":(isReachable == 1?"yes":"unknown")) 
                + "]";
    }


	
}
