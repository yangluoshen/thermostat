package com.usr.thermostat;

import java.io.BufferedReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class Operations {
	private static Operations operationInstance = null;
	
	public static final int SETWIND = 0;
	public static final int SETMENU = 1;
	public static final int SETUPTEMP = 2;
	public static final int SETDOWNTEMP = 3;
	public static final int SETCLOSE = 4;
	public static final int SETCONNECT = 5;
	public static boolean isConnected = false;
	public boolean threadMarker = false;
	
//	private Context context;
	
	public Socket mSocket = null;
	public InetSocketAddress mISA = null;
	public int socketTimeOut = 3000;
//	private BufferedReader mBufferedReader  = null;
//	private PrintWriter mPrintWriter = null;
	private DataInputStream mDataInputeStream = null;
	private DataOutputStream mPrintWriter = null;
	
	public static final int WIND_MODE_AUTO   = 0;	
	public static final int WIND_MODE_LOW    = 1;
	public static final int WIND_MODE_MIDDLE = 2;
	public static final int WIND_MODE_HIGH   = 3;

	
	public static final int MENU_MODE_COLD    = 0;
	public static final int MENU_MODE_WARM    = 1;
	public static final int MENU_MODE_VENTILATE = 2;
	
	public double recvTemperature = 0.0;
	public static Handler handler = null;
	Thread recvThread;
	Thread getTemperatureRequest = null;
	byte[] commands = {(byte) 0xa1,(byte) 0xa0,(byte) 0xa8};
	
	byte checkSum;
	byte command;
	byte ID0 = 0x00;
	byte ID1 = 0x00;
	byte Data0;
	byte Data1;
	byte Data2;
	byte Data3;
	/**dataPackage[0] is command
	*  dataPackage[1] is ip0;
	*  dataPackage[2] is ip1;
	*  dataPackage[3] is data0;
	*  dataPackage[4] is  data1;
	*  dataPackage[5] is data 2;
	*  dataPackage[6] is data3;
	*  dataPackage[7] is checkSum;
	*/
	byte[] dataPackage   = new byte[8];
	byte windResetByte   = (byte) 0xfc;
	byte switchResetByte = (byte) 0xef;
	byte menuResetByte   = (byte) 0x9f;
	
	Time time = new Time();

	private Operations() {
		// TODO Auto-generated constructor stub
//		this.context = context;
		recvThread = new Thread(mRecvThread);
		getTemperatureRequest = new Thread(mGetTemperatureRequest);
		//initDataPackage();		
	}
	/**
	 * unique instance
	 * @param ctx
	 * @param han
	 * @return
	 */
	public static Operations GetOperation(){
		
		if (operationInstance == null){
			operationInstance = new Operations();
		}
		
		return operationInstance;
	}
	
	void initDataPackage(){
		dataPackage[0] = commands[0];
		dataPackage[1] = 0x00;
		dataPackage[2] = 0x00;
		dataPackage[3] = 0x18;  //data0
		dataPackage[4] = 0x00;
		dataPackage[5] = 0x2c;  //init temperature
		dataPackage[6] = 0x00;
		dataPackage[7] = 0x00;
		CalcCheckSum(dataPackage);
		
	}
	public boolean Connect(String ip, String port){
		
		try {
			Log.i("yangluo","connect1");
			int int_port = Integer.valueOf(port).intValue(); 
			
			mSocket = new Socket();
			mISA = new InetSocketAddress(ip, int_port);
			mSocket.connect(mISA, socketTimeOut);
			initDataPackage();
			
			mDataInputeStream = new DataInputStream(mSocket.getInputStream());
			mPrintWriter = new DataOutputStream(mSocket.getOutputStream());
			
			//send the init data
			mPrintWriter.write(dataPackage);
			
			sendInitTime();
			//start the  thread
			if (!threadMarker){
				recvThread.start();
				getTemperatureRequest.start();
				threadMarker =  true;
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
//			Toast.makeText(this.context, "connect failed-- unknow host", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			Toast.makeText(this.context, "connect failed2", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			
			return false;
		}
		isConnected = true;
		return true;
	}
	private void sendInitTime() {
		// TODO Auto-generated method stub
		time.setToNow();
		byte[] bytes = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		bytes[0] = (byte) 0xA8;
		bytes[1] = 0x00;
		bytes[2] = 0x00;
		bytes[3] = (byte) time.second;
		bytes[4] = (byte) time.minute;
		bytes[5] = (byte) time.hour;
		if (time.weekDay == 0){
			bytes[6] = 0x00;
		}
		else{
			bytes[6] = (byte) time.weekDay;
		}
		
		CalcCheckSum(bytes);
		try {
			mPrintWriter.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * this function called whenever the menu type changed
	 * @param mode
	 */
	void sendMenuData(int mode){
		dataPackage[0] = commands[0];
		dataPackage[3] &= menuResetByte;
		
		switch(mode){
		
		case MENU_MODE_COLD :
			dataPackage[3] |= 0x00;
//			mPrintWriter.print(dataPackage);
//			mPrintWriter.flush();
			break;
		case MENU_MODE_WARM :
			dataPackage[3] |= 0x20;
//			mPrintWriter.print();
//			mPrintWriter.flush();
			break;
		case MENU_MODE_VENTILATE :
			dataPackage[3] |= 0x40;
//			mPrintWriter.print("menu 3");
//			mPrintWriter.flush();
			break;
		}
		PrintWrite(SETMENU);
		
	}
	/**
	 *  this function called whenever the wind level changed
	 * @param mode
	 */
	void sendWindData(int mode){
		dataPackage[0] = commands[0];
		dataPackage[3] &= windResetByte;
		switch (mode){
		
		case WIND_MODE_AUTO :
			dataPackage[3] |= 0x00;
//			mPrintWriter.print(" wind auto");
//			mPrintWriter.flush();
			break;
		case WIND_MODE_LOW :
			dataPackage[3] |= 0x03;
//			mPrintWriter.print(" wind 1");
//			mPrintWriter.flush();
			break;
		case WIND_MODE_MIDDLE :
			dataPackage[3] |= 0x02;
//			mPrintWriter.print(" wind 2");
//			mPrintWriter.flush();
			break;
		case WIND_MODE_HIGH :
			dataPackage[3] |= 0x01;
//			mPrintWriter.print(" wind 4 ");
//			mPrintWriter.flush();
			break;
		}
		Log.i("yangluo","in send windata "+dataPackage.toString());
		PrintWrite(SETWIND);
//		CalcCheckSum();
//		mPrintWriter.print(dataPackage);
//		mPrintWriter.flush();
		
	}
	
	void sendUpTemperature(double temperature){
		int int_temperature = (int) (temperature * 2);
		dataPackage[0] = commands[0];
		dataPackage[5] = (byte)int_temperature;
		PrintWrite(SETUPTEMP);

	}
	
	void sendDownTemprature(double temperature){
		int int_temperature = (int) (temperature * 2);
		dataPackage[0] = commands[0];
		dataPackage[5] = (byte)int_temperature;
		PrintWrite(SETDOWNTEMP);
	}
	
	void sendCloseSignal(int state){
		dataPackage[0] = commands[0];
		dataPackage[3] &= switchResetByte;
		if (state == MainActivity.SWITCHON){
			dataPackage[3] |= 0x10;
			isConnected = true;
//			getTemperatureRequest.start();
		}
		else if (state == MainActivity.SWITCHOFF){
			dataPackage[3] |= 0x00;
			isConnected = false;
		}
		PrintWrite(SETCLOSE);
	}
	
	/**
	 * convert a byte[] to a chars[]
	 * @param bytes
	 * @return
	 */
	char[] Bytes2Chars(byte[] bytes){
		char[] chars = new char[bytes.length];
		
		for (int i=0; i<bytes.length; i++){
			if ( (bytes[i] & 0x80) == 0){
				chars[i] = (char)bytes[i];
			}else{
				chars[i] = (char)bytes[i];
				chars[i] &= 0x00ff;
			}
		}
		return chars;
	}
	
	void CalcCheckSum(byte[] bytes){
		bytes[7] = 0x00;
		for (int i=0; i<7;i++){
			bytes[7]+= bytes[i];
		}
		bytes[7] = (byte) (bytes[7] & 0xff ^ 0xa5);
	}
	/**
	 * convert a byte[] to String
	 * @param bytes
	 * @return
	 */
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
	
	
	/**
	 * convert a byte to a char
	 * @param b
	 * @return
	 */
	public  char findHex(byte b) {
		int t = new Byte(b).intValue();
		t = t < 0 ? t + 16 : t;
		if ((0 <= t) &&(t <= 9)) {
		return (char)(t + '0');
		}
		return (char)(t-10+'A');
	}
	
	void PrintWrite(int type){
		CalcCheckSum(dataPackage);
		try {
			mPrintWriter.write(dataPackage);
//			printStringResult(type);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	/**
	 * send the datapackage[] through String type
	 * just  used when test
	 * @param type
	 */
	void printStringResult(int type){
		try {
			
			switch(type){
			case SETCONNECT:
				mPrintWriter.writeChars(Bytes2String(dataPackage)+" set connect\n ");
				break;
			case SETWIND:
				mPrintWriter.writeChars(Bytes2String(dataPackage)+" set wind\n ");
				break;
			case SETMENU :
				mPrintWriter.writeChars(Bytes2String(dataPackage)+" set function\n ");
				break;
			case SETUPTEMP :
				mPrintWriter.writeChars(Bytes2String(dataPackage)+" up temperature\n ");
				break;
			case SETDOWNTEMP:
				mPrintWriter.writeChars(Bytes2String(dataPackage)+" down temperature\n ");
				break;
			case SETCLOSE:
				mPrintWriter.writeChars(Bytes2String(dataPackage)+" close device\n ");
	//			mPrintWriter.flush();
				break;
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				while ( true){
					if (mDataInputeStream.read(readBuffer) != -1){
						
//						Message msg = new Message();
						int int_temp =(int) readBuffer[6]; 
						double temp = (double) (int_temp*1.0/2.0);
						MainActivity.currentTemperature = temp;
//						msg.obj = new String( "" + temp);
						
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
			while (true){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (isConnected){
					try {
						mPrintWriter.write(data);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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

	public Handler getHandler() {
		return handler;
	}
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	
	
}
