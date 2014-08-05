package com.wifreemaps;

import java.util.List;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        

        
        
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
        
        
        
        
       
        
        
        //ADD GROUND OVERLAY which represents wifi network
        Bitmap radiusImageSource=BitmapFactory.decodeResource(getResources(), R.drawable.fadingout);
        
        
        
        //ADD SOME COLOR   
        BitmapDescriptor radiusImage=GetCustomBitmapDescriptor(radiusImageSource, Color.CYAN);//BitmapDescriptorFactory.fromBitmap(radiusImageSource);
        
       
        
        // IJS coordinates 46.042931, 14.487516
        LatLng IJSLocation = new LatLng(46.042931, 14.487516);
        GroundOverlayOptions openWifiSpot = new GroundOverlayOptions()
        .image(radiusImage)
        .position(IJSLocation, 5f, 5f) //Na tem mestu poraèunati natanènost in sinhronizirati z velikostjo
        .transparency(0.5f); //odvisno od kvalitete signala pobarvati med 0 in 1, izdelati naèin da se porazdeli na prostor- > (moc.signala/max.moc)/povrsina
        mMap.addGroundOverlay(openWifiSpot);
        
        //add other networks
        float lat=46.042931f;
        float lon=14.487516f;
        GroundOverlayOptions[] allKnownPoints = new GroundOverlayOptions[20];
        allKnownPoints = getAllKnownPoints(allKnownPoints.length,Color.GREEN,lat,lon);
        for(int i= 0; i < allKnownPoints.length;i++)
        {
        	mMap.addGroundOverlay(allKnownPoints[i]);
        }
        
        lat = 46.051351f;
        lon = 14.487600f;
        allKnownPoints = new GroundOverlayOptions[40];
        allKnownPoints = getAllKnownPoints(allKnownPoints.length, Color.BLUE,lat,lon);
        for(int i= 0; i < allKnownPoints.length;i++)
        {
        	mMap.addGroundOverlay(allKnownPoints[i]);
        }
        
        
        //now focus on that point
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(IJSLocation, 25.0f));
       
        
        //simulate adding new point every second or so - not very good
//        mHandler = new Handler();
//        startRepeatingTask();
        
        //for debugging 
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        mainText.setText("\nStarting Scan...\n");
        mainText.setMovementMethod(new ScrollingMovementMethod());
        
        //try with database
        MySQLiteHelper db = new MySQLiteHelper(this);
        
        db.addNetwork(new OpenNetwork("bssid1", "network1", 2100, new LatLng(46.051470, 14.506019), 0.8f, 80.0f));
        db.addNetwork(new OpenNetwork("bssid1", "network1", 2100, new LatLng(46.051460, 14.506029), 0.9f, 80.0f));
        db.addNetwork(new OpenNetwork("bssid2", "network2", 2100, new LatLng(46.051560, 14.506089), 0.9f, 80.0f));
        
        List<OpenNetwork> network1= db.getNetworkPoints("bssid1");
        
        OpenNetwork delete = network1.get(0);
        db.deleteEntireNetwork(delete);
        
        
        
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