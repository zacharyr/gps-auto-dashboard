package com.zachrohde.gpsautodash.Fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.zachrohde.gpsautodash.MainActivity;
import com.zachrohde.gpsautodash.R;

/**
 * This fragment shows the preferences.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    // The fragment argument representing the section number for this fragment.
    private static final String ARG_SECTION_NUMBER = "section_number";

    // List of keys.
    public static final String PREF_KEY_DIST_ACC_MIN = "pref_key_dist_acc_min";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static SettingsFragment newInstance(int sectionNumber) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        setPrefDistAccMinSummary(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_KEY_DIST_ACC_MIN)) {
            // Get the new value.
            String newValue = sharedPreferences.getString(key, getActivity().getString(R.string.pref_value_dist_acc_min));

            // Set the new value to the summary.
            setPrefDistAccMinSummary(newValue);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * TODO
     */
    private void setPrefDistAccMinSummary(String newValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Check to see if we were passed a value, if not, look it up.
        String value;
        if (newValue == null) {
            // Get the existing value.
            value = prefs.getString(PREF_KEY_DIST_ACC_MIN, getActivity().getString(R.string.pref_value_dist_acc_min));
        } else {
            value = newValue;
        }

        // Set the new value to the summary.
        Preference accMinPref = findPreference(PREF_KEY_DIST_ACC_MIN);
        accMinPref.setSummary(value + getString(R.string.pref_summary_dist_acc_min));
    }
}