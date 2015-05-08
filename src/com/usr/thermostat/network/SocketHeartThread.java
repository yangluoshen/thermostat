package com.usr.thermostat.network;

import java.io.IOException;

import android.text.TextUtils;


class SocketHeartThread extends Thread
{
	boolean isStop = false;
	boolean mIsConnectSocketSuccess = false;
	static SocketHeartThread s_instance;
	
	private TCPClient mTcpClient = null;
	
	static final String tag = "SocketHeartThread";
	
	public static synchronized SocketHeartThread instance()
	{
		if (s_instance == null)
		{
			s_instance = new SocketHeartThread();
		}
		return s_instance;
	}
	
	public SocketHeartThread()
	{
	   TCPClient.instance();
				// 连接服务器
	//	mIsConnectSocketSuccess = connect();

	}

	public void stopThread()
	{
		isStop = true;
	}
	
	/**
	 * 连接socket到服务器, 并发送初始化的Socket信息
	 * 
	 * @return
	 */
	
	
	private boolean reConnect()
	{
		return TCPClient.instance().reConnect();
	}

	public void run()
	{
		isStop = false;
		while (!isStop)
		{
				// 发送一个心跳包看服务器是否正常
				boolean canConnectToServer = TCPClient.instance().canConnectToServer();
				
				if(canConnectToServer == false){
					reConnect();
				}
				try
				{
					Thread.sleep(Const.SOCKET_HEART_SECOND * 1000);
					
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
	}
}
