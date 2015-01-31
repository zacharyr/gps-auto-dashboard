package com.zachrohde.gpsautodash.Services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zachrohde.gpsautodash.Fragments.SettingsFragment;
import com.zachrohde.gpsautodash.R;

/**
 * Using the GPS, GPSService updates all the views on the UI that display GPS data. GPSService
 * runs on the main thread as it operates on an interrupt.
 */
public class GPSService implements LocationListener {
    private static final String TAG = "GPSService";

    // Member fields.
    private Activity mActivity;
    private View mRootView;
    private AccelService mAccelServiceInst;

    // Preference manager.
    SharedPreferences mPrefs;

    // LocationManager for listener management.
    private final LocationManager mLocationManager;

    // TextViews for the UI.
    private TextView mSpeedView;
    private TextView mLatView;
    private TextView mLongView;
    private TextView mAltView;
    private TextView mDistView;

    // For distance calculations.
    private boolean mOldMeasurements = false;
    private double mOldLatitude;
    private double mOldLongitude;
    public static double mDistanceTraveled = 0;

    public GPSService(Activity activity, View rootView, Bundle savedInstanceState, AccelService accelServiceInst) {
        mActivity = activity;
        mRootView = rootView;
        mAccelServiceInst = accelServiceInst;

        // Check to see if there is a saved distance.
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        if (savedInstanceState != null) {
            mDistanceTraveled = Double.longBitsToDouble(mPrefs.getLong(mActivity.getString(R.string.pref_key_distance), Double.doubleToLongBits(0)));
            mPrefs.edit().putLong(mActivity.getString(R.string.pref_key_distance), Double.doubleToRawLongBits(0)).apply();
        }

        // Acquire a reference to the system Location Manager.
        mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

        // Check to see if the GPS is enabled.
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) promptEnableGPS();

        // Instantiate the TextViews.
        mSpeedView = (TextView) mRootView.findViewById(R.id.speed_value);
        mLatView = (TextView) mRootView.findViewById(R.id.latitude_value);
        mLongView = (TextView) mRootView.findViewById(R.id.longitude_value);
        mAltView = (TextView) mRootView.findViewById(R.id.altitude_value);
        mDistView = (TextView) mRootView.findViewById(R.id.distance_value);

        // Initiate the listener.
        startListener();
    }

    /**
     * Called when a new location is found by the location provider.
     */
    @Override
    public void onLocationChanged(Location location) {
        updateSpeed(location);
        updateLatLongAlt(location);
        updateDistance(location);
        mAccelServiceInst.updateAcceleration(location);
    }

    /**
     * Called when the provider status changes.
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    /**
     * Called when the provider is enabled by the user.
     */
    @Override
    public void onProviderEnabled(String provider) {}

    /**
     * Called when the provider is disabled by the user.
     */
    @Override
    public void onProviderDisabled(String provider) {}

    /**
     * Start the LocationListener.
     */
    public void startListener() {
        // Register the listener with the GPS Provider Location Manager to receive GPS updates.
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0, 0, this);
        // Register the listener with the Network Provider Location Manager to receive network updates.
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER , 0, 0, this);
    }

    /**
     * Stop the LocationListener.
     */
    public void stopListener() {
        try {
            mLocationManager.removeUpdates(this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, mActivity.getString(R.string.log_stop_location_manager));
        }
    }

    /**
     * Prompt the user to enable GPS to continue using the app.
     */
    private void promptEnableGPS() {
        Toast.makeText(mActivity, R.string.gps_disabled, Toast.LENGTH_LONG).show();
        mActivity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    /**
     * Update the speedometer with the new location data.
     */
    private void updateSpeed(Location location) {
        // Set the speed.
        if (location.hasSpeed()) {
            float speed = location.getSpeed();

            mSpeedView.setText(Integer.toString((int) (speed * 2.23694)));
        }
    }

    /**
     * Update the lat/long/alt with the new location data.
     */
    private void updateLatLongAlt(Location location) {
        // Set the latitude.
        double latitude = location.getLatitude();

        mLatView.setText(Double.toString(latitude));

        // Set the longitude.
        double longitude = location.getLongitude();

        mLongView.setText(Double.toString(longitude));

        // Set the altitude.
        if (location.hasAltitude()) {
            double altitude = location.getAltitude();

            mAltView.setText(Integer.toString((int) (altitude * 3.28084)));
        }
    }

    /**
     * Update the lat/long/alt with the new location data and calculated lat, long, and alt.
     */
    private void updateDistance(Location location) {
        mDistanceTraveled += 50000;
        mDistView.setText(Integer.toString((int) (mDistanceTraveled * 0.000621371)));

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        float min_accuracy = Float.parseFloat(mPrefs.getString(SettingsFragment.PREF_KEY_DIST_ACC_MIN, mActivity.getString(R.string.pref_value_dist_acc_min)));

        // We only want to calculate the distance when we have high accuracy.
        if (location.hasAccuracy() && location.getAccuracy() < min_accuracy) {
            // Only calculate the distance when we have obtained at least one set of measurements.
            if (!mOldMeasurements) {
                mOldLatitude = latitude;
                mOldLongitude = longitude;
                mOldMeasurements = true;
            } else {
                // The distance in meters is the first element returned from distanceBetween().
                float[] distance = new float[1];
                Location.distanceBetween(mOldLatitude, mOldLongitude, latitude, longitude, distance);

                mDistanceTraveled += distance[0];  // Update the running total of distance traveled.

                mDistView.setText(Integer.toString((int) (mDistanceTraveled * 0.000621371)));

                // Store the new measurements.
                mOldLatitude = latitude;
                mOldLongitude = longitude;
            }
        }
    }
}