package com.eldisprojectexpert.locationaware;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int LOCATION_REQUEST_PERMISSION_CODE = 100;
    private static final long UPDATE_INTERVAL_LOCATION = 5000L;
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    Location currentLocation;

    MapView mapView;
    TextView textViewGetAddressResult;
    private String locationAddress;
    private AddressResultReceiver addressResultReceiver;
    boolean isAddressRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        textViewGetAddressResult = findViewById(R.id.textview_address);
        mapView = findViewById(R.id.mapView);
        Bundle bundle = null;
        if (savedInstanceState != null){
            bundle = savedInstanceState.getBundle(getResources().getString(R.string.google_maps_key));
        }

        mapView.onCreate(bundle);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_LOCATION);

       locationCallback = new LocationCallback(){
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (locationAvailability.isLocationAvailable()){
                }
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

        getLocationPermission();
//        updateValuesFromBundle(savedInstanceState);
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

        // Add a marker in Sydney and move the camera
        LatLng myCity = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(myCity).title("Tanjung Pinang"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCity, 10.0f));
    }



    private void getLocationPermission(){
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//            Toast.makeText(MapsActivity.this, "Location Permission Granted", Toast.LENGTH_LONG).show();
           fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, MapsActivity.this.getMainLooper());
           fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
               @Override
               public void onSuccess(Location location) {
                   currentLocation = location;

//                   // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//                   SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                           .findFragmentById(R.id.map);
//                   mapFragment.getMapAsync(MapsActivity.this);

                   mapView.getMapAsync(MapsActivity.this);
               }
           });
            fusedLocationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(MapsActivity.this, "Permission Needed", Toast.LENGTH_SHORT);
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_PERMISSION_CODE);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getLocationPermission();
    }

    private void stopGettingLocation(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    //getting user address
    public void getUserAddress(View view){
        isAddressRequested = true;
        getAddress(currentLocation);
    }

    //Check whether geocoder is present or not
    private void getAddress(Location location){
        if (!Geocoder.isPresent()){
            Toast.makeText(this, "Geocoder is not present", Toast.LENGTH_LONG).show();
        } else {
            if (isAddressRequested){
                startAddressFetcherService();
            }
        }
    }


    //Trigger to start service
    private void startAddressFetcherService(){
        Intent intent = new Intent(this, AddressFetcherService.class);
        addressResultReceiver = new AddressResultReceiver(new Handler());
        intent.putExtra(Constants.RECEIVER, addressResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, currentLocation);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        stopGettingLocation();
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Bundle bundle = outState.getBundle(getResources().getString(R.string.google_maps_key));
        if (bundle == null){
            bundle = new Bundle();
            outState.putBundle(getResources().getString(R.string.google_maps_key), bundle);
        }

        mapView.onSaveInstanceState(bundle);
    }
//    private void updateValuesFromBundle(Bundle saveInstanceState){
//        if (saveInstanceState != null){
//            if (saveInstanceState.keySet().contains("ADDRESS_REQUEST_KEY")){
//                isAddressRequested = saveInstanceState.getBoolean("ADDRESS_REQUEST_KEY");
//            }
//
//            if (saveInstanceState.keySet().contains("LOCATION_ADDRESS_KEY")){
//                locationAddress = saveInstanceState.getString("LOCATION_ADDRESS_KEY");
//                textViewGetAddressResult.setText(locationAddress);
//                Log.i(TAG, "updateValuesFromBundle: Address " + locationAddress);
//            }
//        }
//    }

    //inner class
    public class AddressResultReceiver extends ResultReceiver{

        public AddressResultReceiver(Handler handler){
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == Constants.SUCCESS_RESULT){
                locationAddress = resultData.getString(Constants.RESULT_DATA_KEY);
                textViewGetAddressResult.setText(locationAddress);
                isAddressRequested = false;
                Toast.makeText(getApplicationContext(), locationAddress, Toast.LENGTH_SHORT).show();
            } else {
                locationAddress = resultData.getString(Constants.RESULT_DATA_KEY);
                textViewGetAddressResult.setText(locationAddress);
            }
        }
    }
}