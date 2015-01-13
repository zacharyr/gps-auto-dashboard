package com.zachrohde.gpsautodash;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

public class DashboardFragment extends Fragment {
    // The fragment argument representing the section number for this fragment.
    private static final String ARG_SECTION_NUMBER = "section_number";

    private View rootView = null;

    private boolean old_measurements = false;
    private double old_latitude = 0.0;
    private double old_longitude = 0.0;
    private double distance_traveled = 0.0;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DashboardFragment newInstance(int sectionNumber) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        setupGPS();

        return rootView;
    }

    /**
     * Setup the GPS's location change listener.
     */
    private void setupGPS() {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getActivity(), R.string.gps_disabled, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        // Define a listener that responds to location updates
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

        // Register the listener with the GPS Provider Location Manager to receive GPS updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0, 0, locationListener);
        // Register the listener with the Network Provider Location Manager to receive network updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER , 0, 0, locationListener);
    }

    /**
     * Update the speedometer with the new location data.
     */
    private void updateSpeed(Location location) {
        // Set the speed.
        if (location.hasSpeed()) {
            float speed = location.getSpeed();

            TextView speed_view = (TextView) rootView.findViewById(R.id.speed_value);
            speed_view.setText(Integer.toString((int) (speed * 2.23694)));
        }
    }

    /**
     * Update the lat/long/alt with the new location data.
     */
    private void updateLatLongAlt(Location location) {
        // Set the latitude.
        double latitude = location.getLatitude();

        TextView lat_view = (TextView) rootView.findViewById(R.id.latitude_value);
        lat_view.setText(Double.toString(latitude));

        // Set the longitude.
        double longitude = location.getLongitude();

        TextView long_view = (TextView) rootView.findViewById(R.id.longitude_value);
        long_view.setText(Double.toString(longitude));

        // Set the altitude.
        if (location.hasAltitude()) {
            double altitude = location.getAltitude();

            TextView alt_view = (TextView) rootView.findViewById(R.id.altitude_value);
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
            if (!old_measurements) {
                old_latitude = latitude;
                old_longitude = longitude;
                old_measurements = true;
            } else {
                // The distance in meters is the first element returned from distanceBetween().
                float[] distance = new float[1];
                Location.distanceBetween(old_latitude, old_longitude, latitude, longitude, distance);

                distance_traveled += distance[0];  // Update the running total of distance traveled.

                // Round to the hundredth decimal place.
                BigDecimal bd = new BigDecimal(Double.toString(distance_traveled * 3.28084));
                bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

                TextView dist_view = (TextView) rootView.findViewById(R.id.distance_value);
                dist_view.setText(Double.toString(bd.doubleValue()));

                // Store the new measurements.
                old_latitude = latitude;
                old_longitude = longitude;
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}