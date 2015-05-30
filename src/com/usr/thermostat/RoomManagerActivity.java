package com.usr.thermostat;

import java.util.LinkedList;
import java.util.List;

import com.usr.thermostat.beans.RoomItemInfo;
import com.usr.thermostat.db.RoomDB;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.GridView;

public class RoomManagerActivity extends Activity {

	private GridView gv_rooms;
	private List<RoomItemInfo> data = new LinkedList<RoomItemInfo>();
	private RoomListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room_manager_layout);
		
		gv_rooms = (GridView) findViewById(R.id.room_gv);
//		adapter = new RoomListAdapter(getApplicationContext(), data);
//		gv_rooms.setAdapter(adapter);
		gv_rooms.setSelector(new ColorDrawable(Color.TRANSPARENT));
		
		//将所有房间状态设置为未操作
//		new RoomDB(getApplicationContext()).setAllNonOperated();
		
	}
	public void setData()
	{
		RoomDB roomdb = new RoomDB(this);
		data.clear();
		data.addAll(roomdb.getAllRecord());
		RoomItemInfo item = new RoomItemInfo();
		data.add(item);
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		data.clear();
		adapter = new RoomListAdapter(getApplicationContext(), data);
		gv_rooms.setAdapter(adapter);
		
		setData();
		
		super.onResume();
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
	

}
