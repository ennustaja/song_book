package com.songbook.view;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.songbook.R;
import com.songbook.SongDbAdapter;

public class SongActivity extends FragmentActivity {

    MyAdapter mAdapter;
    ViewPager2 mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        SongDbAdapter mDbHelper = new SongDbAdapter(this);
        mPager = findViewById(R.id.pager);
        mAdapter = new MyAdapter(this, mDbHelper, getIntent().getStringExtra("type"));
        mPager.setAdapter(mAdapter);
        int aCurrentNumber;
        try {
            aCurrentNumber = Integer.parseInt(getIntent().getStringExtra("number"));
            mPager.setCurrentItem(aCurrentNumber - 1);
        } catch (NumberFormatException e) {
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

    public static class MyAdapter extends FragmentStateAdapter {

        private final SongDbAdapter mDbHelper;
        private final String mType;

        public MyAdapter(FragmentActivity fa, SongDbAdapter sdba, String type) {
            super(fa);

            mDbHelper = sdba;
            mType = type;
        }


        @NonNull
        @Override
        public Fragment createFragment(int song_number) {
            String type = SongDbAdapter.TYPE_SHZ, number = "1", lyrics = "", info = "";
            if (song_number > -1) {
                Cursor songsCursor = mDbHelper.fetchSongsByNumber(song_number + 1, SongDbAdapter.TYPE_SIIONIN_LAULU.equals(mType),
                        SongDbAdapter.TYPE_VIRSI.equals(mType), SongDbAdapter.TYPE_SHZ.equals(mType));
                if (songsCursor != null && songsCursor.getCount() > 0) {
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
            SongFragment sf = new SongFragment();
            Bundle bdl = new Bundle(4);
            bdl.putString(SongDbAdapter.KEY_NUMBER, number);
            bdl.putString(SongDbAdapter.KEY_TYPE, type);
            bdl.putString(SongDbAdapter.KEY_LYRICS, lyrics);
            bdl.putString(SongDbAdapter.KEY_INFO, info);
            sf.setArguments(bdl);
            return sf;
        }

        @Override
        public int getItemCount() {
            if (SongDbAdapter.TYPE_SHZ.equals(mType)) {
                return 604;
            } else if (SongDbAdapter.TYPE_SIIONIN_LAULU.equals(mType)) {
                return 329;
            } else if (SongDbAdapter.TYPE_VIRSI.equals(mType)) {
                return 632;
            }
            return 1;
        }
    }
}
