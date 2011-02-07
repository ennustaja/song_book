package com.songbook;

import com.songbook.R;
import com.songbook.SongDbAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.content.SharedPreferences;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.text.TextWatcher;
import android.text.Editable;

public class Main extends ListActivity implements OnKeyListener, OnClickListener{
	/* For Log.d messages */
	private static final String TAG = "SongBook";
	
	public static final String PREFS_NAME = "songbook_prefs";
	
	private SongDbAdapter mDbHelper;
	
	/* View items */
	private EditText searchField;
	private ImageButton getLyricsBtn;
	
	private Cursor mSongsCursor;
	
	private boolean useInstantSearch;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        searchField = (EditText)findViewById(R.id.searchFieldEditText);
        getLyricsBtn = (ImageButton)findViewById(R.id.getLyricsButton);
        
        searchField.setOnKeyListener(this);
        getLyricsBtn.setOnClickListener(this);
        
        /** Set initial focus to the search field */
        searchField.requestFocus();
        
        mDbHelper = new SongDbAdapter(this);
        mDbHelper.open();
        
        //fetchAllSongs();
        
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
       	useInstantSearch = settings.getBoolean("useInstantSearch", true);
       	
       	TextWatcher searchWatch = new TextWatcher()
       	{
			public void afterTextChanged(Editable s) {
   		        //((TextView)findViewById(R.id.searchFieldEditText)).setText(String.format(getString(R.string.searchFieldEditText), s.length()));
   		    }
   		 
   		    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
   		    }
   		 
   		    public void onTextChanged(CharSequence s, int start, int before, int count) {
   		    }	 
       	};

       	((EditText)findViewById(R.id.searchFieldEditText)).addTextChangedListener(searchWatch);
    }
    
    /**
     * Captures key events from the text field.
     */
    public boolean onKey(View v, int keyCode, KeyEvent event){
    	if ((event.getAction() == KeyEvent.ACTION_UP) &&
    			(useInstantSearch || keyCode == KeyEvent.KEYCODE_ENTER)){
    		if(searchField.getText().toString() != ""){
    			fetchSongs(searchField.getText().toString());
    		}
        	return true;
    	}
    	return false;
    }
    
    /**
     * Captures click events from the button.
     */
    public void onClick(View v){
    	fetchSongs(searchField.getText().toString());
    }
    
    private void fetchAllSongs(){
    	fetchSongs("");
    }
    private void fetchSongs(String searchTxt){
    	int songNum;
    	String searchStr = "";
    	boolean searchByText = false;

		try{
			songNum = Integer.parseInt(searchTxt);
			mSongsCursor = mDbHelper.fetchSongsByNumber(songNum);
		} catch(NumberFormatException e){
			// Entered text is not a song number.
			// Parse it as a String.
			// Setting searchStr to non-empty causes search text to be highlighted.
			searchStr = searchTxt;
			searchByText = true;
			mSongsCursor = mDbHelper.fetchSongsByString(searchTxt);
		}
		
		// A song was found
    	if(mSongsCursor != null && mSongsCursor.getCount() > 0){
    		// Only one song was found --> Display list and then single song
    		// List display is seen only when returning from single song screen
    		if(mSongsCursor.getCount() == 1){
    			displayList(searchStr, searchByText);
    			displaySong();
    		} else { // More than one song was found --> Display list of results
    			displayList(searchStr, searchByText);
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
    
    private void displayList(String searchStr, boolean textSearch){;
        startManagingCursor(mSongsCursor);
        
        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{SongDbAdapter.KEY_TYPE, SongDbAdapter.KEY_NUMBER,
        		SongDbAdapter.KEY_LYRICS, SongDbAdapter.KEY_INFO};
        
        // and an array of the fields we want to bind those fields to
        int[] to = new int[]{R.id.rowSongTitle, R.id.rowSongLyrics, R.id.rowSongInfo};
        
        // Now create a simple cursor adapter and set it to display
        //SimpleCursorAdapter songs = new SimpleCursorAdapter(this, R.layout.result_row, mSongsCursor, from, to);
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
    
    @Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean("useInstantSearch", useInstantSearch);

      // Commit the edits!
      editor.commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.d(TAG, "called onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
    public boolean onPrepareOptionsMenu(final Menu menu) {
    	if(useInstantSearch){
    		menu.getItem(0).setTitle("Disable instant search");
    	} else {
    		menu.getItem(0).setTitle("Enable instant search");
    	}

        return super.onPrepareOptionsMenu(menu); 
   }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.instant_search:
        	useInstantSearch = !useInstantSearch;
	        	
        	// Commit changes
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("useInstantSearch", useInstantSearch);
            editor.commit();
            
            if(useInstantSearch){
            	item.setTitle("Disable instant search");
            } else {
            	item.setTitle("Enable instant search");
            }
            
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	    
}
