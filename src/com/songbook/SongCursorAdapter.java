package com.songbook;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SongCursorAdapter extends SimpleCursorAdapter {
	/* For Log.d messages */
	private static final String TAG = "SongBook";

    private Context context;
    private int layout;
    
    private int mTypeIndex;
    private int mNumberIndex;
    private int mLyricsIndex;
    private int mInfoIndex;
    
    private String mHighlightStr;
    
    private LayoutInflater mInflater;

    public SongCursorAdapter (Context context, int layout, Cursor c, String[] from, int[] to, String highlightStr) {
        super(context, layout, c, from, to);
        this.context = context;
        this.layout = layout;
        
        mTypeIndex = c.getColumnIndex(SongDbAdapter.KEY_TYPE);
        mNumberIndex = c.getColumnIndex(SongDbAdapter.KEY_NUMBER);
        mLyricsIndex = c.getColumnIndex(SongDbAdapter.KEY_LYRICS);
        mInfoIndex = c.getColumnIndex(SongDbAdapter.KEY_INFO);
        
        mHighlightStr = highlightStr;
        
        mInflater = LayoutInflater.from(context);
    }

	@Override
    public void bindView(View view, Context context, Cursor cursor) {
		TextView title = (TextView)view.findViewById(R.id.rowSongTitle);
		TextView lyrics = (TextView)view.findViewById(R.id.rowSongLyrics);
		TextView info = (TextView)view.findViewById(R.id.rowSongInfo);
		
		title.setText(formatTitle(cursor.getString(mTypeIndex), cursor.getString(mNumberIndex)));
		
		lyrics.setText("");
		lyrics.append(formatLyrics(cursor.getString(mLyricsIndex)));
		
		info.setText("");
		String infoText = cursor.getString(mInfoIndex);
		
		if(!mHighlightStr.equals("")){ // Certain text should be highlighted
			Spannable sText;
			int index;
			
			if((index = lyrics.getText().toString().toLowerCase().indexOf(mHighlightStr.toLowerCase())) != -1){ 
				// Highlighted text found in lyrics
				sText = (Spannable)lyrics.getText();
				sText.setSpan(new BackgroundColorSpan(Color.WHITE), index, index + mHighlightStr.length(), 0);
				sText.setSpan(new ForegroundColorSpan(Color.BLACK), index, index + mHighlightStr.length(), 0);
			} else if((index = infoText.toLowerCase().indexOf(mHighlightStr.toLowerCase())) != -1){
				// Highlighted text found in info
				// Only show info text in this case
				info.append(formatInfo(cursor.getString(mInfoIndex)));
				sText = (Spannable)info.getText();
				sText.setSpan(new BackgroundColorSpan(Color.WHITE), index, index + mHighlightStr.length(), 0);
				sText.setSpan(new ForegroundColorSpan(Color.BLACK), index, index + mHighlightStr.length(), 0);
			} else {
				// This else statement should never occur.
			}
		}
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
	
	private String formatLyrics(String lyrics){
    	String[] verses = lyrics.split("#");
    	String[] lowerCaseVerses = lyrics.toLowerCase().split("#");
    	if(mHighlightStr.equals("")){ // No text to highlight
    		return "1. " + verses[0]; // Return first verse.
    	} else {
    		int i = 0;
    		while(i < verses.length && lowerCaseVerses[i].indexOf(mHighlightStr.toLowerCase()) == -1){
    			i++;
    		}
    		if(i >= verses.length){ // String to be highlighted was not found
    			return "1. " + verses[0];
    		} else {
    			return "" + (i + 1) + ". " + verses[i];
    		}
    	}
	}
	
	private String formatInfo(String info){
    	String[] infoLines = info.split("\\$");
    	String parsedInfo = infoLines[0];
    	
    	for(int i = 1; i < infoLines.length; i++){
    		parsedInfo += "\n  " + infoLines[i];
    	}
    	
		return parsedInfo;
	}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
    	return mInflater.inflate(R.layout.result_row, null);
    }
}
