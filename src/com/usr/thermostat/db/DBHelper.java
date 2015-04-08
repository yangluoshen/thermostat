package com.usr.thermostat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static int version = 1;
	public  static String dbname = "usr.db";
	public  static final String IDRECORD_TABLE = "idrecord";

	public DBHelper(Context context) {
		super(context, dbname, null, version);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "create table if not exists idrecord (id INTEGER PRIMARY KEY AUTOINCREMENT, record String, lastlogin bit)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
