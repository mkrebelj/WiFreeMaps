package com.wifreemaps;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.util.Log;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

public class NetworkMarking {
	
	private static final double EARTH_RADIUS = 6378100.0;
	
	private List<NetworkPoint> listOfPoints;
	private int color;
	private double areaSize;
	private LatLng centerPoint;
	
	public NetworkMarking(){
		
	}

	public NetworkMarking(List<NetworkPoint> points, int networkcolor)
	{
		this.listOfPoints = points;
		this.color=networkcolor;
		
		
		//find center point of network
		this.centerPoint = findCenterPoint();
		//find radius
		this.areaSize = findAreaSize();
	
		
		
	}
	private LatLng findCenterPoint()
	{
		LatLng result = new LatLng(0.0, 0.0);
		
		int i=0;
		double latitudes=0.0,longitudes=0.0;
		for(NetworkPoint ntwkpnt:this.listOfPoints)
		{
			Log.d("Point"+i, "Lat"+ntwkpnt.getLocation().latitude + " Lon"+ntwkpnt.getLocation().longitude);
			latitudes+=ntwkpnt.getLocation().latitude;
			longitudes+=ntwkpnt.getLocation().longitude;
			i++;
		}
		if(i>0)
		{
			result= new LatLng(latitudes/i, longitudes/i);
			Log.d("AveragePointLoc:",result.toString());
		}
		
		return result;
	}
	
	private double findAreaSize()
	{
		//find point with worst accuracy, that is the furtherest from center point. Measure difference between center point and
		//that point, add accuracy and that is result- area which covers every noted point in network.
	
		double currentResult = 0.0;
		float[] distance = new float[1];
		for(NetworkPoint point:this.listOfPoints)
		{
			Location.distanceBetween(centerPoint.latitude, centerPoint.longitude, point.getLocation().latitude, point.getLocation().longitude, distance);
			if( distance[0] + point.getGpsAccuracy() > currentResult)
				currentResult = distance[0] + point.getGpsAccuracy();
		
		}
		
		return currentResult;
	}
	
	
	public boolean updateNetworkMarking(List<NetworkPoint> points, int networkcolor){
		boolean changed=false;
		if(points.size() != this.listOfPoints.size())
		{
			this.listOfPoints = points;
			//find center point of network
			this.centerPoint = findCenterPoint();
			//find radius
			this.areaSize = findAreaSize();
			changed= true;
		}
		if(networkcolor != this.color)
		{
			this.color=networkcolor;
			changed= true;
		}
		return changed;
	}
	
	public BitmapDescriptor returnNetworkMarkingAsImage(GoogleMap map){
		BitmapDescriptor result = null;
		
		
		int radius = convertMetersToPixels(centerPoint.latitude, centerPoint.longitude, areaSize, map);
		Log.d("RADIUS","in meters:"+areaSize +" in pixels:"+radius);
		
		Bitmap myBitmap = Bitmap.createBitmap(radius, radius, Bitmap.Config.ALPHA_8);
		Canvas canvas = new Canvas(myBitmap);
		Paint paint = new Paint();
		//draw on this canvas
		
		for(NetworkPoint point:listOfPoints)
		{
			
			float[] oddaljenostY=new float[3];
			float[] oddaljenostX=new float[3];
			Location.distanceBetween(centerPoint.latitude, centerPoint.longitude, centerPoint.latitude, point.getLocation().longitude, oddaljenostY);
			Location.distanceBetween(centerPoint.latitude, centerPoint.longitude, point.getLocation().latitude, centerPoint.longitude, oddaljenostX);
			
			int networkR = convertMetersToPixels(point.getLocation().latitude, point.getLocation().longitude, point.getGpsAccuracy(), map);
			paint.setColor(color);
			///+sranje NARIŠI
			
			canvas.drawCircle(radius/2, radius/2, networkR, paint);
		}
		
		return result;
	}
	public LatLng returnCenterPoint()
	{
		return centerPoint;
	}
	
	
	 public int convertMetersToPixels(double lat, double lng, double radiusInMeters, GoogleMap map) {

		 Log.d("----LATLNG","---"+lat+";"+lng);
		 
         double lat1 = radiusInMeters / EARTH_RADIUS;
         double lng1 = radiusInMeters / (EARTH_RADIUS * Math.cos((Math.PI * lat / 180)));

         double lat2 = lat + lat1 * 180 / Math.PI;
         double lng2 = lng + lng1 * 180 / Math.PI; 

         if(map.getProjection().toScreenLocation(new LatLng(lat, lng)).x != 0){
         Point p1 = map.getProjection().toScreenLocation(new LatLng(lat, lng));
         Point p2 = map.getProjection().toScreenLocation(new LatLng(lat2, lng2));
         return Math.abs(p1.x - p2.x);
         }
         
         return 100;
         
    }
	
	
}
