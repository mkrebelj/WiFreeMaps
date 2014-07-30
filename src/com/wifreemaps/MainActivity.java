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




import android.R.drawable;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
        
        //ADD GROUND OVERLAY which represents wifi network
        Bitmap radiusImageSource=BitmapFactory.decodeResource(getResources(), R.drawable.radius);
        //ADD SOME COLOR 
        radiusImageSource = changeImageColor(radiusImageSource,Color.GREEN);
        
        
        BitmapDescriptor radiusImage=BitmapDescriptorFactory.fromBitmap(radiusImageSource);
        
       
        
        // IJS coordinates 46.042931, 14.487516
        LatLng IJSLocation = new LatLng(46.042931, 14.487516);
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
        .image(radiusImage)
        .position(IJSLocation, 5f, 5f) //Na tem mestu poraèunati natanènost in sinhronizirati z velikostjo
        .transparency(0.5f); //odvisno od kvalitete signala pobarvati med 0 in 1, izdelati naèin da se porazdeli na prostor- > (moc.signala/max.moc)/povrsina
        mMap.addGroundOverlay(newarkMap);
        
        //now focus on that point
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(IJSLocation, 25.0f));
       
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
    
    //Najdi nekaj bolj ucinkovitega za barvanje...
     public static Bitmap changeImageColor(Bitmap srcBmp, int dstColor) {

    	    int width = srcBmp.getWidth();
    	    int height = srcBmp.getHeight();

    	    float srcHSV[] = new float[3];
    	    float dstHSV[] = new float[3];

    	    Bitmap dstBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);

    	    for (int row = 0; row < height; row++) {
    	        for (int col = 0; col < width; col++) {
    	        	
    	            Color.colorToHSV(srcBmp.getPixel(col, row), srcHSV);
    	            Color.colorToHSV(dstColor, dstHSV);

    	            // If it area to be painted set only value of original image
    	            dstHSV[2] = srcHSV[2];  // value

    	            dstBitmap.setPixel(col, row, Color.HSVToColor(dstHSV));
    	        }
    	    }

    	    return dstBitmap;
    	}
    	  
    	  
}