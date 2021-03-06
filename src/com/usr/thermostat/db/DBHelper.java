package com.usr.thermostat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static int version = 1;
	public  static String dbname = "usr.db";
	public  static final String ROOMTABLE = "roomtable";

	public DBHelper(Context context) {
		super(context, dbname, null, version);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//(id, registid, roomname, wind, mode, settemp, isoperated)
		String sql = "create table if not exists " + ROOMTABLE + " (id INTEGER PRIMARY KEY AUTOINCREMENT, registid varchar(20), " +
				"roomname varchar(40) , wind INTEGER, mode INTEGER, settemp varchar(20), isoperated bit);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
