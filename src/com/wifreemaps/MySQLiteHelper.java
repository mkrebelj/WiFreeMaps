package com.wifreemaps;

import java.util.ArrayList;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper{
	private static final int DATABASE_VERSION = 6;
	private static final String DATABASE_NAME = "OpenNetworkDB";
	
	
	public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); 
    }


	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// SQL statement to create book table
	    Log.d("CreateDB", "networks");
		
        String CREATE_OPENNETWORK_TABLE = "CREATE TABLE networks ( " +
        		"id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "bssid TEXT, " +
                "ssid TEXT, "+
                "location TEXT, "+
                "gpsstrength TEXT, "+
                "wifistrength TEXT, "+
                "frequency TEXT, "+
                "livenet INTEGER)";
 
        // create books table
        db.execSQL(CREATE_OPENNETWORK_TABLE);
        
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS networks");
 
        // create fresh table
        this.onCreate(db);
    }
	//---------------------------------------------------------------------
	 
    /**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */
	
	private static final String TABLE_NETWORKS = "networks";
	
	
	//table column names
	private static final String KEY_ID = "id";
	private static final String KEY_BSSID = "bssid";
	private static final String KEY_SSID = "ssid";
	private static final String KEY_COORDINATES = "location";
	private static final String KEY_GPSSTRENGTH = "gpsstrength";
	private static final String KEY_WIFISTRENGTH = "wifistrength";
	private static final String KEY_FREQUENCY = "frequency";
	private static final String KEY_LIVENET = "livenet";
	
	private static final String[] COLUMNS = {KEY_ID, KEY_BSSID,KEY_SSID, KEY_COORDINATES, KEY_GPSSTRENGTH,KEY_WIFISTRENGTH,KEY_FREQUENCY,KEY_LIVENET};
	
	
	
	
	public void addNetwork(OpenNetwork network){
		Log.d("addNetwork", network.toString());
		
		// 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_BSSID, network.getBSSID()+""); // get BSSID
        values.put(KEY_SSID, network.getSSID()+""); // get SSID
        values.put(KEY_COORDINATES, network.getLocations()+""); //get location
        values.put(KEY_GPSSTRENGTH, network.getGPSaccuracy()+""); //get accuracy
        values.put(KEY_WIFISTRENGTH, network.getWiFiStrengths()+""); //get wifi strength
        values.put(KEY_FREQUENCY, network.getWiFiFrequency()+""); //get wifi freqency-channel
        values.put(KEY_LIVENET, network.getIsLive()); //get info if wifi is connected to internet
 
        // 3. insert
        db.insert(TABLE_NETWORKS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
 
        // 4. close
        db.close(); 
		
	}
	
	public List<OpenNetwork> getNetworkPoints(String searchBssid){
		List<OpenNetwork> networkPoints = new ArrayList<OpenNetwork>();
		// 1. build query
		String query = "SELECT * FROM "+TABLE_NETWORKS+" WHERE "+KEY_BSSID+" = " +"'"+searchBssid+"'";
		
		// 2. get reference to writable DB
	       SQLiteDatabase db = this.getWritableDatabase();
	       Cursor cursor = db.rawQuery(query, null);
		
	    // 3. go over each row, build network and add it to list
	       OpenNetwork network = null;
	       String bssid, ssid;
	       float wifiStrength,gpsaccuracy;
	       double lat,lng;
	       int freq=0,live=0;
	       LatLng gpsLocation=new LatLng(0, 0);
	       if (cursor.moveToFirst()) {
	           do {
//	        	   COLUMNS = {KEY_ID, KEY_BSSID,KEY_SSID, KEY_COORDINATES, KEY_GPSSTRENGTH,KEY_WIFISTRENGTH,KEY_FREQUENCY};
	        	   bssid=cursor.getString(1);
	        	   ssid=cursor.getString(2);
	        	   wifiStrength=Float.parseFloat(cursor.getString(5)+"");
	        	   gpsaccuracy=Float.parseFloat(cursor.getString(4)+"");
	        	   freq=Integer.parseInt(cursor.getString(6));
	        	   lat=Double.parseDouble(cursor.getString(3).split(";")[0]);
	        	   lng=Double.parseDouble(cursor.getString(3).split(";")[1]);
	        	   live=cursor.getInt(7);
	        	   gpsLocation=new LatLng(lat, lng);
	        	   
	               network = new OpenNetwork(bssid, ssid, freq, gpsLocation, gpsaccuracy, wifiStrength,live);
	               
	 
	               // Add to network list
	               networkPoints.add(network);
	           } while (cursor.moveToNext());
	       }
	 
	       Log.d("getNetworkPoints()", networkPoints.toString());
	       
	       db.close();
	       // return books
	       return networkPoints;
		
	}
	
	public List<OpenNetwork> getAllNetworkPoints(){
		List<OpenNetwork> networkPoints = new ArrayList<OpenNetwork>();
		// 1. build query
		String query = "SELECT * FROM "+TABLE_NETWORKS;
		
		// 2. get reference to writable DB
	       SQLiteDatabase db = this.getWritableDatabase();
	       Cursor cursor = db.rawQuery(query, null);
		
	    // 3. go over each row, build network and add it to list
	       OpenNetwork network = null;
	       String bssid, ssid;
	       float wifiStrength,gpsaccuracy;
	       double lat,lng;
	       int freq=0,live=0;
	       LatLng gpsLocation=new LatLng(0, 0);
	       if (cursor.moveToFirst()) {
	           do {
//	        	   COLUMNS = {KEY_ID, KEY_BSSID,KEY_SSID, KEY_COORDINATES, KEY_GPSSTRENGTH,KEY_WIFISTRENGTH,KEY_FREQUENCY};
	        	   bssid=cursor.getString(1);
	        	   ssid=cursor.getString(2);
	        	   wifiStrength=Float.parseFloat(cursor.getString(5)+"");
	        	   gpsaccuracy=Float.parseFloat(cursor.getString(4)+"");
	        	   freq=Integer.parseInt(cursor.getString(6));
	        	   lat=Double.parseDouble(cursor.getString(3).split(";")[0]);
	        	   lng=Double.parseDouble(cursor.getString(3).split(";")[1]);
	        	   live=cursor.getInt(7);
	        	   gpsLocation=new LatLng(lat, lng);
	        	   
	               network = new OpenNetwork(bssid, ssid, freq, gpsLocation, gpsaccuracy, wifiStrength,live);
	               
	 
	               // Add to network list
	               networkPoints.add(network);
	           } while (cursor.moveToNext());
	       }
	 
	       Log.d("getNetworkPoints()", networkPoints.toString());
	       
	       db.close();
	       // return books
	       return networkPoints;
	}
	
	public int updateNetwork(OpenNetwork network){
		
		String searchBssid=network.getBSSID();
		
		//check if there are more than 30 points for this bssid
		SQLiteDatabase db = this.getWritableDatabase();
		
		//check if there are more than 30 points for this bssid
		// 1. build query
		String query = "SELECT COUNT(*) FROM "+TABLE_NETWORKS+" WHERE "+KEY_BSSID+" = " +"'"+searchBssid+"'";
		Cursor cursor = db.rawQuery(query, null);	
		if(cursor.moveToFirst())
			if(cursor.getInt(0) > 30){
				
				//find Weakest And Update
				int updateIndex=findWorstPoint(cursor.getString(1));
				
				//update row
				ContentValues values = new ContentValues();
		        values.put(KEY_BSSID, network.getBSSID()); // get BSSID
		        values.put(KEY_SSID, network.getSSID()); // get SSID
		        values.put(KEY_COORDINATES, network.getLocations()); //get location
		        values.put(KEY_GPSSTRENGTH, network.getGPSaccuracy()); //get accuracy
		        values.put(KEY_WIFISTRENGTH, network.getWiFiStrengths()); //get wifi strength
		        values.put(KEY_FREQUENCY, network.getWiFiFrequency()); //get wifi freqency-channel
			    values.put(KEY_LIVENET, network.getIsLive()+""); //get info if wifi is connected to internet
			    
			    db.update(TABLE_NETWORKS, //table
			            values, // column/value
			            KEY_ID+" = ?", // selections
			            new String[] { updateIndex+""}); //selection args
			    
				
				db.close();
				return updateIndex;
			}
			else
			{
				//add new
				addNetwork(network);
				db.close();
				return 1;
			}
		else
		{
			//no entries for this bssid yet, add new
			addNetwork(network);
			db.close();
			return 1;
		}
	}
	
	
	public void deleteEntireNetwork(OpenNetwork network)
	{
		 // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. delete
        db.delete(TABLE_NETWORKS, //table name
                KEY_BSSID+" = ?",  // selections
                new String[] { String.valueOf(network.getBSSID()) }); //selections args
 
        // 3. close
        db.close();
 
        //log
    Log.d("deleteEntireNetwork ", network.toString());
	}
	
	
	
	public int findWorstPoint(String bssid){
		SQLiteDatabase db = this.getWritableDatabase();
		String query= "SELECT "+KEY_ID+", "+KEY_GPSSTRENGTH+", "+KEY_WIFISTRENGTH+" FROM "+TABLE_NETWORKS+" WHERE "+KEY_BSSID+"= '"+bssid+"'";
		Cursor cursor = db.rawQuery(query, null);
		float currentSignal,worstSignal=Float.MAX_VALUE;
		int worstSignalID=-1;
		
		if(cursor.moveToFirst())
			do{
				currentSignal=Float.parseFloat(cursor.getString(1))*Float.parseFloat(cursor.getString(2));
				if(currentSignal < worstSignal)
				{
					worstSignal=currentSignal;
					worstSignalID= cursor.getInt(0);
				}
			}while(cursor.moveToNext());
		
		db.close();
		
		Log.d("findWorstPoint id=", worstSignalID+"");
		
		return worstSignalID;
		
	}
	
}
