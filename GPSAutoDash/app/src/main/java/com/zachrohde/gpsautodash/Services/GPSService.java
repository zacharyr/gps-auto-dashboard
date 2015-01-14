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

public class GPSService {
    // Member fields.
    private Activity mActivity;
    private View mRootView;

    // For distance calculations.
    private boolean mOldMeasurements = false;
    private double mOldLatitude = 0.0;
    private double mOldLongitude = 0.0;

    // For distance calculations.
    private double mDistanceTraveled = 0.0;

    /**
     * Using the GPS, GPSService updates all the views on the UI that display GPS data. GPSService
     * runs on the main thread as it operates on an interrupt.
     */
    public GPSService(Activity activity, View rootView) {
        mActivity = activity;
        mRootView = rootView;

        setupGPS();
    }

    /**
     * Setup the GPS's location change listener.
     */
    private void setupGPS() {
        // Acquire a reference to the system Location Manager.
        LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

        // Check to see if the GPS is enabled.
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(mActivity, R.string.gps_disabled, Toast.LENGTH_LONG).show();
            mActivity.finish();
        }

        // Define a listener that responds to location updates.
        LocationListener locationListener = new LocationListener() {
            /**
             * Called when a new location is found by the location provider.
             */
            public void onLocationChanged(Location location) {
                updateSpeed(location);
                updateLatLongAlt(location);
                updateDistance(location);
                // updateAcceleration(location);
            }

            /**
             * Called when the provider status changes.
             */
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            /**
             * Called when the provider is enabled by the user.
             */
            public void onProviderEnabled(String provider) {}

            /**
             * Called when the provider is disabled by the user.
             */
            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the GPS Provider Location Manager to receive GPS updates.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0, 0, locationListener);
        // Register the listener with the Network Provider Location Manager to receive network updates.
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER , 0, 0, locationListener);
    }

    /**
     * Update the speedometer with the new location data.
     */
    private void updateSpeed(Location location) {
        // Set the speed.
        if (location.hasSpeed()) {
            float speed = location.getSpeed();

            TextView speed_view = (TextView) mRootView.findViewById(R.id.speed_value);
            speed_view.setText(Integer.toString((int) (speed * 2.23694)));
        }
    }

    /**
     * Update the lat/long/alt with the new location data.
     */
    private void updateLatLongAlt(Location location) {
        // Set the latitude.
        double latitude = location.getLatitude();

        TextView lat_view = (TextView) mRootView.findViewById(R.id.latitude_value);
        lat_view.setText(Double.toString(latitude));

        // Set the longitude.
        double longitude = location.getLongitude();

        TextView long_view = (TextView) mRootView.findViewById(R.id.longitude_value);
        long_view.setText(Double.toString(longitude));

        // Set the altitude.
        if (location.hasAltitude()) {
            double altitude = location.getAltitude();

            TextView alt_view = (TextView) mRootView.findViewById(R.id.altitude_value);
            alt_view.setText(Integer.toString((int) (altitude * 3.28084)));
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

                // Round to the hundredth decimal place.
                BigDecimal bd = new BigDecimal(Double.toString(mDistanceTraveled * 3.28084));
                bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

                TextView dist_view = (TextView) mRootView.findViewById(R.id.distance_value);
                dist_view.setText(Double.toString(bd.doubleValue()));

                // Store the new measurements.
                mOldLatitude = latitude;
                mOldLongitude = longitude;
            }
        }
    }
}
