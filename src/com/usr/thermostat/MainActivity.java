package com.usr.thermostat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("HandlerLeak") public class MainActivity extends Activity  {
	ImageView iv_menu;
	ImageView iv_wind;
	ImageView[] windList = new ImageView[4];
	ImageView[] menuList = new ImageView[3];
	TextView tv_set;
	ImageView iv_mark;
	int currentMenu = 0;
	int currentWind = 0;
	double currentTemperature = 16.0;
	static final int countDownTime = 2;
	int countDown = countDownTime;
	double initTemperature = 20.5;
	
	TextView tv_temp ;
	ImageView iv_up;
	ImageView iv_down;
	
	Counter count;
	static final int TIMEUP = 0;
	static final int TIMEDOWN = 1;
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
//			super.handleMessage(msg);
			if (msg.what == TIMEUP){
				SetTemperature(currentTemperature);
				msg.what = TIMEDOWN;
				
				iv_mark.setVisibility(View.VISIBLE);
				tv_set.setVisibility(View.INVISIBLE);
			}
		}
		
	};
	
	
	Operations operation = new Operations();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		
		initViews();
		addEvents();
		count = new Counter();
		Thread t = new Thread(count);
		t.start();
		
		
	}

	private void addEvents() {
		// TODO Auto-generated method stub
		iv_wind.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (currentWind >= 3){
					windList[currentWind].setVisibility(View.INVISIBLE);
					currentWind = 0;
					windList[0].setVisibility(View.VISIBLE);
				}
				else{
					windList[currentWind].setVisibility(View.INVISIBLE);
					currentWind++;
					windList[currentWind].setVisibility(View.VISIBLE);
				}
				operation.WindClicked();
				
			}
		});
		iv_menu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i("yangluo","-->"+countDown);
				if (currentMenu >=2){
					menuList[currentMenu].setVisibility(View.INVISIBLE);
					currentMenu = 0;
					menuList[0].setVisibility(View.VISIBLE);
				}
				else{
					menuList[currentMenu].setVisibility(View.INVISIBLE);
					currentMenu++;
					menuList[currentMenu].setVisibility(View.VISIBLE);
				}
				
				operation.MenuClicked();
			}
		});
		iv_up.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				countDown = countDownTime;
				initTemperature += 0.5;
				tv_temp.setText(""+initTemperature);
				
				iv_mark.setVisibility(View.INVISIBLE);
				tv_set.setVisibility(View.VISIBLE);
				
				operation.UpTemperature();
				
			}
		});
		iv_down.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				countDown = countDownTime;
				initTemperature -= 0.5;
				tv_temp.setText(""+initTemperature);
				
				iv_mark.setVisibility(View.INVISIBLE);
				tv_set.setVisibility(View.VISIBLE);
				
				operation.DownTemperature();
			}
		});
		
	}

	private void initViews() {
		// TODO Auto-generated method stub
		iv_menu = (ImageView) findViewById(R.id.iv_menu);
		iv_wind = (ImageView) findViewById(R.id.iv_time);
		windList[0] = (ImageView) findViewById(R.id.iv_wind1);
		windList[1] = (ImageView) findViewById(R.id.iv_wind2);
		windList[2] = (ImageView) findViewById(R.id.iv_wind3);
		windList[3] = (ImageView) findViewById(R.id.iv_wind_auto);
		
		menuList[0] = (ImageView) findViewById(R.id.iv_cold);
		menuList[1] = (ImageView) findViewById(R.id.iv_warm);
		menuList[2] = (ImageView) findViewById(R.id.iv_ventilate);
		
		tv_temp = (TextView) findViewById(R.id.tv_temperature);
		iv_up   = (ImageView) findViewById(R.id.iv_up);
		iv_down = (ImageView) findViewById(R.id.iv_down);
		
		tv_set  = (TextView) findViewById(R.id.tv_set);
		iv_mark = (ImageView) findViewById(R.id.iv_mark);
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	class Counter implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (countDown >= 0){
				
				//Log.i("yangluo","->"+countDown);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				countDown--;
				if (countDown == 0){
					//SetTemperature(currentTemperature);
					Message msg = new Message();
					msg.what = TIMEUP;
					handler.sendMessage(msg);
					
					countDown = countDownTime;
					
				}
			}
			
		}
		
	}
	
	void  SetTemperature(double currentTemperature2){
		tv_temp.setText(""+currentTemperature);
	}

}
