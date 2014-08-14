package com.wifreemaps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;



import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	private static GoogleMap mMap = null;
	private static Handler handler = new Handler(Looper.getMainLooper());

	TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	StringBuilder sb = new StringBuilder();
	PromptWifi popupWifi;
	PromptGPS popupGPS;
	LocationManager mainLocationManager;
	MySQLiteHelper db;
	private static DataHandler dataHandler;
	private static int databaseState=0;
	
	List<OpenNetwork> currentNetworks;
	List<NetworkPoint> networkPoints;

	//simulate adding new points with this handler
	private Handler mHandler;
	private int mInterval = 5000;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main); 

		mMap=((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.setMyLocationEnabled(true);

		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			private float currentZoom = -1;
			@Override
			public void onCameraChange(CameraPosition position) {
				if (position.zoom != currentZoom){
					currentZoom = position.zoom;  // here you get zoom level

					Log.d("CurrentZoomLevel","LVL:"+currentZoom);

					if(currentZoom > 19)
					{	//FOR zoom levels 20 and 21
						//SHOW MOST DETAILED VIEW
					}
					else if(currentZoom == 19)
					{
						//First simplified shape of network, expect up to 10 networks
					}
					else if(currentZoom == 18)
					{
						//second simplified view of network, up to 30 networks
					}
					else if(currentZoom == 17)
					{
						//Every network is represented with colored circle
					}
					else if(currentZoom == 16)
					{
						//Every network is a single spot/dot
					}
					else if(currentZoom == 15)
					{
						//group up to 10 networks in single circle
					}
					else if(currentZoom == 14)
					{
						//goup all networks into 6 areas with networks, circles or something
					}
					else if(currentZoom < 14)
					{
						//group networks that are more than x meters(7x7 grid to zoom, center is average position = average position) apart into dots
					}
				}
			}

		});


		

		mainText = (TextView) findViewById(R.id.wifilist);


		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		if(!mainWifi.isWifiEnabled()){
			//wifi not enabled, prompt to enable
			popupWifi=new PromptWifi();
			popupWifi.show(getFragmentManager(), "popup WiFi");


		}

		mainLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!mainLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			//GPS not enabled, prompt
			popupGPS=new PromptGPS();
			popupGPS.show(getFragmentManager(),"popup GPS");
		}



		
		//find current city and get data from database...
		
		
		// The minimum time (in miliseconds) the system will wait until checking if the location changed
		int minTime = 60000;
		// The minimum distance (in meters) traveled until you will be notified
		float minDistance = 15;
		// Create a new instance of the location listener
		MyLocationListener myLocListener = new MyLocationListener();
		// Get the location manager from the system
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Get the criteria you would like to use
		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setSpeedRequired(false);
		// Get the best provider from the criteria specified, and false to say it can turn the provider on if it isn't already
		String bestProvider = locationManager.getBestProvider(criteria, false);
		// Request location updates
		locationManager.requestLocationUpdates(bestProvider, minTime, minDistance, myLocListener);
		
		
		

		//try with database
		db = new MySQLiteHelper(this);
		dataHandler = new DataHandler(db);

		//ADD GROUND OVERLAY which represents wifi network
//		Bitmap radiusImageSource=BitmapFactory.decodeResource(getResources(), R.drawable.fadingout);



		//ADD SOME COLOR   
//		BitmapDescriptor radiusImage=GetCustomBitmapDescriptor(radiusImageSource, Color.CYAN);//BitmapDescriptorFactory.fromBitmap(radiusImageSource);

		//add something to database

		if(db.isDBempty()){
			//addSomeData();
			handler.post(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					dataHandler.addSamplePointsToDatabase(getBaseContext());
			    	databaseState=1;
					
				} 
				   // your UI code here 
				});
		}
		else
			databaseState = 1;


		

		//draw points on map
		handler.post(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (databaseState != 1)
				{
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				updateMapWithPoints();
				handler.post(new Runnable(){

					@Override
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//now focus on average point
						LatLng locationtocenter = findCenterPoint();
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationtocenter, 19.0f));
				}
				});
				
				
			} 
			   // your UI code here 
			});
		//updateMap(); //in seperate thread
//		updateMapWithPoints();


		//simulate adding new point every second or so - not very good
		//        mHandler = new Handler();
		//        startRepeatingTask();

		//for debugging 
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		mainWifi.startScan();
		mainText.setText("\nStarting Scan...\n");
		mainText.setMovementMethod(new ScrollingMovementMethod());

		

		



	}

	private boolean updateMapWithPoints() {
		
		
		
		
		if(databaseState != 0) {
		
		
		currentNetworks = db.getAllNetworks();
		//List<OpenNetwork> currentNetworkPoints = new ArrayList<OpenNetwork>();
		List<String> uniqueNetworks=new ArrayList<String>();
		List<Integer> networkColors = new ArrayList<Integer>();
		//ADD GROUND OVERLAY which represents wifi network
		Bitmap radiusImageSource=BitmapFactory.decodeResource(getResources(), R.drawable.fadingout);
		
		
		
				
		
		//count different newtorks and mix some colors
		for(OpenNetwork ntwk : currentNetworks)
		{
			if(! uniqueNetworks.contains(ntwk.getBSSID()) )
			{
				uniqueNetworks.add(ntwk.getBSSID());
				int mixNewColor=Color.argb(255, (int)(Math.random()*200 + 55), (int)(Math.random()*200 + 55), (int)(Math.random()*200 + 55));
				networkColors.add(mixNewColor);
			}
		}
		
		//draw this points with corresponding colors
		for(String bssid : uniqueNetworks)
		{
			
			BitmapDescriptor radiusImage=GetCustomBitmapDescriptor(radiusImageSource, networkColors.get(uniqueNetworks.indexOf(bssid)));

			
			db=new MySQLiteHelper(this);
			networkPoints=db.getNetworkPoints(bssid);
			
			for(NetworkPoint point: networkPoints)
			{
				GroundOverlayOptions openWifiSpot = new GroundOverlayOptions()
				.image(radiusImage)
				.position(point.getLocation(), (float)point.getGpsAccuracy(), (float)point.getGpsAccuracy()) //Na tem mestu poraèunati natanènost in sinhronizirati z velikostjo
				.transparency(1.0f-(float)point.getQuality()); //odvisno od kvalitete signala pobarvati med 0 in 1, izdelati naèin da se porazdeli na prostor- > (moc.signala/max.moc)/povrsina
				mMap.addGroundOverlay(openWifiSpot);
			}
			//Log.v("WIFIADDED"," bssid:"+bssid);
		}
		
		return true;
		}
		return false;
	}



	private LatLng findCenterPoint(){
		float avglat=0,avglng=0;
		int pointCount=0;
		List<OpenNetwork> allpoints = returnAllNetworksFromDB();

		for(OpenNetwork ntwk : allpoints)
		{
			pointCount++;
			avglat += ntwk.getLat();
			avglng += ntwk.getLng();
		}

		avglat = avglat / pointCount;
		avglng = avglng / pointCount;
		Log.v("FoundCenterPoint","Lat:"+avglat+";Lng:"+avglng + ", Pointcnt:"+pointCount);
		return new LatLng(avglat, avglng);

	}


	private List<OpenNetwork> returnAllNetworksFromDB() {
		List<OpenNetwork> result = db.getAllNetworks();
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		mainWifi.startScan();
		mainText.setText("Starting Scan");
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(receiverWifi);
		super.onPause();
	}

	@Override
	protected void onResume() {
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context c, Intent intent) {
			sb = new StringBuilder();
			wifiList = mainWifi.getScanResults();
			for(int i = 0; i < wifiList.size(); i++){
				sb.append(new Integer(i+1).toString() + ".");
				sb.append((wifiList.get(i)).toString());
				sb.append("\n");
			}
			mainText.setText(sb);
		}
	}

	@Override
	protected void onStop(){
		super.onStop();
		db.close();
	}


	private BitmapDescriptor GetCustomBitmapDescriptor (Bitmap basicBitmap, int wifiColor)
	{
		if(wifiColor == -1)
		{
			wifiColor = Color.RED;
		}
		Bitmap resultBitmap = basicBitmap.copy(Bitmap.Config.ARGB_8888, true);
		Paint selectedPaint = new Paint();
		//selectedPaint.setColor(Color.RED);
		ColorFilter filter = new LightingColorFilter(wifiColor, 1);
		Matrix matrix= new Matrix();
		selectedPaint.setColorFilter(filter);

		Canvas canvas = new Canvas (resultBitmap);

		canvas.drawBitmap(resultBitmap, matrix, selectedPaint);
		//canvas.drawCircle(resultBitmap.getWidth()/2, resultBitmap.getHeight()/2, 100f, selectedPaint);//(resultBitmap,2f,2f,selectedPaint);

		BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resultBitmap);

		return icon;




	}


	private GroundOverlayOptions[] getAllKnownPoints(int size, int wificolor, float GPSlat, float GPSlon){
		GroundOverlayOptions[] result = new GroundOverlayOptions[size];



		//ADD GROUND OVERLAY which represents wifi network
		Bitmap radiusImageSource=BitmapFactory.decodeResource(getResources(), R.drawable.circles);
		//ADD SOME COLOR   
		BitmapDescriptor radiusImage=GetCustomBitmapDescriptor(radiusImageSource, wificolor);

		// IJS coordinates 46.042931, 14.487516
		float lat=GPSlat;
		float lon=GPSlon;
		float addLat,addLon;
		LatLng WifiLocation = null;
		float wifiPower = 0.0f;
		float gpsAccuracy= 0.0f;


		//TODO: recover points from database in the future
		for(int i=0; i < result.length; i++)
		{
			//read
			if(Math.random() > 0.5)
				addLat=(float)(Math.random()*0.0001);
			else
				addLat=(float) (Math.random()*0.0001)*-1;
			if(Math.random() > 0.5)
				addLon=(float)(Math.random()*0.0001);
			else
				addLon=(float)(Math.random()*0.0001)*-1;

			WifiLocation =  new LatLng(lat+addLat, lon+addLon);
			gpsAccuracy = (float) (Math.random()*20)+1.0f;
			wifiPower = 0.9f - (float) (Math.random() / gpsAccuracy); //1.0 means no signal at all, 0 means full power
			//end read

			result[i] = new GroundOverlayOptions()
			.image(radiusImage)
			.position(WifiLocation, gpsAccuracy, gpsAccuracy) //Na tem mestu poraèunati natanènost in sinhronizirati z velikostjo
			.transparency(wifiPower); //odvisno od kvalitete signala pobarvati med 0 in 1, izdelati naèin da se porazdeli na prostor- > (moc.signala/max.moc)/povrsina

		}

		return result;
	}


	Runnable mStatusChecker = new Runnable() {
		@Override 
		public void run() {
			addNewPoint(); //this function can change value of mInterval.
			mHandler.postDelayed(mStatusChecker, mInterval);
		}
	};

	void startRepeatingTask() {
		mStatusChecker.run(); 
	}

	void stopRepeatingTask() {
		mHandler.removeCallbacks(mStatusChecker);
	}

	private void addNewPoint()
	{

		float lat=46.042931f;
		float lon=14.487516f;
		GroundOverlayOptions[] allKnownPoints = new GroundOverlayOptions[20];
		allKnownPoints = getAllKnownPoints(allKnownPoints.length, Color.MAGENTA,lat,lon);
		//System.out.println("Adding some data to map...");
		for(int i= 0; i < allKnownPoints.length;i++)
		{
			mMap.addGroundOverlay(allKnownPoints[i]);
		}
	}
	
	
	
	
	private void findCurrentLocation(double latitude, double longitude)
	{
		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
		StringBuilder builder = new StringBuilder();
		try {
		    List<Address> address = geoCoder.getFromLocation(latitude, longitude, 1);
		    int maxLines = address.get(0).getMaxAddressLineIndex();
		    for (int i=0; i<maxLines; i++) {
		    String addressStr = address.get(0).getAddressLine(i);
		    builder.append(addressStr);
		    builder.append(" ");
		    }

		String finalAddress = builder.toString(); //This is the complete address.
		Log.d("LOCATION IDENTIFIED","Current address:"+finalAddress);
		} catch (IOException e) {}
		  catch (NullPointerException e) {}
	}
	 
	
	
	
	private class MyLocationListener implements LocationListener
	{
	   @Override
	   public void onLocationChanged(Location loc)
	   {
	      if (loc != null)
	      {
	         // Do something knowing the location changed by the distance you requested
	    	  findCurrentLocation(loc.getLatitude(),loc.getLongitude());
	      }
	   }

	   @Override
	   public void onProviderDisabled(String arg0)
	   {
	      // Do something here if you would like to know when the provider is disabled by the user
	   }

	   @Override
	   public void onProviderEnabled(String arg0)
	   {
	      // Do something here if you would like to know when the provider is enabled by the user
	   }

	   @Override
	   public void onStatusChanged(String arg0, int arg1, Bundle arg2)
	   {
	      // Do something here if you would like to know when the provider status changes
	   }
	}
	
	//test runnable thread
	
	  public void updateMap() {
		  
		 
		    // do something long
		    Runnable runnable = new Runnable() {
		      @Override
		      public void run() {
		        
		          
		          updateMapWithPoints();
		        
		      }
		    };
		    new Thread(runnable).start();
		  }

	public void addSomeData() {
		// do something long
	    Runnable runnable = new Runnable() {
	      @Override
	      public void run() {
	        
	          
	    	  dataHandler.addSamplePointsToDatabase(getBaseContext());
	    	  databaseState=1;
	      }
	    };
	    new Thread(runnable).start();
	}
	
	
}