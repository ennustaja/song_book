package com.songbook.view;

import android.preference.PreferenceManager;
import android.text.InputType;
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

import com.songbook.R;
import com.songbook.SongCursorAdapter;
import com.songbook.SongDbAdapter;

public class MainActivity extends ListActivity {
	private SongDbAdapter dbAdapter;
	private EditText searchField;
	private Cursor songsCursor;
	private ImageButton alphaBtn;
	private ImageButton getLyricsBtn;

	private boolean useInstantSearch;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		searchField = (EditText) findViewById(R.id.searchFieldEditText);
		getLyricsBtn = (ImageButton) findViewById(R.id.getLyricsButton);
		alphaBtn = (ImageButton) findViewById(R.id.alpha);

		dbAdapter = new SongDbAdapter(this);

        restorePreferences();
        setActions();
        setInitialFocusToSearchField();
		displayAllSongs();
    }

	@Override
	protected void onResume() {
		super.onResume();
        restorePreferences();
        searchForSongsWithEnteredText();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        openSettings();
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		songsCursor.moveToPosition(position);
        displaySelectedSong();
    }

    private void restorePreferences() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		useInstantSearch = settings.getBoolean("useInstantSearch", true);

        if(useInstantSearch) {
            getLyricsBtn.setVisibility(View.GONE);
        } else {
            getLyricsBtn.setVisibility(View.VISIBLE);
        }

		if (settings.getBoolean("numericDefault", false)) {
			searchField.setInputType(InputType.TYPE_CLASS_NUMBER);
			alphaBtn.setVisibility(View.VISIBLE);
		} else {
			searchField.setInputType(InputType.TYPE_CLASS_TEXT);
			alphaBtn.setVisibility(View.GONE);
		}
    }

    private void setActions() {
        searchForSongsOnEnter();
        searchForSongsOnTextEntered();
        searchForSongsOnButtonPress();
        displayPreferencesOnButtonClick();
        toggleAlphanumericInputOnClick();
    }

    private void displayPreferencesOnButtonClick() {
		ImageButton preferencesBtn = (ImageButton) findViewById(R.id.preferences);
		preferencesBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
                openSettings();
			}
		});
    }
    
    private void openSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(intent, 0);
    }

    private void toggleAlphanumericInputOnClick() {
		alphaBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (searchField.getInputType() == InputType.TYPE_CLASS_NUMBER) {
					searchField.setInputType(InputType.TYPE_CLASS_TEXT);
				} else {
					searchField.setInputType(InputType.TYPE_CLASS_NUMBER);
				}
			}
		});
    }

    private void setInitialFocusToSearchField() {
		searchField.requestFocus();
    }

    private void searchForSongsOnTextEntered() {
		searchField.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(useInstantSearch){
					searchForSongsWithEnteredText();
				}
			}
			public void afterTextChanged(Editable arg0) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
       });
	}

    private void searchForSongsOnEnter() {
		searchField.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event){
                    boolean isKeyRelease = (event.getAction() == KeyEvent.ACTION_UP);
                    boolean isEnter = (keyCode == KeyEvent.KEYCODE_ENTER);
                    boolean enterWasReleased = isKeyRelease && isEnter;
                    if (enterWasReleased) {
                        searchForSongsWithEnteredText();
                        return true;
                    }
                    return false;
                }
        });
    }
    
    private void searchForSongsOnButtonPress() {
        getLyricsBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                searchForSongsWithEnteredText();
            }
        });
    }

    private void searchForSongsWithEnteredText() {
        String searchText = searchField.getText().toString();
        displaySongsFromSearch(searchText);
    }

	private void displayAllSongs(){
		displaySongsFromSearch("");
	}

	private void displaySongsFromSearch(String searchTxt){
		int songNum;
		String searchStr = "";
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        boolean siioninLaulu = settings.getBoolean("siionin_laulu", true);
        boolean virsi = settings.getBoolean("virsi", true);
        boolean shz = settings.getBoolean("shz", true);
		try{
			// First try parsing text as a song number
			songNum = Integer.parseInt(searchTxt);
			songsCursor = dbAdapter.fetchSongsByNumber(songNum, siioninLaulu, virsi, shz);
		} catch(NumberFormatException e){
			// Entered text is not a song number, search for a String
			// Setting searchStr to non-empty causes search text to be highlighted.
			searchStr = searchTxt;
			songsCursor = dbAdapter.fetchSongsByString(searchTxt, siioninLaulu, virsi, shz);
		}

		// A song was found
		if(songsCursor != null && songsCursor.getCount() > 0){
            displaySongList(songsCursor, searchStr); // Display list of results
		} else {
			songsCursor = null;
			setListAdapter(null);
		}
	}

	private void displaySongList(Cursor songsCursor, String highlightStr){
		startManagingCursor(songsCursor);
        SongCursorAdapter songCursorAdapter = SongCursorAdapter.createSongCursorAdapter(this, songsCursor, highlightStr);
		setListAdapter(songCursorAdapter);
	}

    private void displaySelectedSong() {
		String selectedNumber = songsCursor.getString(songsCursor.getColumnIndex(SongDbAdapter.KEY_NUMBER));
		String selectedType = songsCursor.getString(songsCursor.getColumnIndex(SongDbAdapter.KEY_TYPE));
        displaySong(selectedNumber, selectedType);
    }

    private void displaySong(String selectedNumber, String selectedType) {
		Intent songPager = new Intent(this, SongActivity.class);
		songPager.putExtra("number", selectedNumber);
		songPager.putExtra("type", selectedType);
		startActivity(songPager);
	}
}
