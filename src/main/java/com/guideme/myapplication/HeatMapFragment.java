package com.guideme.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnHeatMapSelectedListener} interface
 * to handle interaction events.
 * Use the {@link HeatMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeatMapFragment extends Fragment implements OnMapReadyCallback
{
    protected OnHeatMapSelectedListener mListener;

    public CrimeDisplay crimeDisplay;
    protected GoogleMap mMap;

    public HeatMapFragment() {}

    public static HeatMapFragment newInstance(String param1, String param2) {
        HeatMapFragment fragment = new HeatMapFragment();
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

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.activity_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return root;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHeatMapSelectedListener) {
            mListener = (OnHeatMapSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void attachCrimeMapDisplayController(GoogleMap mMap, CrimeDisplay crimeDisplay)
    {
        this.mMap = mMap;
        this.crimeDisplay = crimeDisplay;
    }

    @Override public void onMapReady(GoogleMap googleMap)
    {
        this.mMap = googleMap;
        setupGoogleMapScreenSettings(mMap);
        this.crimeDisplay = new CrimeDisplay(mMap, getActivity());

        this.mListener.onCrimeMapReady(this, this.crimeDisplay);

        LatLng chicago = new LatLng(41.8781, -87.6298);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chicago, 10));

        //this.crimeDisplay.drawCrimeMarkers(Chicago.Area.DISTRICT, 5);
        //this.crimeDisplay.drawCrimeMarkers(Chicago.Area.COMMUNITY, 19);
        //this.crimeDisplay.drawCrimeMarkers(Chicago.Area.COMMUNITY, 10);
//
        //this.crimeDisplay.drawCrimeHeatMap(Chicago.Area.BEAT, 111);
        //this.crimeDisplay.drawCrimeHeatMap();
//
        //no longer call these
        //this.crimeDisplay.drawCrimeArea(Chicago.Area.COMMUNITY, 19, false);
        //this.crimeDisplay.drawCrimeArea(Chicago.Area.COMMUNITY, true);
//
        //crimeDisplay.displayAreasMap(true);
    }
    private void setupGoogleMapScreenSettings(GoogleMap mMap)
    {
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
    }

    public interface OnHeatMapSelectedListener
    {
        void onCrimeMapReady(HeatMapFragment frag, CrimeDisplay crimeDisplay);
    }
}
