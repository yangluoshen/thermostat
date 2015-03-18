package com.usr.thermostat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class ConnectActivity extends Activity {
	EditText et_ip;
	EditText et_port;
	ImageButton ibtn_connect;
	Operations operation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_layout);
		operation = Operations.GetOperation();
		
		initView();
		addEvent();
		
	}

	private void addEvent() {
		// TODO Auto-generated method stub
		
		ibtn_connect.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (operation.Connect(et_ip.getText().toString(), et_port.getText().toString())){
				Intent _intent  = new Intent(ConnectActivity.this,MainActivity.class);
				ConnectActivity.this.startActivity(_intent);
			}else {
				Toast.makeText(ConnectActivity.this, "connect failed !", Toast.LENGTH_SHORT).show();
			}
			
//			v.setClickable(false);
		}
	});
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		et_ip = (EditText) findViewById(R.id.et_ip);
		et_port = (EditText) findViewById(R.id.et_port);
		ibtn_connect = (ImageButton) findViewById(R.id.btn_connect);
	}
	

}
