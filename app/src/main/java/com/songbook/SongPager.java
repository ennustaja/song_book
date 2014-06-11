package com.songbook;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by bob on 1/1/14.
 */
public class SongPager extends FragmentActivity {

	MyAdapter mAdapter;
	ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.song_pager);
		SongDbAdapter mDbHelper = new SongDbAdapter(this);

		mAdapter = new MyAdapter(getSupportFragmentManager(), mDbHelper, getIntent().getStringExtra("type"));

		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		int aCurrentNumber;
		try
		{
			aCurrentNumber = Integer.parseInt(getIntent().getStringExtra("number"));
			mPager.setCurrentItem(aCurrentNumber - 1);
		} catch (NumberFormatException e)
		{
			// Defaults to song 1 if unable to parse number
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && Build.VERSION.SDK_INT >= 19) {
			mPager.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	public static class MyAdapter extends FragmentStatePagerAdapter {

		private SongDbAdapter mDbHelper;
		private String mType;

		public MyAdapter(FragmentManager fm, SongDbAdapter sdba, String type) {
			super(fm);
			mDbHelper = sdba;
			mType = type;
		}

		@Override
		public Fragment getItem(int song_number) {
			String type = SongDbAdapter.TYPE_SHZ, number = "1", lyrics = "", info = "";
			if (song_number > -1) {
				mDbHelper.open();
				Cursor songsCursor = mDbHelper.fetchSongsByNumber(song_number + 1, SongDbAdapter.TYPE_SIIONIN_LAULU.equals(mType),
						SongDbAdapter.TYPE_VIRSI.equals(mType), SongDbAdapter.TYPE_SHZ.equals(mType));
				if(songsCursor != null && songsCursor.getCount() > 0){
					songsCursor.moveToFirst();
					type = songsCursor.getString(songsCursor.getColumnIndex(SongDbAdapter.KEY_TYPE));
					number = songsCursor.getString(songsCursor.getColumnIndex(SongDbAdapter.KEY_NUMBER));
					lyrics = songsCursor.getString(songsCursor.getColumnIndex(SongDbAdapter.KEY_LYRICS));
					info = songsCursor.getString(songsCursor.getColumnIndex(SongDbAdapter.KEY_INFO));

					mDbHelper.close();
				} else {
					mDbHelper.close();
				}
			}
			return SongActivity.newInstance(number, type, lyrics, info);
		}

		@Override
		public int getCount() {
			if (SongDbAdapter.TYPE_SHZ.equals(mType))
			{
				return 604;
			} else if (SongDbAdapter.TYPE_SIIONIN_LAULU.equals(mType))
			{
				return 329;
			} else if (SongDbAdapter.TYPE_VIRSI.equals(mType))
			{
				return 632;
			}
			return 1;
		}
	}
}
