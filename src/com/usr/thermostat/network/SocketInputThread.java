package com.usr.thermostat.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.os.Handler;
import android.os.Message;

import com.usr.thermostat.GlobalData;
import com.usr.thermostat.MainActivity;
import com.usr.thermostat.Operations;

/**
 * 客户端读消息线程
 * 
 * @author yangluo
 * 
 */
public class SocketInputThread extends Thread
{
	private static final int ADD_DATA = 1;
	private boolean isStart = true;
	
	private static String tag = "socket";
	
//	private List<byte[]> recvMsgList = new CopyOnWriteArrayList<byte[]>();
//	private LinkedList<byte[]> recvMsgList = new LinkedList<byte[]>();
//	private byte[] DataBuffer = new byte[8];
	
	// private MessageListener messageListener;// 消息监听接口对象
	
	public   SocketInputThread()
	{
	}
	
	public void setStart(boolean isStart)
	{
		this.isStart = isStart;
	}
	/**
	 * 从接收缓冲区中读取一个数据包
	 * @return
	 */
//	public byte[] readByteArray()
//	{
//		if (recvMsgList.size()<=0)
//		{
//			return null;
//		}
//		byte[] data = new byte[8];
//		synchronized (recvMsgList) 
//		{
//			data = recvMsgList.get(recvMsgList.size()-1);
////			recvMsgList.clear();
//		}
//		
//		return data;
//	}
	
	@Override
	public void run()
	{
		while (isStart)
		{
			// 手机能联网，读socket数据
			if (NetManager.instance().isNetworkConnected())
			{
				
				if (!TCPClient.instance().isConnect())
				{
					
					try
					{
						sleep(Const.SOCKET_SLEEP_SECOND * 1000);
					} 
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				readSocket();
				
				// 如果连接服务器失败,服务器连接失败，sleep固定的时间，能联网，就不需要sleep
				
				
				
			}
		}
	}
	
	public void readSocket()
	{
		Selector selector = TCPClient.instance().getSelector();
		if (selector == null)
		{
			return;
		}
		try
		{
			// 如果没有数据过来，一直柱塞
			while (selector.select() > 0)
			{
				for (SelectionKey sk : selector.selectedKeys())
				{
					// 如果该SelectionKey对应的Channel中有可读的数据
					if (sk.isReadable())
					{
						// 使用NIO读取Channel中的数据
						SocketChannel sc = (SocketChannel) sk.channel();
						ByteBuffer buffer = ByteBuffer.allocate(8);
						
						try
						{
							sc.read(buffer);
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							// continue;
						}
						buffer.flip();
						
						//处理收到的数据****
						byte[] dataTmp = new byte[8];
						for (int i = 0; i < buffer.limit(); i++)
						{
							dataTmp[i] = buffer.get();
						}
						
						//检查校验和
						byte responseCheckSum = dataTmp[7];
						Operations.CalcCheckSum(dataTmp);
						if (responseCheckSum == dataTmp[7]){
//							synchronized (recvMsgList) 
//							{
//								recvMsgList.add(dataTmp);
							synchronized (GlobalData.Instance().getRecvMsgList()) {
								GlobalData.Instance().getRecvMsgList().add(dataTmp);
							}
							
//							DataBuffer = dataTmp;
//							Message msg = new Message();
//							msg.what = ADD_DATA;
//							msg.obj = dataTmp;
//							mainHandler.sendMessage(msg);
							
//							}
						}
						

						buffer.clear();
						buffer = null;
						
						try
						{
							// 为下一次读取作准备
							sk.interestOps(SelectionKey.OP_READ);
							// 删除正在处理的SelectionKey
							selector.selectedKeys().remove(sk);
							
						} catch (CancelledKeyException e)
						{
							e.printStackTrace();
						}
						
					
					}
				}
				
//				synchronized (this)
//				{
//					try
//					{
//						wait();
//						
//					} catch (InterruptedException e)
//					{
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}// 发送完消息后，线程进入等待状态
//				}
			}
			// selector.close();
			// TCPClient.instance().repareRead();
			
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClosedSelectorException e2)
		{
		}
	}
	
//	Handler mainHandler = new Handler()
//	{
//
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
////			super.handleMessage(msg);
//			if (msg.what == ADD_DATA)
//			{
////				DataBuffer = (byte[]) msg.obj;
////				byte[] d = DataBuffer;
////				int a = 1;
//			}
//		}
//		
//	};
	
}
