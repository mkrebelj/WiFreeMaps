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

public class ShowNetworksActivity extends ListActivity {
	
	private List<OpenNetwork> networkList;
	private MySQLiteHelper db;
	
	  public void onCreate(Bundle icicle) {
		    super.onCreate(icicle);
		   
//		    setContentView(R.layout.rowlayout); 
		    db=new MySQLiteHelper(getBaseContext());
		    
		    
		    this.setTitle("List of all networks");
		    
		    //get all different networks
		     networkList=db.getAllNetworks();
		     
		     String[] values = new String[networkList.size()];
		     Log.d("NetworkListSize",networkList.size()+"networks");
		     int i=0;
		     for(OpenNetwork ntwk : networkList)
		     {
		    	 values[i]=ntwk.getSSID();
		    	 i++;
		     }
		     
		     
		    
		    // use your custom layout
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        R.layout.rowlayout, R.id.label, values);
		    setListAdapter(adapter);
		    
		    

		  }

		  @Override
		  protected void onListItemClick(ListView l, View v, int position, long id) {
		   
		    //show detailed view of network
		    Log.d("Showing network with bssid", networkList.get(position).getBSSID());
		    Intent intent=new Intent(getBaseContext(), ShowNetworkDetails.class);
		    intent.putExtra("SELECTED_BSSID", networkList.get(position).getBSSID());
		    intent.putExtra("NETWORK_SSID", networkList.get(position).getSSID());
		    intent.putExtra("LOCATION", networkList.get(position).getCityName());
		    Toast.makeText(this, "Showing details of: "+networkList.get(position).getBSSID(), Toast.LENGTH_LONG).show();
			startActivity(intent);
			
		  }
} 