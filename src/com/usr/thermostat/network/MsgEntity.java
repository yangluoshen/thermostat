package com.usr.thermostat.network;

import android.os.Handler;


/**
 * �洢����socket���࣬����Ҫ���͵�BufTest���Լ���Ӧ�ķ��ؽ����Handler
 * @author Administrator
 *
 */
public class MsgEntity
{
	//Ҫ���͵���Ϣ
	private byte [] bytes;
	//�������handler
//	private Handler mHandler;
	
	public MsgEntity( byte [] bytes)
	{	
		 this.bytes = bytes;
//		 mHandler = handler;
	}
	
	public byte []  getBytes()
	{
		return this.bytes;
	}
	

}
