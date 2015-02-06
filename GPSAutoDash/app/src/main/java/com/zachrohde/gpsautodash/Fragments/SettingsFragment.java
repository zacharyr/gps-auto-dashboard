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

package com.zachrohde.gpsautodash.Fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.zachrohde.gpsautodash.MainActivity;
import com.zachrohde.gpsautodash.R;

import java.util.HashMap;
import java.util.Map;

/**
 * This fragment shows the preferences.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    // The fragment argument representing the section number for this fragment.
    private static final String ARG_SECTION_NUMBER = "section_number";

    // List of keys.
    public static final String PREF_KEY_DIST_ACC_MIN = "pref_key_dist_acc_min";
    public static final String PREF_KEY_MAX_NEG_ACCEL = "pref_key_max_neg_accel";
    public static final String PREF_KEY_MAX_POS_ACCEL = "pref_key_max_pos_accel";

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

        setDefaultSummaries();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Get the new value that was set by the user, then set the summary to the new value.
        if (key.equals(PREF_KEY_DIST_ACC_MIN)) {
            String newValue = sharedPreferences.getString(key, getString(R.string.pref_value_dist_acc_min));

            setSummary(key, newValue);
        } else if (key.equals(PREF_KEY_MAX_NEG_ACCEL)) {
            String newValue = sharedPreferences.getString(key, getString(R.string.pref_summary_max_neg_accel));

            setSummary(key, newValue);
        } else if (key.equals(PREF_KEY_MAX_POS_ACCEL)) {
            String newValue = sharedPreferences.getString(key, getString(R.string.pref_summary_max_pos_accel));

            setSummary(key, newValue);
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
     * Set all the summaries to their default values.
     */
    private void setDefaultSummaries() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Get the default values for all the preferences.
        Map<String, String> prefsDict = new HashMap<String, String>();
        prefsDict.put(PREF_KEY_DIST_ACC_MIN, prefs.getString(PREF_KEY_DIST_ACC_MIN, getString(R.string.pref_value_dist_acc_min)));
        prefsDict.put(PREF_KEY_MAX_NEG_ACCEL, prefs.getString(PREF_KEY_MAX_NEG_ACCEL, getString(R.string.pref_value_max_neg_accel)));
        prefsDict.put(PREF_KEY_MAX_POS_ACCEL, prefs.getString(PREF_KEY_MAX_POS_ACCEL, getString(R.string.pref_value_max_pos_accel)));

        // Loop through each of the preferences and set their summary.
        for (Map.Entry<String, String> entry : prefsDict.entrySet())
            setSummary(entry.getKey(), entry.getValue());
    }

    /**
     * Set an individual summary corresponding to the preference passed in.
     */
    private void setSummary(String key, String value) {
        Preference pref = findPreference(key);

        if (key.equals(PREF_KEY_DIST_ACC_MIN)) {
            pref.setSummary(value + getString(R.string.pref_summary_dist_acc_min));
        } else if (key.equals(PREF_KEY_MAX_NEG_ACCEL)) {
            pref.setSummary("-" + value + getString(R.string.pref_summary_max_neg_accel));
        } else if (key.equals(PREF_KEY_MAX_POS_ACCEL)) {
            pref.setSummary(value + getString(R.string.pref_summary_max_pos_accel));
        }
    }
}