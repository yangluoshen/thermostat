package com.usr.thermostat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalData {
	
	private int currentRoomID;
	private static GlobalData instance = null;
	public  List<byte[]> recvMsgList = new CopyOnWriteArrayList<byte[]>();
	
//	private Map<String , StateRecord> stateMap = new HashMap<String, StateRecord>();
	
	
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
		return this.recvMsgList;
	}

	public void setRecvMsgList(List<byte[]> recvMsgList) {
		this.recvMsgList = recvMsgList;
	}
	

//	public Map<String, StateRecord> getStateMap() {
//		return this.stateMap;
//	}
//
//	public void setStateMap(Map<String, StateRecord> stateMap) {
//		this.stateMap = stateMap;
//	}
	




	public int getCurrentRoomID() {
		return this.currentRoomID;
	}

	public void setCurrentRoomID(int currentRoomID) {
		this.currentRoomID = currentRoomID;
	}





	public static class StateRecord 
	{
		private double setTemperature;
		private int setWind;
		private int setMode;
		public double getSetTemperature() {
			return setTemperature;
		}
		public void setSetTemperature(double setTemperature) {
			this.setTemperature = setTemperature;
		}
		public int getSetWind() {
			return setWind;
		}
		public void setSetWind(int setWind) {
			this.setWind = setWind;
		}
		public int getSetMode() {
			return setMode;
		}
		public void setSetMode(int setMode) {
			this.setMode = setMode;
		}
		
	}
	
}
