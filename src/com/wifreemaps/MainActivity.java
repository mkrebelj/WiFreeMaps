package com.wifreemaps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.internal.hh;
import com.google.android.gms.internal.ln;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.MarkerManager.Collection;
import com.google.maps.android.heatmaps.Gradient;

import com.google.maps.android.heatmaps.WeightedLatLng;




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
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;



import com.google.maps.android.heatmaps.HeatmapTileProvider.Builder;




public class MainActivity extends FragmentActivity {
	
	
	private static final int DISPLAY_MODE=2; //1= my custom method; 2= heatmaps
	
	
	private static GoogleMap mMap = null;
	private static Handler handler = new Handler(Looper.getMainLooper());
	private float currentZoomLevel;
	private boolean zoomingIn = true;
	private boolean detailedView = false, detailedViewLoaded = false;
	private List<String> uniqueNetworks=new ArrayList<String>();
	private List<Integer> networkColors = new ArrayList<Integer>();
	private List<NetworkMarking> drawableNetworkMarkings = new ArrayList<NetworkMarking>();
	private List<NetworkPoint> allAvailableNetworkPoints = new ArrayList<NetworkPoint>();
	private ArrayList<LatLng> networkPointsInRange = new ArrayList<LatLng>();
	private LatLngBounds curScreen;
	
	public static final String PSK = "PSK";
    public static final String WEP = "WEP";
    public static final String EAP = "EAP";
    public static final String OPEN = "Open";

	
	//statics
    private static final int TIME_BETWEEN_ADDING_NEW_POINT=15000; //how often can system add new point to data
    private static final int TIME_FOR_MAP_UPDATE=10000; //map update rate
	private boolean newPointDetected = false;

	TextView mainText;
	TextView debugText;
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
	private LatLng myCurrentLocation;
	private double currentGPSAccuracy=50;
	private List <OpenNetwork> networksInRange=new ArrayList<OpenNetwork>();
	
	
	List<OpenNetwork> currentNetworks=new ArrayList<OpenNetwork>();
	List<NetworkPoint> networkPoints=new ArrayList<NetworkPoint>();
	
	ArrayList<LatLng> currentNetworkPointsForHeatmaps = new ArrayList<LatLng>();
	
	//simulate adding new points with this handler
	private int mInterval = 10000; // 5 seconds by default, can be changed later
	private Handler mHandler;
	private long lastRefreshTime;
	private long lastMapRefresh;
	
	double lat1,lon1;
	
	//NOVO
	 /**
     * Alternative radius for convolution
     */
	private static final int ALT_HEATMAP_RADIUS_20 = 20;
    private static final int ALT_HEATMAP_RADIUS_19 = 30;
    private static final int ALT_HEATMAP_RADIUS_18 = 40;
    private static final int ALT_HEATMAP_RADIUS_17 = 55;
    private static final int ALT_HEATMAP_RADIUS_16 = 75;
    private static final int ALT_HEATMAP_RADIUS_15 = 110;
    private static final int ALT_HEATMAP_RADIUS_14 = 160;
    private static final int ALT_HEATMAP_RADIUS_13 = 199;
    
    

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.4;

    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);
    private MyHeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
	boolean alreadyAdded=false;
	private boolean mDefaultGradient = true;
    private boolean mDefaultRadius = true;
    private boolean mDefaultOpacity = true;
    private HashMap<String, DataSet> mLists = new HashMap<String, DataSet>();

    protected int getLayoutId() {
        return R.layout.activity_main;
    }
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main); 
		mHandler = new Handler();

		mMap=((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mMap.setMyLocationEnabled(true);

		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			private float currentZoom = -1;
			@Override
			public void onCameraChange(CameraPosition position) {
				if (position.zoom != currentZoom){
					currentZoom = position.zoom;  // here you get zoom level
					
					

					Log.d("CurrentZoomLevel","LVL:"+currentZoom);
					if(currentZoomLevel > currentZoom)
						zoomingIn=false;
					else
						zoomingIn=true;
					currentZoomLevel=currentZoom;
					
					
					curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
					
					Log.d("CURRENTSCREENBOUNDS", curScreen.toString());
					
					showSimplifiedNetworkInViewport();
					
				}
			}

		});

		//SETUP
		myCurrentLocation = new LatLng(41.903616, 12.465888); //for historic reasons, Rome is the center of the world
		lastRefreshTime=System.currentTimeMillis();
		
		//VIEWS
		mainText = (TextView) findViewById(R.id.wifilist);
		debugText = (TextView) findViewById(R.id.status);

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
		int minTime = 1000;
		// The minimum distance (in meters) traveled until you will be notified
		float minDistance = 4;
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
		///another method for location...
		
		
		
		_getLocation();
		

		//try with database
		db = new MySQLiteHelper(this);
		dataHandler = new DataHandler(db);

		//ADD GROUND OVERLAY which represents wifi network
//		Bitmap radiusImageSource=BitmapFactory.decodeResource(getResources(), R.drawable.fadingout);



		//ADD SOME COLOR   
//		BitmapDescriptor radiusImage=GetCustomBitmapDescriptor(radiusImageSource, Color.CYAN);//BitmapDescriptorFactory.fromBitmap(radiusImageSource);

		//add something to database

		if(db.isDBempty()){
			databaseState = 0;
		}
		else
			databaseState = 1;


		

		//draw points on map
		handler.post(new Runnable(){

			@Override
			public void run() {	

				//load initial data from database
				if(databaseState != 0)
				{
					updateMapWithPointsFromDatabase();
					handler.post(new Runnable(){

						@Override
						public void run() {
							
							//now focus on average point
							LatLng locationtocenter = findCenterPoint();
							mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationtocenter, 19.0f));
							
			                
			               
						}
					});
				}
				
				
				

				
				
			} 
			   
			});
		


		//for debugging 
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		mainWifi.startScan();
		mainText.setText("\nStarting Scan...\n");
		mainText.setMovementMethod(new ScrollingMovementMethod());
		debugText.setMovementMethod(new ScrollingMovementMethod());
		
		//periodic refresh of googlemaps
		
		startRepeatingTask();

		

	}

	protected void showSimplifiedNetworkInViewport() {
		// TODO Auto-generated method stub
		

//		if(currentZoomLevel == 15)
//		{
//			
//			//group up to 10 networks in single circle
//		}
//		else if(currentZoomLevel == 14)
//		{
//			
//			//goup all networks into 6 areas with networks, circles or something
//		}
//		else if(currentZoomLevel < 14)
//		{
//			
//			//group networks that are more than x meters(7x7 grid to zoom, center is average position = average position) apart into dots
//		}
//		
		if(DISPLAY_MODE == 2)
		{
		
			//put this to different thread, so it would not crash
			
			
			
			handler.post(new Runnable(){

				@Override
				public void run() {	
					
					

					if(currentZoomLevel <= 17 && !zoomingIn)
					{
						detailedView=false;
						detailedViewLoaded = false;
						//Only one point for whole network
						networkPointsInRange.clear();
						for(OpenNetwork ntwk : currentNetworks)
						{
							if(curScreen.contains(ntwk.getLocationAsCoordinate()))
							{
								
								networkPointsInRange.add(ntwk.getLocationAsCoordinate());
								newPointDetected = true;
								
								
							}
						}
						currentNetworkPointsForHeatmaps = networkPointsInRange;
						updateMapWithPointsFromMemory();
					}
					else if(currentZoomLevel > 17 )
					{
						detailedView=true;
						if(!detailedViewLoaded){
							networkPointsInRange.clear();
							for(OpenNetwork ntwk : currentNetworks){
	
								
	
								if(curScreen.contains(ntwk.getLocationAsCoordinate()))
								{
									for(NetworkPoint point : allAvailableNetworkPoints)
									{
										if(point.getBSSID().equals(ntwk.getBSSID())){
											networkPointsInRange.add(point.getLocation());
											newPointDetected = true;
										}
											
									}
								}
							}	
							detailedViewLoaded = true;
							currentNetworkPointsForHeatmaps = networkPointsInRange;
							updateMapWithPointsFromMemory();
						}
						
						
					}
					
					}

				});
			
			
			
			
		}
		
	}



	/**
	 * @return Returns true if reading from database is successful
	 */
	private boolean updateMapWithPointsFromDatabase() {
		
		
		Log.d("UPDATING MAP","points should be added");
		Log.d("UPDATING WITH ZOOM LEVEL:","ZOOMLVL:"+currentZoomLevel);
		
		
		
		switch(DISPLAY_MODE)
		{
		case 1:
		{
			if(databaseState != 0) {


				currentNetworks = db.getAllNetworks();
				//List<OpenNetwork> currentNetworkPoints = new ArrayList<OpenNetwork>();
				mMap.clear();
				
				
				//MY method for drawing onto map
				//ADD GROUND OVERLAY which represents wifi network
				Bitmap radiusImageSource=BitmapFactory.decodeResource(getResources(), R.drawable.fadingout);


				Log.d("UPDATING MAP","searching for networks in database");


				//count different newtorks and mix some colors. Add new networks to current map if necessary
				for(OpenNetwork ntwk : currentNetworks)
				{
					if(! uniqueNetworks.contains(ntwk.getBSSID()) )
					{
						uniqueNetworks.add(ntwk.getBSSID());
						int mixNewColor=Color.argb(255, (int)(Math.random()*200 + 55), (int)(Math.random()*200 + 55), (int)(Math.random()*200 + 55));
						networkColors.add(mixNewColor);
						Log.d("UPDATING MAP","added new unique network and color");
					}
					
				}

				
				
				
				
				Log.d("UPDATING MAP","about to draw points on map");
				
				allAvailableNetworkPoints.clear();
				
				for(String bssid : uniqueNetworks)
				{
					
					
					BitmapDescriptor radiusImage=GetCustomBitmapDescriptor(radiusImageSource, networkColors.get(uniqueNetworks.indexOf(bssid)));


					db=new MySQLiteHelper(this);
					networkPoints=db.getNetworkPoints(bssid);

					
					//TODO: poskusi razdelit toèke v velikostne razrede in dodaj veè razliènih moverlayev na mapo- z razliènmi velikostmi toèk
					
					
					
					float pointIntensity=0;
					for(NetworkPoint point: networkPoints)
					{
						
						//add every point to working set for later use
						allAvailableNetworkPoints.add(point);
						
						pointIntensity=0.9f-(float)point.getQuality();
						
						GroundOverlayOptions openWifiSpot = new GroundOverlayOptions()
						.image(radiusImage)
						.position(point.getLocation(), (float)point.getGpsAccuracy(), (float)point.getGpsAccuracy()) //Na tem mestu poraèunati natanènost in sinhronizirati z velikostjo
						.transparency(pointIntensity); //odvisno od kvalitete signala pobarvati med 0 in 1, izdelati naèin da se porazdeli na prostor- > (moc.signala/max.moc)/povrsina
						mMap.addGroundOverlay(openWifiSpot);
						Log.d("Point quality:",point.getQuality()+"");
					}
					Log.d("WIFIADDED on MAP"," bssid:"+bssid);
				}
				alreadyAdded=true;
			}
			return true;
		}
			
			
			
		case 2:
		{


			//dodajanje heatmapsov

			if(databaseState != 0) {

				currentNetworks = db.getAllNetworks();
				
				//in case we would need all data from points
				allAvailableNetworkPoints.clear();
				mMap.clear();
				for(OpenNetwork ntwk : currentNetworks)
				{
					networkPoints=db.getNetworkPoints(ntwk.getBSSID());
					
					for(NetworkPoint point:networkPoints)
					{
						allAvailableNetworkPoints.add(point);
						currentNetworkPointsForHeatmaps.add(point.getLocation());
					}
					
				}

				

				mLists.clear();
				mLists.put("WifiTocke", new DataSet( currentNetworkPointsForHeatmaps,
						"fakeurl"));
				mProvider = new MyHeatmapTileProvider.Builder().data(
						mLists.get("WifiTocke").getData()).build();
				
				mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
				mOverlay.clearTileCache();
			}
		}
			
		default:
			Log.d("NODATATOADD","No data in database to add on map...");
			return false;
		}
		



	}

	
	
	
	
private boolean updateMapWithPointsFromMemory() {
	
		//data is already in memory, no need to read from database
		
	
		Log.d("UPDATING MAP","points should be added");
		Log.d("UPDATING WITH ZOOM LEVEL:","ZOOMLVL:"+currentZoomLevel);
		
		
		
		switch(DISPLAY_MODE)
		{
		case 1:
		{

			if(newPointDetected){
				//ADD GROUND OVERLAY which represents wifi network
				Bitmap radiusImageSource=BitmapFactory.decodeResource(getResources(), R.drawable.fadingout);
				mMap.clear();

				Log.d("UPDATING MAP","searching for networks in working memory");

				
				Log.d("UPDATING MAP","about to draw points on map");
				for(String bssid : uniqueNetworks)
				{

					BitmapDescriptor radiusImage=GetCustomBitmapDescriptor(radiusImageSource, networkColors.get(uniqueNetworks.indexOf(bssid)));

					networkPoints.clear();
					for(NetworkPoint point:allAvailableNetworkPoints)
					{
						if(point.getBSSID().equals(bssid))
							networkPoints.add(point);
					}
					
					float pointIntensity=0;
					for(NetworkPoint point: networkPoints)
					{
						pointIntensity=0.9f-(float)point.getQuality();
						
						GroundOverlayOptions openWifiSpot = new GroundOverlayOptions()
						.image(radiusImage)
						.position(point.getLocation(), (float)point.getGpsAccuracy(), (float)point.getGpsAccuracy()) //Na tem mestu poraèunati natanènost in sinhronizirati z velikostjo
						.transparency(pointIntensity); //odvisno od kvalitete signala pobarvati med 0 in 1, izdelati naèin da se porazdeli na prostor- > (moc.signala/max.moc)/povrsina
						mMap.addGroundOverlay(openWifiSpot);
						Log.d("Point quality:",point.getQuality()+"");
					}
					Log.d("WIFIADDED on MAP from Memory"," bssid:"+bssid);
				}
				newPointDetected=false;
				return true;
				}
			}
			
		
		
			
			
			
		case 2:
		{


			//dodajanje heatmapsov

			if(newPointDetected)
			{
			
				mLists.clear();
				
				
				mLists.put("WifiTocke", new DataSet( currentNetworkPointsForHeatmaps,
						"fakeurl"));
				
				
				
				mProvider = new MyHeatmapTileProvider.Builder().data(
						mLists.get("WifiTocke").getData()).build();
				
				mMap.clear();
				
				
				mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
				mOverlay.clearTileCache();
				
				newPointDetected=false;
				return true;
			}
			
			Log.d("NUMBER OF POINTS","Currently shown on map:"+currentNetworkPointsForHeatmaps.size()+" points");
			
		}
			
		default:
			Log.d("NODATATOADD","No new data to add on map...");
			return false;
		}
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
		menu.add(1,1,1,"Show Networks");
		menu.add(2,2,2,"Add Dummy Data");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int selected=item.getItemId();
		if(selected == 0){
			mainWifi.startScan();
			mainText.setText("Starting Scan");
		}
		else if(selected == 1)
		{
			Intent intent=new Intent(getBaseContext(), ShowNetworksActivity.class);
			startActivity(intent);
		}
		else if(selected == 2)
		{
			//add some dummy data to database
			int newDummyNetworkCount = addDummyData();
			Toast.makeText(this, "Added "+newDummyNetworkCount+ " dummy points to database", Toast.LENGTH_SHORT).show();
			
		}
		Log.d("ITEM ID:",item.getItemId()+" pressed");
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
			
			List <OpenNetwork> currentNetworksInRange = new ArrayList<OpenNetwork>();
			OpenNetwork temp;
			
			sb = new StringBuilder();
			wifiList = mainWifi.getScanResults();
//			for(int i = 0; i < wifiList.size(); i++){
//				sb.append(new Integer(i+1).toString() + ".");
//				sb.append((wifiList.get(i)).toString());
//				sb.append("\n");
//			}
//			mainText.setText(sb);
			
			
			//check if there is any open access points
			
			String [] securityModes = {WEP, PSK, EAP};
			boolean open=true;
			int idx=0;
			currentNetworksInRange.clear();
			for(ScanResult scan : wifiList)
			{
				//opened until proved secure
				open = true;
				
				for (int i = securityModes.length - 1; i >= 0; i--) {
		            if (scan.capabilities.contains(securityModes[i])) {
		                 open=false;
		            }
		            
		        }
				
				if(open)
				{
					sb.append(new Integer(idx+1).toString() + ".");
					sb.append("SSID: "+ scan.SSID);
					sb.append("BSSID: " +scan.BSSID);
					sb.append("Capas: "+ scan.capabilities);
					sb.append("Freq:" +scan.frequency);
					
					sb.append("\n");
					idx++;
					
					temp=new OpenNetwork(scan.BSSID, scan.SSID, "cityname", scan.frequency, myCurrentLocation, 0);
					currentNetworksInRange.add(temp);
					
				}

			}
			mainText.setText(sb);
			
			//try to add this networks to existing networks
			networksInRange.clear();
			networksInRange = currentNetworksInRange;
			
			for(OpenNetwork ntwk:networksInRange)
			{
				addNewNetwork(ntwk);
			}
			
			
			
			
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


	
	
	
	
	
	
	private void findCurrentLocation(double latitude, double longitude)
	{
		
		myCurrentLocation = new LatLng(latitude, longitude);
		
		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
		StringBuilder builder = new StringBuilder();
		String finalAddress = "";
		try {
		    List<Address> address = geoCoder.getFromLocation(latitude, longitude, 4);
		    int maxLines = address.get(0).getMaxAddressLineIndex();
		    for (int i=0; i<maxLines; i++) {
		    String addressStr = address.get(0).getAddressLine(i);
		    builder.append(addressStr);
		    builder.append(" ");
		    }

		finalAddress = builder.toString(); //This is the complete address.
		Log.d("LOCATION IDENTIFIED","Current address:"+finalAddress);
		} catch (IOException e) {
			Log.d("LOCATION IDENTIFIED","failed");
		}
		  catch (NullPointerException e) {}
		
		debugText.setText("Current location:"+ String.format ("%s",latitude)+ " "+String.format ("%s",longitude));
//		if(finalAddress != "")
//			debugText.setText(debugText.getText() + "/n" + finalAddress);
		
		
		
	}
	 
	
	
	
	private void addPointsOnCurrentLocation() {
		Log.d("AddNewPoints","Adding new points in current networks, if any");
		double normalisedLevel=0;
		String [] securityModes = {WEP, PSK, EAP};
		boolean open=true;
		if(lastRefreshTime - System.currentTimeMillis() < TIME_BETWEEN_ADDING_NEW_POINT){
			for(ScanResult ntwkScan:wifiList)
			{
				{
					//opened until proved secure
					open = true;

					for (int i = securityModes.length - 1; i >= 0; i--) {
						if (ntwkScan.capabilities.contains(securityModes[i])) {
							open=false;
						}

					}

					if(open)
					{
						Log.d("DetailsForAddedPoint:","ScanLVL:"+ntwkScan.level +" current accuracy:"+currentGPSAccuracy);
						normalisedLevel=(-35.0f/(ntwkScan.level))/currentGPSAccuracy*1.0f *10;
						Log.d("NormalisedLevelForAddedPoint:",""+normalisedLevel);
						NetworkPoint newPoint=new NetworkPoint(ntwkScan.BSSID,myCurrentLocation,normalisedLevel,currentGPSAccuracy,ntwkScan.timestamp);
						
						//check if point already exists on this exact location. Add if not and replace if accuracy is better
						if(!networkPointAlreadyExistsOnLocation(newPoint))
						{
							db.addNetworkPoint(newPoint);
							//add data to current working memory
							updateCurrentData(newPoint);
							Log.d("ADDPOINT", "success");
						}
						else
							Log.d("ADDPOINT", "failed");
						
					}

				}


			}
			lastRefreshTime = System.currentTimeMillis();
		}
	}
	private class MyLocationListener implements LocationListener
	{
	   @Override
	   public void onLocationChanged(Location loc)
	   {
	      if (loc != null)
	      {
	         // Do something knowing the location changed by the distance you requested
	    	  Log.d("LocationChanged","Current:"+loc.getLatitude()+" "+loc.getLongitude());
	    	  currentGPSAccuracy=loc.getAccuracy();
	    	  findCurrentLocation(loc.getLatitude(),loc.getLongitude());
	    	  addPointsOnCurrentLocation();
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
	
//	  public void updateMap() {
//		  
//		 
//		    // do something long
//		    Runnable runnable = new Runnable() {
//		      @Override
//		      public void run() {
//		        
//		          
//		          updateMapWithPoints();
//		        
//		      }
//		    };
//		    new Thread(runnable).start();
//		  }

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
	
	
	public void addNewNetwork(OpenNetwork newNetwork)
	{
		boolean isNew=true;
		for(OpenNetwork ntwk : currentNetworks)
		{
			if(ntwk.getBSSID().equals(newNetwork.getBSSID()))
			{
				isNew = false;
			}
		}
		
		if(isNew)
		{
			//add network with information to currentnetworks and update database
			
			
			//add city name and post code
			Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
			StringBuilder builder = new StringBuilder();
			String finalAddress = "";
			try {
			    List<Address> address = geoCoder.getFromLocation(newNetwork.getLat(), newNetwork.getLng(), 1);
			    int maxLines = address.get(0).getMaxAddressLineIndex();
			    for (int i=0; i<maxLines; i++) {
			    String addressStr = address.get(0).getPostalCode()+"-"+address.get(0).getLocality();
			    builder.append(addressStr);
			    }

			finalAddress = builder.toString(); //includes postal code and city name in format "xxxxx-cityname"
			//Log.d("LOCATION IDENTIFIED","Current address:"+finalAddress);
			} catch (IOException e) {
				//Log.d("LOCATION IDENTIFIED","failed");
			}
			  catch (NullPointerException e) {}
			
			if(!finalAddress.equals(""))
				newNetwork.setCityName(finalAddress);
			
			currentNetworks.add(newNetwork);
			//mix color for newly added network
			int mixNewColor=Color.argb(255, (int)(Math.random()*200 + 55), (int)(Math.random()*200 + 55), (int)(Math.random()*200 + 55));
    		networkColors.add(mixNewColor);
			
    		db.addNetwork(newNetwork);
			if(!db.isDBempty())
				databaseState=1;
			
			debugText.setText("Added new network to DATABASE and WORKINGSET " +newNetwork.getSSID()+ " on location:"+newNetwork.getLocation()+ " in city: "+ newNetwork.getCityName());
			addPointsOnCurrentLocation();
		}
		
		
		
	}
	
	Runnable mStatusChecker = new Runnable() {
	    @Override 
	    public void run() {
	    	//_getLocation();
	      if(lastMapRefresh - System.currentTimeMillis() < TIME_FOR_MAP_UPDATE){
	    	  Log.d("CLEAR_MAP","Try to clear map...");
	    	  //mMap.clear();
	    	  Log.d("CLEAR MAP","map was cleared");
		      if(updateMapWithPointsFromMemory())
		    	  Log.d("UPDATE MAP", "success"); //this function can change value of mInterval.
		      else
		    	  Log.d("UPDATE MAP", "failed");
		      lastMapRefresh = System.currentTimeMillis();
		      Log.d("LASTREFRESH:",lastMapRefresh+"");
		      mHandler.postDelayed(mStatusChecker, mInterval);
	      }
	    }
	  };

	  void startRepeatingTask() {
	    mStatusChecker.run(); 
	  }

	  void stopRepeatingTask() {
	    mHandler.removeCallbacks(mStatusChecker);
	  }
	  
	  
	  private void _getLocation() {
		    // Get the location manager
		    LocationManager locationManager = (LocationManager) 
		            getSystemService(LOCATION_SERVICE);
		    Criteria criteria = new Criteria();
		    String bestProvider = locationManager.getBestProvider(criteria, false);
		    Location location = locationManager.getLastKnownLocation(bestProvider);
		    try {
		        lat1 = location.getLatitude();
		        lon1 = location.getLongitude();
		        myCurrentLocation=new LatLng(lat1, lon1);
		        Log.d("LOCATION:",lat1 + " " +lon1);
		    } catch (NullPointerException e) {
		    	lat1 = -1.0;
		    	lon1 = -1.0;
		    	Log.d("NO LOCATION","NONE");

		    	//try with network
		    	criteria=new Criteria();
		    	bestProvider=LocationManager.NETWORK_PROVIDER;
		    	location=locationManager.getLastKnownLocation(bestProvider);
		    	try {
		    		lat1 = location.getLatitude();
		    		lon1 = location.getLongitude();
		    		myCurrentLocation=new LatLng(lat1, lon1);
		    		Log.d("LOCATION via network:",lat1 + " " +lon1);
		    	} catch (NullPointerException e1) {
		    		lat1 = -1.0;
		    		lon1 = -1.0;
		    		Log.d("NO LOCATION","NONE");
		    		//try with passive
		    		criteria=new Criteria();
		    		bestProvider=LocationManager.PASSIVE_PROVIDER;
		    		location=locationManager.getLastKnownLocation(bestProvider);
		    		try {
		    			lat1 = location.getLatitude();
		    			lon1 = location.getLongitude();
		    			myCurrentLocation=new LatLng(lat1, lon1);
		    			Log.d("LOCATION via PASSIVE:",lat1 + " " +lon1);
		    		} catch (NullPointerException e2) {
		    			lat1 = -1.0;
		    			lon1 = -1.0;
		    			Log.d("NO LOCATION","NONE,nada, fail!");
		    		}

		    	}
		    }
		    
		    Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
			StringBuilder builder = new StringBuilder();
			String finalAddress = "";
			try {
			    List<Address> address = geoCoder.getFromLocation(lat1, lon1, 4);
			    int maxLines = address.get(0).getMaxAddressLineIndex();
			    for (int i=0; i<maxLines; i++) {
			    String addressStr = address.get(0).getAddressLine(i);
			    builder.append(addressStr);
			    builder.append(i+". ");
			    }
			    builder.append(" Postal code:" +address.get(0).getPostalCode());
			    builder.append(" City name:" + address.get(0).getLocality());

			finalAddress = builder.toString(); //This is the complete address.
			Log.d("LOCATION IDENTIFIED","Current address:"+finalAddress);
			} catch (IOException e) {
				Log.d("LOCATION IDENTIFIED","failed");
			}
			  catch (NullPointerException e) {}
		    
		    
		}
	  
	  //NOVO
	  public void changeRadius(View view) {
	        if (mDefaultRadius) {
	            mProvider.setRadius(ALT_HEATMAP_RADIUS_19);
	        } else {
	            mProvider.setRadius(MyHeatmapTileProvider.DEFAULT_RADIUS);
	        }
	        mOverlay.clearTileCache();
	        mDefaultRadius = !mDefaultRadius;
	    }

	    public void changeGradient(View view) {
	        if (mDefaultGradient) {
	            mProvider.setGradient(ALT_HEATMAP_GRADIENT);
	        } else {
	            mProvider.setGradient(MyHeatmapTileProvider.DEFAULT_GRADIENT);
	        }
	        mOverlay.clearTileCache();
	        mDefaultGradient = !mDefaultGradient;
	    }

	    public void changeOpacity(View view) {
	        if (mDefaultOpacity) {
	            mProvider.setOpacity(ALT_HEATMAP_OPACITY);
	        } else {
	            mProvider.setOpacity(MyHeatmapTileProvider.DEFAULT_OPACITY);
	        }
	        mOverlay.clearTileCache();
	        mDefaultOpacity = !mDefaultOpacity;
	    }
	    private class DataSet {
	        private ArrayList<LatLng> mDataset;
	        private String mUrl;

	        public DataSet(ArrayList<LatLng> dataSet, String url) {
	            this.mDataset = dataSet;
	            this.mUrl = url;
	        }

	        public ArrayList<LatLng> getData() {
	            return mDataset;
	        }

	        public String getUrl() {
	            return mUrl;
	        }
	    }
	    
	    private void updateCurrentData(NetworkPoint newPoint){
	    	//add point to dataset for my display
	    	
	    	allAvailableNetworkPoints.add(newPoint);


	    	//add point to dataset for heatmap
	    	currentNetworkPointsForHeatmaps.add(newPoint.getLocation());
	    	
	    	//mark that there was some change in working memory
	    	newPointDetected = true;
	    	
	    }
	    
	    private boolean networkPointAlreadyExistsOnLocation(NetworkPoint checkPoint)
	    {
	    	for(NetworkPoint point : allAvailableNetworkPoints)
	    	{
	    		if(checkPoint.getLocation().equals(point.getLocation()))
	    		{
	    			//if point on same location is fond, check if accuracy is any better
	    			if(checkPoint.getGpsAccuracy() > point.getGpsAccuracy())
	    				return true;
	    		}
	    			
	    	}
	    	return false;
	    }
	    
	    
	    private int addDummyData(){
	    	
			//add random number of networks to working set
	    	//works only with mode 2
	    	
	    	if(DISPLAY_MODE == 2){
	    		
		    	int howManyToAdd= (int)(Math.random()*50)+1;
		    	
				String bssid,ssid,city;
				int freq,alive,newpoints;
				float startLat,startLng,latitude,longitude,accuracy,signal;
				
				LatLngBounds findDisplayCenter = mMap.getProjection().getVisibleRegion().latLngBounds;;
				
				
				
				LatLng approxCoordinates;
				LatLng ntwkPntGPS;
				
				OpenNetwork newNetwork;
				NetworkPoint newPoint;

				for(int i = 0; i < howManyToAdd; i++)
				{
					//for each add from 5 to 15 new points
					bssid="bssid"+i+"sample";
					ssid="network"+i+"name";
					freq= i%3;//(int)(Math.random()*14);
					alive=( i%4==3 ? 0:1 );
					
					
					
					
					startLat = (float)findDisplayCenter.getCenter().latitude +(float)(Math.random()*0.002*howManyToAdd);
					startLng =(float)findDisplayCenter.getCenter().longitude +(float)(Math.random()*0.002*howManyToAdd);
					approxCoordinates = new LatLng(startLat, startLng);
					
					
					
					city ="fake ljubljana";
					
					Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
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
					//db.addNetwork(newNetwork);
					currentNetworks.add(newNetwork);
					
					newpoints=(int)(Math.random()*11)+5;

					//create point
					

					for (int j = 0;j<newpoints;j++){

						latitude = startLat + (float)(Math.random()*0.0001);
						longitude = startLng +  +(float)(Math.random()*0.0001);
						ntwkPntGPS = new LatLng(latitude, longitude);
						accuracy = (int)(Math.random()*10+1);
						signal = (float) (Math.random());
						newPoint = new NetworkPoint(bssid, ntwkPntGPS, signal, accuracy, System.currentTimeMillis());
						allAvailableNetworkPoints.add(newPoint);
						currentNetworkPointsForHeatmaps.add(newPoint.getLocation());
						//db.addNetworkPoint(newPoint);
						
					}

				}
				newPointDetected = true;
		    	return howManyToAdd;
	    		
	    	}

	    	return 0;

	    }
	    
	  
}