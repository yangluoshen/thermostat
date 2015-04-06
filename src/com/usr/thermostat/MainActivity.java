package com.usr.thermostat;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity  {
	private int time_chip = 5000;
	private static final double MIN_INIT_TEMPERATURE = 0.0;
	private static final double MAX_INIT_TEMPERATURE = 30.0;
	private static final double MIN_CURRENT_TEMPERATURE = 0.0;
	private static final double MAX_CURRENT_TEMPERATURE = 50.0;
	
	private static final String FONT_DIGITAL_7 = "fonts" + File.separator + "digital.ttf";
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
//	TextView[] tv_dayofweek = new TextView[7];   // text day of week
	TextView tv_dayweek;
	int[] dayofweekID = {R.id.tv_monday,R.id.tv_tuesday,R.id.tv_wednesday,
			  			 R.id.tv_thursday,R.id.tv_friday,R.id.tv_saturday,R.id.tv_sunday};
	TextView tv_week;        //the text "week"
	Spinner spinner_num;
	SeekBar skb_temp;
	
	int mID1 = 1;
	
	private List<String> spinnerDataList = new ArrayList<String>();
	private ArrayAdapter<String> spinnerAdapter;

	
//	LinearLayout content_layout;
	
	int currentMenu = Operations.MENU_MODE_COLD;
	int currentWind = Operations.WIND_MODE_AUTO;
	double initTemperature = 22.0;
	double currentTemperature = 0.0;
	int currentSwitchState = SWITCHON;
	int currentSpinnerSelected = 1;
	
	int nextMenu = Operations.MENU_MODE_WARM;
	int nextWind = Operations.WIND_MODE_LOW;
	double nextInitTemperature = 22.5;
//	double nextCurrentTemperature = 0.0;
	int nextSwitchState = SWITCHOFF;
	
	int currentMenu_fork = currentMenu;
	int currentWind_fork = currentWind;
	double initTemperature_fork = initTemperature;
	double currentTemperature_fork = currentTemperature;
	int currentSwitchState_fork = currentSwitchState;
	int currentSpinnerSelected_fork = currentSpinnerSelected;
	
	static final int countDownTime = 3;
	int countDown = 0;
	int operationCountDownTime = 6;
	int operationCountDown = 0;
	
	
	
	
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
	
	static final int UPDATEALL = 4;
	static final int UPDATE_INIT_TEMPERATURE = 5;
	static final int UPDATE_CURRENT_TEMPERATURE = 6;
	
	Thread countThread;
	Thread timeThread;
	Thread recvThread;
	Thread getTemperatureRequest;
	boolean threadRun = true;
	boolean isSwitchDevice = false;
	boolean isOperated = false;
//	boolean isRecvResponse = true;
//	boolean isSetTime = false;
	
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
//				tv_dayofweek.setText(""+dayOfWeek);
			}
		}
		
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		addEvents();
		
		operation = Operations.GetOperation(this);
		
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
				isOperated = true;
				operationCountDown = operationCountDownTime;
				operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
//				if (isRecvResponse){
					if (currentWind_fork == Operations.WIND_MODE_AUTO){
						nextWind = Operations.WIND_MODE_LOW;
					}else if(currentWind_fork == Operations.WIND_MODE_LOW){
//						operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
						nextWind = Operations.WIND_MODE_MIDDLE;
//						operation.sendWindData(nextWind);
					}else if(currentWind_fork == Operations.WIND_MODE_MIDDLE){
//						operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
						nextWind = Operations.WIND_MODE_HIGH;
//						operation.sendWindData(nextWind);
					}else if (currentWind_fork == Operations.WIND_MODE_HIGH){
//						operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
						nextWind  = Operations.WIND_MODE_AUTO;
//						operation.sendWindData(nextWind);
					}
					
					windList[currentWind_fork].setVisibility(View.INVISIBLE);
//					currentWind++;
					windList[nextWind].setVisibility(View.VISIBLE);
					currentWind_fork = nextWind;
					operation.sendWindData(nextWind);
//			   }
					
//					if (currentWind == Operations.WIND_MODE_HIGH){
//						nextWind = Operations.WIND_MODE_AUTO;
////						windList[currentWind].setVisibility(View.INVISIBLE);
////						currentWind = 0;
////						windList[0].setVisibility(View.VISIBLE);
//					}
//					else{
//						nextWind = currentWind+1;
////						windList[currentWind].setVisibility(View.INVISIBLE);
////						currentWind++;
////						windList[currentWind].setVisibility(View.VISIBLE);
//					}
////					operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
//////					operation.sendWindData(currentWind);
////					operation.sendWindData(nextWind);
				
				
			}
		});
		iv_menu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				isOperated = true;
				operationCountDown = operationCountDownTime;
				operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
				if (currentMenu_fork == Operations.MENU_MODE_COLD){
					nextMenu = Operations.MENU_MODE_WARM;
				}else if (currentMenu_fork == Operations.MENU_MODE_WARM){
					nextMenu = Operations.MENU_MODE_VENTILATE;
				}else if (currentMenu_fork == Operations.MENU_MODE_VENTILATE){
					nextMenu = Operations.MENU_MODE_COLD;
				}
				menuList[currentMenu_fork].setVisibility(View.INVISIBLE);
				menuList[nextMenu].setVisibility(View.VISIBLE);
				operation.sendMenuData(nextMenu);
				currentMenu_fork = nextMenu;
				
				
////				if (isRecvResponse){
//					int nextMenu = 0;
//					if (currentMenu >=2){
//						nextMenu = 0;
////						menuList[currentMenu].setVisibility(View.INVISIBLE);
////						currentMenu = 0;
////						menuList[0].setVisibility(View.VISIBLE);
//					}
//					else{
//						nextMenu = currentMenu+1;
////						menuList[currentMenu].setVisibility(View.INVISIBLE);
////						currentMenu++;
////						menuList[currentMenu].setVisibility(View.VISIBLE);
//					}
//					operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
////					operation.sendMenuData(currentMenu);
//					operation.sendMenuData(nextMenu);
					
//				}
				
				
			}
		});
		iv_up.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				isOperated = true;
				operationCountDown = operationCountDownTime;
//				if (isRecvResponse){
					countDown = countDownTime;
//					isSetTime = true;
					nextInitTemperature = initTemperature_fork;
					
					nextInitTemperature += 0.5;
					if (nextInitTemperature >= MAX_INIT_TEMPERATURE){
						nextInitTemperature = MAX_INIT_TEMPERATURE;
					}
					initTemperature_fork = nextInitTemperature;
					tv_temp.setText(""+initTemperature_fork);
					iv_mark.setVisibility(View.INVISIBLE);
					tv_set.setVisibility(View.VISIBLE);
					
					operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
					operation.sendUpTemperature(nextInitTemperature);

					
//					initTemperature += 0.5;
//					if (initTemperature >= 30.0){
//						initTemperature = 30.0;
//					}
//					tv_temp.setText(""+initTemperature);
//					iv_mark.setVisibility(View.INVISIBLE);
//					tv_set.setVisibility(View.VISIBLE);
					
//					operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
//					operation.sendUpTemperature(initTemperature);
	
//				}
								
			}
		});
		iv_down.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				isOperated = true;
				operationCountDown = operationCountDownTime;
//				if (isRecvResponse){
					countDown = countDownTime;
//					isSetTime = true;
					nextInitTemperature = initTemperature_fork;
					
					nextInitTemperature -= 0.5;
					if (nextInitTemperature <= MIN_INIT_TEMPERATURE){
						nextInitTemperature = MIN_INIT_TEMPERATURE;
					}
					initTemperature_fork = nextInitTemperature;
					tv_temp.setText(""+initTemperature_fork);
					iv_mark.setVisibility(View.INVISIBLE);
					tv_set.setVisibility(View.VISIBLE);
					
					operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
					operation.sendDownTemprature(nextInitTemperature);
					
//					initTemperature -= 0.5;
//					if (initTemperature <= 10.0){
//						initTemperature = 10.0;
//					}
//					tv_temp.setText(""+initTemperature);
//					iv_mark.setVisibility(View.INVISIBLE);
//					tv_set.setVisibility(View.VISIBLE);
					
//					operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
//					operation.sendDownTemprature(initTemperature);	
//				}
				
			}
		});

		iv_close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if (isRecvResponse){
					isOperated = true;
					operationCountDown = operationCountDownTime;
					operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
					if (currentSwitchState_fork == SWITCHON){
						currentSwitchState_fork = SWITCHOFF;
						operation.sendCloseSignal(SWITCHOFF);
						turnDownDevice();
					}
					else{
						
						currentSwitchState_fork = SWITCHON;
						operation.sendCloseSignal(SWITCHON);
						turnOnDevice();
					}
//				}
					
				
			}
		});
		
		spinner_num.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
//				Toast.makeText(MainActivity.this, ""+spinnerAdapter.getItem(position), Toast.LENGTH_SHORT).show();
				isOperated = true;
				operationCountDown = operationCountDownTime;
				currentSpinnerSelected_fork = position;
				if (currentSpinnerSelected_fork == 0){
					mID1 = 0;
				}else{
					mID1 = 1;
				}
				
				tv_temp.setText("00.0");
				currentTemperature = 0.0;
				currentTemperature_fork = currentTemperature;
				isSwitchDevice = true;
				
				byte[] data = {(byte) 0xA0,(byte)currentSpinnerSelected_fork, (byte) mID1, 0x00, 0x00, 0x00, 0x00,0x00};
				Operations.CalcCheckSum(data);
				try {
					operation.getmPrintWriter().write(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		skb_temp.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			private double selectProgress=20;
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
//				if (isRecvResponse){
					countDown = countDownTime;
					isOperated = true;
					operationCountDown = operationCountDownTime;
//					isSetTime = true;
					
					operation.setDataPackgeID0AndID1(currentSpinnerSelected, mID1);
					operation.sendUpTemperature(selectProgress);


//				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				selectProgress = progress*1.0/2;
				tv_temp.setText(""+(double)progress/2);
				
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
		tv_dayweek.setVisibility(View.INVISIBLE);
		
		iv_menu.setClickable(false);
		iv_wind.setClickable(false);
		iv_up.setClickable(false);
		iv_down.setClickable(false);
//		content_layout.setBackgroundColor(getResources().getColor(R.color.lightgray));
		spinner_num.setVisibility(View.INVISIBLE);
		
		
	}
	void turnOnDevice(){

		windList[currentWind].setVisibility(View.VISIBLE);
		menuList[currentMenu].setVisibility(View.VISIBLE);
		tv_temp.setVisibility(View.VISIBLE);
//		iv_mark.setVisibility(View.VISIBLE);
		iv_degree.setVisibility(View.VISIBLE);
//		tv_set.setVisibility(View.VISIBLE);
		tv_time.setVisibility(View.VISIBLE);
		
		tv_week.setVisibility(View.VISIBLE);
		tv_dayweek.setVisibility(View.VISIBLE);
		spinner_num.setVisibility(View.VISIBLE);
		
		
		iv_menu.setClickable(true);
		iv_wind.setClickable(true);
		iv_up.setClickable(true);
		iv_down.setClickable(true);
//		content_layout.setBackgroundColor(getResources().getColor(R.color.lightblue));
	}

	private void initViews() {
		// TODO Auto-generated method stub
		//set font
		AssetManager assets = getAssets();
		final Typeface font = Typeface.createFromAsset(assets, FONT_DIGITAL_7);
		
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
		tv_time = (TextView) findViewById(R.id.tv_time);
		spinner_num = (Spinner) findViewById(R.id.spinner_num);
		tv_dayweek = (TextView) findViewById(R.id.tv_dayofweek);
		skb_temp = (SeekBar) findViewById(R.id.skb_temp);

		tv_temp.setTypeface(font);
		tv_time.setTypeface(font);
		
//		for (int i=0; i<dayofweekID.length;i++){
//			tv_dayofweek[i] = (TextView) findViewById(dayofweekID[i]);
//			tv_dayofweek[i].setTypeface(font);
//		}
		time.setToNow();
		dayOfWeek = time.weekDay-1;
		if (dayOfWeek == -1){
			dayOfWeek = 6;
			tv_dayweek.setText("7");
			tv_dayweek.setVisibility(View.VISIBLE);
		}else{
			tv_dayweek.setText(""+time.weekDay);
			tv_dayweek.setVisibility(View.VISIBLE);
		}
		minute = time.minute;
		hour = time.hour;
		

//		content_layout = (LinearLayout) findViewById(R.id.content_layout);
		
		//spinner num
		for (int i=0;i<10; i++){
			spinnerDataList.add("0"+i);
		}
		spinnerDataList.add("10");
		spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,spinnerDataList);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_num.setAdapter(spinnerAdapter);
		spinner_num.setSelection(1);
		
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
				if (operationCountDown == 0){
					isOperated = false;
					operationCountDown = operationCountDownTime;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				operationCountDown--;
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
//				if (dayOfWeek != time.weekDay){
//					msg.what = WEEKDAYCHANGED;
//					dayOfWeek = time.weekDay;
//					TimerHandler.sendMessage(msg);
//				}
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
//		boolean isFirstLoop = true;
		public void run(){
			try {
				byte[] readBuffer = new byte[8];
				while ( threadRun ){
//					Toast.makeText(Operations.this.context.getApplicationContext(),"1 data is ", Toast.LENGTH_LONG).show();
					if (operation.getmDataInputeStream().read(readBuffer) != -1){
						
						if (!isSwitchDevice){
							byte responseCheckSum = readBuffer[7];
							Operations.CalcCheckSum(readBuffer);
							if (responseCheckSum == readBuffer[7]){
								Message msg = new Message();
								msg.what = UPDATEALL;
								msg.obj = readBuffer;
								updateHandle.sendMessage(msg);

							}
							
						}
						isSwitchDevice = false;
					}
//					isRecvResponse = true;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	private Runnable mGetTemperatureRequest = new Runnable(){
		
		public void run(){
		
			while (threadRun){
				
//				if (isRecvResponse){
					byte[] data = {(byte) 0xA0,(byte)currentSpinnerSelected, (byte) mID1, 0x00, 0x00, 0x00, 0x00,0x00};
					byte command = Operations.commands[0];
					parseCurrentState(data, command);
//					Operations.CalcCheckSum(data);
					
					try {
						Thread.sleep(200);
						operation.getmPrintWriter().write(data);
						Thread.sleep(time_chip-200);
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
//				}
				
			}
		}
	};
	
	void parseRecvData(byte[] results){
		
		byte ctrlInfo = results[3];
		int  initTempInfo = (int) results[5];
		int  currentTempInfo = (int )results[6];
		
		byte wind = (byte) (ctrlInfo & 0x03);
		byte state = (byte) (ctrlInfo & 0x10);
		byte menu = (byte) (ctrlInfo & 0x60);
		
		//parse wind state
//		int lastWind = currentWind;
		switch(wind){
		case 0x00:
			currentWind = Operations.WIND_MODE_AUTO;
			break;
		case 0x01:
			currentWind = Operations.WIND_MODE_HIGH;
			break;
		case 0x02:
			currentWind = Operations.WIND_MODE_MIDDLE;
			break;
		case 0x03:
			currentWind = Operations.WIND_MODE_LOW;
			break;
		}
		windList[currentWind_fork].setVisibility(View.INVISIBLE);
		windList[currentWind].setVisibility(View.VISIBLE);
		currentWind_fork = currentWind;
		
		//menu state
		switch (menu){
		case 0x00:
			currentMenu = Operations.MENU_MODE_COLD;
			break;
		case 0x20:
			currentMenu = Operations.MENU_MODE_WARM;
			break;
		case 0x40:
			currentMenu = Operations.MENU_MODE_VENTILATE;
			break;
		}
		menuList[currentMenu_fork].setVisibility(View.INVISIBLE);
		menuList[currentMenu].setVisibility(View.VISIBLE);
		currentMenu_fork = currentMenu;
		
		//switch state
		switch (state){
		case 0x00:
			currentSwitchState = SWITCHOFF;
			turnDownDevice();
			break;
		case 0x10:
			currentSwitchState = SWITCHON;
			turnOnDevice();
			break;
		}
		currentSwitchState_fork = currentSwitchState;
		
		//set temperature 
		initTemperature = (double) (initTempInfo*1.0/2.0);
		initTemperature_fork = initTemperature;
//		if (isSetTime){
//			Message msg = new Message();
//			msg.what = UPDATE_INIT_TEMPERATURE;
//			updateHandle.sendMessage(msg);
//			isSetTime = false;
//		}
		
		//current temperature
		currentTemperature = (double)(currentTempInfo*1.0/2.0);
		currentTemperature_fork = currentTemperature;
		
	}
	
	void parseCurrentState(byte[] data,byte command){
		/**dataPackage[0] is command
		*  dataPackage[1] is ip0;
		*  dataPackage[2] is ip1;
		*  dataPackage[3] is data0;
		*  dataPackage[4] is  data1;
		*  dataPackage[5] is data 2;
		*  dataPackage[6] is data3;
		*  dataPackage[7] is checkSum;
		*/
		data[0] = command;
		data[1] = (byte) currentSpinnerSelected_fork;
		data[2] = (byte) mID1;
		data[3] = 0x18;
		data[3] = operation.WindDataParse(data[3], currentWind_fork);
		data[3] = operation.MenuDataParse(data[3], currentMenu_fork);
		data[3] = operation.SwitchStateParse(data[3], currentSwitchState_fork);
		data[4] = 0x00;
		int temp_initTemperature = (int) (initTemperature_fork*2);
		data[5] = (byte) temp_initTemperature;
		data[6] = 0x00;
		Operations.CalcCheckSum(data);
		
	}
	

	void  SetTemperature(double temp){
		if (temp>50.0){
			temp = 50.0;
		}
		if (temp<0.0){
			temp = 0.0;
		}
		if (temp<10.0){
			tv_temp.setText("0"+temp);
		}
		else{
			tv_temp.setText(""+temp);
		}
		
	}
	
	Handler updateHandle = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == UPDATEALL){
				
				if (!isOperated){
					parseRecvData((byte[]) msg.obj);	
				}
				
			}
			if (msg.what == UPDATE_INIT_TEMPERATURE){
				SetTemperature(initTemperature);

				iv_mark.setVisibility(View.INVISIBLE);
				tv_set.setVisibility(View.VISIBLE);
			}
			if (msg.what == UPDATE_CURRENT_TEMPERATURE){
//				SetTemperature(currentTemperature);
//
//				iv_mark.setVisibility(View.VISIBLE);
//				tv_set.setVisibility(View.INVISIBLE);
			}
//			super.handleMessage(msg);
		}
		
	};

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
