package com.zachrohde.gpsautodash.Services;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.location.Location;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zachrohde.gpsautodash.R;

import java.util.Random;

/**
 *
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
    private final double POS_END = 2.5;
    private final double NEG_END = -2.5;

    public AccelService(Activity activity, View rootView) {
        mActivity = activity;
        mRootView = rootView;

        mAccelView = (TextView) mRootView.findViewById(R.id.acceleration_value);

        mAccelBar = (ProgressBar) mRootView.findViewById(R.id.acceleration_bar);
        mAccelBarRes = mActivity.getResources();
    }

    /**
     *
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

                Random rand = new Random();
                velocity = (float) ((rand.nextInt((80 - 70) + 1) + 70) / 2.23694);

                long currentTime = System.currentTimeMillis();

                float acceleration = calculateAcceleration(velocity, currentTime);

                // Round to the hundredth decimal place.
                //BigDecimal accelRounded = new BigDecimal(acceleration);
                //accelRounded = accelRounded.setScale(2, BigDecimal.ROUND_HALF_UP);
                //acceleration = accelRounded.floatValue();

                //mAccelView.setText(accelRounded.toString());
                mAccelView.setText(Float.toString(acceleration));

                // If it is positive, negative, or zero.
                if (acceleration > 0) {
                    int accelPercentage = (int) findPercentage(0, POS_END, acceleration);

                    if (accelPercentage <= 75) {
                        animateProgress(accelPercentage);
                        mAccelBar.setProgressDrawable(mAccelBarRes.getDrawable(R.drawable.main_progress));
                        System.out.println("positive, normal accel!: " + accelPercentage);
                    } else {
                        animateProgress(accelPercentage);
                        mAccelBar.setProgressDrawable(mAccelBarRes.getDrawable(R.drawable.pwr_progress));
                        System.out.println("positive, hard accel!: " + accelPercentage);
                    }
                } else if (acceleration < 0) {
                    int accelPercentage = (int) findPercentage(0, NEG_END, acceleration);

                    animateProgress(accelPercentage);
                    mAccelBar.setProgressDrawable(mAccelBarRes.getDrawable(R.drawable.brk_progress));
                    System.out.println("negative, applying brake!: " + accelPercentage);
                } else {
                    animateProgress(0);
                    mAccelBar.setProgressDrawable(mAccelBarRes.getDrawable(R.drawable.main_progress));
                    System.out.println("zero!: " + 0);
                }

                mOldVelocity = velocity;
                mOldTime = currentTime;
            }
        }
    }

    /**
     *
     */
    private float calculateAcceleration(float velocity, long currentTime) {
        float deltaVelocity = velocity - mOldVelocity;  // meters/second
        long timeDelta = currentTime - mOldTime;  // milliseconds
        return (deltaVelocity / timeDelta) * 1000;  // meters/second^2
    }

    /**
     *
     */
    private static double findPercentage(double start, double end, double val) {
        double range = end - start;
        double val_from_start = val - start;

        return (val_from_start / range) * 100;
    }

    /**
     *
     */
    private void animateProgress(int progress) {
        // Update the "progress" propriety of progress bar until it reaches progress.
        ObjectAnimator animation = ObjectAnimator.ofInt(mAccelBar, "progress", progress);
        animation.setDuration(500); // 0.5 second
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }
}