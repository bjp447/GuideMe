package com.guideme.myapplication;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

//* Draws various information on a Google activity_map using the specified Crime Database.
public class CrimeDisplay implements GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener
{
    public static final double MILE = 1609.34;

    private final Activity context; //activity
    private CrimeDataBase db;
    public final GoogleMap mMap;

    private LinearLayout areaSumContent; //ui base
    private boolean reDisplay = false;

    private HeatmapTileProvider provider; //heat, can only hold one data set, so either one area at a time or all
    private Chicago.Area currAreaType = Chicago.Area.COMMUNITY;
    private TreeMap<Integer, Polygon> areaPolygons; //all areas, (Area ID, Polygon(contains CrimeArea))
    private Pair<CrimeArea, Circle> radiusArea; //only one radius at a time will be displayed

    private boolean viewingSummaries;
    private int currView = 0; //for polygons
    private int currPage = 0; //for incident pages
    private TextView pageNumberTxt;

    private GestureDetector gestureDetector;

    //------------------------
    private CrimeArea routeArea;
    private boolean routeWithCrime = false;

    private class CrimeRouteTask extends AsyncTask<LatLng, Integer, ArrayList<Crime>>
    {
        @Override protected ArrayList<Crime> doInBackground(LatLng[] path)
        {
            ArrayList<Crime> crimes = db.getCrimePath(Arrays.asList(path));
            return crimes;
        }

        @Override protected void onPostExecute(ArrayList<Crime> crimes)
        {
            buildHeatMap(crimes);
//            routeArea = db.getCrimeArea(crimes);
            //routeWithCrime = true;
        }
    }

    public void drawCrimeAlongRoute(List<LatLng> points)
    {
        new CrimeRouteTask().execute(points.toArray(new LatLng[0]));
    }

    //------------------------

    private void setUpGestures()
    {
        final ScrollView scrollView = this.context.findViewById(R.id.scroll);

        this.gestureDetector = new GestureDetector(this.context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
                    {
                        final int SWIPE_MIN_DISTANCE = 120;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try
                        {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                            {
                                return false;
                            }
                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                            {
                                if (viewingSummaries) {
                                    changeSummaryView(1);
                                }
                                else {
                                    changePage(1);
                                }
                            }
                            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE //thumb ->
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                            {
                                if (viewingSummaries) {
                                    changeSummaryView(-1);
                                }
                                else {
                                    changePage(-1);
                                }
                            }
                        } catch (Exception e) {
                            // nothing
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });

        scrollView.setOnTouchListener(new View.OnTouchListener() {
                    @Override public boolean onTouch(View v, MotionEvent event) {
                        return gestureDetector.onTouchEvent(event);
                    }});
    }

    private void initUI()
    {
        setUpGestures();

        //--------------------------------
        final LinearLayout contentContainer = this.context.findViewById(R.id.ContentView);
        contentContainer.setVisibility(View.GONE);
        contentContainer.setBackgroundColor(Color.WHITE);

        this.areaSumContent = context.findViewById(R.id.ContentView);

        //hide summary onClick
        final Button exitBtn = context.findViewById(R.id.ExitBtn);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                areaSumContent.setVisibility(View.GONE);
                currView = 0;
                viewingSummaries = false;
            }});

        //testing
//        final Button tBtn = ((Button)((LinearLayout)areaSumContent.getChildAt(0)).getChildAt(1));
//        tBtn.setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v)
//            {
//                changeDateAndTime("01/01/2018", "09/16/2018", "01:00:00 AM", "04:58:00 AM");
//
//                //LatLng chicago = new LatLng(41.8781, -87.6298);
//                //getCrimeInRadius(chicago, MILE, true);
//
//                //drawCrimeHeatMap();
//                //setAreaFillColor(Color.TRANSPARENT);
//            }});
        //--------------------------------

        final ScrollView incidentsScroll = this.context.findViewById(R.id.IncidentsScroll);
        incidentsScroll.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event)
            {
                return gestureDetector.onTouchEvent(event);
            }});

        final LinearLayout inView = context.findViewById(R.id.IncidentView);
        inView.setVisibility(View.GONE);

        final FloatingActionButton incidentsBtn = this.context.findViewById(R.id.IncidentsListBtn);
        incidentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                inView.setVisibility(View.VISIBLE);
                incidentsBtn.setVisibility(FloatingActionButton.GONE);

                if (areaSumContent.getVisibility() == View.VISIBLE)
                {
                    areaSumContent.setVisibility(View.GONE);
                    reDisplay = true;
                    viewingSummaries = false;
                }

                changePage(1);
            }});

        final Button IBtn = this.context.findViewById(R.id.IncidentExitBtn);
        IBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                inView.setVisibility(View.GONE);
                incidentsBtn.setVisibility(FloatingActionButton.VISIBLE);
                currPage = 0;

                if (reDisplay)
                {
                    viewingSummaries = true;
                    areaSumContent.setVisibility(View.VISIBLE);
                }
            }});

        final LinearLayout btnLayout = this.context.findViewById(R.id.PageBtnLayout);
        final Button frontBtn = (Button)btnLayout.getChildAt(0);
        final Button backBtn = (Button)btnLayout.getChildAt(1);
        this.pageNumberTxt = (TextView)btnLayout.getChildAt(2);
        final Button nextBtn = (Button)btnLayout.getChildAt(3);
        final Button endBtn = (Button)btnLayout.getChildAt(4);

        frontBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                changePage(-2);
            }});
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                changePage(-1);
            }});
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                changePage(1);
            }});
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                changePage(2);
            }});
    }

    CrimeDisplay(GoogleMap mMap, Activity context)
    {
        this.db = new CrimeDataBase(context);
        this.mMap = mMap;
        this.context = context;
        this.areaPolygons = new TreeMap<Integer, Polygon>();
        this.viewingSummaries = false;
        initUI();
    }

    CrimeDisplay(CrimeDataBase db, GoogleMap mMap, Activity context)
    {
        this.db = db;
        this.mMap = mMap;
        this.context = context;
        this.areaPolygons = new TreeMap<Integer, Polygon>();
        initUI();
    }

    public void changeDateAndTime(String dateStart, String dateEnd, String timeStart, String timeEnd)
    {
        this.db.globalSummary.date = android.util.Pair.create(dateStart + " " + timeStart, dateEnd + " "  + timeEnd);
        OnFilterChange();
    }

    private void OnFilterChange()
    {
        //this.db.buildGlobalSummary(false);
        this.db.buildGlobalSummaryFast();

        if (this.viewingSummaries || this.currPage != 0) //list or summary up
        {
            if (this.radiusArea != null)
            {
                if (this.currPage != 0) //list is up, redraw
                {
                    getCrimeInRadius(this.radiusArea.second.getCenter(), this.radiusArea.second.getRadius(), true);
                    changePage(3);

                }
                else if (this.viewingSummaries) //has summary up
                {
                    //upadate area radius
                    getCrimeInRadius(this.radiusArea.second.getCenter(), this.radiusArea.second.getRadius(), true);
                    onCircleClick(this.radiusArea.second);
                }
                else //list and summary closed
                {

                }
            }
            else
                {
                if (this.currPage != 0) //list is up, redraw
                {
                    //force update on crime area, then remake list
                    Polygon p = this.areaPolygons.get(this.currView);

                    for (Map.Entry<Integer, Polygon> entry : this.areaPolygons.entrySet()) {
                        entry.getValue().setTag(null);
                    }

                    CrimeArea crimeArea = setNewCrimeArea(p);
                    p.setTag(crimeArea);
                    setSummaryDisplay(crimeArea.getCrimeAreaSummary());
                    changePage(3);
                }
                else
                    {
                        //destroy takes to indicate that areas must be recomputed on poly click
                        Polygon p = this.areaPolygons.get(this.currView);

                        for (Map.Entry<Integer, Polygon> entry : this.areaPolygons.entrySet()) {
                            entry.getValue().setTag(null);
                        }
                        onPolygonClick(p);
                }
            }
        }
        else
            {
            for (Map.Entry<Integer, Polygon> entry : this.areaPolygons.entrySet()) {
                entry.getValue().setTag(null);
            }

            if (this.radiusArea != null)
            {
                //update area radius
                getCrimeInRadius(this.radiusArea.second.getCenter(), this.radiusArea.second.getRadius(), true);
            }
        }
    }

    private CrimeArea setNewCrimeArea(Polygon polygon)
    {
        CrimeArea crimeArea;
        int key = 0;
        for (Map.Entry<Integer, Polygon> entry : this.areaPolygons.entrySet()) {
            if (Objects.equals(polygon, entry.getValue())) {
                key = entry.getKey();
                break;
            }
        }
        crimeArea = this.db.getCrimeArea(this.currAreaType, key, true);
        CrimeAreaSummary summary = crimeArea.getCrimeAreaSummary();
        //setAreaFillColor(summary.areaID, summary.color);
        this.areaPolygons.get(key).setTag(crimeArea);
        return crimeArea;
    }

    private void changeSummaryView(int dir)
    {
        int nextView = this.currView + dir;

        int maxView = this.currAreaType.getAreasAmount();
        if (nextView > maxView) {
            nextView = 1;
        }
        else if (nextView < 1) {
            nextView = maxView;
        }

        final Polygon polygon = this.areaPolygons.get(nextView);
        if (polygon != null) {
            this.onPolygonClick(polygon);
        }
    }

    private void drawIncidentsList()
    {}

    //0: Chicago
    //1: next
    //-1: prev
    //2: begin
    //-2: end
    //3: reset curr page
    private void changePage(int op)
    {
        ArrayList<Crime> crimes;
        boolean routePaging = false;

        final int amountPerPage = 30;
        int start = 0;
        int maxPage = 0;

        if (this.currView == 0)
        {
            if (this.routeWithCrime)
            {
                maxPage = (int)Math.ceil((double)this.routeArea.getCrimeAreaSummary().totalCrimes/ (double)amountPerPage);

                final TextView name = this.context.findViewById(R.id.IncidentsAreaName);
                name.setText("Crime Along Route");
                routePaging = true;
            }
            else
            {
                maxPage = (int)Math.ceil((double)this.db.globalSummary.totalCrimes / (double)amountPerPage);

                final TextView name = this.context.findViewById(R.id.IncidentsAreaName);
                name.setText("Chicago");
            }
        }
        else
        {
            final CrimeArea area;
            if (this.radiusArea != null)
            {
                area = (CrimeArea)this.radiusArea.second.getTag();
            }
            else
            {
                area = (CrimeArea)this.areaPolygons.get(this.currView).getTag();
            }
            maxPage = (int)Math.ceil(((double)area.getCrimeAreaSummary().totalCrimes) / ((double)amountPerPage));

            final TextView name = this.context.findViewById(R.id.IncidentsAreaName);
            name.setText(area.getCrimeAreaSummary().name);
        }

        if (op == 2)
        {
            if (this.currPage+1 > maxPage) {
                return;
            }
            this.currPage = (int)maxPage;
            start = (this.currPage-1) * amountPerPage;
            pageNumberTxt.setText(String.valueOf(maxPage));
        }
        else if (op == -2)
        {
            if (this.currPage-1 < 1) {
                return;
            }
            start = 0;
            this.currPage = 1;
            pageNumberTxt.setText("1");
        }
        else if (op == 3)
        {
            this.currPage = 1;
            start = (this.currPage-1) * amountPerPage;
            pageNumberTxt.setText(String.valueOf(this.currPage));
        }
        else if (this.currPage+op > maxPage) {
            return;
        }
        else if (this.currPage+op < 1) {
            return;
        }
        else
            {
            this.currPage += op;
            start = (this.currPage-1) * amountPerPage;
            pageNumberTxt.setText(String.valueOf(this.currPage));
        }

//        if (routePaging)
//        {
//            //crimes = this.db.getCrimePath(Arrays.asList(path));
//            //this.db.getIncidentsArea(this.currAreaType, this.currView, start, amountPerPage);
//        }
//        else
//        {
            crimes = this.db.getIncidentsArea(this.currAreaType, this.currView, start, amountPerPage);
//        }

        //-----------------------

        final LinearLayout linearLayout = this.context.findViewById(R.id.IncidentsContent);
        linearLayout.removeAllViews();

        for (Crime crime : crimes)
        {
            final TextView textView = new TextView(this.context);
            final View divider = new View(this.context);

            String[] strings = crime.date.split(" ");
            strings[1] = strings[1].concat(" ").concat(strings[2]);
            textView.setText(crime.primaryType + "\n\n"
                    + crime.block + "             " + strings[0] + "\n\n"
                    + crime.description);

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(params);
            divider.setBackgroundColor(Color.DKGRAY);

            linearLayout.addView(textView);
            linearLayout.addView(divider);
        }
        linearLayout.refreshDrawableState();
        //LatLng chicago = new LatLng(41.8781, -87.6298);
        //this.db.getIncidentsRadius(chicago, MILE, 0, 100);
    }

    @Override public void onMapClick(LatLng latLng)
    {
        //Hide all UI.
        if (this.areaSumContent != null)
        {
            this.areaSumContent.setVisibility(View.GONE);
            this.viewingSummaries = false;
            this.currView = 0;
            reDisplay = false;
        }
    }

    @Override public void onCircleClick(Circle circle)
    {
        final CrimeArea crimeArea = (CrimeArea)circle.getTag();
        setSummaryDisplay(crimeArea.getCrimeAreaSummary());
        areaSumContent.setVisibility(View.VISIBLE);

        this.currView = crimeArea.getID();
        this.viewingSummaries = true;
    }

    @Override public void onPolygonClick(Polygon polygon)
    {
        CrimeArea crimeArea = (CrimeArea)polygon.getTag();
        if (crimeArea == null) {
            crimeArea = setNewCrimeArea(polygon);
        }
        else {
            crimeArea = (CrimeArea)polygon.getTag();
        }
        this.currView = crimeArea.getID();
        this.viewingSummaries = true;

        setSummaryDisplay(crimeArea.getCrimeAreaSummary());
        areaSumContent.setVisibility(View.VISIBLE);
    }

    private void setSummaryDisplay(CrimeAreaSummary summary)
    {
        final TextView title = ((TextView)((LinearLayout)areaSumContent.getChildAt(0)).getChildAt(0));

        final LinearLayout contentLayout = ((LinearLayout)((ScrollView)areaSumContent.getChildAt(1)).getChildAt(0));
        final TextView dateRange = ((TextView)contentLayout.getChildAt(0));
        final TextView content = ((TextView)contentLayout.getChildAt(1));

        title.setText(summary.areaID + ": " + summary.name);
        String string = "[" + summary.date.first + "-" + summary.date.second + "]";
        dateRange.setText(string);

        float averageCrimesAmount = this.db.globalSummary.totalCrimes / (float)Chicago.Area.COMMUNITY.getAreasAmount();
        float avgPercent = (averageCrimesAmount / this.db.globalSummary.totalCrimes) * 100;

        string = this.db.globalSummary.totalCrimes +  " total incidents across Chicago for the specified date range." + "\n\n"
                + summary.totalCrimes + " total incidents, "
                + ((summary.totalCrimes > averageCrimesAmount)
                ? String.valueOf(summary.totalCrimes - averageCrimesAmount) + " above"
                : String.valueOf(averageCrimesAmount - summary.totalCrimes) + " below") + " average"
                + "\n"
                + summary.percentCrime + "% of all incidents occur in this area, "
                + ((avgPercent < summary.percentCrime)
                ? String.valueOf(summary.percentCrime - avgPercent) + "% above"
                : String.valueOf(avgPercent - summary.percentCrime) + "% below") + " average."
                + "\n"
                + "         Incidents by Type \n"
                + "Violent: " + summary.violentCrimeAmount + "\n"
                + "Property: " + summary.propertCrimeAmount + "\n"
                + "Quality of Life: " + summary.qualityOfLifeAmount + "\n"
                + "Other: " + summary.otherAmount + "\n"
                + "\n"
                + "         Incidents by season: \n"
                + "Spring: " + summary.springAmount + "\n"
                + "Summer: " + summary.summerAmount + "\n"
                + "Fall: " + summary.fallAmount + "\n"
                + "Winter: " + summary.winterAmount + "\n";
        content.setText(string);
    }

    // *Draws all markers for the entire data set.
    public void drawCrimeMarkers()
    {
        Chicago.Area areaType = Chicago.Area.COMMUNITY;
        for (int i=1; i <= areaType.getAreasAmount(); ++i)
        {
            drawCrimeMarkers(areaType, i);
        }
    }

    //* Draws markers for the specified area from the data set.
    public void drawCrimeMarkers(Chicago.Area areaType, int areaID)
    {
        ArrayList<Crime> crimes = db.getCrimeByArea(areaType, areaID);

        for (Crime crime : crimes)
        {
            if (crime.hasLocation())
            {
                LatLng point = new LatLng(crime.getLat(), crime.getLong());
                mMap.addMarker(new MarkerOptions().position(point).title("crime"));
            }
        }
    }

    //--------HeatMap-----------

    // *Draws heat activity_map from all data.
    public void drawCrimeHeatMap()
    {
        ArrayList<LatLng> coords = new ArrayList<LatLng>();
        ArrayList<Crime> crimes;

        Chicago.Area areaType = Chicago.Area.COMMUNITY;
        for (int i=0; i < areaType.getAreasAmount(); ++i)
        {
            crimes = db.getCrimeByArea(areaType, i);

            for (Crime crime : crimes)
            {
                if (crime.hasLocation())
                {
                    coords.add(new LatLng(crime.getLat(), crime.getLong()));
                }
            }
        }
        if (provider == null)
        {
            provider = new HeatmapTileProvider.Builder().data(coords).build();
            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
        }
        else
        {
            this.provider.setData(coords);
        }
    }

    private void buildHeatMap(ArrayList<Crime> crimes)
    {
        ArrayList<LatLng> coords = new ArrayList<LatLng>();

        for (Crime crime : crimes)
        {
            if (crime.hasLocation())
            {
                coords.add(new LatLng(crime.getLat(), crime.getLong()));
            }
        }
        if (provider == null)
        {
            provider = new HeatmapTileProvider.Builder().data(coords).build();
            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
        }
        else
        {
            this.provider.setData(coords);
        }
    }

    public void drawCrimeHeatMapWeighted(ArrayList<String> ranks) {}

    // *Draws heat activity_map for specified area.
    public void drawCrimeHeatMap(Chicago.Area areaType, int areaID)
    {
        buildHeatMap(db.getCrimeByArea(areaType, areaID));
    }

    public void displayHeatMap()
    {
        setAreaFillColor(Color.TRANSPARENT);
    }

    public void hideHeatMap()
    {
        resetAreaMapColor();
    }
    //--------HeatMap--------end

    //--------CrimeRadius-------
    //* Generates and displays a radius of crime around a point.
    //* Generate summary for summary ui pop up and clickable circle
    public void getCrimeInRadius(LatLng center, double radius, boolean genSummary)
    {
        final Circle circle = mMap.addCircle(
                new CircleOptions().center(center).clickable(false).radius(radius)
                        .strokeColor(Color.BLACK).fillColor(Color.TRANSPARENT).strokeWidth(3.0f));

        Pair<ArrayList<Crime>, CrimeArea> crimeAreaPair = db.getCrimesInRadius(center, radius, genSummary);
        buildHeatMap(crimeAreaPair.first);
        this.radiusArea = Pair.create(crimeAreaPair.second, circle);
        circle.setClickable(true);
        circle.setTag(crimeAreaPair.second);
        circle.setFillColor(crimeAreaPair.second.getCrimeAreaSummary().color);
        this.mMap.setOnCircleClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
    }

    //* TODO: Test
    //* Heat map points display, on or off, separate from radius circle
    public void toggleRadiusHeatDisplay(boolean toggle)
    {
        if (toggle)
        {
            this.provider.setOpacity(0.0);
        }
        else
        {
            this.provider.setOpacity(1);
        }
    }

    //* display entire radius, outline and fill
    public void displayRadius(boolean toggle)
    {
        toggleRadiusAreaFill(toggle);
        toggleRadiusOutline(toggle);
    }

    //TODO: Test decoder
    //* set the visibility of the radius
    public void setRadiusOpacity(int opacity)
    {
        if (this.radiusArea.first != null)
        {
            int[] values = CrimeAreaSummary.decodeColor(this.radiusArea.second.getFillColor());
            int color = Color.argb(opacity, values[1], values[2], values[3]);
            this.radiusArea.second.setFillColor(color);
        }
    }

    //* Radius outline
    public void toggleRadiusOutline(boolean toggle)
    {
        if (toggle)
        {
            if (this.radiusArea.first != null)
            {
                this.radiusArea.second.setClickable(false);
            }
            this.radiusArea.second.setVisible(false);
        }
        else
        {
            if (this.radiusArea.first != null)
            {
                this.radiusArea.second.setClickable(true);
            }
            this.radiusArea.second.setVisible(true);
        }
    }

    //* Radius fill
    public void toggleRadiusAreaFill(boolean toggle)
    {
        if (toggle && this.radiusArea.first != null)
        {
            this.radiusArea.second.setFillColor(this.radiusArea.first.getCrimeAreaSummary().color);
        }
        else
        {
            this.radiusArea.second.setFillColor(Color.TRANSPARENT);
        }
    }
    //--------CrimeRadius----end

    //--------CrimeArea------------
    public void resetAreaMapColor()
    {
        for (Map.Entry<Integer, Polygon> polygonEntry : this.areaPolygons.entrySet())
        {
            Polygon polygon = polygonEntry.getValue();
            CrimeArea crimeArea = (CrimeArea)polygon.getTag();
            polygon.setFillColor(crimeArea.getCrimeAreaSummary().color);
        }
    }

    //* All areas
    //color should be sent Color.RED, Color.BLUE, etc.
    public void setAreaFillColor(int color)
    {
        for (Map.Entry<Integer, Polygon> polygonEntry : this.areaPolygons.entrySet())
        {
            Polygon polygon = polygonEntry.getValue();
            polygon.setFillColor(color);
        }
    }

    //* area with given ID
    //color should be sent Color.RED, Color.BLUE, etc.
    public void setAreaFillColor(int areaID, int color)
    {
        Polygon polygon = this.areaPolygons.get(areaID);
        if (polygon != null)
        {
            polygon.setFillColor(color);
        }
    }

    public void setAreaOutlineColor(int color)
    {
        for (Map.Entry<Integer, Polygon> polygonEntry : this.areaPolygons.entrySet())
        {
            Polygon polygon = polygonEntry.getValue();
            polygon.setStrokeColor(color);
        }
    }

    public void setAreaOutlineColor(int areaID, int color)
    {
        Polygon polygon = this.areaPolygons.get(areaID);

        if (polygon != null)
        {
            polygon.setStrokeColor(color);
        }
    }

    //hides and disables areas activity_map
    public void hideAreasMap()
    {
        setAreasMapVisibleAll(false);
    }

    //the single area to display, hide all other areas.
    public void filterArea(int areaID)
    {
        setAreasMapVisibleAll(false);
        setAreaMapVisible(areaID, true);
    }

    //*Generates and displays ALL areas.
    //*genSummary: Generate summary at same time
    public void displayAreasMap(boolean genSummary)
    {
        if (this.radiusArea != null)
        {
            this.radiusArea = null;
        }

        //if not all areas are generated, generate the remaining
        if (this.areaPolygons.size() != Chicago.Area.getAreasAmount(Chicago.Area.COMMUNITY))
        {
            Chicago.Area area = Chicago.Area.COMMUNITY;
            for (int i=1; i <= area.getAreasAmount(); ++i)
            {
                if (!this.areaPolygons.containsKey(i))
                {
                    drawCrimeArea(Chicago.Area.COMMUNITY, i, genSummary);
                }
            }
        }
        else
        {
            setAreasMapVisibleAll(true);
        }
    }

    //*Generates and displays area.
    //*genSummary: Generate summary at same time
    public void displayAreaMap(int ID, boolean genSummary)
    {
        if (this.radiusArea != null)
        {
            this.radiusArea = null;
        }

        if (!this.areaPolygons.containsKey(ID))
        {
            drawCrimeArea(Chicago.Area.COMMUNITY, ID, genSummary);
        }
        else
        {
            setAreaMapVisible(ID, true);
        }
    }

    //Hides/displays already generated areas, to generate area and display, call displayAreaMap.
    public void setAreasMapVisibleAll(boolean visible)
    {
        for (Map.Entry<Integer, Polygon> crimeAreaEntry : this.areaPolygons.entrySet())
        {
            Polygon polygon = crimeAreaEntry.getValue();
            polygon.setVisible(visible);
            polygon.setClickable(visible);
        }
    }

    //Hides/displays already generated area, to generate area and display, call displayAreaMap.
    public void setAreaMapVisible(int areaID, boolean visible)
    {
        if (this.areaPolygons.containsKey(areaID))
        {
            Polygon polygon = this.areaPolygons.get(areaID);
            if (polygon != null)
            {
                polygon.setVisible(visible);
                polygon.setClickable(visible);
            }
        }
    }

    //draws all
    private void drawCrimeArea(Chicago.Area area, boolean genInfoWin)
    {
        for (int i=1; i <= area.getAreasAmount(); ++i) {
            drawCrimeArea(area, i, genInfoWin);
        }
    }

    //draws single
    private void drawCrimeArea(Chicago.Area area, int areaID, boolean genInfoWin)
    {
        ArrayList<CrimeArea> areaDef = this.db.getAreaPolygon(area, areaID, genInfoWin);
        if (areaDef == null || areaDef.isEmpty()) {
            return;
        }

        //draws areas, creates summary strings for each area
        for (CrimeArea crimeArea : areaDef)
        {
            Polygon poly = this.mMap.addPolygon(
                    new PolygonOptions().clickable(true).addAll(crimeArea.getGeometry())
                            .strokeColor(Color.BLACK).fillColor(crimeArea.getCrimeAreaSummary().color).strokeWidth(3.0f));

            if (!crimeArea.geometryHoles.isEmpty())
            {
                ArrayList<ArrayList<LatLng>> holes = new ArrayList<>();
                holes.add(crimeArea.geometryHoles);
                poly.setHoles(holes);
            }
            poly.setTag(crimeArea);

            crimeArea.clearGeometry(); //clear out memory, polygons make copies.

            this.areaPolygons.remove(areaID);
            this.areaPolygons.put(areaID, poly);
        }

        this.mMap.setOnPolygonClickListener(this);
        this.mMap.setOnMapClickListener(this);
    }
    //--------CrimeArea---------end

}
