package com.usr.thermostat;

import java.util.LinkedList;
import java.util.List;

import com.usr.thermostat.autolink.AutoLinkActivity;
import com.usr.thermostat.beans.RoomItemInfo;
import com.usr.thermostat.db.RoomDB;
import com.usr.thermostat.network.NetManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class RoomManagerActivity extends Activity {

	private GridView gv_rooms;
	private List<RoomItemInfo> data = new LinkedList<RoomItemInfo>();
	private RoomListAdapter adapter;
	private ImageView iv_autolink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room_manager_layout);
		
		gv_rooms = (GridView) findViewById(R.id.room_gv);
//		adapter = new RoomListAdapter(getApplicationContext(), data);
//		gv_rooms.setAdapter(adapter);
		gv_rooms.setSelector(new ColorDrawable(Color.TRANSPARENT));
		
		iv_autolink = (ImageView) findViewById(R.id.room_auto_link_iv);
		iv_autolink.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent _intent = new Intent(RoomManagerActivity.this,AutoLinkActivity.class);
				startActivity(_intent);
			}
		});
		
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
		NetManager.instance().init(this);
		Operations.GetOperation().setHandler(mainHanlder);
		
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
	
	private Handler mainHanlder = new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
//			super.handleMessage(msg);
			if (msg.what == Constant.SOCKET_ERROR){
				Toast.makeText(RoomManagerActivity.this, "connect failed !", Toast.LENGTH_SHORT).show();
			}
			if (msg.what == Constant.SOCKET_OK)
			{
				
				NetManager.instance().release();
//				//跳转到mainactivity前的准备
//				CommonUtils.readyIntentMain(int_registID);
				
				Intent _intent = new Intent(RoomManagerActivity.this, MainActivity.class);
//				_intent.putExtra("id", list.get(pos).getId());
//				_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				startActivity(_intent);
				
			}
		}
		
	};

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		
		super.onStop();
	}
	
	

}
