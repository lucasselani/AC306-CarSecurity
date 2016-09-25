package br.inatel.carsecurity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import br.inatel.carsecurity.provider.NumberManagement;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String DEBUG_TAG = getClass().getSimpleName();
    LocationRequest mLocationRequest = null;
    GoogleApiClient mGoogleApiClient = null;
    Location mCurrentLocation = null;
    Location mLastLocation = null;

    LatLng mCarLatLgn = null;
    LatLng mCurrlatLng = null;
    GoogleMap mGoogleMap = null;
    SupportMapFragment mFragment = null;
    Marker carLocationMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        TEST_POPULATE();
    }

    private void TEST_POPULATE() {
        NumberManagement mNumberManagement = new NumberManagement(this);
        mNumberManagement.setCarNumber("+5535998346484");
    }

    private void getSmsExtras() {
        Intent i = getIntent();
        if(i.hasExtra("latlgn")){
            String latlgn = i.getExtras().getString("latlgn");
            String[] content = latlgn.split(",");
            mCarLatLgn = new LatLng(Double.parseDouble(content[0]), Double.parseDouble(content[1]));
            updateCarLocation();
        }
    }

    private void updateCarLocation() {
        if(mGoogleMap != null) mGoogleMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mCarLatLgn);
        markerOptions.title("Localização do Carro");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        carLocationMarker = mGoogleMap.addMarker(markerOptions);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mCarLatLgn).zoom(mGoogleMap.getMaxZoomLevel()).build();
        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    private void askPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS};
            ActivityCompat.requestPermissions(this, permissions, 3030);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        askPermissions();

        try{
            mGoogleMap.setMyLocationEnabled(true);
        } catch (SecurityException se){
            se.printStackTrace();
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        getSmsExtras();
    }

    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(this,"buildGoogleApiClient",Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        Log.v("locationUpdates", "Gettins Last Location");
        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mCurrlatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mCurrlatLng).zoom(14).build();
            mGoogleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

        } catch (SecurityException se){
            mLastLocation = null;
        }
        try{
            Log.v("locationUpdates", "Gettins Current Location");
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000); //5 seconds
            mLocationRequest.setFastestInterval(3000); //3 seconds
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException se){
            mCurrentLocation = null;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrlatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.v(DEBUG_TAG, mCurrlatLng.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.d("Main", "Successfully got location permission. Starting updates.");
        } catch (SecurityException se) {
             mCurrentLocation = null;
        }
    }
}
