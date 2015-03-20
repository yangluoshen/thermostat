package com.usr.thermostat;


import java.io.IOException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
	static TextView tv_temp ;       // text temprature
	TextView tv_dayofweek;   // text day of week
	TextView tv_week;        //the text "week"
//	TextView tv_divide;      //Ã°ºÅ
	
//	
//	EditText et_ip ;
//	EditText et_port;
//	Button btn_connect;
	
	LinearLayout content_layout;
	
	int currentMenu = 0;
	int currentWind = 0;
	double currentTemperature = 0.0;
	static final int countDownTime = 3;
	int countDown = 0;
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
	
	Thread countThread;
	Thread timeThread;
	Thread recvThread;
	Thread getTemperatureRequest;
	boolean threadRun = true;
	
	Operations operation;
	
	Time time = new Time();
	
	Handler CounterHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
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

//	Handler socketHandler = new Handler(){
//
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
//			//super.handleMessage(msg);
//			if (msg.what == 1){
//				currentTemperature = (double)Double.valueOf((String) msg.obj).doubleValue();
//				tv_temp.setText((String)msg.obj);
//				Log.i("yangluo","socketHandle "+msg.obj);
////				Toast.makeText(MainActivity.this,"data is ", Toast.LENGTH_LONG).show();
//			}
//			
//		}
//		
//	};
	public StringBuffer Bytes2String(byte[] bytes){
		byte maskHigh = (byte) 0xf0;
		byte maskLow  = (byte) 0x0f;
		
		StringBuffer buf = new StringBuffer();
		buf.append("data above means: ");
		for (byte b : bytes){
			byte high, low;
			high = (byte) ((b & maskHigh)>>4);
			low  = (byte) ((b & maskLow));
			buf.append(findHex(high));
			buf.append(findHex(low));
			buf.append(" ");
		}
		
		return buf;
	}
	public  char findHex(byte b) {
		int t = new Byte(b).intValue();
		t = t < 0 ? t + 16 : t;
		if ((0 <= t) &&(t <= 9)) {
		return (char)(t + '0');
		}
		return (char)(t-10+'A');
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		

		
		initViews();
		addEvents();
		
		operation = Operations.GetOperation(this);
//		operation.setHandler(socketHandler);
		
		count = new Counter();
		countThread = new Thread(count);
		countThread.start();
		
		timer = new Timer();
		timeThread = new Thread(timer);
		timeThread.start();
		

		getTemperatureRequest = new Thread(mGetTemperatureRequest);
		getTemperatureRequest.start();
		
		recvThread = new Thread(mRecvThread);
		recvThread.start();
		
		
		
		
		
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

		iv_close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (currentSwitchState == SWITCHON){
					currentSwitchState = SWITCHOFF;
					operation.sendCloseSignal(currentSwitchState);
					turnDownDevice();
				}
				else{
					currentSwitchState = SWITCHON;
					operation.sendCloseSignal(currentSwitchState);
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
		windList[0] = (ImageView) findViewById(R.id.iv_wind_auto);
		windList[1] = (ImageView) findViewById(R.id.iv_wind1);
		windList[2] = (ImageView) findViewById(R.id.iv_wind2);
		windList[3] = (ImageView) findViewById(R.id.iv_wind3);
		
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
			while (threadRun){
				if (countDown == 0){
					//SetTemperature(currentTemperature);
					Message msg = new Message();
					msg.what = TIMEUP;
					CounterHandler.sendMessage(msg);
					
					countDown = countDownTime;
					
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				countDown--;
				
			}
		}
	}
	class Timer implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (threadRun){
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
	
	
	/**
	 * a thread that receive the message from server. 
	 * the message could only contents (double)temperature
	 * if necessary, you should convert the message to a double format (such as "22.5")
	 */
	private Runnable mRecvThread = new Runnable(){
		
		public void run(){
			Log.i("yangluo","in mRecvThread ");
//			String temperature;
			try {
				byte[] readBuffer = new byte[8];
				while ( threadRun){
//					Toast.makeText(Operations.this.context.getApplicationContext(),"1 data is ", Toast.LENGTH_LONG).show();
					if (operation.getmDataInputeStream().read(readBuffer) != -1){
//						mDataInputeStream.readFully(readBuffer);
//						Message msg = new Message();
						int int_temp =(int) readBuffer[6]; 
						double temp = (double) (int_temp*1.0/2.0);
						currentTemperature = temp;
//						String s  = Bytes2String(readBuffer).toString();
//						Toast.makeText(Operations.this.context,"data is ", Toast.LENGTH_LONG).show();
//						msg.obj = readBuffer;
						
//						msg.what = 1;
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						handler.sendMessage(msg);
						
//						Log.i("yangluo","mRecvThread "+(String) msg.obj);
					}
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("yangluo","mRecvThread failed ");
		}
	};
	
	private Runnable mGetTemperatureRequest = new Runnable(){
		
		public void run(){
			byte[] data = {(byte) 0xA0,0x10, 0x01, 0x00, 0x00, 0x00, 0x00,0x14};
			while (threadRun){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					operation.getmPrintWriter().write(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(4800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		}
	};


	
	
	void  SetTemperature(double temp){
		if (temp>30.0){
			temp = 30.0;
		}
		if (temp<0.0){
			temp = 0.0;
		}
		tv_temp.setText(""+temp);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == event.KEYCODE_BACK && event.getRepeatCount() == 0){
			try {
				operation.mSocket.close();
				operation.isConnected = false;
				threadRun = false;
				countThread.interrupt();
				timeThread.interrupt();
				getTemperatureRequest.interrupt();
				recvThread.interrupt();
				
				
//				finish();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	

}
