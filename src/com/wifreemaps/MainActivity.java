package com.wifreemaps;

import java.util.List;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;




import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;



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
        
       
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        mainText.setText("\nStarting Scan...\n");
        mainText.setMovementMethod(new ScrollingMovementMethod());
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
    
    
    
    	  
    	  
}