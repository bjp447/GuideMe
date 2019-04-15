package com.guideme.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.GeolocationApi;
import com.google.maps.GeolocationApiRequest;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.GeocodedWaypoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.GeolocationResult;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NavigationFragment extends HeatMapFragment implements OnMapReadyCallback
{
    //Some required stuff
    private static final int overview = 0;

    public NavigationFragment() {}

    //Contructor for NavFrag
    public static NavigationFragment newInstance(String param1, String param2)
    {
        NavigationFragment fragment = new NavigationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.activity_navigation, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return root;
    }

    //Basically the main function that does the routing
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Gets the map from super class
        super.onMapReady(googleMap);
        //Sets up the UI on the map like the zoom in and zoom out, look at this function below to see what is enabled
        setupGoogleMapScreenSettings(googleMap);
        //Called the getDirectionsDetails function by passing in two strings and the MODE. Mode just how a person want to travel like WALKING
        DirectionsResult results = getDirectionsDetails("1200 W Harrison St, Chicago, IL 60607", "600 E Grand Ave, Chicago, IL 60611",TravelMode.DRIVING);
//        DirectionsResult results = null;

        //If we have results, the if statement will be executed
        if (results != null)
        {
            //Adds the polyLines based on the results on the map. See function for more details
            addPolyline(results, googleMap);
            //Don't think this is relevant to this project since we are focusing on Chicago but this just moves the camera to the where
            //the route will be displayed
            positionCamera(results.routes[overview], googleMap);
            //This just adds the origin and destination markers on the map.
            addMarkersToMap(results, googleMap);
        }
    }
    //This function will fetch the route from Google services
    private DirectionsResult getDirectionsDetails(String origin,String destination,TravelMode mode) {
        DateTime now = new DateTime();
        try {
            //Here we request the route from the Directions API. GeoContext is just a verifier for DirectionsAPI
            //so it doesn't take some random requests. It knows this API is trying to access the API and will return the route
            return DirectionsApi.newRequest(getGeoContext())
                    //Pass in the MODE, origin, destination, and time. Time is set to right now based on the the line of code above
                    .mode(mode)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now)
                    .await();
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //This is the UI settings for the map
    private void setupGoogleMapScreenSettings(GoogleMap mMap) {
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
    }

    //Function that adds markers to the map
    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        //Adds a marker based on the origin based on the route
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview].legs[overview].startLocation.lat,results.routes[overview].legs[overview].startLocation.lng)).title(results.routes[overview].legs[overview].startAddress));
        //Adds a marker based on the destination based on the route
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview].legs[overview].endLocation.lat,results.routes[overview].legs[overview].endLocation.lng)).title(results.routes[overview].legs[overview].startAddress).snippet(getEndLocationTitle(results)));
    }

    //This function positions the Camera based on the origin location
    private void positionCamera(DirectionsRoute route, GoogleMap mMap)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng), 12));
    }

    //This function adds a polyline based on the route received from getDirectionDetails
    private void addPolyline(DirectionsResult results, GoogleMap mMap)
    {
        //Converts the result received on to a List of Lat and Lng values
        final List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());

        this.crimeDisplay.drawCrimeAlongRoute(decodedPath);

        //The following code creates a polyline to include all the LatLng values and changes how it works
        PolylineOptions polyline = new PolylineOptions();
        polyline = polyline.addAll(decodedPath);
        polyline = polyline.width(15);
        polyline = polyline.color(Color.RED);
        polyline = polyline.visible(true);
        polyline = polyline.clickable(true);
        //Adds the polyline to the map
        mMap.addPolyline(polyline);
    }
    //This function adds the Time and distance in the destination marker. This allows users to click/press on the markers to display info
    private String getEndLocationTitle(DirectionsResult results)
    {
        return  "Time :"+ results.routes[overview].legs[overview].duration.humanReadable + " Distance :" + results.routes[overview].legs[overview].distance.humanReadable;
    }

    //Geocontext to verify that this program is allowed to use Google APIs
    private GeoApiContext getGeoContext()
    {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_maps_key))
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .setReadTimeout(5, TimeUnit.SECONDS)
                .setWriteTimeout(5, TimeUnit.SECONDS);
    }
}
