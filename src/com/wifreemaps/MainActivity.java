package com.wifreemaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.internal.hh;
import com.google.android.gms.internal.ln;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
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



import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;



import android.support.v4.app.FragmentActivity;
import android.text.InputFilter.LengthFilter;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;



import com.google.maps.android.heatmaps.HeatmapTileProvider.Builder;




public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
	private static LocationClient mLocationClient;
	private static LocationRequest mLocationRequest;

	private static final int DISPLAY_MODE=2; //1= my custom method; 2= heatmaps

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private static GoogleMap mMap = null;
	private static Handler handler = new Handler(Looper.getMainLooper());
	private float currentZoomLevel;
	private boolean zoomingIn = true;
	private boolean detailedView = false, detailedViewLoaded = false;
	private boolean somethingNewToAddToDatabase = false;
	private long databaseLastUpdated = 0;
	private List<String> uniqueNetworks=new ArrayList<String>();
	private List<Integer> networkColors = new ArrayList<Integer>();
	private List<NetworkMarking> drawableNetworkMarkings = new ArrayList<NetworkMarking>();
	private List<NetworkPoint> allAvailableNetworkPoints = new ArrayList<NetworkPoint>();
	private ArrayList<LatLng> networkPointsInRange = new ArrayList<LatLng>();
	private LatLngBounds curScreen;
	private static boolean inBackground=false;

	public static final String PSK = "PSK";
	public static final String WEP = "WEP";
	public static final String EAP = "EAP";
	public static final String OPEN = "Open";


	//statics
	private static final int TIME_TO_WRITE_TO_DATABASE = 60000; //add data to database each 60 seconds
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
	private int networksInViewport=0;

	private List<OpenNetwork> currentNetworks=new ArrayList<OpenNetwork>();
	List<NetworkPoint> networkPoints=new ArrayList<NetworkPoint>();
	List<NetworkPoint> pointsToAddToDatabase = new ArrayList<NetworkPoint>();

	ArrayList<LatLng> currentNetworkPointsForHeatmaps = new ArrayList<LatLng>();

	//simulate adding new points with this handler
	private int mInterval = 10000; // 5 seconds by default, can be changed later
	private Handler mHandler;
	private long lastRefreshTime;
	private long lastMapRefresh;
	private boolean goToSleep=false;
	private boolean allowActiveScan;

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

	
	//Server url
	private String baseServerUrl="http://212.235.208.12:31173/webservice/";
	private HttpRequestHelper myHttpRequestHelper = new HttpRequestHelper();

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
		mLocationClient = new LocationClient(this, this, this);

		mMap=((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

		mMap.setMyLocationEnabled(true);



		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		//checkForSettings();



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




				}
				curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;

				Log.d("CURRENTSCREENBOUNDS", curScreen.toString());

				showSimplifiedNetworkInViewport();

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


		if(servicesConnected()){
			Toast.makeText(this, "Services connected!", Toast.LENGTH_SHORT).show();

			mLocationRequest = LocationRequest.create();
			mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			mLocationRequest.setInterval(5000);
			mLocationRequest.setNumUpdates(1);
			mLocationRequest.setFastestInterval(1000);
		}
		else
			Toast.makeText(this, "Services NOT connected!", Toast.LENGTH_SHORT).show();


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
			Log.d("empty","empty db");
			Toast.makeText(this, "No data yet", Toast.LENGTH_SHORT).show();
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

							LatLng locationtocenter = myCurrentLocation;
							mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationtocenter, 14.0f));



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

		//startRepeatingTask();

		Toast.makeText(this, "Points in db: "+db.pointsCount(), Toast.LENGTH_LONG).show();


	}

	private void checkForSettings() {
		// TODO Auto-generated method stub

		//check for map type in perfs
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		String currentMapView=sharedPrefs.getString("perfMapMode", "NULL");
		allowActiveScan = sharedPrefs.getBoolean("allowActiveScan", true);
		if(!allowActiveScan)
		{
			Toast.makeText(this, "Active scan not allowed", Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(this, "Active scan allowed", Toast.LENGTH_SHORT).show();
		}

		if(currentMapView.equals("Sattelite view")){
			//			Toast.makeText(this, "Satteliteview as preffered", Toast.LENGTH_SHORT).show();
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		}
		else if(currentMapView.equals("Hybrid view")){
			//			Toast.makeText(this, "Hybrid as preffered", Toast.LENGTH_SHORT).show();
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		}
		else if(currentMapView.equals("Terrain view")){
			//			Toast.makeText(this, "Terrain as preffered", Toast.LENGTH_SHORT).show();
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		}
		else if(currentMapView.equals("None")){
			//			Toast.makeText(this, "None as preffered", Toast.LENGTH_SHORT).show();
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
		}
		else if(currentMapView.equals("Normal view"))
		{
			//			Toast.makeText(this, "Normal as preffered", Toast.LENGTH_SHORT).show();
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
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

					networksInViewport=0;

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
								networksInViewport++;

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
									networksInViewport++;
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
					//just show points on map in current view port
					else
					{
						detailedView=false;
						detailedViewLoaded = false;
						//Only one point for whole network
						networkPointsInRange.clear();
						for(OpenNetwork ntwk : currentNetworks)
						{
							if(curScreen.contains(ntwk.getLocationAsCoordinate()))
							{
								networksInViewport++;
								networkPointsInRange.add(ntwk.getLocationAsCoordinate());
								newPointDetected = true;


							}
						}
						currentNetworkPointsForHeatmaps = networkPointsInRange;
						updateMapWithPointsFromMemory();
					}

				}

			});




		}

	}



	/**
	 * @return Returns true if reading from database is successful
	 */
	private boolean updateMapWithPointsFromDatabase() {

		if(!inBackground){


			Log.d("UPDATING MAP","points should be added");
			Log.d("UPDATING WITH ZOOM LEVEL:","ZOOMLVL:"+currentZoomLevel);



			switch(DISPLAY_MODE)
			{
			case 1:
			{
				if(databaseState != 0) {


					currentNetworks = db.getAllNetworks();
					if(currentNetworks.size()==0)
						break;
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

					//remove remove

					//					NetworkPoint initialPoint = new NetworkPoint();
					//					for (OpenNetwork ntwk : currentNetworks)
					//					{
					//						double normalisedLevel=(-35.0f/-80)/8*1.0f *10;
					//						initialPoint = new NetworkPoint(ntwk.getBSSID(), ntwk.getLocationAsCoordinate(), 1, normalisedLevel, 8, System.currentTimeMillis());
					//						pointsToAddToDatabase.add(initialPoint);
					//					}
					//					databaseLastUpdated = System.currentTimeMillis() - (TIME_TO_WRITE_TO_DATABASE + 10);
					//					updateDatabaseWithAnyNewData();

					//remove remove



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

					if(currentNetworkPointsForHeatmaps.size() == 0){
						Toast.makeText(getBaseContext(), "No information yet...", Toast.LENGTH_SHORT).show();

					}
					else{
						mProvider = new MyHeatmapTileProvider.Builder().data(
								mLists.get("WifiTocke").getData()).build();

						mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
						mOverlay.clearTileCache();
					}
				}
			}

			default:
				Log.d("NODATATOADD","No data in database to add on map...");
				return false;
			}


		}
		return false;

	}





	private boolean updateMapWithPointsFromMemory() {

		//data is already in memory, no need to read from database

		if(!inBackground){
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


					if(currentNetworkPointsForHeatmaps.size()==0)
					{
						Toast.makeText(getBaseContext(), "No data yet", Toast.LENGTH_SHORT).show();
					}
					else{
						mProvider = new MyHeatmapTileProvider.Builder().data(
								mLists.get("WifiTocke").getData()).build();

						mMap.clear();


						mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
						mOverlay.clearTileCache();
					}
					newPointDetected=false;

					return true;
				}

				Log.d("NUMBER OF POINTS","Currently shown on map:"+currentNetworkPointsForHeatmaps.size()+" points");
				debugText.setText("Currently showing "+ networksInViewport +" different networks and "+currentNetworkPointsForHeatmaps.size()+" points on map");
			}

			default:
				Log.d("NODATATOADD","No new data to add on map...");
				return false;
			}
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
		List<OpenNetwork> result = new ArrayList<OpenNetwork>();
		if(databaseState != 0)
			result = db.getAllNetworks();
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		menu.add(1,1,1,"Show Networks");
		menu.add(2,2,2,"Add Dummy Data");
		menu.add(3,3,3,"Settings");
		menu.add(4,4,4,"Send data to server");
		menu.add(5,5,5,"Get Network Data");
		menu.add(6,6,6,"Get Points Data");
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
		else if(selected == 3)
		{
			//Show options
			Intent intent= new Intent(getBaseContext(), SettingsActivity.class);
			startActivity(intent);
		}
		else if(selected == 4)
		{
			Log.d("SYNC", "Sending data to server..");
			if(internetConnected()){
				//First make sure that all values are set in local database
				updateUnknownPointLocations();
				myHttpRequestHelper.sendNetworksToServer(this, baseServerUrl+"uploaddata.php", currentNetworks);
				//new HttpAsyncTask().execute(baseServerUrl+"uploaddata.php");
			}
		}
		else if(selected == 5)
		{
			Log.d("SYNC", "Geting data from server..");
			if(internetConnected())
				myHttpRequestHelper.getNetworksRequest(this, baseServerUrl+"getdata.php");
				//new HttpAsyncTaskGETNetworks().execute(baseServerUrl+"getdata.php");

		}
		else if(selected == 6)
		{
			Log.d("SYNC", "Getting point data from server...");
			if(internetConnected())
				myHttpRequestHelper.getPointsRequest(this,baseServerUrl+"getdatapoints.php");
				//new HttpAsyncTaskGETNetworks().execute(baseServerUrl+"getdatapoints.php");
		}
		Log.d("ITEM ID:",item.getItemId()+" pressed");
		return super.onMenuItemSelected(featureId, item);
	}

	private void updateUnknownPointLocations() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Corrected location info on: "+db.updateUnknownLocations(), Toast.LENGTH_LONG).show();
	}



	@Override
	protected void onPause() {

		unregisterReceiver(receiverWifi);
		super.onPause();
	}

	@Override
	protected void onResume() {
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		checkForSettings();
		super.onResume();
	}

	class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context c, Intent intent) {

			if(!goToSleep){
				//find location first
				_getLocation();

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

						temp=new OpenNetwork(scan.BSSID, scan.SSID, "unknown", scan.frequency, myCurrentLocation, 0);
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
			else
			{
				Log.d("Sleep","currently sleeping, not doing anything");
			}


		}

	}

	@Override
	protected void onStop(){


		applicationDidEnterBackground();
		Log.d("APPSTAT", "onStop app went to background ");
		db.close();
		super.onStop();
	}

	@Override
	protected void onStart() {
		Log.d("APPSTAT", "onStart app went to foreground ");
		applicationWillEnterForeground();

		super.onStart();
	}



	private void applicationWillEnterForeground() {
		// TODO Auto-generated method stub
		currentNetworks=returnAllNetworksFromDB();
		inBackground=false;
		goToSleep=false;
		if(!mLocationClient.isConnected()){
			mLocationClient.connect();
			Log.d("wakeup","mlocation connected");
		}


	}



	private void applicationDidEnterBackground() {
		// TODO Auto-generated method stub
		if(!allowActiveScan)
			goToSleep=true;

		inBackground=true;
		currentNetworks.clear();
		if(goToSleep)
		{
			mLocationClient.disconnect();
			Log.d("sleep","mlocationclient disconnected");
		}
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

		String finalAddress = "unknown";
		if(isInternetConnectionAvailable())
		{
			finalAddress = returnResultFromGeocoder(myCurrentLocation);
		}


		debugText.setText("Current location address:"+finalAddress+", coordinates:"+ String.format ("%s",latitude)+ " "+String.format ("%s",longitude));


	}




	private void addPointsOnCurrentLocation(boolean isInitialPoint) {
		Log.d("AddNewPoints","Adding new points in current networks, if any");
		double normalisedLevel=0;
		long timeStamp=0;
		String [] securityModes = {WEP, PSK, EAP};
		boolean open=true;
		if(System.currentTimeMillis() - lastRefreshTime < TIME_BETWEEN_ADDING_NEW_POINT || isInitialPoint){
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

						timeStamp=System.currentTimeMillis();
						somethingNewToAddToDatabase = true;
						Log.d("DetailsForAddedPoint:","ScanLVL:"+ntwkScan.level +" current accuracy:"+currentGPSAccuracy);
						normalisedLevel=(-35.0f/(ntwkScan.level))/currentGPSAccuracy*1.0f *10;
						Log.d("NormalisedLevelForAddedPoint:",""+normalisedLevel);

						NetworkPoint newPoint=new NetworkPoint(ntwkScan.BSSID,myCurrentLocation,normalisedLevel,currentGPSAccuracy,timeStamp);


						pointsToAddToDatabase.add(newPoint);

						//add data to current working memory
						updateCurrentData(newPoint);




					}

				}


			}
			lastRefreshTime = System.currentTimeMillis();

			//wait till there are a few new points and then add them to database, so there are less db queries
			if(System.currentTimeMillis() - databaseLastUpdated > TIME_TO_WRITE_TO_DATABASE){

				updateDatabaseWithAnyNewData();

			}
		}
	}


	//	private class MyLocationListener implements LocationListener
	//	{
	//		@Override
	//		public void onLocationChanged(Location loc)
	//		{
	//			if (loc != null)
	//			{
	//				// Do something knowing the location changed by the distance you requested
	//				Log.d("LocationChanged","Current:"+loc.getLatitude()+" "+loc.getLongitude());
	//				currentGPSAccuracy=loc.getAccuracy();
	//				findCurrentLocation(loc.getLatitude(),loc.getLongitude());
	//				addPointsOnCurrentLocation(false);
	//			}
	//		}
	//
	//		@Override
	//		public void onProviderDisabled(String arg0)
	//		{
	//			// Do something here if you would like to know when the provider is disabled by the user
	//		}
	//
	//		@Override
	//		public void onProviderEnabled(String arg0)
	//		{
	//			// Do something here if you would like to know when the provider is enabled by the user
	//		}
	//
	//		@Override
	//		public void onStatusChanged(String arg0, int arg1, Bundle arg2)
	//		{
	//			// Do something here if you would like to know when the provider status changes
	//		}
	//	}


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
			String finalAddress = "unknown";
			if(isInternetConnectionAvailable())
			{
				finalAddress = returnResultFromGeocoder(newNetwork.getLocationAsCoordinate());
			}


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

			addPointsOnCurrentLocation(true);
		}



	}

	private void addInitialPoint(OpenNetwork newNetwork) {
		// TODO Auto-generated method stub

		for(ScanResult ntwkScan:wifiList)
		{
			if(newNetwork.getBSSID().equals(ntwkScan.BSSID))
			{
				long timeStamp=System.currentTimeMillis();
				somethingNewToAddToDatabase = true;
				Log.d("DetailsForINITIALPoint:","ScanLVL:"+ntwkScan.level +" current accuracy:"+currentGPSAccuracy);
				double normalisedLevel=(-35.0f/(ntwkScan.level))/currentGPSAccuracy*1.0f *10;
				Log.d("NormalisedLevelForAddedPoint:",""+normalisedLevel);

				NetworkPoint newPoint=new NetworkPoint(ntwkScan.BSSID,myCurrentLocation,normalisedLevel,currentGPSAccuracy,timeStamp);


				pointsToAddToDatabase.add(newPoint);

				//add data to current working memory
				updateCurrentData(newPoint);


				return;
			}
		}

		//wait till there are a few new points and then add them to database, so there are less db queries
		if(System.currentTimeMillis() - databaseLastUpdated > TIME_TO_WRITE_TO_DATABASE){

			updateDatabaseWithAnyNewData();

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

		if(location == null)
		{



			if(servicesConnected()){
				//use other method

				if(mLocationClient != null)
				{
					Location mCurrentLocation = mLocationClient.getLastLocation();
					myCurrentLocation=new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
					Log.d("LOCATION via Fused Location","Location found:"+myCurrentLocation.toString());
				}
			}
		}

		if(isInternetConnectionAvailable())
		{
			String finalAddress = returnResultFromGeocoder(myCurrentLocation);
			Log.d("LOCATION IDENTIFIED","Current address:"+finalAddress);
		}

		Log.d("FINDLOCATION","current location:"+myCurrentLocation.toString());

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
				//if point on same location is found, check if accuracy is any better
				//if(checkPoint.getGpsAccuracy() > point.getGpsAccuracy())
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

				if(isInternetConnectionAvailable())
				{
					city=returnResultFromGeocoder(approxCoordinates);
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

	private void updateDatabaseWithAnyNewData()
	{
		for(NetworkPoint pnt : pointsToAddToDatabase)
		{
			if(!networkPointAlreadyExistsOnLocation(pnt))
			{
				db.addNetworkPoint(pnt);

				Log.d("ADDPOINT", "success");
			}
			else
				Log.d("ADDPOINT", "failed");
		}

		pointsToAddToDatabase.clear();
		databaseLastUpdated = System.currentTimeMillis();
	}

	private boolean isInternetConnectionAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}


	private String returnResultFromGeocoder(LatLng location){
		String result="unknown";

		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
		StringBuilder builder = new StringBuilder();

		try {
			List<Address> address = geoCoder.getFromLocation(location.latitude, location.longitude, 1);
			//int maxLines = address.get(0).getMaxAddressLineIndex();
			//for (int i=0; i<maxLines; i++) {
			String addressStr = address.get(0).getLocality()+" - "+ address.get(0).getCountryName();
			builder.append(addressStr);
			//}

			result = builder.toString(); //includes postal code and city name in format "cityname - countryname"
			//Log.d("LOCATION IDENTIFIED","Current address:"+finalAddress);
		} catch (IOException e) {
			//Log.d("LOCATION IDENTIFIED","failed");
		}
		catch (NullPointerException e) {}

		return result;
	}

	public ArrayList<String> determineAddress(ArrayList<LatLng> locations){

		String partialResult="unknown";
		ArrayList<String> result = new ArrayList<String>();

		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
		StringBuilder builder = new StringBuilder();

		//decode city names from all provided locations
		for(LatLng location:locations){
			try {
				List<Address> address = geoCoder.getFromLocation(location.latitude, location.longitude, 1);
				//int maxLines = address.get(0).getMaxAddressLineIndex();
				//for (int i=0; i<maxLines; i++) {
				String addressStr = address.get(0).getLocality()+" - "+ address.get(0).getCountryName();
				builder.append(addressStr);
				//}

				partialResult = builder.toString(); //includes postal code and city name in format "cityname - countryname"
				//Log.d("LOCATION IDENTIFIED","Current address:"+finalAddress);
			} catch (IOException e) {
				//Log.d("LOCATION IDENTIFIED","failed");
			}
			catch (NullPointerException e) {}

			result.add(partialResult);

		}
		return result;		
	}

	@Override
	public void onLocationChanged(Location newLocation) {
		// TODO Auto-generated method stub

		if(newLocation != null){

			myCurrentLocation=new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
			Log.d("Found alternative location","Location:"+myCurrentLocation.toString());
		}

	}



	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}



	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(
						this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the
			 * user with the error.
			 */
			Toast.makeText(this, "Error code:"+connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
		}
	}



	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
	}



	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();

	}

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode =
				GooglePlayServicesUtil.
				isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates",
					"Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason.
			// resultCode holds the error code.
		} else {
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode,
					this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST);

			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				Log.d("Location Updates","Google Play services error:" +errorDialog.toString());
			}
			return false;
		}
	}

	public boolean internetConnected(){
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else
			return false;    
	}



	//server exchange
	private String sendDataToServer(String url,JSONObject networksObject) {
		// TODO Auto-generated method stub
		String result ="";
		InputStream inputStream = null;


		try {

			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();
			Log.d("InputStream", "create http client");
			// 2. make POST request to the given URL
			HttpPost httpPost = new HttpPost(url);
			Log.d("InputStream", "makepost");
			String json = "";

			// 3. add jsonObject
			JSONObject test =  new JSONObject();
			try {
				test.put("bssid", "1");
				test.put("ssid","2");
				test.put("cityname", "3");
				test.put("approxgps", "4");
				test.put("frequency", "3");
				test.put("reachable", "3");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			json = networksObject.toString();
			// 4. convert JSONObject to JSON to String
			//json = networksObject.toString();
			//            json = test.toString();
			//			Log.d("InputStream", "converted json to string");
			// 
			//
			//            // 5. set json to StringEntity
			//            StringEntity se = new StringEntity(json);
			//            Log.d("InputStream", "string entity");
			// 
			//            // 6. set httpPost Entity
			//            httpPost.setEntity(se);
			//            Log.d("InputStream", "set entity:"+json);
			// 
			//            // 7. Set some headers to inform server about the type of the content   
			//            httpPost.setHeader("Accept", "application/json");
			//            httpPost.setHeader("Content-type", "application/json");
			//           
			//            Log.d("InputStream", "headers set");
			// 
			//            // 8. Execute POST request to the given URL
			//            
			//            ResponseHandler responseHandler = new BasicResponseHandler();
			//            
			//            HttpResponse response = httpclient.execute(httpPost, responseHandler);
			//            
			//            //HttpResponse httpResponse = httpclient.execute(httpPost);
			//            Log.d("InputStream", "executed post request");
			// 
			//            // 9. receive response as inputStream
			//            inputStream = response.getEntity().getContent();
			//            Log.d("InputStream", "receieved input stream");
			// 
			//            // 10. convert inputstream to string
			//            if(inputStream != null)
			//                result = MyServerExchangeHelper.convertInputStreamToString(inputStream);
			//            else
			//                result = "Did not work!";



			//url with the post data
			HttpPost httpost = new HttpPost(url);

			//convert parameters into JSON object
			//            JSONObject holder = test;

			//passes the results to a string builder/entity
			StringEntity se = new StringEntity(json);

			//sets the post request as the resulting string
			httpost.setEntity(se);
			//sets a request header so the page receving the request
			//will know what to do with it
			httpost.setHeader("Accept", "application/json");
			httpost.setHeader("Content-type", "application/json");

			//Handles what is returned from the page 
			ResponseHandler responseHandler = new BasicResponseHandler();
			String result1 = httpclient.execute(httpost, responseHandler);
			debugText.setText(result1);
			return result1;

		} catch (Exception e) {
			e.printStackTrace();

			//        		Log.d("InputStream", e.getMessage());
		}


		return result;
	}

//	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
//	@Override
//	protected String doInBackground(String... urls) {
//
//
//		HttpParams httpParameters = new BasicHttpParams();
//		// set the connection timeout and socket timeout parameters (milliseconds)
//		HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
//		HttpConnectionParams.setSoTimeout(httpParameters, 5000);
//
//		HttpClient client = new DefaultHttpClient(httpParameters);
//		Log.d("HTTP responded","starting http request...");
//
//		String result = null;
//		try {
//			// Add your data
//			Log.d("HTTP responded","adding data...");
//			ArrayList<OpenNetwork> networksToSend= new ArrayList<OpenNetwork>();
//			for(OpenNetwork na:currentNetworks)
//			{
//				networksToSend.add(na);
//			}
//			JSONObject payload = serverHelper.prepareJSON(networksToSend);
//
//
//
//
//			HttpPost httppost = new HttpPost( urls[0]);
//			httppost.setHeader("Accept", "application/json");
//			httppost.setHeader("Content-type", "application/json");
//			httppost.setEntity(new ByteArrayEntity(
//					payload.toString().getBytes("UTF8")));
//			Log.d("HTTP responded","execute request...");
//
//
//
//			HttpResponse response = client.execute(httppost);
//			HttpEntity responseEntity = response.getEntity();
//			Log.d("HTTP responded","reading response...");
//			String thisline="";
//			if (responseEntity != null) {
//				BufferedReader reader = new BufferedReader(
//						new InputStreamReader(responseEntity.getContent(),
//								"UTF-8"));
//
//				while( (thisline = reader.readLine()) != null)
//					result=result + thisline+"\n";
//				Log.d("HTTP responded","Http said:"+result);
//			} else {
//				Log.d("HTTP responded","nothing");
//			}
//
//		} catch (IllegalArgumentException e1) {
//			e1.printStackTrace();
//		} catch (IOException e2) {
//			e2.printStackTrace();
//		}
//		return result;
//
//
//
//
//
//
//	}
//	// onPostExecute displays the results of the AsyncTask.
//	@Override
//	protected void onPostExecute(String result) {
//		super.onPostExecute(result);
//		Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
//		debugText.setText("");
//		debugText.setText("Response:"+result);
//
//	}
//
//}



//private class HttpAsyncTaskGETNetworks extends AsyncTask<String, Void, String> {
//	@Override
//	protected String doInBackground(String... urls) {
//
//
//		HttpParams httpParameters = new BasicHttpParams();
//		// set the connection timeout and socket timeout parameters (milliseconds)
//		HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
//		HttpConnectionParams.setSoTimeout(httpParameters, 5000);
//
//		HttpClient client = new DefaultHttpClient(httpParameters);
//		Log.d("HTTP responded","starting http request...");
//
//		String result = null;
//		try {
//			// Add your data
//			Log.d("HTTP responded","request mode: networks");
//
//			JSONObject payload = new JSONObject();
//			try {
//
//				payload.put("mode", "networks");
//
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//
//			HttpPost httppost = new HttpPost( urls[0]);
//			httppost.setHeader("Accept", "application/json");
//			httppost.setHeader("Content-type", "application/json");
//			httppost.setEntity(new ByteArrayEntity(
//					payload.toString().getBytes("UTF8")));
//			Log.d("HTTP responded","execute request...");
//
//
//
//			HttpResponse response = client.execute(httppost);
//			HttpEntity responseEntity = response.getEntity();
//			Log.d("HTTP responded","reading response...");
//			String thisline="";
//			if (responseEntity != null) {
//				BufferedReader reader = new BufferedReader(
//						new InputStreamReader(responseEntity.getContent(),
//								"UTF-8"));
//
//				while( (thisline = reader.readLine()) != null)
//					result=result + thisline+"\n";
//				Log.d("HTTP responded","Http said:"+result);
//			} else {
//				Log.d("HTTP responded","nothing");
//			}
//
//		} catch (IllegalArgumentException e1) {
//			e1.printStackTrace();
//		} catch (IOException e2) {
//			e2.printStackTrace();
//		}
//		return result;
//
//
//
//
//
//
//	}
//	// onPostExecute displays the results of the AsyncTask.
//	@Override
//	protected void onPostExecute(String result) {
//		super.onPostExecute(result);
//		Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
//		debugText.setText("");
//		debugText.setText("Response:"+result);
//
//	}
//
//}

}