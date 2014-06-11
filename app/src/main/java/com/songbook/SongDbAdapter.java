package com.songbook;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SongDbAdapter extends SQLiteOpenHelper{
	private static final String TAG = "SongBook";

	// Android default system path for app database
	private static String ASSETS_DB_NAME = "songbook_database";
	private static String DB_PATH = "/data/data/com.songbook/databases/";
	private static String DB_NAME = "db";
    private static String DB_FULL_PATH = DB_PATH + DB_NAME;
	private static String DB_TABLE = "songs";

	public static final String KEY_ROWID = "_id";
	public static final String KEY_TYPE = "type";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_LYRICS = "lyrics";
	public static final String KEY_INFO = "info";

	public static final String TYPE_SIIONIN_LAULU = "SiioninLaulu";
	public static final String TYPE_VIRSI = "Virsi";
	public static final String TYPE_SHZ = "SHZ";

	private SQLiteDatabase database;

	public SongDbAdapter(Context context) {
		super(context, DB_NAME, null, 1);
        openDatabaseAndCreateIfNeeded(context);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	@Override
	public synchronized void close() {
		if(database != null) database.close();
		super.close();
	}

	public void openDatabaseAndCreateIfNeeded(Context context){
        boolean databaseOpened = openDatabase();
        if(!databaseOpened) {
            createAndOpenDatabase(context);
        }
    }

    private void createAndOpenDatabase(Context context) {
        createEmptyDatabaseInDefaultPath();
        try {
            copyDatabaseFromAssets(context);
        } catch(IOException e) {
            Log.d(TAG, "Database could not be copied from assets", e);
        }
        openDatabase();
    }

    private void createEmptyDatabaseInDefaultPath() {
        this.getReadableDatabase();
    }

	private void copyDatabaseFromAssets(Context context) throws IOException{
		InputStream assetsDb = context.getAssets().open(ASSETS_DB_NAME);
		OutputStream systemDb = new FileOutputStream(DB_FULL_PATH);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = assetsDb.read(buffer)) > 0){
			systemDb.write(buffer, 0, length);
		}

		systemDb.flush();
		systemDb.close();
		assetsDb.close();
	}

    public boolean openDatabase() {
        if(database != null && database.isOpen()) return true;

		try {
			database = SQLiteDatabase.openDatabase(DB_FULL_PATH, null, SQLiteDatabase.OPEN_READONLY);
		} catch(SQLiteException e){
            Log.d(TAG, "Database '" + DB_FULL_PATH + "' could not be opened", e);
            return false;
		}
        return true;
	}

	public Cursor fetchAllSongs(boolean siioninLaulu, boolean virsi, boolean shz) {
        openDatabase();
		return database.query(DB_TABLE, 
                new String[] {KEY_ROWID, KEY_NUMBER, KEY_LYRICS}, 
                getTypeWhereClause(siioninLaulu, virsi, shz), 
                null, null, null, null);
	}

	public Cursor fetchSongsByNumber(int songNum, boolean siioninLaulu, boolean virsi, boolean shz){
        openDatabase();
		Cursor result;
		try{
            String queryStr = KEY_NUMBER + "=" + songNum + " AND (" 
                    + getTypeWhereClause(siioninLaulu, virsi, shz) 
                    + ")";

			result = database.query(DB_TABLE, 
                    new String[] {KEY_ROWID, KEY_TYPE, KEY_NUMBER, KEY_LYRICS, KEY_INFO},
                    queryStr,
                    null, null, null, null);
		} catch (SQLiteException e){
			Log.d(TAG, e.toString());
			result = null;
		}
		return result;
	}

	public Cursor fetchSongsByString(String str, boolean siioninLaulu, boolean virsi, boolean shz){
        openDatabase();
		Cursor result;
		try{
            String queryStr = "(" + KEY_LYRICS + " LIKE '%" + str + "%'" +
					"OR " + KEY_INFO + " LIKE '%" + str + "%'" + 
                    ") AND (" + getTypeWhereClause(siioninLaulu, virsi, shz) + ")";
			result = database.query(DB_TABLE, 
                    new String[] {KEY_ROWID, KEY_TYPE, KEY_NUMBER, KEY_LYRICS, KEY_INFO},
                    queryStr,
                    null, null, null, null);
		} catch (SQLiteException e){
			Log.d(TAG, e.toString());
			result = null;
		}
		return result;
	}

	private String getTypeWhereClause(boolean siioninLaulu, boolean virsi, boolean shz)
	{
		StringBuilder aTypeWhereBuilder = new StringBuilder();
		boolean appendOr = false;
		if (siioninLaulu)
		{
			aTypeWhereBuilder.append(KEY_TYPE).append("='").append(TYPE_SIIONIN_LAULU).append("'");
			appendOr = true;
		}
		if (virsi)
		{
			if (appendOr)
			{
				aTypeWhereBuilder.append(" OR ");
			}
			aTypeWhereBuilder.append(KEY_TYPE).append("='").append(TYPE_VIRSI).append("'");
			appendOr = true;
		}
		if (shz)
		{
			if (appendOr)
			{
				aTypeWhereBuilder.append(" OR ");
			}
			aTypeWhereBuilder.append(KEY_TYPE).append("='").append(TYPE_SHZ).append("'");
		}
		return aTypeWhereBuilder.toString();
	}
}
