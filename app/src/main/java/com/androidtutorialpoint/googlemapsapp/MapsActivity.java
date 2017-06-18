package com.androidtutorialpoint.googlemapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationListener {

	private GoogleMap mMap;
	double latitude;
	double longitude;
	private int PROXIMITY_RADIUS = 5000;
	GoogleApiClient mGoogleApiClient;
	Location mLastLocation;
	Marker mCurrLocationMarker;
	LocationRequest mLocationRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			checkLocationPermission();
		}
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}


	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mMap.getUiSettings().setTiltGesturesEnabled(false);

		//Initialize Google Play Services
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				buildGoogleApiClient();
				mMap.setMyLocationEnabled(true);
			}
		} else {
			buildGoogleApiClient();
			mMap.setMyLocationEnabled(true);
		}

	}


	public void carregaDentista() {
		String url = getUrl(latitude, longitude, "dentist");
		Object[] DataTransfer = new Object[2];
		DataTransfer[0] = mMap;
		DataTransfer[1] = url;
		GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
		getNearbyPlacesData.execute(DataTransfer);
		Log.d("URL", url);
	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnected(Bundle bundle) {

		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(3600000);
		mLocationRequest.setFastestInterval(60000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
		}

		Toast.makeText(MapsActivity.this, "Conectado com sucesso, carregando...", Toast.LENGTH_LONG).show();

	}

	private String getUrl(double latitude, double longitude, String nearbyPlace) {

		StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
		googlePlacesUrl.append("location=" + latitude + "," + longitude);
		googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
		googlePlacesUrl.append("&type=" + nearbyPlace);
		googlePlacesUrl.append("&sensor=true");
		googlePlacesUrl.append("&key=" + "AIzaSyDOa3cNh_7o15n_mbq3FCvAgDxFIj4W9LU");
		Log.d("getUrl", googlePlacesUrl.toString());
		return (googlePlacesUrl.toString());
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onLocationChanged(Location location) {

		mLastLocation = location;
		if (mCurrLocationMarker != null) {
			mCurrLocationMarker.remove();
			Log.d("VERIFICAÇÃO", "Current = last");
		}

		//Place current location marker
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		Log.d("latitude",  String.valueOf(latitude));
		Log.d("longitude", String.valueOf(longitude));
		LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latLng);
		markerOptions.title("Você está Aqui");
		markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
		mCurrLocationMarker = mMap.addMarker(markerOptions);

		//move map camera
		CameraUpdate mUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15); //latlng and zomm simultaneously

		mMap.animateCamera(mUpdate);
		Toast.makeText(MapsActivity.this, "Conectado com sucesso, carregando...", Toast.LENGTH_LONG).show();

		/*
		mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		//chama nearby
		carregaDentista();

		//stop location updates
		if (mGoogleApiClient != null) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
		}

	}


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

	public boolean checkLocationPermission() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

			// Asking user if explanation is needed
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.ACCESS_FINE_LOCATION)) {

				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.

				//Prompt the user once explanation has been shown
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						MY_PERMISSIONS_REQUEST_LOCATION);


			} else {
				// No explanation needed, we can request the permission.
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						MY_PERMISSIONS_REQUEST_LOCATION);
			}
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted. Do the
					// contacts-related task you need to do.
					if (ContextCompat.checkSelfPermission(this,
							Manifest.permission.ACCESS_FINE_LOCATION)
							== PackageManager.PERMISSION_GRANTED) {

						if (mGoogleApiClient == null) {
							buildGoogleApiClient();
						}
						mMap.setMyLocationEnabled(true);
					}

				} else {

					// Permission denied, Disable the functionality that depends on this permission.
					Toast.makeText(this, "Permissão negada", Toast.LENGTH_LONG).show();
				}
				return;
			}

			// other 'case' lines to check for other permissions this app might request.
			// You can add here other case statements according to your requirement.
		}
	}
}
