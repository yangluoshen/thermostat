package com.usr.thermostat.beans;

public class RoomItemInfo {
	private int id;
	
	private String name;
	
	private String registid;
	
	private int wind;
	
	private int mode;
	
	private String settemp;
	
	private int isoperated;
	
	
	public RoomItemInfo()
	{
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRegistid() {
		return registid;
	}

	public void setRegistid(String registid) {
		this.registid = registid;
	}

	public int getWind() {
		return wind;
	}

	public void setWind(int wind) {
		this.wind = wind;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public String getSettemp() {
		return settemp;
	}

	public void setSettemp(String settemp) {
		this.settemp = settemp;
	}

	public int getIsoperated() {
		return isoperated;
	}

	public void setIsoperated(int isoperated) {
		this.isoperated = isoperated;
	}


	
}
