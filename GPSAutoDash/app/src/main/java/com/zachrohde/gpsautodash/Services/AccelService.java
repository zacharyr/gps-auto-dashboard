package com.zachrohde.gpsautodash.Services;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zachrohde.gpsautodash.R;

public class AccelService implements SensorEventListener {
    // Member fields.
    private Activity mActivity;
    private View mRootView;

    // Accelerometer related.
    private SensorManager mManager;
    private Sensor mAccel;

    // Three main progress bars.
    private ProgressBar mProgressBRK;
    private ProgressBar mProgressMain;
    private ProgressBar mProgressPWR;

    private TextView mAccelView;

    public AccelService(Activity activity, View rootView) {
        mActivity = activity;
        mRootView = rootView;
        mManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mAccel = mManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mAccelView = (TextView) mRootView.findViewById(R.id.acceleration_value);

        mProgressBRK = (ProgressBar) mRootView.findViewById(R.id.brk_progress_bar);
        mProgressMain = (ProgressBar) mRootView.findViewById(R.id.main_progress_bar);
        mProgressPWR = (ProgressBar) mRootView.findViewById(R.id.pwr_progress_bar);

        startListener();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // In landscape mode:
        // X is up(+) and down(-)
        // Y is left(-) and right(+)
        // Z is forward(+) and backward(-)
        float zValue = event.values[2];

        mAccelView.setText(Float.toString(zValue));

        if (zValue < 1) {
            mProgressMain.setProgress(0);
        } else if (zValue > 5) {
            mProgressMain.setProgress(50);
        } else if (zValue > 10) {
            mProgressMain.setProgress(100);
        }

        //System.out.println("zValue: " + zValue);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void startListener() {
        mManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void stopListener() {
        mManager.unregisterListener(this);
    }
}