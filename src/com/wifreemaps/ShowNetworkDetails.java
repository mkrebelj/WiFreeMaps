package com.wifreemaps;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.widget.Toast;

public class ShowNetworkDetails extends ListActivity {
	
	private List<NetworkPoint> pointList;
	String myBSSID;
	
	private MySQLiteHelper db;
	
	  public void onCreate(Bundle icicle) {
		    super.onCreate(icicle);
		   
		    
		    db=new MySQLiteHelper(getBaseContext());
		    String title = getIntent().getStringExtra("NETWORK_SSID");
		    if(title != null)
		    {
		    	this.setTitle(title);
		    }
		    
		    String ekstra=getIntent().getStringExtra("SELECTED_BSSID");
		    if (ekstra != null) {
		       myBSSID = ekstra;
		    }
		    
		    
		    //get all different networks
		     pointList=db.getNetworkPoints(myBSSID);
		     
		     String[] values = new String[pointList.size()];
		     Log.d("NetworkListSize",pointList.size()+" points");
		     int i=0;
		     for(NetworkPoint point : pointList)
		     {
		    	 values[i]=point.getLocationAsString();
		    	 i++;
		     }
		     
		     
		    
		    // use your custom layout
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        R.layout.rowlayout, R.id.label, values);
		    setListAdapter(adapter);
		    
		    

		  }

		  @Override
		  protected void onListItemClick(ListView l, View v, int position, long id) {
		   
		    
		    String pointInfo = pointList.get(position).toString();
		    
		    Toast.makeText(this, pointInfo, Toast.LENGTH_LONG).show();
		    
		    
		  }
} 