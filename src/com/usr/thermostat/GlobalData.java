package com.usr.thermostat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalData {
	
	private static GlobalData instance = null;
	public  List<byte[]> recvMsgList = new CopyOnWriteArrayList<byte[]>();
	
	private GlobalData ()
	{
		
	}
	
	public static GlobalData Instance()
	{
		if (null == instance)
		{
			instance = new GlobalData();
		}
		return instance;
	}

	public List<byte[]> getRecvMsgList() {
		return recvMsgList;
	}

	public void setRecvMsgList(List<byte[]> recvMsgList) {
		this.recvMsgList = recvMsgList;
	}
	

}
