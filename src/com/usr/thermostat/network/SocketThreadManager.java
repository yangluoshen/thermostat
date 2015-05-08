package com.usr.thermostat.network;

import android.os.Handler;


public class SocketThreadManager
{
	
	private static SocketThreadManager s_SocketManager = null;
	
	private SocketInputThread mInputThread = null;
	
	private SocketOutputThread mOutThread = null;
	
//	private SocketHeartThread mHeartThread = null;

	
	// 获取单例
	public static SocketThreadManager sharedInstance()
	{
		if (s_SocketManager == null)
		{
			s_SocketManager = new SocketThreadManager();
			s_SocketManager.startThreads();
		}
		return s_SocketManager;
	}
	
	// 单例，不允许在外部构建对象
	private SocketThreadManager()
	{
//		mHeartThread = new SocketHeartThread();
		mInputThread = new SocketInputThread();
//		mInputThread = SocketInputThread.InputInstance();
		mOutThread = new SocketOutputThread();
	}
	
	/**
	 * 启动线程
	 */
	
	private void startThreads()
	{
//		mHeartThread.start();
		mInputThread.setStart(true);
		mInputThread.start();
		mOutThread.setStart(true);
		mOutThread.start();
		// mDnsthread.start();
	}
	
	/**
	 * stop线程
	 */
	public void stopThreads()
	{
//		mHeartThread.stopThread();
		mInputThread.setStart(false);
		try
		{
			mInputThread.notify();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		mInputThread.interrupt();
		mOutThread.setStart(false);
		try
		{
			mOutThread.notify();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
//		
		mOutThread.interrupt();
		mInputThread = null;
		mOutThread = null;
	}
	
	public static void releaseInstance()
	{
		if (s_SocketManager != null)
		{
			s_SocketManager.stopThreads();
			s_SocketManager = null;
		}
	}
	
	public void sendMsg(byte [] buffer, Handler handler)
	{
		MsgEntity entity = new MsgEntity(buffer, handler);
		mOutThread.addMsgToSendList(entity);
	}


	
	
}
