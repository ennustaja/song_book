package com.songbook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class SongActivity extends Activity{

	/* For Log.d messages */
	@SuppressWarnings("unused")
	private static final String TAG = "SongBook";

	private SongDbAdapter mDbHelper;
	private Cursor mSongsCursor;
	private boolean SL, SHZ, V;
	private String number;
	
	/* View items */
	private TextView songTitle;
	private TextView songLyricsAndInfo;

	WakeLock mWakeLock;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.song);

		songTitle = (TextView)findViewById(R.id.songTitle);
		songLyricsAndInfo = (TextView)findViewById(R.id.songLyricsAndInfo);

		displaySong();
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "SongBook WakeLock");
		mWakeLock.acquire();
	}

	/**
	 * Releases the WakeLock when the Activity is no longer active.
	 */
	protected void onStop() {
		super.onStop();

		if (mWakeLock.isHeld())
		{
			mWakeLock.release();
		}
	}
	
	private void displaySong(){
		String type = getIntent().getStringExtra("type");
		if(type.equals(SongDbAdapter.TYPE_SIIONIN_LAULU)){
			SL = true;
			SHZ = V = false;
		} else if(type.equals(SongDbAdapter.TYPE_VIRSI)){
			V = true;
			SHZ = SL = false;
		} else {
			SHZ = true;
			SL = V = false;
		}
		number = getIntent().getStringExtra("number");
		String lyrics = getIntent().getStringExtra("lyrics");
		String info = getIntent().getStringExtra("info");

		String title = formatTitle(type, number);
		Spanned lyricsAndInfo = formatLyricsAndInfo(lyrics, info);

		songTitle.setText(title);
		songLyricsAndInfo.setText(lyricsAndInfo);
	}

	private String formatTitle(String type, String number){
		if(type.equals(SongDbAdapter.TYPE_SIIONIN_LAULU)){
			return "Siionin laulu " + number;
		} else if(type.equals(SongDbAdapter.TYPE_VIRSI)){
			return "Virsi " + number;
		} else {
			return "SHZ " + number;
		}
	}

	private Spanned formatLyricsAndInfo(String lyrics, String info){
		String[] verses = lyrics.split("#");
		String parsedLyrics = "  1. " + verses[0];

		for(int i = 1; i < verses.length; i++){
			parsedLyrics += "<br />&nbsp;&nbsp;" + (i + 1) + ". " + verses[i];
		}

		String[] infoLines = info.split("\\$");
		String parsedInfo = infoLines[0];

		for(int i = 1; i < infoLines.length; i++){
			parsedInfo += "<br />" + infoLines[i];
		}

		return Html.fromHtml(parsedLyrics +
				"<br />&nbsp;&nbsp;<small><font color='0x666666'>" + parsedInfo + "</small></font>");
	}
	
	/**
	 * Creates options menu from options_menu template.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.song_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Integer song_number = Integer.parseInt(number);
		Intent songIntent = new Intent();
		
		songIntent.setClassName("com.songbook", "com.songbook.SongActivity");

		mDbHelper = new SongDbAdapter(this);
		mDbHelper.open();
		
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.previous_song:
			song_number -= 1;
			if (song_number > 0) {
				mSongsCursor = mDbHelper.fetchSongsByNumber(song_number, SL, V, SHZ);
				if(mSongsCursor != null && mSongsCursor.getCount() > 0){
					mSongsCursor.moveToFirst();
					songIntent.putExtra("type", mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_TYPE)));
					songIntent.putExtra("number", mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_NUMBER)));
					songIntent.putExtra("lyrics", mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_LYRICS)));
					songIntent.putExtra("info", mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_INFO)));
	
					mDbHelper.close();
					songIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(songIntent);
				} else {
					mDbHelper.close();
				}
			}
			return true;
		case R.id.next_song:
			song_number += 1;
			
			mSongsCursor = mDbHelper.fetchSongsByNumber(song_number, SL, V, SHZ);
			if(mSongsCursor != null && mSongsCursor.getCount() > 0){
				mSongsCursor.moveToFirst();
				songIntent.putExtra("type", mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_TYPE)));
				songIntent.putExtra("number", mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_NUMBER)));
				songIntent.putExtra("lyrics", mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_LYRICS)));
				songIntent.putExtra("info", mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_INFO)));
	
				mDbHelper.close();
				songIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(songIntent);
			} else {
				mDbHelper.close();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
