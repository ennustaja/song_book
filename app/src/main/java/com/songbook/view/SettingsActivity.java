package com.songbook.view;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.songbook.R;

public class SettingsActivity extends PreferenceActivity {
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
