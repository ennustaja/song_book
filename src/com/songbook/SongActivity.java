package com.songbook;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

public class SongActivity extends Activity{
	
	/* For Log.d messages */	
	@SuppressWarnings("unused")
	private static final String TAG = "SongBook";
	
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
    	
    	mWakeLock.release();
    }
    
    private void displaySong(){
    	String type = getIntent().getStringExtra("type");
    	String number = getIntent().getStringExtra("number");
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
}
