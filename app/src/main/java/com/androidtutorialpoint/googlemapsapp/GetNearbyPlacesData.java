package com.androidtutorialpoint.googlemapsapp;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

	String googlePlacesData;
	GoogleMap mMap;
	String url;

	@Override
	protected String doInBackground(Object... params) {
		try {
			Log.d("GetNearbyPlacesData", "doInBackground entered");
			mMap = (GoogleMap) params[0];
			url = (String) params[1];
			DownloadUrl downloadUrl = new DownloadUrl();
			googlePlacesData = downloadUrl.readUrl(url);
			Log.d("GooglePlacesReadTask", "doInBackground Exit");
		} catch (Exception e) {
			Log.d("GooglePlacesReadTask", e.toString());
		}
		return googlePlacesData;
	}

	@Override
	protected void onPostExecute(String result) {
		Log.d("GooglePlacesReadTask", "onPostExecute Entered");
		List<HashMap<String, String>> nearbyPlacesList = null;
		DataParser dataParser = new DataParser();
		nearbyPlacesList = dataParser.parse(result);
		ShowNearbyPlaces(nearbyPlacesList);
		Log.d("GooglePlacesReadTask", "onPostExecute Exit");
	}

	private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {
		for (int i = 0; i < nearbyPlacesList.size(); i++) {
			Log.d("onPostExecute", "Entered into showing locations");
			MarkerOptions markerOptions = new MarkerOptions();
			HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
			double lat = Double.parseDouble(googlePlace.get("lat"));
			double lng = Double.parseDouble(googlePlace.get("lng"));
			String placeName = googlePlace.get("place_name");
			String vicinity = googlePlace.get("vicinity");
			String rating = googlePlace.get("rating");
			LatLng latLng = new LatLng(lat, lng);
			markerOptions.position(latLng)
					.title(placeName)
					.snippet(vicinity + "\n Classificação: " + rating )
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
			mMap.addMarker(markerOptions);

			//move map camera
			CameraUpdate mUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);

			mMap.animateCamera(mUpdate);
		}
	}
}
