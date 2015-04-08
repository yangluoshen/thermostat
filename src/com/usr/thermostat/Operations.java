package com.usr.thermostat;

import java.io.BufferedReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
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
	
	private Context context;
	private String serverIp = "d2d.usr.cn";
	private int serverPort = 25565;
	
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
//	Thread recvThread;
	Thread getTemperatureRequest = null;
	static byte[] commands = {(byte) 0xa1,(byte) 0xa0,(byte) 0xa8};
	
	byte checkSum;
	byte command;
	byte ID0 = 0x01;
	byte ID1 = 0x01;
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

	private Operations(Context conetxt) {
		// TODO Auto-generated constructor stub
		this.context = context;
//		recvThread = new Thread(mRecvThread);
//		getTemperatureRequest = new Thread(mGetTemperatureRequest);
		//initDataPackage();		
	}
	/**
	 * unique instance
	 * @param ctx
	 * @param han
	 * @return
	 */
	public static Operations GetOperation(Context context){
		
		if (operationInstance == null){
			operationInstance = new Operations(context);
		}
		
		return operationInstance;
	}
	
	void initDataPackage(){
		dataPackage[0] = commands[1];
		dataPackage[1] = ID0;
		dataPackage[2] = ID1;
		dataPackage[3] = 0x18;  //data0
		dataPackage[4] = 0x00;
		dataPackage[5] = 0x2c;  //init temperature
		dataPackage[6] = 0x00;
		dataPackage[7] = 0x00;
		CalcCheckSum(dataPackage);
		
	}
	public byte[] Connect( String registID){
		byte[] readBuffer = new byte[8];
		try {
//			Log.i("yangluo","connect1");
//			int int_port = Integer.valueOf(port).intValue(); 
			
			//parse as ip
			InetAddress inetHost;
			inetHost = InetAddress.getByName(serverIp);
			String ip = inetHost.getHostAddress();
			
			mSocket = new Socket();
			mISA = new InetSocketAddress(ip, serverPort);
			mSocket.connect(mISA, socketTimeOut);
			initDataPackage();
			
			mDataInputeStream = new DataInputStream(mSocket.getInputStream());
			mPrintWriter = new DataOutputStream(mSocket.getOutputStream());
			
			int int_registID = Integer.valueOf(registID).intValue(); 
			sendRegist(int_registID);
			//send the init data
			mPrintWriter.write(dataPackage);
			
			
			if (mDataInputeStream.read(readBuffer) != -1){
				Operations.CalcCheckSum(readBuffer);
					
			}else{
				return null;
			}
			
			
			sendInitTime();
			//start the  thread
			if (!threadMarker){
//				recvThread.start();
//				getTemperatureRequest.start();
				threadMarker =  true;
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
//			Toast.makeText(this.context, "connect failed-- unknow host", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			Toast.makeText(this.context, "connect failed2", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			
			return null;
		}
		isConnected = true;
		return readBuffer;
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
			break;
		case MENU_MODE_WARM :
			dataPackage[3] |= 0x20;
			break;
		case MENU_MODE_VENTILATE :
			dataPackage[3] |= 0x40;
			break;
		}
		PrintWrite(SETMENU);
		
	}
	byte MenuDataParse(byte data, int mode){
		data &= menuResetByte;
				
		switch(mode){
		
		case MENU_MODE_COLD :
			data |= 0x00;
			break;
		case MENU_MODE_WARM :
			data |= 0x20;
			break;
		case MENU_MODE_VENTILATE :
			data |= 0x40;
			break;
		}
		return data;
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

			break;
		case WIND_MODE_LOW :
			dataPackage[3] |= 0x03;

			break;
		case WIND_MODE_MIDDLE :
			dataPackage[3] |= 0x02;
			break;
		case WIND_MODE_HIGH :
			dataPackage[3] |= 0x01;
			break;
		}
		Log.i("yangluo","in send windata "+dataPackage.toString());
		PrintWrite(SETWIND);
	}
	byte WindDataParse(byte data, int mode){
		data &= windResetByte;
		switch (mode){
			case WIND_MODE_AUTO :
				data |= 0x00;
				break;
			case WIND_MODE_LOW :
				data |= 0x03;
				break;
			case WIND_MODE_MIDDLE :
				data |= 0x02;
				break;
			case WIND_MODE_HIGH :
				data |= 0x01;
				break;
			}
		return data;
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
	byte SwitchStateParse(byte data, int state){
		data &= switchResetByte;
		if (state == MainActivity.SWITCHON){
			data |= 0x10;
//			isConnected = true;
//			getTemperatureRequest.start();
		}
		else if (state == MainActivity.SWITCHOFF){
			data |= 0x00;
//			isConnected = false;
		}
		return data;
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
	
	static void CalcCheckSum(byte[] bytes){
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
	
	public Handler getHandler() {
		return handler;
	}
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	public DataInputStream getmDataInputeStream() {
		return mDataInputeStream;
	}
	public void setmDataInputeStream(DataInputStream mDataInputeStream) {
		this.mDataInputeStream = mDataInputeStream;
	}
	public DataOutputStream getmPrintWriter() {
		return mPrintWriter;
	}
	public void setmPrintWriter(DataOutputStream mPrintWriter) {
		this.mPrintWriter = mPrintWriter;
	}
	
	public void setDataPackgeID0AndID1(int id0,int id1){
		dataPackage[1] = (byte) id0;
		dataPackage[2] = (byte) id1;
	}
	public void sendRegist(int registID){
		long id = registID*65536 +65535 - registID;
		
		byte[] data = new byte[4];
		data[3] = (byte) (id%256);
		data[2] = (byte) ((id>>8)%256);
		data[1] = (byte) ((id>>16)%256);
		data[0] = (byte) ((id>>24)%256);
		
		try {
			mPrintWriter.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
}
