package com.usr.thermostat.db;

import java.util.LinkedList;
import java.util.List;

import com.usr.thermostat.beans.RoomItemInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RoomDB {
	private DBHelper dbhelper = null;
	public static final int COLUMN_ID = 0;
	public static final int COLUMN_RECORD = 1;
	public static final int COLUMN_LASTLOGIN = 2;
	
	//(id, registid, roomname, wind, mode, settemp, isoperated)
	public RoomDB(Context context){
		dbhelper = new DBHelper(context);
	}
	
	/**
	 * @param params {registid,roomname}
	 * @return id
	 */
	public int addRecord(Object[] params){
		SQLiteDatabase database = null;
		String sql = "";
		int id = 0;
		try{
			sql = "insert into "+ DBHelper.ROOMTABLE + " (registid,roomname,isoperated) values(?,?,0)";
			database  = dbhelper.getWritableDatabase();
			database.execSQL(sql,params);
			
			sql = "SELECT id FROM " + DBHelper.ROOMTABLE + " ORDER BY id DESC ";
			Cursor cursor = database.rawQuery(sql, null);
			cursor.moveToFirst();
			id = cursor.getInt(cursor.getColumnIndex("id"));
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			if (database != null){
				database.close();
			}
		}
		
		return id;
		
	}
	
	public List<RoomItemInfo> getAllRecord()
	{
		List<RoomItemInfo> list = new LinkedList<RoomItemInfo>();
		String sql = "SELECT * from " + DBHelper.ROOMTABLE;
		SQLiteDatabase database = null;
		
		try
		{
			database = dbhelper.getWritableDatabase();
			Cursor cursor = database.rawQuery(sql, null);
			int columns = cursor.getColumnCount();
			
			while(cursor.moveToNext())
			{
				RoomItemInfo item = new RoomItemInfo();
				
				int id_value = cursor.getInt(cursor.getColumnIndex("id"));
				String registid_value = cursor.getString(cursor.getColumnIndex("registid"));
				String roomname_value = cursor.getString(cursor.getColumnIndex("roomname"));
				int wind_value = cursor.getInt(cursor.getColumnIndex("wind"));
				int mode_value = cursor.getInt(cursor.getColumnIndex("mode"));
				String settmp_value = cursor.getString(cursor.getColumnIndex("settemp"));
				int isoperated_value = cursor.getInt(cursor.getColumnIndex("isoperated"));
				
				item.setId(id_value);
				item.setRegistid(registid_value);
				item.setName(roomname_value);
				item.setWind(wind_value);
				item.setMode(mode_value);
				item.setSettemp(settmp_value);
				item.setIsoperated(isoperated_value);
				
				list.add(item);
				
				
			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally
		{
			if (database != null)
			{
				database.close();
			}
		}
		
		
		return list;
	}
	
	public String findRoomByID (int id)
	{
		String str_id = "" + id;
		
		
		String sql = "SELECT registid from " + DBHelper.ROOMTABLE + " where id = ?";
		
		SQLiteDatabase database = null;
		database = dbhelper.getWritableDatabase();
		
		Cursor cursor = database.rawQuery(sql, new String[]{str_id});
		
		if (cursor.getCount() < 1)
		{
			return null;
		}
		cursor.moveToFirst();
		String value = cursor.getString(0);
		
		return value;
	}
	
	
	/**
	 * execute sql = "update roomtable set registid = ?,roomname = ? where id = ?";
	 * params registid , roomname , id
	 * @param params
	 * @return
	 */
	public boolean updateRoomInfo(Object[] params){
		boolean flag = false;
		SQLiteDatabase database = null;
		
		ContentValues cv = new ContentValues();
		
		cv.put("registid", (String)params[0]);
		cv.put("roomname", (String)params[1]);
		try
		{
			database = dbhelper.getWritableDatabase();
			database.update(DBHelper.ROOMTABLE, cv, "id = ?", new String[]{Integer.toString((Integer)params[2])});
			flag = true;
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally
		{
			if (database != null)
			{
				database.close();
			}
		}
		
		return flag;
		
	}
	
	/**
	 * execute sql = "update roomtable set wind = ?,set mode = ?, set settemp = ? where id = ?";
	 * params wind , mode , settemp, id
	 * @param params
	 * @return
	 */
	public boolean updateStateInfo(Object[] params){
		boolean flag = false;
		SQLiteDatabase database = null;
		
		ContentValues cv = new ContentValues();
		
		cv.put("wind", (Integer)params[0]);
		cv.put("mode", (Integer)params[1]);
		cv.put("settemp", (String)params[2]);
		try
		{
			database = dbhelper.getWritableDatabase();
			database.update(DBHelper.ROOMTABLE, cv, "id = ?", new String[]{Integer.toString((Integer)params[3])});
			flag = true;
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally
		{
			if (database != null)
			{
				database.close();
			}
		}
		
		return flag;
		
	}
	
	public boolean setRecordOperated(int isoperated, int id)
	{
		boolean flag = false;
		
		SQLiteDatabase database = null;
		
		ContentValues cv = new ContentValues();
		
		cv.put("isoperated", isoperated);
		try
		{
			database = dbhelper.getWritableDatabase();
			database.update(DBHelper.ROOMTABLE, cv, "id = ?", new String[]{Integer.toString(id)});
			flag = true;
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally
		{
			if (database != null)
			{
				database.close();
			}
		}
		
		return flag;
	}
	
	public boolean setAllNonOperated()
	{
		boolean flag = false;
		
		SQLiteDatabase database = null;
		
		ContentValues cv = new ContentValues();
		
		cv.put("isoperated", 0);
		try
		{
			database = dbhelper.getWritableDatabase();
			database.update(DBHelper.ROOMTABLE, cv, null,null);
			flag = true;
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally
		{
			if (database != null)
			{
				database.close();
			}
		}
		
		return flag;
	}
	
	/**
	 * sql = "delete from roomtable where id = ?";
	 * @param params
	 * @return
	 */
	public boolean deleteRecord(Object[] params){
		boolean  flag = false;
		SQLiteDatabase database = null;
		try{
			String sql = "delete from "+ DBHelper.ROOMTABLE + " where id  = ?";
			database = dbhelper.getWritableDatabase();
			database.execSQL(sql,params);
			flag = true;
		}catch (Exception e){
			
		}finally{
			if (database != null){
				database.close();
			}
		}
		
		return flag;
	}
	
	public boolean IsRecordTableEmpty(){
		boolean flag= true;
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from "+DBHelper.ROOMTABLE, null);
		if (cursor.getCount()>0){
			flag = false;
		}
		db.close();
		return flag;
		
	}
	
//	public String GetLastLoginRecord(){
////		Map<String,String> map = new HashMap<String,String>();
//		SQLiteDatabase db = dbhelper.getWritableDatabase();
//		Cursor cursor = db.rawQuery("select * from "+DBHelper.IDRECORD_TABLE+" where lastlogin=1", null);
//		if (cursor.getCount() < 1){
//			db.close();
//			return null;
//		}else{
//			cursor.moveToFirst();
//			String email_name = cursor.getColumnName(COLUMN_RECORD);
//			String email_value = cursor.getString(cursor.getColumnIndex(email_name));
//			db.close();
//			return email_value;
//		}
//		
//	}
	
//	public void setUserLastLogin(Object[] params){
//		
//		SQLiteDatabase db = dbhelper.getWritableDatabase();
//		String sql = null;
//		
//		//if the table is empty now
//		Cursor cursor = db.rawQuery("select * from "+DBHelper.IDRECORD_TABLE, null);
//		if (cursor.getCount() < 1){
//			Object[] obj = {(Object) params[0],1};
//			this.addRecord(obj);
//			db.close();
//			return;
//		}
//		//if the given user is not in table
//		String[] str_obj = {(String) params[0]};
//		cursor = db.rawQuery("select * from "+DBHelper.IDRECORD_TABLE+" where record=?", str_obj);
//		if (cursor.getCount() < 1){
//			sql = "update "+DBHelper.IDRECORD_TABLE+" set lastlogin=0 ";
//			db.execSQL(sql);
////			sql = "update userinfo set lastlogin=1 where email=?";
////			db.execSQL(sql,params);
//			db.close();
//			
//			Object[] obj = {(Object) params[0],1};
//			this.addRecord(obj);
//			return;
//		}
//		// if the given user is already in table
//		sql = "update "+DBHelper.IDRECORD_TABLE+" set lastlogin=0 ";
//		db.execSQL(sql);
//		sql = "update "+DBHelper.IDRECORD_TABLE+" set lastlogin=1 where record=?";
//		db.execSQL(sql,params);
//		db.close();
//		return;
//	
//	}
	
}
