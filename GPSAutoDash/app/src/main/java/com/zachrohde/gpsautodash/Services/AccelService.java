/*
 * Copyright 2015 Zachary Rohde (http://zachrohde.com)
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.mozilla.org/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zachrohde.gpsautodash.Services;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zachrohde.gpsautodash.Fragments.SettingsFragment;
import com.zachrohde.gpsautodash.MainActivity;
import com.zachrohde.gpsautodash.R;

import java.math.BigDecimal;

/**
 * Using the GPS data, every time the GPS has a new location object, determine the acceleration and
 * update the acceleration progress bar.
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

    public AccelService(Activity activity, View rootView) {
        mActivity = activity;
        mRootView = rootView;

        mAccelView = (TextView) mRootView.findViewById(R.id.acceleration_value);

        mAccelBar = (ProgressBar) mRootView.findViewById(R.id.acceleration_bar);
        mAccelBarRes = mActivity.getResources();
    }

    /**
     * Using the location data passed in from GPSService, update the acceleration widget.
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

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);

                // If it is positive, negative, or zero.
                if (acceleration > 0) {
                    double max_pos = Double.parseDouble(prefs.getString(SettingsFragment.PREF_KEY_MAX_POS_ACCEL, mActivity.getString(R.string.pref_value_max_pos_accel)));
                    int accelPercentage = (int) findPercentage(0, max_pos, acceleration);

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
                    double max_neg = (-1) * Double.parseDouble(prefs.getString(SettingsFragment.PREF_KEY_MAX_NEG_ACCEL, mActivity.getString(R.string.pref_value_max_neg_accel)));
                    int accelPercentage = (int) findPercentage(0, max_neg, acceleration);

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