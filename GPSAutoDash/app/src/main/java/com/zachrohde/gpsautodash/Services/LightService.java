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

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.zachrohde.gpsautodash.MainActivity;

/**
 * Using the Ambient Light sensor, detect if it is day or night and change the app theme to either
 * the Light or Dark Holo theme.
 */
public class LightService implements SensorEventListener {
    private static final String TAG = "LightService";

    // Member fields.
    private Activity mActivity;

    // Sensor related.
    private final SensorManager mSensorManager;
    private final Sensor mLightSensor;

    public LightService(Activity activity) {
        mActivity = activity;

        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        startListener();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            // Get the ambient light level in SI lux units.
            float light_level = event.values[0];

            // Activate either night mode when the light is less than overcast.
            if (light_level < SensorManager.LIGHT_OVERCAST) {
                // Activate night mode.
                if (MainActivity.mThemeId != android.R.style.Theme_Holo) {
                    MainActivity.mThemeId = android.R.style.Theme_Holo;
                    mActivity.recreate(); // We have acquired a new theme, destroy the activity.
                }
            } else {
                // Activate day mode.
                if (MainActivity.mThemeId != android.R.style.Theme_Holo_Light_DarkActionBar) {
                    MainActivity.mThemeId = android.R.style.Theme_Holo_Light_DarkActionBar;
                    mActivity.recreate(); // We have acquired a new theme, destroy the activity.
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * Start the SensorEventListener.
     */
    public void startListener() {
        mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Stop the SensorEventListener.
     */
    public void stopListener() {
        mSensorManager.unregisterListener(this);
    }
}