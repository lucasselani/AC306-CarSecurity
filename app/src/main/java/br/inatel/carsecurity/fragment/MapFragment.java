package br.inatel.carsecurity.fragment;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import br.inatel.carsecurity.R;
import br.inatel.carsecurity.activity.MainActivity;
import br.inatel.carsecurity.provider.NumberManagement;


/**
 * Created by Lucas on 03/10/2016.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = MapFragment.class.getSimpleName();
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Marker carLocationMarker = null;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public MapFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.map_fragment, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showNumberDialog();
            }
        });

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        ((MainActivity) getActivity()).askPermissions();
        try{
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException se){
            se.printStackTrace();
        }
        Log.v(TAG,"MapReady");
        ((MainActivity) getActivity()).mapReady();
    }

    public void updateMap(LatLng latlgn){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latlgn).zoom(14).build();
        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    public void updateCarLocation(LatLng latlgn) {
        if(mGoogleMap != null) mGoogleMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlgn);
        markerOptions.title("Localização do Carro");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        carLocationMarker = mGoogleMap.addMarker(markerOptions);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latlgn).zoom(mGoogleMap.getMaxZoomLevel()).build();
        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }
}
