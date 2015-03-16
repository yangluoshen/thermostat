package com.usr.thermostat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Operations {
	
	public static Socket mSocket = null;
	private BufferedReader mBufferedReader  = null;
	private PrintWriter mPrintWriter = null;
	
	public static final int WIND_MODE_AUTO   = 0;	
	public static final int WIND_MODE_LOW    = 1;
	public static final int WIND_MODE_MIDDLE = 2;
	public static final int WIND_MODE_HIGH   = 3;

	
	public static final int MENU_MODE_COLD    = 0;
	public static final int MENU_MODE_WARM    = 1;
	public static final int MENU_MODE_VENTILATE = 2;
	
	public double recvTemperature = 0.0;
	Handler handler = null;
	Thread recvThread;
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
	byte[] dataPackage = new byte[8];
	byte windResetByte = 0x3f;
	byte switchResetByte = (byte) 0xf7;
	byte menuResetByte = (byte) 0xf9;
	
	


	public Operations(Handler handler) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		recvThread = new Thread(mRecvThread);
		initDataPackage();
		
		Log.i("yangluo",Bytes2Chars(dataPackage).toString());
		
		
		
	}
	void initDataPackage(){
		dataPackage[0] = commands[0];
		dataPackage[1] = 0x00;
		dataPackage[2] = 0x00;
		dataPackage[3] = 0x00;
		dataPackage[4] = 0x00;
		dataPackage[5] = 0x10;
		dataPackage[6] = 0x00;
		dataPackage[7] = 0x00;
		CalcCheckSum();
		
	}
	
	public boolean Connect(String ip, String port){
		
		try {
			int int_port = Integer.valueOf(port).intValue(); 
			mSocket = new Socket(ip, int_port);
			mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			mPrintWriter = new PrintWriter(mSocket.getOutputStream());
			
			PrintWrite();
			//********send init datapackage**********************
//			CalcCheckSum();
//			mPrintWriter.print(dataPackage);
//			mPrintWriter.flush();
//			
//			mPrintWriter.print(Bytes2Chars(dataPackage));
//			mPrintWriter.flush();
			
			//start the recv thread
			//recvThread.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	//this function called whenever the menu type changed
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
			dataPackage[3] |= 0x02;
//			mPrintWriter.print();
//			mPrintWriter.flush();
			break;
		case MENU_MODE_VENTILATE :
			dataPackage[3] |= 0x04;
//			mPrintWriter.print("menu 3");
//			mPrintWriter.flush();
			break;
		}
		PrintWrite();
//		CalcCheckSum();
//		mPrintWriter.print(dataPackage);
//		mPrintWriter.flush();
		
	}
	// this function called whenever the wind level changed
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
			dataPackage[3] |= 0xc0;
//			mPrintWriter.print(" wind 1");
//			mPrintWriter.flush();
			break;
		case WIND_MODE_MIDDLE :
			dataPackage[3] |= 0x80;
//			mPrintWriter.print(" wind 2");
//			mPrintWriter.flush();
			break;
		case WIND_MODE_HIGH :
			dataPackage[3] |= 0x40;
//			mPrintWriter.print(" wind 4 ");
//			mPrintWriter.flush();
			break;
		}
		Log.i("yangluo","in send windata "+dataPackage.toString());
		PrintWrite();
//		CalcCheckSum();
//		mPrintWriter.print(dataPackage);
//		mPrintWriter.flush();
		
	}
	
	void sendUpTemperature(double temperature){
		int int_temperature = (int) (temperature * 2);
		dataPackage[0] = commands[0];
		dataPackage[5] = (byte)int_temperature;
		PrintWrite();
//		CalcCheckSum();
//		mPrintWriter.print(dataPackage);
//		mPrintWriter.flush();
	}
	
	void sendDownTemprature(double temperature){
		int int_temperature = (int) (temperature * 2);
		dataPackage[0] = commands[0];
		dataPackage[5] = (byte)int_temperature;
		PrintWrite();
//		CalcCheckSum();
//		mPrintWriter.print(dataPackage);
//		mPrintWriter.flush();
	}
	
	void sendCloseSignal(){
		
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
	
	void CalcCheckSum(){
		for (int i=0; i<dataPackage.length-1;i++){
			dataPackage[7]+= dataPackage[i];
		}
		dataPackage[7] = (byte) (dataPackage[7] & 0xff ^ 0xa5);
	}
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
	
	void PrintWrite(){
		CalcCheckSum();
		mPrintWriter.print(dataPackage);
		mPrintWriter.flush();
		
		mPrintWriter.print(Bytes2String(dataPackage));
		mPrintWriter.flush();
		
	}
	
	//a thread that receive the message from server. 
	//the message could only contents (double)temperature
	//if necessary, you should convert the message to a double format (such as "22.5")
	private Runnable mRecvThread = new Runnable(){
		
		public void run(){
			Log.i("yangluo","in mRecvThread ");
			String temperature;
			try {
				while ( (temperature = mBufferedReader.readLine()) != null){
					Message msg = new Message();
					msg.obj = temperature;
					msg.what = 1;
					handler.sendMessage(msg);
					mPrintWriter.print(" get ");
					mPrintWriter.flush();
					Log.i("yangluo","mRecvThread "+(String) msg.obj);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("yangluo","mRecvThread failed ");

		}
	};
	
	
	
}
