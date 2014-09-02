package com.songbook.view;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.songbook.R;
import com.songbook.SongDbAdapter;

public class SongFragment extends ListFragment {
	private String number;
	private String type;
	private String lyrics;
	private String info;

	public SongFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.number = getArguments().getString(SongDbAdapter.KEY_NUMBER);
		this.type = getArguments().getString(SongDbAdapter.KEY_TYPE);
		this.lyrics = getArguments().getString(SongDbAdapter.KEY_LYRICS);
		this.info = getArguments().getString(SongDbAdapter.KEY_INFO);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_song, container, false);
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
