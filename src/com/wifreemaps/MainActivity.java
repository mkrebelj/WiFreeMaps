package com.wifreemaps;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;




import android.R.color;
import android.R.drawable;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;



import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	private GoogleMap mMap = null;


	TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	StringBuilder sb = new StringBuilder();
	PromptWifi popupWifi;
	PromptGPS popupGPS;
	LocationManager mainLocationManager;
	MySQLiteHelper db;
	List<OpenNetwork> currentNetworkPoints;


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




		//try with database
		db = new MySQLiteHelper(this);


		//ADD GROUND OVERLAY which represents wifi network
		Bitmap radiusImageSource=BitmapFactory.decodeResource(getResources(), R.drawable.fadingout);



		//ADD SOME COLOR   
//		BitmapDescriptor radiusImage=GetCustomBitmapDescriptor(radiusImageSource, Color.CYAN);//BitmapDescriptorFactory.fromBitmap(radiusImageSource);

		//add something to database

		addSamplePointsToDatabase();

		// IJS coordinates 46.042931, 14.487516
//		LatLng IJSLocation = new LatLng(46.042931, 14.487516);
//		GroundOverlayOptions openWifiSpot = new GroundOverlayOptions()
//		.image(radiusImage)
//		.position(IJSLocation, 5f, 5f) //Na tem mestu poraèunati natanènost in sinhronizirati z velikostjo
//		.transparency(0.5f); //odvisno od kvalitete signala pobarvati med 0 in 1, izdelati naèin da se porazdeli na prostor- > (moc.signala/max.moc)/povrsina
//		mMap.addGroundOverlay(openWifiSpot);

		//add other networks
		//        float lat=46.042931f;
		//        float lon=14.487516f;
		//        GroundOverlayOptions[] allKnownPoints = new GroundOverlayOptions[20];
		//        allKnownPoints = getAllKnownPoints(allKnownPoints.length,Color.GREEN,lat,lon);
		//        for(int i= 0; i < allKnownPoints.length;i++)
		//        {
		//        	mMap.addGroundOverlay(allKnownPoints[i]);
		//        }
		//        
		//        lat = 46.051351f;
		//        lon = 14.487600f;
		//        allKnownPoints = new GroundOverlayOptions[40];
		//        allKnownPoints = getAllKnownPoints(allKnownPoints.length, Color.BLUE,lat,lon);
		//        for(int i= 0; i < allKnownPoints.length;i++)
		//        {
		//        	mMap.addGroundOverlay(allKnownPoints[i]);
		//        }
		//        
		//        int[] colors = {Color.BLACK,
		//        		Color.CYAN,
		//        		Color.MAGENTA,
		//        		Color.WHITE,
		//        		Color.YELLOW, 
		//        		Color.argb(255, 255, 255, 0),
		//        		Color.argb(255, 0, 255, 100),
		//        		Color.argb(255, 100, 255, 5),
		//        		Color.argb(255, 55, 25, 150),
		//        		Color.argb(255, 75, 155, 10)};
		//        for(int j=0; j < 10; j++)
		//        {
		//        	
		//	        lat = 46.042931f + (float)(Math.random()*0.0011);
		//	        lon = 14.487516f + (float)(Math.random()*0.0011);
		//	        
		//	        allKnownPoints = new GroundOverlayOptions[30];
		//	        allKnownPoints = getAllKnownPoints(allKnownPoints.length, colors[j],lat,lon);
		//	        for(int i= 0; i < allKnownPoints.length;i++)
		//	        {
		//	        	mMap.addGroundOverlay(allKnownPoints[i]);
		//	        }
		//        }

		//now focus on average point
		LatLng locationtocenter = findCenterPoint();
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationtocenter, 19.0f));

		//draw points on map

		updateMapWithPoints();


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

	private void updateMapWithPoints() {
		currentNetworkPoints = db.getAllNetworkPoints();
		//List<OpenNetwork> currentNetworkPoints = new ArrayList<OpenNetwork>();
		List<String> uniqueNetworks=new ArrayList<String>();
		List<Integer> networkColors = new ArrayList<Integer>();
		//ADD GROUND OVERLAY which represents wifi network
		Bitmap radiusImageSource=BitmapFactory.decodeResource(getResources(), R.drawable.fadingout);
		
		
		currentNetworkPoints = db.getAllNetworkPoints();
				
		
		//count different newtorks and mix some colors
		for(OpenNetwork ntwk : currentNetworkPoints)
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

			
			currentNetworkPoints=db.getNetworkPoints(bssid);
			for(OpenNetwork point: currentNetworkPoints)
			{
				GroundOverlayOptions openWifiSpot = new GroundOverlayOptions()
				.image(radiusImage)
				.position(point.getLocationAsCoordinate(), point.getGPSaccuracy(), point.getGPSaccuracy()) //Na tem mestu poraèunati natanènost in sinhronizirati z velikostjo
				.transparency(1.0f-point.getWiFiStrengths()/(float)Math.sqrt(point.getGPSaccuracy())); //odvisno od kvalitete signala pobarvati med 0 in 1, izdelati naèin da se porazdeli na prostor- > (moc.signala/max.moc)/povrsina
				mMap.addGroundOverlay(openWifiSpot);
			}
			Log.v("WIFIADDED"," bssid:"+bssid);
		}
		
		

	}

	private void addSamplePointsToDatabase() {
		//only for debugging

		//add 10 networks to database
		String bssid,ssid;
		int freq,alive,newpoints;
		float startLat,startLng,latitude,longitude,accuracy,signal;
		OpenNetwork toAdd;

		for(int i = 0; i < 10; i++)
		{
			//for each add from 5 to 15 new points
			bssid="bssid"+i+"sample";
			ssid="network"+i+"name";
			freq= i%3;//(int)(Math.random()*14);
			alive=( i%4==3 ? 0:1 );
			startLat =46.042931f +(float)(Math.random()*0.001);
			startLng =14.487516f +(float)(Math.random()*0.001);
			//signal -35dbm(and higher) = 100%; -95dbm = 1%, linear
			//accuracy is location in meters, 68% confidence that location is inside circle with r as accuracy: 
			//accuracy of 4 means that location is roughly inside 8 meter of diameter
			newpoints=(int)(Math.random()*11+5);


			for (int j = 0;j<newpoints;j++){

				latitude = startLat + (float)(Math.random()*0.0001);
				longitude = startLng +  +(float)(Math.random()*0.0001);
				accuracy = (int)(Math.random()*10+1);
				signal = (float) (Math.random());
				toAdd = new OpenNetwork(bssid, ssid, freq, new LatLng(latitude, longitude), accuracy, signal,alive);
				db.addNetwork(toAdd);
				
			}

		}



	}

	private LatLng findCenterPoint(){
		float avglat=0,avglng=0;
		int pointCount=0;
		List<OpenNetwork> allpoints = returnAllPointsFromDB();

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


	private List<OpenNetwork> returnAllPointsFromDB() {
		List<OpenNetwork> result = db.getAllNetworkPoints();
		return result;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		mainWifi.startScan();
		mainText.setText("Starting Scan");
		return super.onMenuItemSelected(featureId, item);
	}

	protected void onPause() {
		unregisterReceiver(receiverWifi);
		super.onPause();
	}

	protected void onResume() {
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	class WifiReceiver extends BroadcastReceiver {
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

}