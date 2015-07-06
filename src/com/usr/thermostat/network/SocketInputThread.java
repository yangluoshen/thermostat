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
 * �ͻ��˶���Ϣ�߳�
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
	
	// private MessageListener messageListener;// ��Ϣ�����ӿڶ���
	
	public   SocketInputThread()
	{
	}
	
	public void setStart(boolean isStart)
	{
		this.isStart = isStart;
	}
	/**
	 * �ӽ��ջ������ж�ȡһ�����ݰ�
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
			// �ֻ�����������socket����
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
				
				// ������ӷ�����ʧ��,����������ʧ�ܣ�sleep�̶���ʱ�䣬���������Ͳ���Ҫsleep
				
				
				
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
			// ���û�����ݹ�����һֱ����
			while (selector.select() > 0)
			{
				for (SelectionKey sk : selector.selectedKeys())
				{
					// �����SelectionKey��Ӧ��Channel���пɶ�������
					if (sk.isReadable())
					{
						// ʹ��NIO��ȡChannel�е�����
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
						
						//�����յ�������****
						byte[] dataTmp = new byte[8];
						for (int i = 0; i < buffer.limit(); i++)
						{
							dataTmp[i] = buffer.get();
						}
						
						//���У���
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
							// Ϊ��һ�ζ�ȡ��׼��
							sk.interestOps(SelectionKey.OP_READ);
							// ɾ�����ڴ����SelectionKey
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
//					}// ��������Ϣ���߳̽���ȴ�״̬
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
