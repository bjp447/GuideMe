package com.guideme.myapplication;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

//have this use inheritance, a base class exists. also have DistAreaRow, BeatAreaRow
public class CommAreaRow extends AreaRow
{
    public
    String geometry;
    ArrayList<LatLng> geometryList;
    ArrayList<LatLng> geoHoles;

    public CommAreaRow(String name, int ID)
    {
        super(name, ID);
    }

    public CommAreaRow(String name, int ID, String geometry)
    {
        super(name, ID);
        this.geometry = geometry;
    }

    public CommAreaRow(String name, int ID, ArrayList<LatLng> geometry, ArrayList<LatLng> geoHoles)
    {
        super(name, ID);
        this.geometryList = geometry;
        this.geoHoles = geoHoles;
    }
}
