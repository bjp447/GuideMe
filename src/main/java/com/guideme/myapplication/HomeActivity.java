package com.guideme.myapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class HomeActivity  extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,

        HeatMapFragment.OnHeatMapSelectedListener,

        dateFilterFragment.OnDateFilterSelected,
        radiusFilterFragment.OnRadiusFilterSelected,
        timeFilterFragment.OnTimeFilterSelected,
        EndDrawerToggle.onDrawerInteract
{
    private DrawerLayout drawer;
    private EndDrawerToggle rightDrawer;

    ArrayList<Fragment> fragments = new ArrayList<>();

    CrimeDisplay navCrimeDisplay = null;
    CrimeDisplay heatMapCrimeDisply = null;
    int currCrimeMap = 0;
    public boolean updateMap = false;

    public double radius = CrimeDisplay.MILE;
    public Pair<String, String> dateRange = new Pair<>("", "");
    public Pair<String, String> timeRange = new Pair<>("", "");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_Layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        drawer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                v.performClick();
            }});
        toggle.syncState();

        rightDrawer = new EndDrawerToggle(this, drawer, toolbar, R.string.pref_drawer_open, R.string.pref_drawer_close);
        drawer.addDrawerListener(rightDrawer);

        rightDrawer.attach(this); //attach this activity to the drawer for callbacks

        NavigationView preferenceView = findViewById(R.id.pref_view);
        preferenceView.setNavigationItemSelectedListener(this);

        //frags
        this.fragments.add(new NavigationFragment());
        this.fragments.add(new HeatMapFragment());

        this.fragments.add(new radiusFilterFragment());
        this.fragments.add(new dateFilterFragment());
        this.fragments.add(new timeFilterFragment());

        this.dateRange = dateFilterFragment.defaultDateRange;
        this.timeRange = timeFilterFragment.defaultTimeRange;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, this.fragments.get(0)).commit();
    }

    @Override public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
    {
        //NOTE: DO NOT CALL CrimeDisplay methods until onCrimeMapReady is called for the specified map.
        //NOTE: DO NOT CALL CrimeDisplay methods until onCrimeMapReady is called for the specified map.
        //NOTE: DO NOT CALL CrimeDisplay methods until onCrimeMapReady is called for the specified map.

        switch (menuItem.getItemId())
        {
            case R.id.navigation:
                this.currCrimeMap = 0;
                this.fragments.set(0, null); //clear memory //TODO: make sure memory is actual freed
                NavigationFragment frag = new NavigationFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).commit();
                this.fragments.set(0, frag);
                this.fragments.set(2, new radiusFilterFragment());
                this.fragments.set(3, new dateFilterFragment());
                this.fragments.set(4, new timeFilterFragment());
                this.dateRange = dateFilterFragment.defaultDateRange;
                this.timeRange = timeFilterFragment.defaultTimeRange;
                break;
            case R.id.heatmap:
                this.currCrimeMap = 1;
                this.fragments.set(1, null);
                HeatMapFragment fragment = new HeatMapFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                this.fragments.set(1, fragment);
                this.fragments.set(2, new radiusFilterFragment());
                this.fragments.set(3, new dateFilterFragment());
                this.fragments.set(4, new timeFilterFragment());
                this.dateRange = dateFilterFragment.defaultDateRange;
                this.timeRange = timeFilterFragment.defaultTimeRange;
                break;
            case R.id.filter_radius:
                getSupportFragmentManager().beginTransaction().replace(R.id.pref_fragment, this.fragments.get(2)).commit();
                break;
            case R.id.filter_date:
                getSupportFragmentManager().beginTransaction().replace(R.id.pref_fragment, this.fragments.get(3)).commit();
                break;
            case R.id.filter_time:
                getSupportFragmentManager().beginTransaction().replace(R.id.pref_fragment, this.fragments.get(4)).commit();
                break;
        }
        return true;
    }

    @Override public void onBackPressed()
    {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    } //onBackPressed

    @Override protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        rightDrawer.syncState();
    }

    @Override public void onDrawerClosed()
    {
        if (currCrimeMap == 0 && this.navCrimeDisplay != null && this.updateMap)
        {
            Pair<String, String> date = this.dateRange;
            Pair<String, String> time = this.timeRange;
            this.navCrimeDisplay.changeDateAndTime(date.first, date.second, time.first, time.second);
            this.updateMap = false;
        }
        else if (currCrimeMap == 1 && this.heatMapCrimeDisply != null && this.updateMap)
        {
            Pair<String, String> date = this.dateRange;
            Pair<String, String> time = this.timeRange;
            this.heatMapCrimeDisply.changeDateAndTime(date.first, date.second, time.first, time.second);
            this.updateMap = false;
        }
    }

    @Override public void onDateChanged(String dateStart, String dateEnd)
    {
        this.updateMap = true;
        this.dateRange = new Pair<>(dateStart, dateEnd);
    }

    @Override public void onRadiusChanged(double radius)
    {
        this.updateMap = true;
        this.radius = radius;
    }

    @Override public void onTimeChanged(String startTime, String endTime)
    {
        this.updateMap = true;
        this.timeRange = new Pair<>(startTime, endTime);
    }

    @Override public void onCrimeMapReady(HeatMapFragment fragment, CrimeDisplay crimeDisplay)
    {
        this.updateMap = false;

        if (this.fragments.get(0) == fragment)
        {
            this.navCrimeDisplay = crimeDisplay;
        }
        else if (this.fragments.get(1) == fragment)
        {
            this.heatMapCrimeDisply = crimeDisplay;
            crimeDisplay.displayAreasMap(true);
//            crimeDisplay.drawCrimeHeatMap();
//            crimeDisplay.displayAreaMap(76, true);
        }
        this.drawer.closeDrawers();
    }
}




