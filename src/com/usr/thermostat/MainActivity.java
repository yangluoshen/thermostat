package com.usr.thermostat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.Inflater;

import com.usr.thermostat.R.id;
import com.usr.thermostat.db.RoomDB;
import com.usr.thermostat.network.NetManager;
import com.usr.thermostat.network.NetworkDetectorService;
import com.usr.thermostat.network.NetworkDetectorService.GetConnectState;
import com.usr.thermostat.network.SocketThreadManager;
import com.usr.thermostat.network.TCPClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.format.Time;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
	private int time_chip = 4000;
	private static final double MIN_INIT_TEMPERATURE = 10.0;
	private static final double MAX_INIT_TEMPERATURE = 30.0;
	private static final double MIN_CURRENT_TEMPERATURE = 0.0;
	private static final double MAX_CURRENT_TEMPERATURE = 50.0;
	//because of the progress range from 0~40, but the init_temp range from 10~30 ,so need a offset
	private static final int INIT_TEMPERATURE_OFFSET = 20;
	
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
	TextView tv_temp ;       // text temprature
	TextView tv_dayweek;
	TextView tv_week;        //the text "week"
//	Spinner spinner_num;
	SeekBar skb_temp;
	
//	private ArrayList<String> spinnerDataList = new ArrayList<String>();
//	private ArrayAdapter<String> spinnerAdapter;


	State currentState = new State();
	
	int nextSwitchState = SWITCHOFF;
	
	static final int countDownTime = 3;
	int countDown = countDownTime;
	int operationCountDownTime = 2;
	int operationCountDown = 0;
	
	
//	byte[] recvDataBuffer = new byte[8];
//	LinkedList<byte[]> recvDataBuffer  = new LinkedList<byte[]>();
	
	int dayOfWeek;
	int hour;
	int minute;
	
	Counter count;
//	Timer timer;
	static final int TIMEUP = 0;
	static final int TIMEDOWN = 1;
	static final int TIMECHANGED = 2;
	static final int WEEKDAYCHANGED =3;
	static final int SWITCHOFF = 0;
	static final int SWITCHON = 1;
	
	public static final int SEND_MESSAGE_FAILED = 0;
	public static final int SEND_MESSAGE_SUCCESS = 1;
	static final int UPDATEALL = 4;
	static final int UPDATE_INIT_TEMPERATURE = 5;
	static final int UPDATE_CURRENT_TEMPERATURE = 6;
	static final int SEND_HEART_CLOCK = 7;
	static final int RECV_DATA = 8;
	
	public static final int NETMANAGER_NOTIFY_ERROR = 9;
	public static final int NETMANAGER_NOTIFY_OK = 10;
	public static final int NETWORK_NOTOK = 11;
	
	Thread countThread;

	Thread recvThread;
	Thread getTemperatureRequest;
	boolean threadRun = true;

	boolean isFirstIn = true;
	CountDownTimer opCountDown;
	
	Operations operation;
	private NetworkDetectorService mNDS;
	private boolean connectState = true;
	
	Time time = new Time();
	int currentRoomId;
	
	
	Handler CounterHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == TIMEUP && currentState.getSwitchState() == SWITCHON){
				SetTemperature(currentState.getTemperature());
				msg.what = TIMEDOWN;
				
				iv_mark.setVisibility(View.VISIBLE);
				tv_set.setVisibility(View.INVISIBLE);
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_new_version);
		initViews();
		addEvents();
		
		bindNetworkService();
		
		
		currentState.setMenu(Operations.MENU_MODE_COLD);
		currentState.setWind(Operations.WIND_MODE_AUTO);
		currentState.setSetTemperature(0.0);
		currentState.setTemperature(0.0);
		currentState.setSwitchState(SWITCHON);
		currentState.setSpinnerSelected(1);
		currentState.setmID1(1);
		
//		currentState_fork = new State(currentState);
		NetManager.instance().init(this);
		NetManager.instance().setHandler(mainHandler);
		
		operation = Operations.GetOperation();
		operation.setHandler(mainHandler);
		
		count = new Counter();
		countThread = new Thread(count);
		countThread.start();
		
		
//		recvThread = new Thread(mRecvThread);
//		recvThread.start();
		
		getTemperatureRequest = new Thread(mGetTemperatureRequest);
		getTemperatureRequest.start();
		
		opCountDown = new CountDownTimer();
		opCountDown.setCountDownMax(2);
		opCountDown.setCountDown(2);
		opCountDown.setFlag(true);
		opCountDown.setInitFlag(true);
		opCountDown.setHandler(mainHandler);
		opCountDown.setHandlerMsg(UPDATEALL);
		opCountDown.startTimer();
		
		//获取上个activity传来的数据
		currentRoomId = getIntent().getExtras().getInt("id");
		
//		Bundle bundle = getIntent().getExtras();
//		if (bundle !=null){
//			byte[] initState = bundle.getByteArray("initstate");
//			recvDataBuffer = initState;
//			parseRecvData(initState);
//			SetTemperature(currentTemperature);
//			skb_temp.setThumb(getResources().getDrawable(R.drawable.thumb));
////			isFirstIn = false;
//		}
		

		
	}
	private void addEvents() {
		// TODO Auto-generated method stub
		iv_wind.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				isOperated = true
				opCountDown.reSetCountDown();
				operationCountDown = operationCountDownTime;
				operation.setDataPackgeID0AndID1(currentState.getSpinnerSelected(), currentState.getmID1());
				
				int nextWind = Operations.WIND_MODE_AUTO;
				
				if (currentState.getWind() == Operations.WIND_MODE_AUTO){
					nextWind = Operations.WIND_MODE_LOW;
				}else if(currentState.getWind() == Operations.WIND_MODE_LOW){
					nextWind = Operations.WIND_MODE_MIDDLE;
				}else if(currentState.getWind() == Operations.WIND_MODE_MIDDLE){
					nextWind = Operations.WIND_MODE_HIGH;
				}else if (currentState.getWind() == Operations.WIND_MODE_HIGH){
					nextWind  = Operations.WIND_MODE_AUTO;
				}
				
				windList[currentState.getWind()].setVisibility(View.INVISIBLE);
				windList[nextWind].setVisibility(View.VISIBLE);
				currentState.setWind(nextWind);
				operation.sendWindData(nextWind);
				skb_temp.setThumb(getResources().getDrawable(R.drawable.seekthumb_wait));
				

			}
		});
		iv_menu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				isOperated = true;
				opCountDown.reSetCountDown();
				operationCountDown = operationCountDownTime;
				
				int nextMenu = Operations.MENU_MODE_WARM;
				operation.setDataPackgeID0AndID1(currentState.getSpinnerSelected(), currentState.getmID1());
				if (currentState.getMenu() == Operations.MENU_MODE_COLD){
					nextMenu = Operations.MENU_MODE_WARM;
				}else if (currentState.getMenu() == Operations.MENU_MODE_WARM){
					nextMenu = Operations.MENU_MODE_VENTILATE;
				}else if (currentState.getMenu() == Operations.MENU_MODE_VENTILATE){
					nextMenu = Operations.MENU_MODE_COLD;
				}
				menuList[currentState.getMenu()].setVisibility(View.INVISIBLE);
				menuList[nextMenu].setVisibility(View.VISIBLE);
				operation.sendMenuData(nextMenu);
				currentState.setMenu(nextMenu);
				skb_temp.setThumb(getResources().getDrawable(R.drawable.seekthumb_wait));


			}
		});
		iv_up.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				isOperated = true;
				opCountDown.reSetCountDown();
				operationCountDown = operationCountDownTime;
//				if (isRecvResponse){
					countDown = countDownTime;
//					isSetTime = true;
					double nextInitTemperature;
					
					nextInitTemperature = currentState.getSetTemperature();
					
					nextInitTemperature += 0.5;
					if (nextInitTemperature >= MAX_INIT_TEMPERATURE){
						nextInitTemperature = MAX_INIT_TEMPERATURE;
					}
					currentState.setSetTemperature(nextInitTemperature);
//					initTemperature_fork = nextInitTemperature;
					tv_temp.setText(""+currentState.getSetTemperature());
					iv_mark.setVisibility(View.INVISIBLE);
					tv_set.setVisibility(View.VISIBLE);
					
					operation.setDataPackgeID0AndID1(currentState.getSpinnerSelected(), currentState.getmID1());
					operation.sendUpTemperature(nextInitTemperature);
					skb_temp.setThumb(getResources().getDrawable(R.drawable.seekthumb_wait));
					skb_temp.setProgress((int)(nextInitTemperature*2)-INIT_TEMPERATURE_OFFSET);
							
					
			}
		});
		iv_down.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				isOperated = true;
				opCountDown.reSetCountDown();
				operationCountDown = operationCountDownTime;
				countDown = countDownTime;
//					isSetTime = true;
				
				double nextInitTemperature = 0.0;
				nextInitTemperature = currentState.getSetTemperature();
				
				nextInitTemperature -= 0.5;
				if (nextInitTemperature <= MIN_INIT_TEMPERATURE){
					nextInitTemperature = MIN_INIT_TEMPERATURE;
				}

				currentState.setSetTemperature(nextInitTemperature);
				tv_temp.setText(""+currentState.getSetTemperature());
				iv_mark.setVisibility(View.INVISIBLE);
				tv_set.setVisibility(View.VISIBLE);
				
				operation.setDataPackgeID0AndID1(currentState.getSpinnerSelected(), currentState.getmID1());
				operation.sendDownTemprature(nextInitTemperature);
				
				skb_temp.setThumb(getResources().getDrawable(R.drawable.seekthumb_wait));
				skb_temp.setProgress((int)(nextInitTemperature*2)-INIT_TEMPERATURE_OFFSET);
				

			}
		});

		iv_close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if (isRecvResponse){
//					isOperated = true;
				opCountDown.reSetCountDown();
				operationCountDown = operationCountDownTime;
				operation.setDataPackgeID0AndID1(currentState.getSpinnerSelected(), currentState.getmID1());
				if (currentState.getSwitchState() == SWITCHON){
					currentState.setSwitchState(SWITCHOFF);
					operation.sendCloseSignal(SWITCHOFF);
					turnDownDevice();
				}
				else{
					
					currentState.setSwitchState(SWITCHON);
					operation.sendCloseSignal(SWITCHON);
					turnOnDevice();
				}
				skb_temp.setThumb(getResources().getDrawable(R.drawable.seekthumb_wait));
//				}
					
				
			}
		});
		
//		spinner_num.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> arg0, View arg1,
//					int position, long arg3) {
//				// TODO Auto-generated method stub
//				if (!isFirstIn){
////					isOperated = true;
//					opCountDown.reSetCountDown();
//					operationCountDown = operationCountDownTime;
//					currentState.setSpinnerSelected(position+1);
//
//					currentState.setmID1(1);
//					
//					tv_temp.setText("00.0");
//					currentState.setTemperature(0.0);
//					isSwitchDevice = true;
//					
//					byte[] data = {(byte) 0xA0,(byte)currentState.getSpinnerSelected(), (byte) currentState.getmID1(), 0x00, 0x00, 0x00, 0x00,0x00};
//					Operations.CalcCheckSum(data);
//					try {
//						operation.getmPrintWriter().write(data);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					skb_temp.setThumb(getResources().getDrawable(R.drawable.seekthumb_wait));
//				}
//				else {
////					isFirstIn = false;
//				}
//				
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
		skb_temp.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			private double selectProgress=20;
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

				currentState.setSetTemperature(selectProgress);
				operation.setDataPackgeID0AndID1(currentState.getSpinnerSelected(), currentState.getmID1());
				operation.sendUpTemperature(selectProgress);
				skb_temp.setThumb(getResources().getDrawable(R.drawable.seekthumb_wait));

			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				iv_mark.setVisibility(View.INVISIBLE);
				tv_set.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				countDown = countDownTime;
//				isOperated = true;
				opCountDown.reSetCountDown();
				operationCountDown = operationCountDownTime;
				progress += INIT_TEMPERATURE_OFFSET;
				
				selectProgress = progress*1.0/2;
				tv_temp.setText(""+(double)progress/2);
				
			}
		});
		
	}
	void turnDownDevice(){

		windList[currentState.getWind()].setVisibility(View.INVISIBLE);
		menuList[currentState.getMenu()].setVisibility(View.INVISIBLE);
		tv_temp.setVisibility(View.INVISIBLE);
		iv_mark.setVisibility(View.INVISIBLE);
		iv_degree.setVisibility(View.INVISIBLE);
		tv_set.setVisibility(View.INVISIBLE);
		
		tv_week.setVisibility(View.INVISIBLE);
		tv_dayweek.setVisibility(View.INVISIBLE);
		
		iv_menu.setClickable(false);
		iv_wind.setClickable(false);
		iv_up.setClickable(false);
		iv_down.setClickable(false);
//		spinner_num.setVisibility(View.INVISIBLE);
		
		
	}
	void turnOnDevice(){

		windList[currentState.getWind()].setVisibility(View.VISIBLE);
		menuList[currentState.getMenu()].setVisibility(View.VISIBLE);
		tv_temp.setVisibility(View.VISIBLE);
		iv_mark.setVisibility(View.VISIBLE);
		iv_degree.setVisibility(View.VISIBLE);

		
		tv_week.setVisibility(View.VISIBLE);
		tv_dayweek.setVisibility(View.VISIBLE);
//		spinner_num.setVisibility(View.VISIBLE);
		
		
		iv_menu.setClickable(true);
		iv_wind.setClickable(true);
		iv_up.setClickable(true);
		iv_down.setClickable(true);

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
		tv_set.setVisibility(View.INVISIBLE);
		tv_week = (TextView) findViewById(R.id.tv_week);
		iv_mark = (ImageView) findViewById(R.id.iv_mark);
		iv_degree = (ImageView) findViewById(R.id.iv_degree);
//		tv_time = (TextView) findViewById(R.id.tv_time);
//		spinner_num = (Spinner) findViewById(R.id.spinner_num);
		tv_dayweek = (TextView) findViewById(R.id.tv_dayofweek);
		skb_temp = (SeekBar) findViewById(R.id.skb_temp);

		
		
		tv_temp.setTypeface(font);

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
		
		//spinner num
//		for (int i=0;i<9; i++){
//			spinnerDataList.add(""+(i+1));
//		}
//		spinnerDataList.add("10");
//		spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_check_text,spinnerDataList){
//
//			@Override
//			public View getDropDownView(int position, View convertView,
//					ViewGroup parent) {
//				// TODO Auto-generated method stub
////				return super.getDropDownView(position, convertView, parent);
//				
//				View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, null);
//				TextView text = (TextView) view.findViewById(R.id.spinner_item_num);
//				ImageView check = (ImageView) view.findViewById(R.id.spinner_item_checked_image);
//				text.setText("    "+(position+1));
//				
//				if (spinner_num.getSelectedItemPosition() == position) {
//					check.setImageResource(R.drawable.spinner_item_checked);
////                    view.setBackgroundColor(getResources().getColor(
////                            R.color.lightblue));
////                    check.setImageResource(R.drawable.check_selected);
//                } else {
//                	check.setVisibility(View.INVISIBLE);
////                    view.setBackgroundColor(getResources().getColor(
////                            R.color.skyblue));
////                    check.setImageResource(R.drawable.check_unselect);
//                }
//				
//				return view;
//			}
//			
//		};
//		spinner_num.setAdapter(spinnerAdapter);
//		spinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
//		
//		spinner_num.setSelection(0);
		
//		Bundle bundle = getIntent().getExtras();
//		byte[] initState = bundle.getByteArray("initstate");
//		parseRecvData(initState);
//		SetTemperature(currentTemperature);
//		skb_temp.setThumb(getResources().getDrawable(R.drawable.thumb));
		
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
				operationCountDown--;
				countDown--;
				
			}
		}
	}

//	/**
//	 * a thread that receive the message from server. 
//	 * the message could only contents (double)temperature
//	 * if necessary, you should convert the message to a double format (such as "22.5")
//	 */
//	private Runnable mRecvThread = new Runnable(){
////		boolean isFirstLoop = true;
//		public void run(){
//			while (threadRun){
//				try {
//					byte[] readBuffer = new byte[8];
//					if (operation.getmDataInputeStream().read(readBuffer) != -1){
//						//如果切换了房间操作，将最近的收到的一个数据包丢掉
////						if (!isSwitchDevice){
//							//检查校验和
//							byte responseCheckSum = readBuffer[7];
//							Operations.CalcCheckSum(readBuffer);
//							if (responseCheckSum == readBuffer[7]){
//								recvDataBuffer.addLast(readBuffer);
////								Message msg = new Message();
////								msg.what = UPDATEALL;
////								msg.obj = readBuffer;
////								updateHandle.sendMessage(msg);
//							}
////						}
////						isSwitchDevice = false;
//					}
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//		}
//	};
	
	
	private Runnable mGetTemperatureRequest = new Runnable(){
		
		public void run(){
			while (threadRun){
				try {
					Thread.sleep(time_chip);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				Message msg = new Message();
				msg.what = SEND_HEART_CLOCK;
				mainHandler.sendMessage(msg);
				
			}
		}
	};
	
	State parseRecvData(final byte[] results){
		
		byte ctrlInfo = results[3];
		int  initTempInfo = (int) results[5];
		int  currentTempInfo = (int )results[6];
		
		byte wind = (byte) (ctrlInfo & 0x03);
		byte switchState = (byte) (ctrlInfo & 0x10);
		byte menu = (byte) (ctrlInfo & 0x60);
		
		State parseState = new State(currentState);
		
		//parse wind state
		switch(wind){
		case 0x00:
			parseState.setWind(Operations.WIND_MODE_AUTO);
			break;
		case 0x01:
			parseState.setWind(Operations.WIND_MODE_HIGH);
			break;
		case 0x02:
			parseState.setWind(Operations.WIND_MODE_MIDDLE);
			break;
		case 0x03:
			parseState.setWind(Operations.WIND_MODE_LOW);
			break;
		}
		
		//menu state
		switch (menu){
		case 0x00:
			parseState.setMenu(Operations.MENU_MODE_COLD);
			break;
		case 0x20:
			parseState.setMenu(Operations.MENU_MODE_WARM);
			break;
		case 0x40:
			parseState.setMenu(Operations.MENU_MODE_VENTILATE);
			break;
		}
		
		//switch state
		switch (switchState){
		case 0x00:
//			currentSwitchState = SWITCHOFF;
			parseState.setSwitchState(SWITCHOFF);
//			turnDownDevice();******************************do not forget
			break;
		case 0x10:
//			currentSwitchState = SWITCHON;
			parseState.setSwitchState(SWITCHON);
//			turnOnDevice();**********************************
			break;
		}
//		currentSwitchState_fork = currentSwitchState;
		
		//set temperature 
		parseState.setSetTemperature((double)(initTempInfo*1.0/2.0));;

//		skb_temp.setProgress(initTempInfo-INIT_TEMPERATURE_OFFSET);************************

		
		//current temperature

		parseState.setTemperature((double)(currentTempInfo*1.0/2));
		
		return parseState;
	}

	void  SetTemperature(double temp){
		if (temp > MAX_CURRENT_TEMPERATURE){
			temp = MAX_CURRENT_TEMPERATURE;
		}
		if (temp < MIN_CURRENT_TEMPERATURE){
			temp = MIN_CURRENT_TEMPERATURE;
		}
		if (temp < 10.0){
			tv_temp.setText("0"+temp);
		}
		else{
			tv_temp.setText(""+temp);
		}
		
	}
	
	
	Handler mainHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == UPDATEALL){
//				recvDataBuffer.add((byte[])msg.obj);
				byte[] recvData = new byte[8];
				synchronized (GlobalData.Instance().getRecvMsgList()) {
					if (GlobalData.Instance().getRecvMsgList().isEmpty())
					{
						return;
					}
					recvData = GlobalData.Instance().getRecvMsgList().get(GlobalData.Instance().getRecvMsgList().size()-1);
					GlobalData.Instance().getRecvMsgList().clear();
				}
//				recvData = SocketThreadManager.sharedInstance().getMsg();
					
				if(null == recvData)
				{
					return;
				}
				
				if (isFirstIn)
				{
					currentState.byteArrayToState(recvData);
					operation.sendInitTime();
					
					isFirstIn = false;
					
				}
				else 
				{
					State stateTmp = new State();
					stateTmp.byteArrayToState(recvData);
					if (!currentState.equalto(stateTmp))
					{
						return;
					}
					currentState.setTemperature(stateTmp.getTemperature());
				}
				updateUI(currentState);
			}
			//发送心跳包
			if (msg.what == SEND_HEART_CLOCK)
			{
				if (!opCountDown.isFlag())
				{
					byte[] data = new byte[8];
					byte command = Operations.commands[0];
					data = currentState.toByteArray();
					data[0] = command;
					data[6] = 0x00;
					Operations.CalcCheckSum(data);
					
					SocketThreadManager.sharedInstance().sendMsg(data, mainHandler);
							
				}
			}
			if (msg.what == UPDATE_INIT_TEMPERATURE){
				SetTemperature(currentState.getSetTemperature());

				iv_mark.setVisibility(View.INVISIBLE);
				tv_set.setVisibility(View.VISIBLE);
			}
			if (msg.what == UPDATE_CURRENT_TEMPERATURE){
				
			}
			if (msg.what == NETMANAGER_NOTIFY_ERROR)
			{
				Toast.makeText(getApplicationContext(), "net not ok ", Toast.LENGTH_SHORT).show();
			}
			if (msg.what == NETMANAGER_NOTIFY_OK)
			{
				Toast.makeText(getApplicationContext(), "net  ok ", Toast.LENGTH_SHORT).show();
			}
			if (msg.what == NETWORK_NOTOK){
				Toast.makeText(getApplicationContext(), "network not ok ", Toast.LENGTH_SHORT).show();
			}
		}
		
		
	};
	private void updateUI(State state) {
		// TODO Auto-generated method stub
		
		if (state.getSwitchState() == SWITCHON){
			turnOnDevice();
			fateView(windList);
			windList[state.getWind()].setVisibility(View.VISIBLE);
			
			fateView(menuList);
			menuList[state.getMenu()].setVisibility(View.VISIBLE);
			
			skb_temp.setProgress((int) (state.getSetTemperature()*2)-INIT_TEMPERATURE_OFFSET);
			SetTemperature(state.getTemperature());
		}
		else {
			turnDownDevice();
		}
		
		skb_temp.setThumb(getResources().getDrawable(R.drawable.thumb));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == event.KEYCODE_BACK && event.getRepeatCount() == 0){
			readyToExit();
		}
		
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		
		super.onResume();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		
		super.onStart();
		
		
	}
	
	
 
	private void fateView(ImageView[] viewList)
	{
		for (ImageView iv : viewList)
		{
			iv.setVisibility(View.INVISIBLE);
		}
	}
	
	
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			mNDS = ((NetworkDetectorService.MyBinder) service).getService();
			mNDS.setOnGetConnectState(new GetConnectState() {
				
				@Override
				public void GetState(boolean isConnected) {
					// TODO Auto-generated method stub
					//whenever network state changed occur following event
					if (connectState != isConnected){
						connectState = isConnected;
						if (!connectState){
							Message msg = new Message();
							msg.what = NETWORK_NOTOK;
							mainHandler.sendMessage(msg);
							readyToExit();
							
						}
					}
				}
			});
		}
	};
	
	private void bindNetworkService(){
		Intent _intent = new Intent(MainActivity.this, NetworkDetectorService.class);
		bindService(_intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void readyToExit()
	{
		try {
//			operation.mSocket.close();
//			operation.isConnected = false;
			operation.releaseInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			threadRun = false;
			countThread.interrupt();
//				timeThread.interrupt();
			getTemperatureRequest.interrupt();
//				recvThread.interrupt();
			NetManager.instance().release();
			opCountDown.cancleTask();
			
			RoomDB roomdb = new RoomDB(getApplicationContext());
			//设置当前房间为以操作
			roomdb.setRecordOperated(1, currentRoomId);
			//将当前设置状态写入数据库
			//params wind , mode , settemp, id
			Object[] params = {currentState.getWind(), currentState.getMenu(),
							""+currentState.getSetTemperature(), currentRoomId};
			roomdb.updateStateInfo(params);
			
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		finally
		{

			//释放Mainactivity的所有资源
			//不能用finish()，finish()只是将activity移除栈，资源并没有释放
//			System.exit(0);
			finish();
		}
		
	}

}
