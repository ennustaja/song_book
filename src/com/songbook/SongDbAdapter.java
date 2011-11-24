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
	/* For Log.d messages */
	private static final String TAG = "SongBook";

	//The Android's default system path of your application database.
	private static String DB_PATH = "/data/data/com.songbook/databases/";
	private static String DB_NAME = "db";
	private static String DB_TABLE = "songs";

	public static final String KEY_ROWID = "_id";
	public static final String KEY_TYPE = "type";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_LYRICS = "lyrics";
	public static final String KEY_INFO = "info";

	public static final String TYPE_SIIONIN_LAULU = "SiioninLaulu";
	public static final String TYPE_VIRSI = "Virsi";
	public static final String TYPE_SHZ = "SHZ";

	private SQLiteDatabase mDb;

	private final Context mContext;

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 *
	 * @param ctx	the Context within which to work
	 */
	public SongDbAdapter(Context ctx) {
		super(ctx, DB_NAME, null, 1);
		this.mContext = ctx;
	}

	public void open(){
		try {
			createDatabase();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}

		try {
			openDatabase();
		}catch(SQLException sqle){
			throw sqle;
		}
	}

	  /**
	 * Creates an empty database on the system and rewrites it with your own database.
	 * */
	public void createDatabase() throws IOException{
		//boolean dbExist = checkDatabase();
		// Set this to false to regenerate db from split files
		boolean dbExist = false;

		if(dbExist){
			//do nothing - database already exist
		}else{
			//By calling this method and empty database will be created into the default system path
			   //of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase();

			try {
				copyDatabase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}

	}

	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDatabase(){
		SQLiteDatabase checkDB = null;

		try{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

		}catch(SQLiteException e){
			//database does't exist yet.
		}

		if(checkDB != null){
			checkDB.close();
		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */

	private void copyDatabase() throws IOException{
		//Open your local db as the input stream
		/** File cannot be stored as a single database file because
		 * it exceeds the file size limit of 1048576 bytes.
         *
         * To get this to work use 'split db -b 1048576 split_db_' to split
         * the database into 1048576 byte chunks named split_db_aa, split_db_ab, etc.
         * Then the first time the program launches these chunks will be merged into
         * a single database file in the system folder.
		 * */
		InputStream dbFile1 = mContext.getAssets().open("split_db_aa");
		InputStream dbFile2 = mContext.getAssets().open("split_db_ab");

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = dbFile1.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}
		while ((length = dbFile2.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		dbFile1.close();
		dbFile2.close();
	}

	public void openDatabase() throws SQLException{
		//Open the database
		String myPath = DB_PATH + DB_NAME;
		mDb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

	}

	@Override
	public synchronized void close() {
		if(mDb != null) mDb.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	/**
	 * Return a Cursor over the list of all songs in the database
	 *
	 * @return Cursor over all songs
	 */

	public Cursor fetchAllSongs(boolean siioninLaulu, boolean virsi, boolean shz) {
		return mDb.query(DB_TABLE, new String[] {KEY_ROWID, KEY_NUMBER,
				KEY_LYRICS}, getTypeWhereClause(siioninLaulu, virsi, shz), null, null, null, null);
	}

	public Cursor fetchSongsByNumber(int songNum, boolean siioninLaulu, boolean virsi, boolean shz){
		Cursor result;
		try{
			Log.d(TAG, "db query with songNum = " + songNum);

			result = mDb.query(DB_TABLE, new String[] {KEY_ROWID, KEY_TYPE, KEY_NUMBER, KEY_LYRICS, KEY_INFO},
					KEY_NUMBER + "=" + songNum + " AND (" + getTypeWhereClause(siioninLaulu, virsi, shz) + ")", null, null, null, null);
		} catch (SQLiteException e){
			Log.d(TAG, e.toString());
			result = null;
		}
		return result;
	}

	public Cursor fetchSongsByString(String str, boolean siioninLaulu, boolean virsi, boolean shz){
		Cursor result;
		try{
			result = mDb.query(DB_TABLE, new String[] {KEY_ROWID, KEY_TYPE, KEY_NUMBER, KEY_LYRICS, KEY_INFO},
					"(" + KEY_LYRICS + " LIKE '%" + str + "%'" +
					"OR " + KEY_INFO + " LIKE '%" + str + "%'" + ") AND " + getTypeWhereClause(siioninLaulu, virsi, shz), null, null, null, null);
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
