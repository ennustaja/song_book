package com.songbook;

import android.preference.PreferenceManager;
import android.view.*;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.content.SharedPreferences;
import android.text.TextWatcher;
import android.text.Editable;

public class Main extends ListActivity implements OnKeyListener, OnClickListener{

	private SongDbAdapter mDbHelper;

	/* View items */
	private EditText searchField;

	private Cursor mSongsCursor;

	private boolean useInstantSearch;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		searchField = (EditText)findViewById(R.id.searchFieldEditText);
		ImageButton getLyricsBtn = (ImageButton) findViewById(R.id.getLyricsButton);

		searchField.setOnKeyListener(this);
		getLyricsBtn.setOnClickListener(this);

		/** Set initial focus to the search field */
		searchField.requestFocus();

		mDbHelper = new SongDbAdapter(this);
		mDbHelper.open();

		fetchAllSongs();

		// Restore preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		useInstantSearch = settings.getBoolean("useInstantSearch", true);

		TextWatcher searchWatch = new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(useInstantSearch){
					fetchSongs(searchField.getText().toString(), true);
				}
			}
			public void afterTextChanged(Editable arg0) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		};

		((EditText)findViewById(R.id.searchFieldEditText)).addTextChangedListener(searchWatch);
	}

	/**
	 * Captures key events from the text field.
	 */
	public boolean onKey(View v, int keyCode, KeyEvent event){
		// Do search on ENTER
		if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)){
				fetchSongs(searchField.getText().toString(), true);
			return true;
		}
		return false;
	}

	/**
	 * Captures click events from the button.
	 */
	public void onClick(View v){
		fetchSongs(searchField.getText().toString(), true);
	}

	private void fetchAllSongs(){
		fetchSongs("", true);
	}
	private void fetchSongs(String searchTxt, boolean displaySong){
		int songNum;
		String searchStr = "";
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		try{
			// First try parsing text as a song number
			songNum = Integer.parseInt(searchTxt);
			mSongsCursor = mDbHelper.fetchSongsByNumber(songNum, settings.getBoolean("siionin_laulu", true),
					settings.getBoolean("virsi", true), settings.getBoolean("shz", true));
		} catch(NumberFormatException e){
			// Entered text is not a song number, search for a String
			// Setting searchStr to non-empty causes search text to be highlighted.
			searchStr = searchTxt;
			mSongsCursor = mDbHelper.fetchSongsByString(searchTxt, settings.getBoolean("siionin_laulu", true),
					settings.getBoolean("virsi", true), settings.getBoolean("shz", true));
		}

		// A song was found
		if(mSongsCursor != null && mSongsCursor.getCount() > 0){
			// Only one song was found --> Display list and then single song
			// List display is seen only when returning from single song screen
			if(mSongsCursor.getCount() == 1){
				displayList(searchStr);
				if (displaySong)
				{
					displaySong();
				}
			} else { // More than one song was found --> Display list of results
				displayList(searchStr);
			}
		} else {
			mSongsCursor = null;
			setListAdapter(null);
		}
	}

	private void displaySong(){
		mSongsCursor.moveToFirst();
		displaySong(
				mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_TYPE)),
				mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_NUMBER)),
				mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_LYRICS)),
				mSongsCursor.getString(mSongsCursor.getColumnIndex(SongDbAdapter.KEY_INFO)));
	}

	private void displaySong(String type, String number, String lyrics, String info){
		Intent songIntent = new Intent();
		songIntent.setClassName("com.songbook", "com.songbook.SongActivity");
		songIntent.putExtra("type", type);
		songIntent.putExtra("number", number);
		songIntent.putExtra("lyrics", lyrics);
		songIntent.putExtra("info", info);
		startActivity(songIntent);
	}

	private void displayList(String searchStr){
		startManagingCursor(mSongsCursor);

		// Create an array to specify the fields we want to display in the list
		String[] from = new String[]{SongDbAdapter.KEY_TYPE, SongDbAdapter.KEY_NUMBER,
				SongDbAdapter.KEY_LYRICS, SongDbAdapter.KEY_INFO};

		// and an array of the fields we want to bind those fields to
		int[] to = new int[]{R.id.rowSongTitle, R.id.rowSongLyrics, R.id.rowSongInfo};

		// Now create a simple cursor adapter and set it to display
		SongCursorAdapter songs = new SongCursorAdapter(this, R.layout.result_row, mSongsCursor, from, to, searchStr);

		setListAdapter(songs);
	}

	/**
	 * Displays a single song when the song is selected from the list.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor c = mSongsCursor;
		c.moveToPosition(position);
		displaySong(
				c.getString(c.getColumnIndex(SongDbAdapter.KEY_TYPE)),
				c.getString(c.getColumnIndex(SongDbAdapter.KEY_NUMBER)),
				c.getString(c.getColumnIndex(SongDbAdapter.KEY_LYRICS)),
				c.getString(c.getColumnIndex(SongDbAdapter.KEY_INFO)));
	}

	/**
	 * Creates options menu from options_menu template.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.preferences:
			Intent intent = new Intent(this, SetPrefs.class);
			startActivityForResult(intent, 0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		useInstantSearch = settings.getBoolean("useInstantSearch", true);

		fetchSongs(searchField.getText().toString(), false);
	}
}
