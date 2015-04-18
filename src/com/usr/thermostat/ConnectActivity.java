package com.usr.thermostat;

import com.usr.thermostat.db.IdRecord;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class ConnectActivity extends Activity {
//	EditText et_ip;
//	EditText et_port;
	EditText et_registID;
	ImageView ibtn_connect;
	Operations operation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_new_version);
		
		operation = Operations.GetOperation(this);
		initView();
		addEvent();
		
		IdRecord idrecord = new IdRecord(this);
		String currentRecord = null;
		if ((currentRecord = idrecord.GetLastLoginRecord()) != null){
			et_registID.setText(currentRecord);
			et_registID.setSelection(currentRecord.length());
		}
	}

	private void addEvent() {
		// TODO Auto-generated method stub
		
		ibtn_connect.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			ibtn_connect.setClickable(false);
//			byte[] initState = null;
			
			if ("".equals(et_registID.getText().toString())){
				Toast.makeText(ConnectActivity.this, "ID should not be empty", Toast.LENGTH_SHORT).show();
			}
			else if (operation.Connect(calcRegistID(et_registID.getText().toString())) ){
				
				IdRecord idrecord = new IdRecord(ConnectActivity.this);
				Object[] param = {et_registID.getText().toString()};
				idrecord.setUserLastLogin(param);
				
				Intent _intent  = new Intent(ConnectActivity.this,MainActivity.class);
//				_intent.putExtra("initstate", initState);
				ConnectActivity.this.startActivity(_intent);
//				finish();
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
		ibtn_connect = (ImageView) findViewById(R.id.btn_connect);
		et_registID = (EditText) findViewById(R.id.et_registID);
	}
	private int calcRegistID(String strID)
	{
		int intID = Integer.valueOf(strID).intValue();
		return (intID - 65535)/255;
	}
	

}
