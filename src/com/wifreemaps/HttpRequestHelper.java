package com.wifreemaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.bs;
import com.google.android.gms.maps.model.LatLng;

import android.R.string;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class HttpRequestHelper {

	Context myContext;
	
	MySQLiteHelper db;
	ServerExchangeHelper serverHelper;
	List<OpenNetwork> dataNetwork;
	List<NetworkPoint> dataPoints;

	List<OpenNetwork> resultNetworks;
	List<NetworkPoint> resultPoints;

	public HttpRequestHelper()
	{
		
	}


	public void getNetworksRequest(Context context, String url){
		myContext = context;
		
		db=new MySQLiteHelper(myContext);
		
		new GETNetworks().execute(url);
	}


	public void sendNetworksToServer(Context context, String url, List<OpenNetwork> currentNetworks){
		myContext = context;
		serverHelper = new ServerExchangeHelper();
		dataNetwork = currentNetworks;
		new SendNetworks().execute(url);
	}

	public void getPointsRequest(Context context, String url)
	{
		myContext = context;
		new GETPoints().execute(url);
	}
	
	public void sendPointsToServer(Context context, String url, List<NetworkPoint> currentPoints){
		myContext = context;
		serverHelper = new ServerExchangeHelper();
		dataPoints = currentPoints;
		new SendPoints().execute(url);
	}


	private class GETNetworks extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {

			
			HttpParams httpParameters = new BasicHttpParams();
			// set the connection timeout and socket timeout parameters (milliseconds)
			HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
			HttpConnectionParams.setSoTimeout(httpParameters, 5000);

			HttpClient client = new DefaultHttpClient(httpParameters);
			Log.d("HTTP responded","starting http request...");

			String result = null;

			InputStream inputStream = null;

			resultNetworks=new ArrayList<OpenNetwork>();

			try {
				// Add your data
				Log.d("HTTP responded","request mode: networks");

				JSONObject payload = new JSONObject();
				try {

					payload.put("mode", "networks");

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				HttpPost httppost = new HttpPost( urls[0]);
				httppost.setHeader("Accept", "application/json");
				httppost.setHeader("Content-type", "application/json");
				httppost.setEntity(new ByteArrayEntity(
						payload.toString().getBytes("UTF8")));
				Log.d("HTTP responded","execute request...");



				HttpResponse response = client.execute(httppost);
				HttpEntity responseEntity = response.getEntity();

				inputStream = responseEntity.getContent();

				Log.d("HTTP responded","reading response...");
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null)
				{
					sb.append(line + "\n");
				}
				result = sb.toString();
				
				
				//read json
				
				JSONObject jObject = new JSONObject();

				try {
					jObject = new JSONObject(result);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JSONArray jArray = new JSONArray();

				try {
					jArray=jObject.getJSONArray("networks");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				LatLng location= new LatLng(0, 0);

				for (int i=0; i < jArray.length(); i++)
				{
					try {
						JSONObject oneNetwork = jArray.getJSONObject(i);
						// Pulling items from the array
						String bssid = oneNetwork.getString("bssid");
						String ssid = oneNetwork.getString("ssid");
						String cityname = oneNetwork.getString("cityname");
						String gpslocation = oneNetwork.getString("approxgps");
						int freq = oneNetwork.getInt("frequency");
						int reachable = oneNetwork.getInt("wwwreachable");


						location = new LatLng(Double.parseDouble(gpslocation.split(";")[0]), 
								Double.parseDouble(gpslocation.split(";")[1]));

						resultNetworks.add(new OpenNetwork(bssid,ssid,cityname,freq,location,reachable));

					} catch (JSONException e) {
						// Oops
					}
				}
				Log.d("SERVER RESPONSE", "Got that many networks from server:"+resultNetworks.size());
				
				//TODO: update data in database, then call dbgetnetworks from main activity


			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			finally {
				try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
			}



			return result;




		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				
				//add result to database
				for(OpenNetwork ntwk:resultNetworks)
				{
					db.addNetwork(ntwk);
				}
					
				Toast.makeText(myContext, "Got that many networks from server:"+resultNetworks.size(), Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	//sending the data to server
	private class SendNetworks extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {


			HttpParams httpParameters = new BasicHttpParams();
			// set the connection timeout and socket timeout parameters (milliseconds)
			HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
			HttpConnectionParams.setSoTimeout(httpParameters, 5000);

			HttpClient client = new DefaultHttpClient(httpParameters);
			Log.d("HTTP responded","starting http request...");

			String result = null;
			try {
				// Add your data
				Log.d("HTTP responded","adding data...");
				ArrayList<OpenNetwork> networksToSend= new ArrayList<OpenNetwork>();
				for(OpenNetwork na:dataNetwork)
				{
					networksToSend.add(na);
				}
				JSONObject payload = serverHelper.prepareJSONnetworks(networksToSend);




				HttpPost httppost = new HttpPost( urls[0]);
				httppost.setHeader("Accept", "application/json");
				httppost.setHeader("Content-type", "application/json");
				httppost.setEntity(new ByteArrayEntity(
						payload.toString().getBytes("UTF8")));
				Log.d("HTTP responded","execute request...");



				HttpResponse response = client.execute(httppost);
				HttpEntity responseEntity = response.getEntity();
				Log.d("HTTP responded","reading response...");
				String thisline="";
				if (responseEntity != null) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(responseEntity.getContent(),
									"UTF-8"));

					while( (thisline = reader.readLine()) != null)
						result=result + thisline+"\n";
					Log.d("HTTP responded","Http said:"+result);
				} else {
					Log.d("HTTP responded","nothing");
				}

			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			return result;






		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Toast.makeText(myContext, "Data Sent!", Toast.LENGTH_LONG).show();
		}

	}

	private class GETPoints extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {


			HttpParams httpParameters = new BasicHttpParams();
			// set the connection timeout and socket timeout parameters (milliseconds)
			HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
			HttpConnectionParams.setSoTimeout(httpParameters, 5000);

			HttpClient client = new DefaultHttpClient(httpParameters);
			Log.d("HTTP responded","starting http request...");

			String result = null;
			InputStream inputStream = null;

			resultPoints=new ArrayList<NetworkPoint>();

			try {
				// Add your data
				Log.d("HTTP responded","request mode: points");

				JSONObject payload = new JSONObject();
				try {

					payload.put("mode", "points");

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				HttpPost httppost = new HttpPost( urls[0]);
				httppost.setHeader("Accept", "application/json");
				httppost.setHeader("Content-type", "application/json");
				httppost.setEntity(new ByteArrayEntity(
						payload.toString().getBytes("UTF8")));
				Log.d("HTTP responded","execute request...");



				HttpResponse response = client.execute(httppost);
				HttpEntity responseEntity = response.getEntity();

				inputStream = responseEntity.getContent();

				Log.d("HTTP responded","reading response...");
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null)
				{
					sb.append(line + "\n");
				}
				result = sb.toString();


				
				//get json data
				
				JSONObject jObject = new JSONObject();

				try {
					jObject = new JSONObject(result);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JSONArray jArray = new JSONArray();

				try {
					jArray=jObject.getJSONArray("points");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				LatLng location= new LatLng(0, 0);

				for (int i=0; i < jArray.length(); i++)
				{
					try {
						JSONObject onePoint = jArray.getJSONObject(i);
						// Pulling items from the array
						String pbssid = onePoint.getString("pbssid");
						String gpslocation = onePoint.getString("gpslocation");
						double wifistrength = onePoint.getDouble("wifistrength");
						double gpsaccuracy = onePoint.getDouble("gpsaccuracy");
						long timestamp = Long.parseLong(onePoint.getString("timestamp"));

						location = new LatLng(Double.parseDouble(gpslocation.split(";")[0]), 
								Double.parseDouble(gpslocation.split(";")[1]));

						resultPoints.add(new NetworkPoint(pbssid,location,wifistrength,gpsaccuracy,timestamp));
					} catch (JSONException e) {
						// Oops
					}
				}
				
				Log.d("SERVER RESPONSE", "Got that many points from server:"+resultPoints.size());
				//TODO: update data in database, then call dbgetallpoints from main activity
				
				
				
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			finally {
				try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
			}

			

			return result;






		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				//add result to database
				for(NetworkPoint pnt:resultPoints)
				{
					db.addNetworkPoint(pnt);
				}
				Toast.makeText(myContext, "Got that many points from server:"+resultPoints.size(), Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


	}

	//sending the data to server
	private class SendPoints extends AsyncTask<String, Void, String> {
			@Override
			protected String doInBackground(String... urls) {


				HttpParams httpParameters = new BasicHttpParams();
				// set the connection timeout and socket timeout parameters (milliseconds)
				HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
				HttpConnectionParams.setSoTimeout(httpParameters, 5000);

				HttpClient client = new DefaultHttpClient(httpParameters);
				Log.d("HTTP responded","starting http request...");

				String result = null;
				try {
					// Add your data
					Log.d("HTTP responded","adding data...");
					ArrayList<NetworkPoint> pointsToSend= new ArrayList<NetworkPoint>();
					for(NetworkPoint np:dataPoints)
					{
						pointsToSend.add(np);
					}
					JSONObject payload = serverHelper.prepareJSONpoints(pointsToSend);




					HttpPost httppost = new HttpPost( urls[0]);
					httppost.setHeader("Accept", "application/json");
					httppost.setHeader("Content-type", "application/json");
					httppost.setEntity(new ByteArrayEntity(
							payload.toString().getBytes("UTF8")));
					Log.d("HTTP responded","execute request...");



					HttpResponse response = client.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					Log.d("HTTP responded","reading response...");
					String thisline="";
					if (responseEntity != null) {
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(responseEntity.getContent(),
										"UTF-8"));

						while( (thisline = reader.readLine()) != null)
							result=result + thisline+"\n";
						Log.d("HTTP responded","Http said:"+result);
					} else {
						Log.d("HTTP responded","nothing");
					}

				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				return result;






			}
			// onPostExecute displays the results of the AsyncTask.
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				Toast.makeText(myContext, "Data Sent!", Toast.LENGTH_LONG).show();
			}

		}
}
