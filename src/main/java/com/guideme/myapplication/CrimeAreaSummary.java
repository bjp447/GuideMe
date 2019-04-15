package com.guideme.myapplication;

import android.util.Pair;

//This class contains details to be printed to the user.
public class CrimeAreaSummary
{
    public
    boolean isFullSummary = false;
    Chicago.Area areaType;
    int areaID = 0;
    int color = 0;

    String name; //name of an area
    float percentCrime = 0.0f; //percent of total crime that happens in an area. ie. "10% of all crime occurred here."             \
    int        totalCrimes = 0, //total amount of crimes that occurred in an area
            violentCrimeAmount = 0,
            propertCrimeAmount = 0,
            qualityOfLifeAmount = 0,
            otherAmount = 0,
            springAmount = 0,
            summerAmount = 0,
            fallAmount = 0,
            winterAmount = 0;
    Pair<String, String> date;

    public static int[] decodeColor(int color)
    {
        int A = (color >> 24) & 0xff; // or color >>> 24
        int R = (color >> 16) & 0xff;
        int G = (color >>  8) & 0xff;
        int B = (color      ) & 0xff;

        int[] values = {A, R, G, B};
        return values;
    }
}
