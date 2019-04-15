package com.guideme.myapplication;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class CrimeArea
{
    private CrimeAreaSummary crimeAreaSummary;
    private ArrayList<LatLng> geometry;
    public ArrayList<LatLng> geometryHoles;

//    private Polygon polygon; //uneeded? polygons can have data associated with it.

    //class could contain a reference to a custom InfoWindow that holds the content, MarkerOptions/Marker instead
    public CrimeArea()
    {
        this.crimeAreaSummary = new CrimeAreaSummary();
        this.geometryHoles = this.geometry = null;
    }

    public CrimeArea(Chicago.Area areaType)
    {
        this.crimeAreaSummary = new CrimeAreaSummary();
        this.crimeAreaSummary.isFullSummary = false;
        this.crimeAreaSummary.areaType = areaType;
        this.geometry = null;
    }

    public CrimeArea(CrimeAreaSummary summary)
    {
        this.crimeAreaSummary = summary;
        this.geometry = null;
    }

    public CrimeArea(Chicago.Area areaType, CommAreaRow row)
    {
        this.geometry = row.geometryList;
        this.geometryHoles = row.geoHoles;
        this.crimeAreaSummary = new CrimeAreaSummary();
        this.crimeAreaSummary.isFullSummary = false;
        this.crimeAreaSummary.areaType = areaType;
        this.crimeAreaSummary.areaID = row.ID;
        this.crimeAreaSummary.name = row.Name;
    }

    public CrimeArea(CommAreaRow row, CrimeAreaSummary summary)
    {
        this.geometry = row.geometryList;
        this.geometryHoles = row.geoHoles;
        this.crimeAreaSummary = summary;
    }

    public CrimeArea(ArrayList<LatLng> geometry)
    {
        this.geometry = geometry;
    }

    public int getID() {return  this.crimeAreaSummary.areaID; }
    public String getName() { return this.crimeAreaSummary.name; }
    public ArrayList<LatLng> getGeometry() { return this.geometry; }
    public CrimeAreaSummary getCrimeAreaSummary() { return this.crimeAreaSummary; }

    public void setCrimeAreaSummary(CrimeAreaSummary crimeAreaSummary) { this.crimeAreaSummary = crimeAreaSummary; }

    public void clearGeometry()
    {
        if (!this.geometry.isEmpty())
        {
            this.geometry.clear();
        }
        if (!this.geometryHoles.isEmpty())
        {
            this.geometryHoles.clear();
        }
    }

//    public void setPolygon(Polygon polygon) {
//        this.polygon = polygon;
//    }
//
//    public Polygon getPolygon() {
//        return this.polygon;
//    }

}
