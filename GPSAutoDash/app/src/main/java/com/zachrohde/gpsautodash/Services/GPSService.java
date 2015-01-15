package com.zachrohde.gpsautodash.Services;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zachrohde.gpsautodash.R;

import java.math.BigDecimal;

// TODO (URGENT): fix textview lookups

/**
 * Using the GPS, GPSService updates all the views on the UI that display GPS data. GPSService
 * runs on the main thread as it operates on an interrupt.
 */
public class GPSService implements LocationListener {
    // Member fields.
    private Activity mActivity;
    private View mRootView;

    // TextViews for the UI.
    private TextView mSpeedView;
    private TextView mLatView;
    private TextView mLongView;
    private TextView mAltView;
    private TextView mDistView;

    // For distance calculations.
    private boolean mOldMeasurements = false;
    private double mOldLatitude = 0.0;
    private double mOldLongitude = 0.0;
    private double mDistanceTraveled = 0.0;

    public GPSService(Activity activity, View rootView) {
        mActivity = activity;
        mRootView = rootView;

        // Acquire a reference to the system Location Manager.
        LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

        // Check to see if the GPS is enabled.
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(mActivity, R.string.gps_disabled, Toast.LENGTH_LONG).show();
            mActivity.finish();
        }

        // Instantiate the TextViews.
        mSpeedView = (TextView) mRootView.findViewById(R.id.speed_value);
        mLatView = (TextView) mRootView.findViewById(R.id.latitude_value);
        mLongView = (TextView) mRootView.findViewById(R.id.longitude_value);
        mAltView = (TextView) mRootView.findViewById(R.id.altitude_value);
        mDistView = (TextView) mRootView.findViewById(R.id.distance_value);

        startListener(locationManager);
    }

    /**
     * Called when a new location is found by the location provider.
     */
    @Override
    public void onLocationChanged(Location location) {
        updateSpeed(location);
        updateLatLongAlt(location);
        updateDistance(location);
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
    private void startListener(LocationManager locationManager) {
        // Register the listener with the GPS Provider Location Manager to receive GPS updates.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0, 0, this);
        // Register the listener with the Network Provider Location Manager to receive network updates.
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER , 0, 0, this);
    }

    /**
     * Stop the LocationListener.
     */
    private void stopListener(LocationManager locationManager) {
        if (locationManager != null)
            locationManager.removeUpdates(this);
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
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // We only want to calculate the distance when we have high accuracy.
        // TODO: let the user change this number
        if (location.hasAccuracy() && location.getAccuracy() < 15.0) {
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