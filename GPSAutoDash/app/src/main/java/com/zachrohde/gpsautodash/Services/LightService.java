package com.zachrohde.gpsautodash.Services;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.zachrohde.gpsautodash.MainActivity;

/**
 * TODO
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