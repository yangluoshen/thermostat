package com.usr.thermostat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Operations {
	
	public static Socket mSocket = null;
	private BufferedReader mBufferedReader  = null;
	private PrintWriter mPrintWriter = null;
	
	public static final int WIND_MODE_AUTO = 0;	
	public static final int WIND_MODE_1    = 1;
	public static final int WIND_MODE_2    = 2;
	public static final int WIND_MODE_3    = 3;

	
	public static final int MENU_MODE_1    = 0;
	public static final int MENU_MODE_2    = 1;
	public static final int MENU_MODE_3    = 2;
	
	public double recvTemperature = 0.0;
	Handler handler = null;
	Thread recvThread;
	


	public Operations(Handler handler) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		recvThread = new Thread(mRecvThread);
		
	}
	
	public boolean Connect(String ip, String port){
		
		try {
			int int_port = Integer.valueOf(port).intValue(); 
			mSocket = new Socket(ip, int_port);
			mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			mPrintWriter = new PrintWriter(mSocket.getOutputStream());
			recvThread.start();
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
		
		switch(mode){
		
		case MENU_MODE_1 :
			mPrintWriter.print("menu 1");
			mPrintWriter.flush();
			break;
		case MENU_MODE_2 :
			mPrintWriter.print("menu 2");
			mPrintWriter.flush();
			break;
		case MENU_MODE_3 :
			mPrintWriter.print("menu 3");
			mPrintWriter.flush();
			break;
		}
		
	}
	// this function called whenever the wind level changed
	void sendWindData(int mode){
		
		switch (mode){
		
		case WIND_MODE_AUTO :
			mPrintWriter.print(" wind auto");
			mPrintWriter.flush();
			break;
		case WIND_MODE_1 :
			mPrintWriter.print(" wind 1");
			mPrintWriter.flush();
			break;
		case WIND_MODE_2 :
			mPrintWriter.print(" wind 2");
			mPrintWriter.flush();
			break;
		case WIND_MODE_3 :
			mPrintWriter.print(" wind 4 ");
			mPrintWriter.flush();
			break;
		}
	}
	
	void sendUpTemperature(double temperature){
		
	}
	
	void sendDownTemprature(double temperature){
		
	}
	
	void sendCloseSignal(){
		
	}
	
	//a thread that receive the message from server. 
	//the message only contents (double)temperature
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
