package com.usr.thermostat;

import com.usr.thermostat.Utils.CalculationUtils;
import com.usr.thermostat.beans.RoomItemInfo;
import com.usr.thermostat.db.RoomDB;
import com.usr.thermostat.network.NetManager;
import com.usr.thermostat.network.SocketThreadManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class ConnectActivity extends Activity {
//	EditText et_ip;
//	EditText et_port;
	EditText et_registID;
	Button btn_connect;
	Operations operation;
	private EditText et_roomName;
	
	private String mode;
	private RoomItemInfo roomItemInfo ;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_new_version);
		
		context = getApplicationContext();
		initView();
		addEvent();
		
		Bundle bundle = getIntent().getExtras();
		mode = bundle.getString("mode");
		if (mode.equals(Constant.INTENT_MODE_MODIFY))
		{
			roomItemInfo = new RoomItemInfo();
			roomItemInfo.setId(bundle.getInt("id"));
			roomItemInfo.setName(bundle.getString("room"));
			roomItemInfo.setRegistid(bundle.getString("registid"));
			et_registID.setText(roomItemInfo.getRegistid());
			et_roomName.setText(roomItemInfo.getName());
			btn_connect.setText("OK");
		}
		else 
		{
			roomItemInfo = null;
			btn_connect.setText("GO");
		}
		
//		RoomDB idrecord = new RoomDB(this);
//		String currentRecord = null;
//		if ((currentRecord = idrecord.GetLastLoginRecord()) != null){
//			et_registID.setText(currentRecord);
//			et_registID.setSelection(currentRecord.length());
//		}
	}

	private void addEvent() {
		// TODO Auto-generated method stub
		
		btn_connect.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			ibtn_connect.setClickable(false);
//			byte[] initState = null;
			String roomName = et_roomName.getText().toString();
			String registID = et_registID.getText().toString();
			
			NetManager.instance().init(ConnectActivity.this);
			
			if ("".equals(roomName))
			{
				Toast.makeText(ConnectActivity.this, "Room name should not be empty", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if ("".equals(registID)){
				Toast.makeText(ConnectActivity.this, "ID should not be empty", Toast.LENGTH_SHORT).show();
			}
			else if (operation.Connect(CalculationUtils.calcRegistID(et_registID.getText().toString())) ){
				NetManager.instance().release();
//				RoomDB idrecord = new RoomDB(ConnectActivity.this);
//				Object[] param = {et_registID.getText().toString()};
//				idrecord.setUserLastLogin(param);
				
				if (mode.equals(Constant.INTENT_MODE_ADD))
				{
					RoomDB roomdb = new RoomDB(context);
					Object[] params = {roomName,registID};
					roomdb.addRecord(params);
				}
				else
				{
					RoomDB roomdb = new RoomDB(context);
					Object[] params = {roomName, registID, roomItemInfo.getId()};
					roomdb.updateRecord(params);
				}
				
				
				Intent _intent  = new Intent(ConnectActivity.this,MainActivity.class);
				
				ConnectActivity.this.startActivity(_intent);
				
				
				finish();
			}else {
				Toast.makeText(ConnectActivity.this, "connect failed !", Toast.LENGTH_SHORT).show();
			}
//			ibtn_connect.setClickable(true);
			
//			v.setClickable(false);
		}
	});
		
	}

	private void initView() {
		// TODO Auto-generated method stub
//		et_ip = (EditText) findViewById(R.id.et_ip);
//		et_port = (EditText) findViewById(R.id.et_port);
		btn_connect = (Button) findViewById(R.id.btn_connect);
		et_registID = (EditText) findViewById(R.id.et_registID);
		et_roomName = (EditText) findViewById(R.id.et_roomname);
	}
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		
		NetManager.instance().init(this);
//		SocketThreadManager.sharedInstance().releaseInstance();
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		
		super.onStart();
		operation = Operations.GetOperation();
	}
	
	
	
	

}
