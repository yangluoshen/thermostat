package com.usr.thermostat.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class IdRecord {
	private DBHelper dbhelper = null;
	public static final int COLUMN_ID = 0;
	public static final int COLUMN_RECORD = 1;
	public static final int COLUMN_LASTLOGIN = 2;
	
	//sql = "create table if not exists userinfo (id INTEGER PRIMARY KEY AUTOINCREMENT ,
	//email varchar(50),nickname varchar(30), lastlogin bit)";
	public IdRecord(Context context){
		dbhelper = new DBHelper(context);
	}
	
	/**
	 * @param params
	 * @return
	 */
	public boolean addRecord(Object[] params){
		boolean flag = false;
		SQLiteDatabase database = null;
		try{
			String sql = "insert into "+ DBHelper.IDRECORD_TABLE+" (record,lastlogin) values(?,?)";
			database  = dbhelper.getWritableDatabase();
			database.execSQL(sql,params);
			flag = true;
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			if (database != null){
				database.close();
			}
		}
		return flag;
	}
	/**
	 * execute sql = "update userinfo set nickname = ?,state = ? where email = ?";
	 * params nickname , state , email
	 * @param params
	 * @return
	 */
//	public boolean updateRecord(Object[] params){
//		boolean flag = false;
//		SQLiteDatabase database = null;
//		try{
//			String sql = "update idrecord set nickname = ?,lastlogin = ? where email = ?";
//			database = dbhelper.getWritableDatabase();
//			database.execSQL(sql,params);
//			flag = true;
//		}catch(Exception e){
//			
//		}finally{
//			if (database != null){
//				database.close();
//			}
//		}
//		
//		return flag;
//		
//	}
	/**
	 * sql = "delete from userinfo where email = ?";
	 * @param params
	 * @return
	 */
//	public boolean deleteRecord(Object[] params){
//		boolean  flag = false;
//		SQLiteDatabase database = null;
//		try{
//			String sql = "delete from userinfo where email = ?";
//			database = dbhelper.getWritableDatabase();
//			database.execSQL(sql,params);
//			flag = true;
//		}catch (Exception e){
//			
//		}finally{
//			if (database != null){
//				database.close();
//			}
//		}
//		
//		return flag;
//	}
	
	public boolean IsRecordTableEmpty(){
		boolean flag= true;
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from "+DBHelper.IDRECORD_TABLE, null);
		if (cursor.getCount()>0){
			flag = false;
		}
		db.close();
		return flag;
		
	}
	
	public String GetLastLoginRecord(){
//		Map<String,String> map = new HashMap<String,String>();
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from "+DBHelper.IDRECORD_TABLE+" where lastlogin=1", null);
		if (cursor.getCount() < 1){
			db.close();
			return null;
		}else{
			cursor.moveToFirst();
			String email_name = cursor.getColumnName(COLUMN_RECORD);
			String email_value = cursor.getString(cursor.getColumnIndex(email_name));
			db.close();
			return email_value;
		}
		
	}
	
	public void setUserLastLogin(Object[] params){
		
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		String sql = null;
		
		//if the table is empty now
		Cursor cursor = db.rawQuery("select * from "+DBHelper.IDRECORD_TABLE, null);
		if (cursor.getCount() < 1){
			Object[] obj = {(Object) params[0],1};
			this.addRecord(obj);
			db.close();
			return;
		}
		//if the given user is not in table
		String[] str_obj = {(String) params[0]};
		cursor = db.rawQuery("select * from "+DBHelper.IDRECORD_TABLE+" where record=?", str_obj);
		if (cursor.getCount() < 1){
			sql = "update "+DBHelper.IDRECORD_TABLE+" set lastlogin=0 ";
			db.execSQL(sql);
//			sql = "update userinfo set lastlogin=1 where email=?";
//			db.execSQL(sql,params);
			db.close();
			
			Object[] obj = {(Object) params[0],1};
			this.addRecord(obj);
			return;
		}
		// if the given user is already in table
		sql = "update "+DBHelper.IDRECORD_TABLE+" set lastlogin=0 ";
		db.execSQL(sql);
		sql = "update "+DBHelper.IDRECORD_TABLE+" set lastlogin=1 where record=?";
		db.execSQL(sql,params);
		db.close();
		return;
	
	}
	
}
