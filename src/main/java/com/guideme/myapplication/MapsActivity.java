package com.guideme.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{
    private CrimeDisplay crimeDisplay;
    private CrimeDataBase db;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the activity_map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final LinearLayout contentContainer = findViewById(R.id.ContentView);
        contentContainer.setVisibility(View.GONE);
        contentContainer.setBackgroundColor(Color.WHITE);

        this.db = new CrimeDataBase(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        this.crimeDisplay = new CrimeDisplay(this.db, this.mMap, this);

        LatLng chicago = new LatLng(41.8781, -87.6298);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chicago, 10));

        //look into google maps places for communities.


        //this.crimeDisplay.drawCrimeMarkers(Chicago.Area.DISTRICT, 5);
        //this.crimeDisplay.drawCrimeMarkers(Chicago.Area.COMMUNITY, 19);
        //this.crimeDisplay.drawCrimeMarkers(Chicago.Area.COMMUNITY, 10);

        //this.crimeDisplay.drawCrimeHeatMap(Chicago.Area.BEAT, 111);
        //this.crimeDisplay.drawCrimeHeatMap();

        //no longer call these
            //this.crimeDisplay.drawCrimeArea(Chicago.Area.COMMUNITY, 19, false);
            //this.crimeDisplay.drawCrimeArea(Chicago.Area.COMMUNITY, true);
        this.crimeDisplay.displayAreasMap(true);
//        this.crimeDisplay.displayAreasMap(true);


        //changes the date of the data being searched for.
        //for Heap Maps, clicking on polygon after filter change will regenerate summary
    }
}
