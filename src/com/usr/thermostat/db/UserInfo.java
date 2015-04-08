package com.usr.thermostat.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserInfo {
	private DBHelper dbhelper = null;
	
	//sql = "create table if not exists userinfo (id INTEGER PRIMARY KEY AUTOINCREMENT ,
	//email varchar(50),nickname varchar(30), lastlogin bit)";
	public UserInfo(Context context){
		dbhelper = new DBHelper(context);
	}
	
	/**
	 * 
	 * params email, nickname
	 * @param params
	 * @return
	 */
	public boolean addRecord(Object[] params){
		boolean flag = false;
		SQLiteDatabase database = null;
		try{
			String sql = "insert into idrecord(record) values(?)";
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
	
	public boolean IsUserTableEmpty(){
		boolean flag= true;
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from userinfo", null);
		if (cursor.getCount()>0){
			flag = false;
		}
		db.close();
		return flag;
		
	}
	
}
