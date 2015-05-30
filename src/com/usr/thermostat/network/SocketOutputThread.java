package com.usr.thermostat.network;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import com.usr.thermostat.CountDownTimer;
import com.usr.thermostat.MainActivity;

import android.os.Handler;
import android.os.Message;


/**
 * 客户端写消息线程
 * 
 * @author yangluo
 * 
 */
public class SocketOutputThread extends Thread
{
	
	private Long taskTimeChip = 150L;
	public static final int NOTIFY = 0;
	private boolean isStart = true;
	private static String tag = "socketOutputThread";
	private List<MsgEntity> sendMsgList;
	private Timer timer = new Timer();
//	private CountDownTimer countDown;
	
	
	public SocketOutputThread( )
	{

		sendMsgList = new CopyOnWriteArrayList<MsgEntity>();
		
//		countDown = new CountDownTimer();
//		countDown.setCountDownMax(1);
//		countDown.setCountDown(1);
//		countDown.setFlag(true);
//		countDown.setInitFlag(true);
//		countDown.setHandler(mainHandler);
//		countDown.setHandlerMsg(NOTIFY);
//		countDown.startTimer();
	}
	
	public void setStart(boolean isStart)
	{
		this.isStart = isStart;
		if (isStart)
		{
//			countDown.startTimer();
			timer.schedule(new NotifyTask(), 0, taskTimeChip);
		}
		else
		{
			timer.cancel();
		}
		notifyThread();
	}
	
	class NotifyTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			notifyThread();
		}
		
	}
	
	public void notifyThread()
	{
		synchronized (this)
		{
			try
			{
				notify();
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	// 使用socket发送消息
	public boolean sendMsg(byte[] msg) throws Exception
	{
				
		
		if (msg == null)
		{
			return false;
		}
		
		try
		{
			TCPClient.instance().sendMsg(msg);
			
		} catch (Exception e)
		{
			throw (e);
		}
		
		return true;
	}
	
	// 使用socket发送消息
	public void addMsgToSendList(MsgEntity msg) 
	{

		synchronized (this.sendMsgList)
		{
			this.sendMsgList.add(msg);
//			notify();
		}
	}
	
	@Override
	public void run()
	{
		while (isStart)
		{
			// 锁发送list
			synchronized (sendMsgList)
			{
				// 发送消息
				if (!sendMsgList.isEmpty()){

					MsgEntity msg = sendMsgList.get(sendMsgList.size()-1);
					Handler handler = msg.getHandler();
					try
					{
						
						sendMsg(msg.getBytes());
						sendMsgList.remove(msg);
						// 成功消息，通过hander回传
						if (handler != null)
						{
							Message message =  new Message();
							message.obj = msg.getBytes();
							message.what = MainActivity.SEND_MESSAGE_SUCCESS;
						   handler.sendMessage(message);
						//	handler.sendEmptyMessage(1);
						}
						
					} catch (Exception e)
					{
						e.printStackTrace();
						// 错误消息，通过hander回传
						if (handler != null)
						{
							Message message =  new Message();
							message.obj = msg.getBytes();
							message.what = MainActivity.SEND_MESSAGE_FAILED;
						    handler.sendMessage(message);
						}
					}
					sendMsgList.clear();
				}
			}
			
			synchronized (this)
			{
				try
				{
					wait();
					
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// 发送完消息后，线程进入等待状态
			}
		}
		
	}
	Handler mainHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == NOTIFY)
			{
				notifyThread();
			}
		}
		
	};

	
	
}
