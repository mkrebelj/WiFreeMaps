package com.wifreemaps;

import java.util.ArrayList;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import android.R.string;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper{
	private static final int DATABASE_VERSION = 7;
	private static final String DATABASE_NAME = "OpenNetworkDB";
	Context myParentActivity;
	
	public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); 
        myParentActivity =  context;
    }


	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// SQL statement to create network table
	    Log.d("CreateTable", "networks");
		
        String CREATE_OPENNETWORK_TABLE = "CREATE TABLE networks ( " +
                "bssid TEXT PRIMARY KEY, " +
                "ssid TEXT, "+
                "cityname TEXT, "+
                "approxgps TEXT, "+
                "frequency INTEGER, "+
                "wwwreachable INTEGER)";
        
        
        
        
        // create tables
        db.execSQL(CREATE_OPENNETWORK_TABLE);
     
        // SQL statement to create points table
        Log.d("CreateTable", "points");
        String CREATE_GPSPOINT_TABLE= "CREATE TABLE points ( "+
                "pbssid TEXT, " +
                "gpslocation TEXT UNIQUE ON CONFLICT IGNORE, "+
                "wifistrength REAL, " +
        		"gpsaccuracy REAL, " +
                "quality REAL, "+
                "timestamp INTEGER," +
                "synchronized INTEGER, "+
                "PRIMARY KEY ( pbssid, timestamp) )";
        
        db.execSQL(CREATE_GPSPOINT_TABLE);
        
        
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS networks");
        db.execSQL("DROP TABLE IF EXISTS points");
        // create fresh table
        this.onCreate(db);
    }
	//---------------------------------------------------------------------
	 
    /**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */
	
	private static final String TABLE_NETWORKS = "networks";
	private static final String TABLE_POINTS = "points";
	

	
	//table column names
	private static final String KEY_BSSID = "bssid";
	private static final String KEY_SSID = "ssid";
	private static final String KEY_CITYNAME= "cityname";
	private static final String KEY_APPROXIMATELOCATION = "approxgps";
	private static final String KEY_FREQUENCY = "frequency";
	private static final String KEY_REACHABLE = "wwwreachable";
	private static final String KEY_POINT_BSSID = "pbssid";
	private static final String KEY_GPS_LOCATION = "gpslocation";
	private static final String KEY_WIFISTRENGTH = "wifistrength";
	private static final String KEY_GPSACCURACY = "gpsaccuracy";
	private static final String KEY_QUALITY = "quality";
	private static final String KEY_TIMESTAMP = "timestamp";
	private static final String KEY_SYNC = "synchronized";
	
	private static final String[] COLUMNS_NETWORK = {KEY_BSSID,KEY_SSID, KEY_CITYNAME, KEY_APPROXIMATELOCATION, KEY_FREQUENCY, KEY_REACHABLE};
	private static final String[] COLUMNS_POINT = {KEY_POINT_BSSID, KEY_GPS_LOCATION, KEY_WIFISTRENGTH, KEY_GPSACCURACY, KEY_QUALITY, KEY_TIMESTAMP, KEY_SYNC};
	
	
	
	public void addNetwork(OpenNetwork network){
		Log.d("addNetwork", network.toString());
		
		// 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_BSSID, network.getBSSID()+""); // get BSSID
        values.put(KEY_SSID, network.getSSID()+""); // get SSID
        values.put(KEY_CITYNAME, network.getCityName()+""); //get city name
        values.put(KEY_APPROXIMATELOCATION, network.getLocation()); //get approximate location	
        values.put(KEY_FREQUENCY, network.getWiFiFrequency()+""); //get wifi freqency-channel
        values.put(KEY_REACHABLE, network.isNetworkReachable()); //get info if wifi is connected to internet
 
        // 3. insert
        db.insert(TABLE_NETWORKS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
 
        // 4. close
        db.close(); 
		
	}
	
	public int updateUnknownLocations(){
		
		ArrayList<OpenNetwork> networks = new ArrayList<OpenNetwork>();
		ArrayList<String> bssids = new ArrayList<String>();
		ArrayList<LatLng> gpsCoordinates = new ArrayList<LatLng>();
		// 1. build query
		String query = "SELECT "+KEY_BSSID+" , "+KEY_APPROXIMATELOCATION+" FROM "+
				TABLE_NETWORKS+
				" WHERE "+KEY_CITYNAME+" = " +"'unknown'";

		// 2. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		// 3. go over each row, build network and add it to list
		OpenNetwork ntwk = null;
		String bssid;
		ArrayList<String> updatedAddresses=new ArrayList<String>();

		double lat=0,lng=0;
		LatLng gpsLocation=new LatLng(0, 0);

		//create a list of coordinates that need to be translated to location address
		if (cursor.moveToFirst()) {
			do {
				//			   0     	  "pbssid TEXT PRIMARY, " +
				//			   1     		"indx INTEGER PRIMARY KEY AUTOINCREMENT, " +
				//			   2             "gpslocation TEXT,"+
				//			   3             "wifistrength REAL, " +
				//			   4     		"gpsaccuracy REAL, " +
				//			   5             "quality REAL, "+
				//			   6             "timestamp INTEGER)";


				bssid=cursor.getString(0);
				bssids.add(bssid);
				Log.d("BSSID to update location:", bssid);


				try {
					lat=Double.parseDouble(cursor.getString(1).split(";")[0]);
					lng=Double.parseDouble(cursor.getString(1).split(";")[1]);
					Log.d("Location for that ntwk:", lat+","+lng);
					gpsLocation = new LatLng(lat, lng);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//add coordinates to a list
				gpsCoordinates.add(gpsLocation);

			} while (cursor.moveToNext());
		}



		updatedAddresses =( (MainActivity) myParentActivity).determineAddress(gpsCoordinates);
		
		int i=0;
		for(String address:updatedAddresses)
		{
			//create network from existing and updated data
			ntwk = new OpenNetwork(bssids.get(i), "", address, 0, gpsCoordinates.get(i), 0);
			// Add to network list
			networks.add(ntwk);
			i++;
			Log.d("Updated address:", address);
		}
		
		//everything is prepared, now database can be updated with correct data
		

		int updated = 0;
		for(OpenNetwork updatedNetwork : networks)
		{
			ContentValues cv = new ContentValues();
			cv.put(KEY_CITYNAME,updatedNetwork.getCityName());  
			updated += db.update(TABLE_NETWORKS, cv, KEY_BSSID+" = '"+updatedNetwork.getBSSID()+"'", null);
		}

		if(networks.size() != updated)
		{
			Log.d("Updated rows incorrect", "Should update: "+networks.size()+" but updated "+updated);
		}



		//			       Log.d("getNetworkPoi{nts()", networkPoints.toString());

		db.close();	

		return updated;
	}


	public int deleteAllNetworks()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM "+ TABLE_NETWORKS);
		return 1;
	}
	
	public void addNetworkPoint(NetworkPoint point){
		
		Log.d("addPoint", point.toString());
		// 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
		

        // 2. create ContentValues to add key "column"/value
        // add new point to database first
        ContentValues values = new ContentValues();
        values.put(KEY_POINT_BSSID, point.getBSSID()+""); // get BSSID
        values.put(KEY_GPS_LOCATION, point.getLocationAsString());
        values.put(KEY_WIFISTRENGTH, point.getWifiStrength()); //
        values.put(KEY_GPSACCURACY, point.getGpsAccuracy());
        values.put(KEY_QUALITY, point.getQuality());
        values.put(KEY_TIMESTAMP, point.getTimestamp());
        values.put(KEY_SYNC, 0);
        // 3. insert
        db.insert(TABLE_POINTS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        
        // 4. check if database has more than 30 entries for this network, and delete point with worst quality
        

		//if there are more than 30 points, sort them by descending quality and delete bad ones
		String RETAIN_ONLY_BEST_POINTS= "DELETE FROM "+TABLE_POINTS+" "+
				"WHERE "+KEY_POINT_BSSID+" = '"+point.getBSSID()+"' AND "+KEY_QUALITY+" NOT IN ("+
				 "SELECT "+KEY_QUALITY+" "+
				  "FROM ("+
					"SELECT "+KEY_QUALITY+ " "+
					"FROM "+TABLE_POINTS+" WHERE "+KEY_POINT_BSSID+" = '"+point.getBSSID()+"' "+
					"ORDER BY "+KEY_QUALITY +" DESC "+
					"LIMIT 30"+
				  ") foo"+
				" )";
		Cursor cursor = db.rawQuery(RETAIN_ONLY_BEST_POINTS, null);
		if (cursor.moveToFirst()) {
			Log.d("NESTO","Nesto se dogodilo!");
		}
		//db.execSQL(RETAIN_ONLY_BEST_POINTS, null);
		

        // 5. close
        db.close(); 
	}
	
	public List<NetworkPoint> getNetworkPoints(String searchBssid){
		List<NetworkPoint> networkPoints = new ArrayList<NetworkPoint>();
		// 1. build query
		String query = "SELECT * FROM "+
						TABLE_POINTS+//","+TABLE_NETWORKS +
						" WHERE "+KEY_POINT_BSSID+" = " +"'"+searchBssid+"'";
		
		// 2. get reference to writable DB
	       SQLiteDatabase db = this.getWritableDatabase();
	       Cursor cursor = db.rawQuery(query, null);
		
	    // 3. go over each row, build network and add it to list
	       NetworkPoint point = null;
	       String bssid;
	       
	       double wifiStrength,gpsaccuracy;
	       double lat,lng;
	       long timestamp;
	       
	       LatLng gpsLocation=new LatLng(0, 0);
	       if (cursor.moveToFirst()) {
	           do {
	        	   
//	   0     	   pbssid TEXT, " +
//	   1             "gpslocation TEXT UNIQUE ON CONFLICT IGNORE, "+
//	   2             "wifistrength REAL, " +
//	   3     		"gpsaccuracy REAL, " +
//	   4             "quality REAL, "+
//	   5             "timestamp INTEGER," +
//	   6             "synchronized INTEGER, "+
	        	   
	        	   
	        	   bssid=cursor.getString(0);
	        	   Log.d("cursor0", bssid);
	        	   
	        	   try {
					   lat=Double.parseDouble(cursor.getString(1).split(";")[0]);
					   lng=Double.parseDouble(cursor.getString(1).split(";")[1]);
					   Log.d("cursor2", lat+","+lng);
					   gpsLocation = new LatLng(lat, lng);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	   
	        	   wifiStrength = cursor.getDouble(2);
	        	   gpsaccuracy = cursor.getDouble(3);
	        	   
	        	   timestamp = cursor.getLong(5);
	        	   
	        	   
	               point = new NetworkPoint(bssid, gpsLocation, wifiStrength, gpsaccuracy, timestamp);
	               
	 
	               // Add to network list
	               networkPoints.add(point);
	           } while (cursor.moveToNext());
	       }
	 
//	       Log.d("getNetworkPoints()", networkPoints.toString());
	       
	       db.close();
	       // return books
	       return networkPoints;
		
	}
	
	public List<OpenNetwork> getAllNetworks(){
		List<OpenNetwork> networks = new ArrayList<OpenNetwork>();
		// 1. build query
		String query = "SELECT * FROM "+TABLE_NETWORKS;
		
		// 2. get reference to writable DB
	       SQLiteDatabase db = this.getWritableDatabase();
	       Cursor cursor = db.rawQuery(query, null);
		
	       
	       
//	  0     "bssid TEXT PRIMARY KEY, " +
//    1       "ssid TEXT, "+
//    2       "cityname TEXT, "+
//    3       "approxgps TEXT, "+
//    4       "frequency INTEGER, "+
//    5       "wwwreachable INTEGER)";
	       
	       
	    // 3. go over each row, build network and add it to list
	       OpenNetwork network = null;
	       String bssid, ssid,city;
	       
	       double lat,lng;
	       int freq=0,reachable=2;
	       LatLng approxGpsLocation=new LatLng(0, 0);
	       if (cursor.moveToFirst()) {
	           do {
	        	   bssid=cursor.getString(0);
	        	   ssid=cursor.getString(1);
	        	   city=cursor.getString(2);
	        	   
	        	   lat=Double.parseDouble(cursor.getString(3).split(";")[0]);
	        	   lng=Double.parseDouble(cursor.getString(3).split(";")[1]);
	        	   approxGpsLocation=new LatLng(lat, lng);
	        	   
	        	   freq=Integer.parseInt(cursor.getString(4));
	        	   
	        	   reachable=cursor.getInt(5);
	        	   
	        	   
	               network = new OpenNetwork(bssid, ssid, city, freq, approxGpsLocation, reachable);
	               
	 
	               // Add to network list
	               networks.add(network);
	           } while (cursor.moveToNext());
	       }
	 
	       Log.d("getNetworks()", network.toString());
	       
	       db.close();
	       // return networks
	       return networks;
	}
	
	//TODO: add getAllNetworksFromCity(String city)
	
	
	public void deleteEntireNetwork(OpenNetwork network)
	{
		 // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. delete
        db.delete(TABLE_NETWORKS, //table name
                KEY_BSSID+" = ?",  // selections
                new String[] { String.valueOf(network.getBSSID()) }); //selections args
        // 2.1 delete all corresponding network points
        db.delete(TABLE_POINTS, KEY_POINT_BSSID, new String[] { String.valueOf(network.getBSSID()) });
        // 3. close
        db.close();
 
        //log
    Log.d("deleteEntireNetwork ", network.toString());
	}
	
	public int pointsCount()
	{
		int result=0;
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT COUNT(*) FROM "+TABLE_POINTS;
		Cursor cursor = db.rawQuery(query, null);
		if(cursor.moveToFirst())
		{
			result = cursor.getInt(0);
		}
		cursor.close();
		db.close();
		return result;
	}
	
	public int findWorstPoint(String bssid){
		SQLiteDatabase db = this.getWritableDatabase();
		String query= "SELECT "+KEY_POINT_BSSID+", "+KEY_TIMESTAMP+ 
				" FROM "+TABLE_POINTS+" " +
				"WHERE "+KEY_POINT_BSSID+"= '"+bssid+"'" +
				"ORDER BY " +KEY_QUALITY + " ASC";
		Cursor cursor = db.rawQuery(query, null);
		
		String worst="";
		int indxOdWorst=-1;
		if(cursor.moveToFirst())
		{
			indxOdWorst = cursor.getInt(1);
			worst=cursor.getString(0)+" with indx: "+ cursor.getInt(1)+ "signal quality: "+cursor.getFloat(5);
		}
		
		db.close();
		
		Log.d("findWorstPoint", worst+"");
		
		return indxOdWorst;
		
	}
	
	public boolean isDBempty()
	{
		 SQLiteDatabase db = this.getWritableDatabase();
		 String count = "SELECT count(*) FROM "+TABLE_NETWORKS;
		 Cursor mcursor = db.rawQuery(count, null);
		 mcursor.moveToFirst();
		 int icount = mcursor.getInt(0);
		 if(icount>0)
			 return false;
		 //leave 
		 else
			 return true;
		 //populate table
		
	}


	public List<NetworkPoint> getAllPoints() {
		List<NetworkPoint> points = new ArrayList<NetworkPoint>();
		// 1. build query
		String query = "SELECT * FROM "+TABLE_POINTS;
		
		// 2. get reference to writable DB
	       SQLiteDatabase db = this.getWritableDatabase();
	       Cursor cursor = db.rawQuery(query, null);
		
	       
	       

//	0       "pbssid TEXT, " +
//  1         "gpslocation TEXT UNIQUE ON CONFLICT IGNORE, "+
//  2         "wifistrength REAL, " +
//  3 		"gpsaccuracy REAL, " +
//  4         "quality REAL, "+
//  5         "timestamp INTEGER," +
//  6         "synchronized INTEGER"+
	       
	       
	    // 3. go over each row, build network and add it to list
	       NetworkPoint point = null;
	       String pbssid,gpslocation;
	       double wifistrength,gpsaccuracy,quality;
	       
	       double lat,lng;
	       int timestamp,synchronised;
	       LatLng cGpsLocation=new LatLng(0, 0);
	       if (cursor.moveToFirst()) {
	           do {
	        	   pbssid=cursor.getString(0);
	        	   gpslocation=cursor.getString(1);
	        	   wifistrength=cursor.getDouble(2);
	        	   gpsaccuracy = cursor.getDouble(3);
	        	   quality = cursor.getDouble(4);
	        	   timestamp= cursor.getInt(5);
	        	   synchronised = cursor.getInt(6);
	        	   
	        	   lat=Double.parseDouble(gpslocation.split(";")[0]);
	        	   lng=Double.parseDouble(gpslocation.split(";")[1]);
	        	   cGpsLocation=new LatLng(lat, lng);
	        	   
	        	  
	        	   
	               point = new NetworkPoint(pbssid, cGpsLocation, wifistrength, gpsaccuracy, timestamp);
	               
	 
	               // Add to network list
	               points.add(point);
	           } while (cursor.moveToNext());
	       }
	 
	       //Log.d("getNetworks()", points.toString());
	       
	       db.close();
	       // return networks
	       return points;
	}
	
}
