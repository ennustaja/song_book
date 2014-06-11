package com.songbook;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SongActivity extends ListFragment {

	/* For Log.d messages */
	@SuppressWarnings("unused")
	private static final String TAG = "SongBook";

	private String number;
	private String type;
	private String lyrics;
	private String info;

	/**
	 * Create a new instance of CountingFragment, providing "num"
	 * as an argument.
	 */
	static SongActivity newInstance(String num, String type, String lyrics, String info) {
		SongActivity f = new SongActivity();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putString("num", num);
		args.putString("type", type);
		args.putString("lyrics", lyrics);
		args.putString("info", info);
		f.setArguments(args);

		return f;
	}


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		number = getArguments() != null ? getArguments().getString("num") : "1";
		type = getArguments() != null ? getArguments().getString("type") : SongDbAdapter.TYPE_SHZ;
		lyrics = getArguments() != null ? getArguments().getString("lyrics") : "";
		info = getArguments() != null ? getArguments().getString("info") : "";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.song, container, false);
		TextView songTitle = (TextView) v.findViewById(R.id.songTitle);
		TextView songLyricsAndInfo = (TextView) v.findViewById(R.id.songLyricsAndInfo);

		String title = formatTitle(type, number);
		Spanned lyricsAndInfo = formatLyricsAndInfo(lyrics, info);

		songTitle.setText(title);
		songLyricsAndInfo.setText(lyricsAndInfo);

		return v;
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
