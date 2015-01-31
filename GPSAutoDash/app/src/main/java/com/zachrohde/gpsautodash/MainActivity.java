package com.zachrohde.gpsautodash;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.zachrohde.gpsautodash.Fragments.AboutFragment;
import com.zachrohde.gpsautodash.Fragments.DashboardFragment;
import com.zachrohde.gpsautodash.Fragments.NavigationDrawerFragment;
import com.zachrohde.gpsautodash.Fragments.SettingsFragment;
import com.zachrohde.gpsautodash.Services.GPSService;
import com.zachrohde.gpsautodash.Services.LightService;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private static final String TAG = "MainActivity";

    // Used to set the theme on restart.
    public static int mThemeId = -1;

    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;

    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;

    // Store the service instance.
    private LightService lightServiceInst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (mThemeId != -1) setTheme(mThemeId); // If app was stopped, but mThemeId has a value on restore.
        super.onCreate(savedInstanceState);

        // Check to see if there is a saved theme; we cannot use onRestoreInstanceState because
        // onCreate is called first.
        if (savedInstanceState != null) {
            if (savedInstanceState.getInt("theme", -1) != -1) {
                mThemeId = savedInstanceState.getInt("theme");
                setTheme(mThemeId);
            }
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep the screen on.

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Load the default preferences.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Create a new instance of LightService to enable auto-theme-switching based on light.
        lightServiceInst = new LightService(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // When the activity is ended, save the current theme and distance traveled.
        outState.putInt("theme", mThemeId);
        outState.putDouble("distance", GPSService.mDistanceTraveled);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.getDouble("distance", 0) != 0) {
            GPSService.mDistanceTraveled = savedInstanceState.getDouble("distance");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        lightServiceInst.startListener();
    }

    @Override
    public void onPause() {
        super.onPause();

        lightServiceInst.stopListener();
    }

    @Override
    public void onStop() {
        super.onStop();

        lightServiceInst.stopListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        GPSService.mDistanceTraveled = 0;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();

        switch (position) {
            case 0:
                // Update the main content by setting the Dashboard fragment.
                fragmentManager.beginTransaction()
                        .replace(R.id.container, DashboardFragment.newInstance(position + 1))
                        .commit();
                break;
            case 1:
                // Update the main content by setting the Prefs fragment.
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SettingsFragment.newInstance(position + 1))
                        .commit();
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section_dashboard);
                break;
            case 2:
                mTitle = getString(R.string.title_section_settings);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.option_about:
                // Show the About dialog.
                AboutFragment fragment = new AboutFragment();
                fragment.show(getFragmentManager(), "");

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
