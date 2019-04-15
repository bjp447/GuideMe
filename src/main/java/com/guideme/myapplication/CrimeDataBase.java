package com.guideme.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.util.Pair;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.guideme.myapplication.CrimeDisplay.MILE;

public final class CrimeDataBase
{
    private static final String DB_NAME = "crime_arrest_data.db";
    //PATH to upload into: data/data/user/0/com.guideme.myapplication2/databases
    private final CrimeDataBaseHelper helper;
    public final SQLiteDatabase db;

    //data summary
    public CrimeAreaSummary globalSummary = new CrimeAreaSummary();

    CrimeDataBase(Context context)
    {
        this.helper = new CrimeDataBaseHelper(context, DB_NAME);
        //this.db = SQLiteDatabase.openDatabase(context.getDatabasePath(DB_NAME).getPath(), null, SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.OPEN_READWRITE);
        this.db = helper.getWritableDatabase();

        buildGlobalSummary(true);
    }

    public final void buildGlobalSummaryFast()
    {
        String[] strings;
        String query;
        Cursor cursor;

        //count all crime
        query = "SELECT COUNT(*) FROM CRIMES WHERE " + queryDate();
        cursor = db.rawQuery(query, null);
        if (cursor.moveToNext() && !cursor.isAfterLast()) {
            int cols = cursor.getColumnCount();
            strings = new String[cols];

            for (int i=0; i < cols; ++i) {
                strings[i] = cursor.getString(i);
            }
            this.globalSummary.totalCrimes = Integer.valueOf(strings[0]);
        }
        cursor.close();
    }

    public final void buildGlobalSummary(boolean genPersistent)
    {
        if (genPersistent)
        {
            if (!setGlobalSummary())
            {
                this.helper.buildPersistentData_Global(this.db);
                setGlobalSummary();
            }
            return;
        }

        //build data
        this.globalSummary.areaType = Chicago.Area.GEN;

        if (this.globalSummary.date == null)
        {
            this.globalSummary.date = new android.util.Pair<>("01/01/2018 12:00:00 AM", "09/26/2018 11:59:00 PM");
        }

        Chicago.Area areaType = Chicago.Area.COMMUNITY;
        for (int i=1; i <= areaType.getAreasAmount(); ++i)
        {
            ArrayList<Crime> crimes = getCrimeByArea(Chicago.Area.COMMUNITY, i);
            CrimeAreaSummary summary = buildGeneralAreaSummary(Chicago.Area.GEN, crimes,true);
            this.globalSummary.totalCrimes += summary.totalCrimes;

            this.globalSummary.violentCrimeAmount += summary.violentCrimeAmount;
            this.globalSummary.propertCrimeAmount += summary.propertCrimeAmount;
            this.globalSummary.qualityOfLifeAmount += summary.qualityOfLifeAmount;
            this.globalSummary.otherAmount += summary.otherAmount;

            this.globalSummary.springAmount += summary.springAmount;
            this.globalSummary.summerAmount += summary.summerAmount;
            this.globalSummary.fallAmount += summary.fallAmount;
            this.globalSummary.winterAmount += summary.winterAmount;
        }
        this.globalSummary.percentCrime = 100.0f;
    }

    private final boolean setGlobalSummary()
    {
        String[] strings;
        String query;
        Cursor cursor;

        //if exists take from database,
        // if not take from other tables and build the new table
        try
        {
            String[] sumData = getPersistentData(Chicago.Area.COMMUNITY, "SummaryStatsCommunityAll", 1);

            if (sumData.length >= 14)
            {
                CrimeAreaSummary summary = this.globalSummary;
                summary.isFullSummary = true;
                summary.areaType = Chicago.Area.GEN;
                summary.areaID = Integer.valueOf(sumData[0]);
                summary.name = sumData[1];
                summary.totalCrimes = Integer.valueOf(sumData[2]);
                summary.date = android.util.Pair.create(sumData[8], sumData[9]);
                summary.color = Integer.valueOf(sumData[3]);
                summary.percentCrime = 100f;
                summary.violentCrimeAmount = Integer.valueOf(sumData[4]);
                summary.propertCrimeAmount = Integer.valueOf(sumData[5]);
                summary.qualityOfLifeAmount = Integer.valueOf(sumData[6]);
                summary.otherAmount = Integer.valueOf(sumData[7]);
                summary.springAmount = Integer.valueOf(sumData[10]);
                summary.summerAmount = Integer.valueOf(sumData[11]);
                summary.fallAmount = Integer.valueOf(sumData[12]);
                summary.winterAmount = Integer.valueOf(sumData[13]);
                this.globalSummary = summary;
                return true;
            }
            return false;
        }
        catch (RuntimeException e) {}

        return false;
    }

    public final void delete() {}

    public final ArrayList<Crime> getCrimePath(List<LatLng> points)
    {
        ArrayList<Crime> crimes = new ArrayList<Crime>();
        HashMap<Integer, Crime> crimseHash = new HashMap<>();

        String query;
        String qDate = queryDate();

        final double distanceFromCenterToCorner = 241.4016 * Math.sqrt(2.0); //241.4016 = 0.15 miles
        //final LatLng southwestCorner = SphericalUtil.computeOffset(points.get(0), distanceFromCenterToCorner, 225.0);
        final LatLng northeastCorner = SphericalUtil.computeOffset(points.get(0), distanceFromCenterToCorner, 45.0);
        final double rLatLong = northeastCorner.latitude - points.get(0).latitude; //radius of circle in LatLng terms

        for (int i=0; i < points.size()-1; ++i)
        {
            query = "SELECT * FROM CRIMES WHERE (Latitude "
                    + "BETWEEN " + (points.get(i).latitude - rLatLong)
                    + " AND " + (points.get(i+1).latitude + rLatLong)
                    + ") AND (Longitude BETWEEN "
                    + (points.get(i).longitude - rLatLong)
                    + " AND " + (points.get(i+1).longitude + rLatLong) + ")";

            crimseHash.putAll(getCrimeHash(query));
        }
        crimes = new ArrayList<Crime>(crimseHash.values());
        return crimes;
    }

    public final ArrayList<Crime> getIncidentsRadius(LatLng center, double radius, int start, int amount)
    {
        String query = queryRadius(center, radius) + " AND " + queryDate() + " LIMIT " + start + ", " + amount;
        return getCrime(query);
    }

    public final ArrayList<Crime> getIncidentsArea(Chicago.Area areaType, int id, int start, int amount)
    {
        String query;
        if (id < 1)
        {
            query = "SELECT * FROM CRIMES WHERE " + queryDate() + " LIMIT " + start + ", " + amount;
        }
        else
        {
            query = buildAreaQuery("CRIMES", areaType, id) + " LIMIT " + start + ", " + amount;
        }
        return getCrime(query);
    }

    private final HashMap<Integer, Crime> getCrimeHash(String query)
    {
        HashMap<Integer, Crime> crimes = new HashMap<>();

        if (query.isEmpty())
        {
            return crimes;
        }

        String[] strings;
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext())
        {
            int cols = cursor.getColumnCount();
            strings = new String[cols];

            if (!cursor.isAfterLast())
            {
                for (int i = 0; i < cols; ++i)
                {
                    strings[i] = cursor.getString(i);
                }
                Crime crime = new Crime(strings);
                crimes.put(crime.id, crime);
            }
        }
        cursor.close();
        return crimes;
    }

    private final ArrayList<Crime> getCrime(String query)
    {
        ArrayList<Crime> crimes = new ArrayList<Crime>();

        if (query.isEmpty())
        {
            return crimes;
        }

        String[] strings;
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext())
        {
            int cols = cursor.getColumnCount();
            strings = new String[cols];

            if (!cursor.isAfterLast())
            {
                for (int i = 0; i < cols; ++i)
                {
                    strings[i] = cursor.getString(i);
                }
                crimes.add(new Crime(strings));
            }
        }
        cursor.close();
        return crimes;
    }

    private final String parseDate(String d1)
    {
        return d1.substring(6, 10) + d1.substring(0, 2) + d1.substring(3,5) ;
    }

    private final String parseTime(String dd)
    {
        String p2 = dd.substring(11, 13);
        dd = ((dd.substring(20, 22).equals("PM"))
                ? p2.equals("12") ? "12" : String.valueOf(Integer.valueOf(p2) + 12)
                : p2.equals("12") ? "00" : p2)
                + dd.substring(14, 16);
        return dd;
    }

    private final String queryRadius(LatLng center, double radius)
    {
        final double distanceFromCenterToCorner = radius * Math.sqrt(2.0);
        final LatLng southwestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        final LatLng northeastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        final double rLatLang = northeastCorner.latitude - center.latitude; //radius of circle in LatLng terms
        return "SELECT *, " +
                "((Latitude - " + center.latitude + ")*(Latitude - " + center.latitude + ")) + " +
                "((Longitude - " + center.longitude + ")*(Longitude - " + center.longitude + ")) " +
                "AS dist2 FROM CRIMES WHERE dist2 < " + Math.pow(rLatLang, 2);
    }

    private final String queryDate()
    {
        String d1 = parseDate(this.globalSummary.date.first);
        String d2 = parseDate(this.globalSummary.date.second);

        String dd = parseTime(this.globalSummary.date.first);
//        dd = (!dd.equals("0000") && dd.substring(2, 4).equals("00")) ? String.valueOf(Integer.valueOf(dd)-41)
//                : (!dd.equals("0000")) ? String.valueOf(Integer.valueOf(dd)-1) : dd;
        dd = (!dd.equals("0000")) ? String.valueOf(Integer.valueOf(dd)-1) : dd;
        while (dd.length() != 4)
        {
            dd = "0" + dd;
        }
        String dd2 = parseTime(this.globalSummary.date.second);
        dd2 = String.valueOf(Integer.valueOf(dd2) + 1);
        while (dd2.length() != 4)
        {
            dd2 = "0" + dd2;
        }

        String q = "substr(Crimes.Date , 7, 4) || substr(Crimes.Date, 1, 2) || substr(Crimes.Date, 4, 2) BETWEEN '" + d1 + "' AND '" + d2 + "'"
                + "AND (CASE substr(CRIMES.Date, 21, 2) WHEN 'AM' THEN CASE substr(CRIMES.Date, 12, 2) WHEN '12' THEN '00' ELSE substr(CRIMES.Date, 12, 2) END ELSE CASE substr(CRIMES.Date, 12, 2) WHEN '12' THEN '12' ELSE substr(CRIMES.Date, 12, 2) + '12' END END) || substr(CRIMES.Date, 15, 4)";

        if (Float.valueOf(dd2) < Float.valueOf(dd))
        {
            System.out.println("TIME RANGE INVERTED");
            return q + " NOT BETWEEN '" + dd2 + "' AND '" + dd + "'";
        }
        else
        {
            return q + " BETWEEN '" + dd + "' AND '" + dd2 + "'";
        }
    }

    private final String formatAreaID(Chicago.Area areaType, int areaID)
    {
        if (areaType.equals(Chicago.Area.BEAT)) {
            if (areaID > 1000) {
                return String.valueOf(areaID);
            }
            else if (areaID > 100) {
                return String.format("'0%d'", areaID);
            }
            else if (areaID > 10) {
                return String.format("'00%d'", areaID);
            }
            else {
                return String.format("'000%d'", areaID);
            }
        }
        else if (areaType.equals(Chicago.Area.DISTRICT)) {
            if (areaID > 10) {
                return String.format("'0%d'", areaID);
            }
            else {
                return String.format("'00%d'", areaID);
            }
        }
        else {
            return String.valueOf(areaID);
        }
    }

    private final String buildAreaQuery(String table, Chicago.Area areaType, int id)
    {
        String query = "";

        if (areaType.equals(Chicago.Area.DISTRICT) && areaType.isValidArea(id)) {
            query = "SELECT * FROM " + table + " WHERE District = " +  formatAreaID(areaType, id);
        }

        else if (areaType.equals(Chicago.Area.COMMUNITY) && areaType.isValidArea(id))
        {
            query = "SELECT * FROM " + table + " WHERE CommunityArea = " +  formatAreaID(areaType, id);

            if (!table.equals("CommAreas"))
            {
                query += " AND " + queryDate();
            }
        }

        else if (areaType.equals(Chicago.Area.BEAT) && areaType.isValidArea(id)) {
            query = "SELECT * FROM " + table + " WHERE Beat = " + formatAreaID(areaType, id);
        }
        return query;
    }

    //* Get crimes by area
    public final ArrayList<Crime> getCrimeByArea(Chicago.Area areaType, int id)
    {
        String query = buildAreaQuery("CRIMES", areaType, id);
        return getCrime(query);
    }

    //* Get all crimes.
    public final ArrayList<Crime> getAllCrimes()
    {
        String query = "SELECT * FROM CRIMES";
        return getCrime(query);
    }

    private String getCommunityName(int ID)
    {
        String query = "SELECT NAME FROM CommAreas WHERE CommunityArea = " +  formatAreaID(Chicago.Area.COMMUNITY, ID);

        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext())
        {
            if (!cursor.isAfterLast())
            {
                return cursor.getString(0);
            }
        }
        cursor.close();
        return "";
    }

    // *Returns null if string is empty.
    private final ArrayList<LatLng> parseGeometry(String geom, ArrayList<String> extraGeom, ArrayList<LatLng> hole)
    {
        ArrayList<LatLng> latLngs = new ArrayList<LatLng>();

        if (geom.isEmpty())
        {
            return null;
        }

        String[] positions = geom.split(",");

        if (positions.length > 0)
        {
            String str = positions[0];
            int start = str.lastIndexOf("(");
            str = str.substring(start, str.length());
            str = str.replace("(", " ");
            positions[0] = str;
        }

        if (extraGeom.size() > 3)
        {
            String str1 = extraGeom.get(extraGeom.size() -1);
            str1 = str1.substring(0, str1.indexOf(")"));
            extraGeom.set(extraGeom.size() -1, str1);
        }
        else
        {
            String str1 = positions[positions.length - 1];
            positions[positions.length - 1] = str1.substring(0, str1.indexOf(")"));
        }

        for (String str : positions)
        {
            String[] strings = str.trim().split(" ");
//            System.out.println(strings[0]);
            double lat = Double.parseDouble(strings[1]);
            double lng = Double.parseDouble(strings[0]);
            latLngs.add(new LatLng(lat, lng));
        }

        boolean doHole = false;
        if (extraGeom.size() > 3)
        {
            for (int i=3; i < extraGeom.size(); ++i)
            {
                String[] strings = extraGeom.get(i).split(" ");

                if (strings[1].contains(")"))
                {
                    strings[1] = strings[1].replace(")", "");
                }
                if (strings[0].contains("("))
                {
                    doHole = true;
                    strings[0] = strings[0].replace("(", "");
                }
                //System.out.println(strings[1] + "," + strings[0]);

                double lat = Double.parseDouble(strings[1]);
                double lng = Double.parseDouble(strings[0]);

                if (doHole)
                {
                    hole.add(new LatLng(lat, lng));
//                    doHole = false;
                }
                else
                {
                    latLngs.add(new LatLng(lat, lng));
                }
            }
        }

        return latLngs;
    }

    public final CrimeArea getCrimeArea(ArrayList<Crime> crimes)
    {
        return new CrimeArea(buildGeneralAreaSummary(Chicago.Area.GEN, crimes, false));
    }

    public final CrimeArea getCrimeArea(Chicago.Area areaType, int ID, boolean genSummary)
    {
        return new CrimeArea(buildCrimeAreaSummary(areaType, new AreaRow(getCommunityName(ID), ID), false));
    }

    //Called when generating summaries after polygon creation.
    public final CrimeAreaSummary getFreshCrimeSummary(CrimeArea crimeArea)
    {
        CrimeAreaSummary summary = crimeArea.getCrimeAreaSummary();
        return buildCrimeAreaSummary(summary.areaType, new AreaRow(summary.name, summary.areaID), false);
    }

    private final String[] getPersistentData(Chicago.Area areaType, String table, int ID)
    {
        if (!areaType.equals(Chicago.Area.COMMUNITY))
        {
            System.out.println("ONLY COMMUNITIES ARE IMPLEMENTED");
        }
        String[] strings = new String[0];
        String query;
        Cursor cursor;

        try {
            query = "SELECT 1 FROM " + table;
            cursor = db.rawQuery(query, null);
        }
        catch (RuntimeException e)
        {
            query = "CREATE TABLE " + table + " ( `ID` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `Name` TEXT, `Total` INTEGER, `DataBegin` TEXT, `DateEnd` TEXT, `Color` INTEGER )";
            this.db.execSQL(query);
            return new String[0];
        }

        try
        {
            query = "SELECT * FROM " + table + " WHERE ID = " + ID;

            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext() && !cursor.isAfterLast())
            {
                int cols = cursor.getColumnCount();
                strings = new String[cols];

                for (int i=0; i < cols; ++i)
                {
                    strings[i] = cursor.getString(i);
                }
            }
            cursor.close();
            return strings;
        }
        catch (RuntimeException e) {}

        return strings;
    }

    //no official area association. ie radius areas, or as base for official areas
    private final CrimeAreaSummary buildGeneralAreaSummary(Chicago.Area areaType, ArrayList<Crime> crimes, boolean globalData)
    {
        String[] parts = this.globalSummary.date.first.split(" ");
        String p1 = parts[0];
        parts = this.globalSummary.date.second.split(" ");
        String p2 = parts[0];

        CrimeAreaSummary summary = new CrimeAreaSummary();
        summary.isFullSummary = true;

        summary.areaType = areaType;
        summary.name = "";
        summary.areaID = 0;
        summary.date = android.util.Pair.create(p1, p2);
        summary.totalCrimes = crimes.size();

        if (!globalData)
        {
            summary.percentCrime = ((float)crimes.size() / this.globalSummary.totalCrimes) * 100f;
        }

        //categorize crime type and season
        for (Crime crime : crimes)
        {
            switch (crime.primaryType) //categorize crime type
            {
                case ("ARSON"): case ("CONCEALED CARRY LICENSE VIOLATION"):
                case ("DECEPTIVE PRACTICE"): case ("GAMBLING"): case ("NON-CRIMINAL"):
                case ("INTERFERENCE WITH PUBLIC OFFICER"): case ("SEX OFFENSE"):
                case ("OTHER OFFENSE"): case ("PROSTITUTION"):
                case ("WEAPONS VIOLATION"):
                    ++summary.otherAmount; break;
                case ("ASSAULT"): case ("BATTERY"): case ("HOMICIDE"):
                case ("CRIM SEXUAL ASSAULT"): case ("HUMAN TRAFFICKING"):
                case ("OFFENSE INVOLVING CHILDREN"): case ("KIDNAPPING"):
                case ("STALKING"):
                    ++summary.violentCrimeAmount; break;
                case ("BURGLARY"): case ("CRIMINAL DAMAGE"): case ("ROBBERY"):
                case ("MOTOR VEHICLE THEFT"): case ("THEFT"):
                ++summary.propertCrimeAmount; break;
                case ("CRIMINAL ABORTION"): case ("CRIMINAL TRESPASS"):
                case ("INTIMIDATION"): case ("LIQUOR LAW VIOLATION"):
                case ("NARCOTICS"): case ("OBSCENITY"): case ("PUBLIC INDECENCY"):
                case ("PUBLIC PEACE VIOLATION"): case ("RITUALISM"):
                ++summary.qualityOfLifeAmount; break;
            }

            //spring, begins Vernal Equinox, March 20, 2018, 12:15 p.m.
            //summer, Summer Solstice, June 21, 2018, 6:07 a.m
            //fall, Autumnal Equinox, September 22, 2018, 9:54 p.m.
            //winter, Winter Solstice, December 21, 2018, 5:23 p.m.
            int date = Integer.valueOf(crime.date.substring(0, 2) + crime.date.substring(3,5));
            if (date < 320) {
                ++summary.winterAmount;
            }
            else if (date >= 320 && date < 621) {
                ++summary.springAmount;
            }
            else if (date >= 621 && date < 922) {
                ++summary.summerAmount;
            }
            else if (date >= 922 && date < 1221) {
                ++summary.fallAmount;
            }
            else if (date >= 1221) {
                ++summary.winterAmount;
            }
        }
        summary.color = Color.TRANSPARENT;

        return summary;
    }

    private final CrimeAreaSummary _buildHelper(CrimeAreaSummary summary, AreaRow row)
    {
        int averageCrimesAmount = (int)(this.globalSummary.totalCrimes / (float)Chicago.Area.COMMUNITY.getAreasAmount());

        summary.areaID = row.ID;
        summary.name = row.Name;
        //color code by average crime across all community areas
        final double p = (double)summary.totalCrimes / (double)averageCrimesAmount;

        if (p < 0.25)
        {
            summary.color = Color.BLUE;
        }
        else if (p < 0.45)
        {
            summary.color = Color.GREEN;
        }
        else if (p < 1.05)
        {
            summary.color = Color.YELLOW;
        }
        else if (p < 1.63)
        {
            summary.color = Color.rgb(255,165,0); //orange
        }
        else if (p < 2.69)
        {
            summary.color = Color.RED;
        }
        else
        {
            summary.color = Color.MAGENTA;
        }
        //percent crimes in each season
        //compare percentage/crime amount. e.g. "Average amount of crime: 1234 .This area has a higher than average crime"

        return summary;
    }

    private final CrimeAreaSummary buildCrimeAreaSummary(Chicago.Area areaType, AreaRow row, boolean genPersistant)
    {
        if (genPersistant)
        {
            String[] sumData = getPersistentData(Chicago.Area.COMMUNITY, "SummaryStatsCommunity", row.ID);
            if (sumData.length >= 14)
            {
                CrimeAreaSummary summary = new CrimeAreaSummary();
                summary.isFullSummary = true;
                summary.areaType = areaType;

                summary.areaID = Integer.valueOf(sumData[0]);
                summary.name = sumData[1];
                summary.totalCrimes = Integer.valueOf(sumData[2]);
                summary.date = android.util.Pair.create(sumData[8], sumData[9]);
                summary.color = Integer.valueOf(sumData[3]);
                summary.percentCrime = ((summary.totalCrimes / this.globalSummary.totalCrimes) * 100f);
                summary.violentCrimeAmount = Integer.valueOf(sumData[4]);
                summary.propertCrimeAmount = Integer.valueOf(sumData[5]);
                summary.qualityOfLifeAmount = Integer.valueOf(sumData[6]);
                summary.otherAmount = Integer.valueOf(sumData[7]);
                summary.springAmount = Integer.valueOf(sumData[10]);
                summary.summerAmount = Integer.valueOf(sumData[11]);
                summary.fallAmount = Integer.valueOf(sumData[12]);
                summary.winterAmount = Integer.valueOf(sumData[13]);
                return summary;
            }

            //create summary data
            ArrayList<Crime> crimes = getCrimeByArea(areaType, row.ID);
            CrimeAreaSummary summary = buildGeneralAreaSummary(areaType, crimes, false);
            summary = _buildHelper(summary, row);

            //insert summary
            ContentValues contentValues = new ContentValues();
            contentValues.put("ID", summary.areaID);
            contentValues.put("Name", summary.name);
            contentValues.put("Total", summary.totalCrimes);
            contentValues.put("Color", summary.color);
            contentValues.put("Violent", summary.violentCrimeAmount);
            contentValues.put("Property", summary.propertCrimeAmount);
            contentValues.put("Quality", summary.qualityOfLifeAmount);
            contentValues.put("Other", summary.otherAmount);
            contentValues.put("DateBegin", this.globalSummary.date.first);
            contentValues.put("DateEnd", this.globalSummary.date.second);
            contentValues.put("Spring", summary.springAmount);
            contentValues.put("Summer", summary.summerAmount);
            contentValues.put("Fall", summary.fallAmount);
            contentValues.put("Winter", summary.winterAmount);
            this.db.insert("SummaryStatsCommunity", null, contentValues);

            return buildCrimeAreaSummary(areaType, row, true);
        }

        //create summary data
        ArrayList<Crime> crimes = getCrimeByArea(areaType, row.ID);
        CrimeAreaSummary summary = buildGeneralAreaSummary(areaType, crimes, false);
        return _buildHelper(summary, row);
    }

    //* Get the polygon representing a single area.
    //* Returns null if ID is not valid.
    //* Returns empty list if area has no info.
    public final ArrayList<CrimeArea> getAreaPolygon(Chicago.Area areaType, int id, boolean genInfoWindow)
    {
        ArrayList<CrimeArea> areas = new ArrayList<CrimeArea>();
        ArrayList<String> strings = new ArrayList<>();

        String query = buildAreaQuery("CommAreas", areaType, id);
        if (query.isEmpty()) {
            return null;
        }

        //run for CrimeArea info, builds polygons and summary for each.
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext())
        {
            int cols = cursor.getColumnCount();

            if (!cursor.isAfterLast())
            {
                for (int i=0; i < cols; ++i)
                {
                    String str = cursor.getString(i);

                    if (str.equals(""))
                    {
                        break;
                    }
                    strings.add(str);
                }

                CrimeArea area;
                CommAreaRow row;

                if (areaType.equals(Chicago.Area.COMMUNITY))
                {
                    ArrayList<LatLng> hole = new ArrayList<>();
                    ArrayList<LatLng> geo = parseGeometry(strings.get(2), strings, hole);
                    row = new CommAreaRow(strings.get(1), Integer.valueOf(strings.get(0)), geo, hole);
                }
                else {
                    row = new CommAreaRow(strings.get(1), Integer.valueOf(strings.get(0)));
                }

                if (!genInfoWindow) {
                    area = new CrimeArea(areaType, row);
                }
                else {
                    area = new CrimeArea(row, buildCrimeAreaSummary(areaType, row, true));
                }
                areas.add(area);
            }
        }
        cursor.close();
        return areas;
    }

    //* radius in terms of meters
    public final Pair<ArrayList<Crime>, CrimeArea> getCrimesInRadius(LatLng center, double radius, boolean genSummary)
    {
        String query = queryRadius(center, radius) + " AND " + queryDate();

        ArrayList<Crime> crimes = getCrime(query);
        CrimeArea area;

        if (genSummary)
        {
            CrimeAreaSummary summary = buildGeneralAreaSummary(Chicago.Area.GEN, crimes, false);
            area = new CrimeArea(summary);
        }
        else
        {
            area = new CrimeArea(Chicago.Area.GEN);
        }
        return Pair.create(crimes, area);
    }

    @Override protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }

    public final void close()
    {
        this.db.close();
        this.helper.close();
    }
}
