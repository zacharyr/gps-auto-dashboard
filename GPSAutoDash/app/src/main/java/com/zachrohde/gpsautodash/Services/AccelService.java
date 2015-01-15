package com.zachrohde.gpsautodash.Services;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zachrohde.gpsautodash.R;

import java.math.BigDecimal;

/**
 * Using the SensorService, AccelService updates all the views on the UI that display accelerometer
 * data. AccelService runs on the main thread as it operates on an interrupt.
 */
public class AccelService implements SensorEventListener {
    // Member fields.
    private Activity mActivity;
    private View mRootView;

    // Accelerometer related.
    private SensorManager mSensorManager;
    private Sensor mAccelSensor;

    // Three main progress bars for the UI.
    private ProgressBar mProgressBRK;
    private ProgressBar mProgressMain;
    private ProgressBar mProgressPWR;

    // TextViews for the UI.
    private TextView mAccelView;

    public AccelService(Activity activity, View rootView) {
        mActivity = activity;
        mRootView = rootView;

        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mAccelView = (TextView) mRootView.findViewById(R.id.acceleration_value);

        mProgressBRK = (ProgressBar) mRootView.findViewById(R.id.brk_progress_bar);
        mProgressMain = (ProgressBar) mRootView.findViewById(R.id.main_progress_bar);
        mProgressPWR = (ProgressBar) mRootView.findViewById(R.id.pwr_progress_bar);

        startListener();
    }

    /**
     * Called when sensor values have changed.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // In landscape mode:
        // X is up(+) and down(-)
        // Y is left(-) and right(+)
        // Z is forward(+) and backward(-)
        float zValue = event.values[2];

        // Round to the hundredth decimal place.
        BigDecimal zValueTenth = new BigDecimal(zValue);
        zValueTenth = zValueTenth.setScale(2, BigDecimal.ROUND_HALF_UP);
        zValue = zValueTenth.floatValue();

        mAccelView.setText(zValueTenth.toString());

        // If it is positive or else negative.
        if (zValue > 0) {
            if (zValue < 0.25) {
                animateProgress(0);
            } else if (zValue >= 0.25 && zValue <= 2.5) {
                animateProgress(50);
                System.out.println("zValue >= 0.25 && zValue <= 2.5: " + zValue);
            } else if (zValue > 2.5) {
                animateProgress(100);
                System.out.println("zValue > 2.5: " + zValue);
            }
        } else {
            if (zValue > -0.25) {
                animateProgress(0);
            } else if (zValue <= -0.25 && zValue <= -2.5) {
                animateProgress(50);
                System.out.println("zValue <= -0.25 && zValue <= -2.5: " + zValue);
            } else if (zValue < -2.5) {
                animateProgress(100);
                System.out.println("zValue < -2.5: " + zValue);
            }
        }

        //System.out.println("zValue: " + zValue);
    }

    /**
     * Called when the accuracy of a sensor has changed.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * Start the SensorEventListener.
     */
    private void startListener() {
        mSensorManager.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Stop the SensorEventListener.
     */
    private void stopListener() {
        mSensorManager.unregisterListener(this);
    }

    private void animateProgress(int progress) {
        // Update the "progress" propriety of progress bar until it reaches progress.
        ObjectAnimator animation = ObjectAnimator.ofInt(mProgressMain, "progress", progress);
        animation.setDuration(500); // 0.5 second
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }
}