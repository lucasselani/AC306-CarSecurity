package br.inatel.carsecurity.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.PersistableBundle;
import android.renderscript.Double2;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import br.inatel.carsecurity.R;
import br.inatel.carsecurity.fragment.CarNumberDialog;
import br.inatel.carsecurity.fragment.SmsReceiveDialog;
import br.inatel.carsecurity.provider.NumberManagement;
import br.inatel.carsecurity.fragment.MapFragment;


public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String DEBUG_TAG = getClass().getSimpleName();
    LocationRequest mLocationRequest = null;
    GoogleApiClient mGoogleApiClient = null;
    Location mCurrentLocation = null;
    Location mLastLocation = null;

    LatLng mCarLatLgn = null;
    LatLng mCurrlatLng = null;
    MapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if(askPermissions()){
            mMapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, mMapFragment)
                    .commit();
        }
    }

    private void TEST_POPULATE() {
        NumberManagement mNumberManagement = new NumberManagement(this);
        mNumberManagement.setCarNumber("+5535998346484");
    }

    private LatLng getLatLgnSms(String[] content){
        for(String s : content) Log.v("msgContent: ", s);

        String latitude = content[0].replaceFirst("^0+(?!$)", "");
        String degreeLat = latitude.substring(0,2);
        String minutesLat = latitude.substring(2,latitude.length());
        double degreeValue = Double.parseDouble(degreeLat);
        double minutesValue = Double.parseDouble(minutesLat);
        double latitudeValue = degreeValue + (minutesValue / 60.0);
        if(content[1].equals("S")) latitudeValue *= -1;

        String longitude = content[2].replaceFirst("^0+(?!$)", "");
        String degreeLgn = longitude.substring(0,2);
        String minutesLgn = longitude.substring(2,longitude.length());
        degreeValue = Double.parseDouble(degreeLgn);
        minutesValue = Double.parseDouble(minutesLgn);
        double longitudeValue = degreeValue + (minutesValue / 60.0);
        if(content[3].equals("W")) longitudeValue *= -1;

        return new LatLng(latitudeValue, longitudeValue);
    }
    private void getSmsExtras() {
        Intent i = getIntent();
        if(i.hasExtra("latlgn")){
            String latlgn = i.getExtras().getString("latlgn");
            String[] content = latlgn.split(",");
            try{
                if(content.length != 4) return;
            } catch (Exception e) {
                e.printStackTrace();
            }

            mCarLatLgn = getLatLgnSms(content);
            mMapFragment.updateCarLocation(mCarLatLgn);
            showAlarmDecisionDialog();
        }
    }

    public void mapReady(){
        Log.v("Main", "MapReady");
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        getSmsExtras();
    }

    private void showAlarmDecisionDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment smsDialog = new SmsReceiveDialog();
        smsDialog.show(fragmentManager, "Main");
    }

    public void showNumberDialog(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment numberDialog = new CarNumberDialog();
        numberDialog.show(fragmentManager, "Main");
    }

    public boolean askPermissions() {
        Log.v("Main", "AskingPermissions");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.CALL_PHONE};
            ActivityCompat.requestPermissions(this, permissions, 3030);
            Log.v("Main", "InsideIf");
        }
        else return true;
        return false;
    }



    protected synchronized void buildGoogleApiClient() {
        Log.v("Main","buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        Log.v("locationUpdates", "Gettins Last Location");
        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation != null){
                mCurrlatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMapFragment.updateMap(mCurrlatLng);
            }
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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        //super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionsNotGiven = 0;
        for(int i=0; i<grantResults.length; i++){
            if(grantResults[i] == -1) permissionsNotGiven++;
        }

        if(permissionsNotGiven == 0){
            try {
                mMapFragment = MapFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, mMapFragment)
                        .commitAllowingStateLoss();
                Log.d("Main", "Successfully got location permission. Starting updates.");
            } catch (SecurityException se) {
                mCurrentLocation = null;
            }
        }
        else{
            String[] missedPermissions = new String[permissionsNotGiven];
            int cont = 0;
            for(int i=0; i<grantResults.length; i++){
                if(grantResults[i] == -1){
                    missedPermissions[cont] = permissions[i];
                    cont++;
                }
            }
            ActivityCompat.requestPermissions(this, missedPermissions, 1010);
        }

    }
}
