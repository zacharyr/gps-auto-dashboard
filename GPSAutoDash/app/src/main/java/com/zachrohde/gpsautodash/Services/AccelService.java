package com.zachrohde.gpsautodash.Services;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.location.Location;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zachrohde.gpsautodash.MainActivity;
import com.zachrohde.gpsautodash.R;

import java.math.BigDecimal;

/**
 * Using the GPS data, every time the GPS has a new location object, determine the acceleration and
 * update the acceleration widget.
 */
public class AccelService {
    // Member fields.
    private Activity mActivity;
    private View mRootView;

    // Progress bar.
    private ProgressBar mAccelBar;
    private Resources mAccelBarRes;

    // TextViews for the UI.
    private TextView mAccelView;

    // For acceleration calculations.
    private boolean mOldMeasurements = false;
    private float mOldVelocity;
    private long mOldTime;

    // Constants for finding the percentage.
    private double POS_END = 2.5;
    private double NEG_END = -2.5;

    public AccelService(Activity activity, View rootView) {
        mActivity = activity;
        mRootView = rootView;

        mAccelView = (TextView) mRootView.findViewById(R.id.acceleration_value);

        mAccelBar = (ProgressBar) mRootView.findViewById(R.id.acceleration_bar);
        mAccelBarRes = mActivity.getResources();
    }

    /**
     * Callback function for GPSService to send the new location data.
     */
    public void updateAcceleration(Location location) {
        // We can only proceed when the location object actually has a speed to report.
        if (location.hasSpeed()) {
            // Only calculate the accel. when we have obtained at least one set of measurements.
            if (!mOldMeasurements) {
                mOldVelocity = location.getSpeed();
                mOldTime = System.currentTimeMillis();
                mOldMeasurements = true;
            } else {
                float velocity = location.getSpeed();

                long currentTime = System.currentTimeMillis();

                // Calculate the acceleration with the updated velocity and time.
                float acceleration = calculateAcceleration(velocity, currentTime);

                // Round to the hundredth decimal place.
                BigDecimal accelRounded = new BigDecimal(acceleration);
                accelRounded = accelRounded.setScale(2, BigDecimal.ROUND_HALF_UP);
                acceleration = accelRounded.floatValue();

                //mAccelView.setText(accelRounded.toString());
                mAccelView.setText(Float.toString(acceleration));

                // If it is positive, negative, or zero.
                if (acceleration > 0) {
                    int accelPercentage = (int) findPercentage(0, POS_END, acceleration);

                    // Switch whether the primary accel. theme is shown or power mode.
                    if (accelPercentage <= 75) {
                        animateProgress(accelPercentage);
                        mAccelBar.setProgressDrawable(mAccelBarRes.getDrawable(R.drawable.main_progress));

                        // If the theme is dark, set the theme of the accelerator bar to white.
                        if (MainActivity.mThemeId == android.R.style.Theme_Holo) setDarkTheme();
                    } else {
                        animateProgress(accelPercentage);
                        mAccelBar.setProgressDrawable(mAccelBarRes.getDrawable(R.drawable.pwr_progress));

                        // If the theme is dark, set the theme of the accelerator bar to white.
                        if (MainActivity.mThemeId == android.R.style.Theme_Holo) setDarkTheme();
                    }
                } else if (acceleration < 0) {
                    int accelPercentage = (int) findPercentage(0, NEG_END, acceleration);

                    animateProgress(accelPercentage);
                    mAccelBar.setProgressDrawable(mAccelBarRes.getDrawable(R.drawable.brk_progress));

                    // If the theme is dark, set the theme of the accelerator bar to white.
                    if (MainActivity.mThemeId == android.R.style.Theme_Holo) setDarkTheme();
                } else {
                    animateProgress(0);
                }

                mOldVelocity = velocity;
                mOldTime = currentTime;
            }
        }
    }

    /**
     * Calculate the acceleration using the velocity and time elapsed.
     */
    private float calculateAcceleration(float velocity, long currentTime) {
        float deltaVelocity = velocity - mOldVelocity;  // meters/second
        long timeDelta = currentTime - mOldTime;  // milliseconds
        return (deltaVelocity / timeDelta) * 1000;  // meters/second^2
    }

    /**
     * Map the acceleration value to the corresponding percentage.
     */
    private static double findPercentage(double start, double end, double val) {
        double range = end - start;
        double val_from_start = val - start;

        return (val_from_start / range) * 100;
    }

    /**
     * Animate the progress bar.
     */
    private void animateProgress(int progress) {
        // Update the "progress" propriety of progress bar until it reaches progress.
        ObjectAnimator animation = ObjectAnimator.ofInt(mAccelBar, "progress", progress);
        animation.setDuration(500); // 0.5 second
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    /**
     * Change the theme of the acceleration bar to the dark theme.
     */
    private void setDarkTheme() {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            mRootView.findViewById(R.id.acceleration_bar).setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.white_bg_progress));
        } else {
            mRootView.findViewById(R.id.acceleration_bar).setBackground(mActivity.getResources().getDrawable(R.drawable.white_bg_progress));
        }
    }
}