package com.wifreemaps;

import java.io.BufferedReader;
import java.io.IOException;
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
import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class HttpRequestHelper {

	Context myContext;
	ServerExchangeHelper serverHelper;
	List<OpenNetwork> dataNetwork;
	
	public HttpRequestHelper()
	{
		
	}
	
	
	public void getNetworksRequest(Context context, String url){
		myContext = context;
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
				JSONObject payload = serverHelper.prepareJSON(networksToSend);




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
