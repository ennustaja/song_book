package com.songbook;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 *
 */
public class SetPrefs extends PreferenceActivity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
