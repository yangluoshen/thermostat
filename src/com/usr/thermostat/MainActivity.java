package com.usr.thermostat;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("HandlerLeak") public class MainActivity extends Activity  {
	
	ImageView iv_menu;
	ImageView iv_wind;
	ImageView iv_up;
	ImageView iv_down;
	ImageView iv_close;
	
	ImageView[] windList = new ImageView[4];
	ImageView[] menuList = new ImageView[3];
	ImageView iv_mark;       // image that mark the temperature
	ImageView iv_degree;     // image "C"
	TextView tv_set;         //the text "set"
	TextView tv_time;        //text time
	TextView tv_temp ;       // text temprature
	TextView tv_dayofweek;   // text day of week
	TextView tv_week;        //the text "week"
//	TextView tv_divide;      //冒号
	
//	
//	EditText et_ip ;
//	EditText et_port;
//	Button btn_connect;
	
	LinearLayout content_layout;
	
	int currentMenu = 0;
	int currentWind = 3;
	double currentTemperature = 16.0;
	static final int countDownTime = 2;
	int countDown = countDownTime;
	double initTemperature = 22.0;
	int currentSwitchState = SWITCHON;
	
	
	int dayOfWeek;
	int hour;
	int minute;
	
	Counter count;
	Timer timer;
	static final int TIMEUP = 0;
	static final int TIMEDOWN = 1;
	static final int TIMECHANGED = 2;
	static final int WEEKDAYCHANGED =3;
	static final int SWITCHOFF = 0;
	static final int SWITCHON = 1;
	
	Operations operation;
	
	Time time = new Time();
	
	Handler CounterHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
//			super.handleMessage(msg);
			if (msg.what == TIMEUP && currentSwitchState == SWITCHON){
				SetTemperature(currentTemperature);
				msg.what = TIMEDOWN;
				
				iv_mark.setVisibility(View.VISIBLE);
				tv_set.setVisibility(View.INVISIBLE);
			}
		}
		
	};
	Handler TimerHandler = new Handler(){
		boolean flag = true;

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//super.handleMessage(msg);
			
			if (msg.what == TIMECHANGED){
				String str_minute;
				if (minute<10){
					str_minute = "0"+minute;
				}else {
					str_minute  = ""+minute;
				}
				if (flag){
					tv_time.setText(""+hour+":"+str_minute);
					flag = false;
				}else{
					tv_time.setText(""+hour+" "+str_minute);
					flag = true;
				}
				
			}
		    if (msg.what == WEEKDAYCHANGED){
				tv_dayofweek.setText(""+dayOfWeek);
			}
		}
		
	};

	Handler socketHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//super.handleMessage(msg);
			if (msg.what == 1){
				currentTemperature = (double)Integer.valueOf((String) msg.obj).intValue();
				tv_temp.setText((String)msg.obj);
				Log.i("yangluo","socketHandle "+msg.obj);
			}
			
		}
		
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		

		
		initViews();
		addEvents();
		
		operation = Operations.GetOperation();
		operation.setHandler(socketHandler);
		
		count = new Counter();
		Thread countThread = new Thread(count);
		countThread.start();
		
		timer = new Timer();
		Thread timeThread = new Thread(timer);
		timeThread.start();
		
		
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
				operation.sendWindData(currentWind);
//				operation.WindClicked();
			}
		});
		iv_menu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//Log.i("yangluo","-->"+countDown);
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
				operation.sendMenuData(currentMenu);
//				operation.MenuClicked();
			}
		});
		iv_up.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				countDown = countDownTime;
				initTemperature += 0.5;
				if (initTemperature >= 30.0){
					initTemperature = 30.0;
				}
				tv_temp.setText(""+initTemperature);
				
				iv_mark.setVisibility(View.INVISIBLE);
				tv_set.setVisibility(View.VISIBLE);
				
				operation.sendUpTemperature(initTemperature);
//				operation.UpTemperature();
				
			}
		});
		iv_down.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				countDown = countDownTime;
				initTemperature -= 0.5;
				if (initTemperature <= 10.0){
					initTemperature = 10.0;
				}
				tv_temp.setText(""+initTemperature);
				
				iv_mark.setVisibility(View.INVISIBLE);
				tv_set.setVisibility(View.VISIBLE);
				
				operation.sendDownTemprature(initTemperature);
//				operation.DownTemperature();
			}
		});
		
//		btn_connect.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				operation.Connect(et_ip.getText().toString(), et_port.getText().toString());
//				//判断连接是否成功
////				v.setClickable(false);
//			}
//		});
		iv_close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				operation.sendCloseSignal(currentSwitchState);
				if (currentSwitchState == SWITCHON){
					currentSwitchState = SWITCHOFF;
					turnDownDevice();
				}
				else{
					currentSwitchState = SWITCHON;
					turnOnDevice();
				}
			}
		});
		
	}
	void turnDownDevice(){

		windList[currentWind].setVisibility(View.INVISIBLE);
		menuList[currentMenu].setVisibility(View.INVISIBLE);
		tv_temp.setVisibility(View.INVISIBLE);
		iv_mark.setVisibility(View.INVISIBLE);
		iv_degree.setVisibility(View.INVISIBLE);
		tv_set.setVisibility(View.INVISIBLE);
		tv_time.setVisibility(View.INVISIBLE);
		
		tv_week.setVisibility(View.INVISIBLE);
		tv_dayofweek.setVisibility(View.INVISIBLE);
		
//		et_ip.setVisibility(View.INVISIBLE);
//		et_port.setVisibility(View.INVISIBLE);
//		btn_connect.setVisibility(View.INVISIBLE);
//		tv_divide.setVisibility(View.INVISIBLE);
		
		iv_menu.setClickable(false);
		iv_wind.setClickable(false);
		iv_up.setClickable(false);
		iv_down.setClickable(false);
		content_layout.setBackgroundColor(getResources().getColor(R.color.lightgray));
		

		
	}
	void turnOnDevice(){

		windList[currentWind].setVisibility(View.VISIBLE);
		menuList[currentMenu].setVisibility(View.VISIBLE);
		tv_temp.setVisibility(View.VISIBLE);
		iv_mark.setVisibility(View.VISIBLE);
		iv_degree.setVisibility(View.VISIBLE);
//		tv_set.setVisibility(View.VISIBLE);
		tv_time.setVisibility(View.VISIBLE);
		
		tv_week.setVisibility(View.VISIBLE);
		tv_dayofweek.setVisibility(View.VISIBLE);
		
		
		iv_menu.setClickable(true);
		iv_wind.setClickable(true);
		iv_up.setClickable(true);
		iv_down.setClickable(true);
		content_layout.setBackgroundColor(getResources().getColor(R.color.lightblue));
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
		iv_close = (ImageView) findViewById(R.id.iv_close);
		
		tv_set  = (TextView) findViewById(R.id.tv_set);
		tv_week = (TextView) findViewById(R.id.tv_week);
		iv_mark = (ImageView) findViewById(R.id.iv_mark);
		iv_degree = (ImageView) findViewById(R.id.iv_degree);
		tv_time = (TextView) findViewById(R.id.tv_timeNow);
		tv_dayofweek = (TextView) findViewById(R.id.tv_dayOfWeek);
		time.setToNow();
		dayOfWeek = time.weekDay;
		tv_dayofweek.setText(""+time.weekDay);
		minute = time.minute;
		hour = time.hour;
		//set current time
		String str_minute;
		if (minute<10){
			str_minute = "0"+minute;
		}else {
			str_minute  = ""+minute;
		}
		tv_time.setText(""+hour+":"+str_minute);

		
		content_layout = (LinearLayout) findViewById(R.id.content_layout);
		
		
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
					CounterHandler.sendMessage(msg);
					
					countDown = countDownTime;
					
				}
			}
		}
	}
	class Timer implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true){
				time.setToNow();
				//dayOfWeek = time.weekDay;
				hour = time.hour;
				minute = time.minute;
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Message msg = Message.obtain();
				if (dayOfWeek != time.weekDay){
					msg.what = WEEKDAYCHANGED;
					dayOfWeek = time.weekDay;
					TimerHandler.sendMessage(msg);
				}
				msg.what = TIMECHANGED;
				TimerHandler.sendMessage(msg);
				
				
			}
		}
		
	}
	
	
	void  SetTemperature(double currentTemperature2){
		tv_temp.setText(""+currentTemperature);
	}

}
