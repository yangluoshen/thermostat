package com.usr.thermostat.Utils;

public class CalculationUtils {
	public static int calcRegistID(String strID)
	{
		int intID = Integer.valueOf(strID).intValue();
		return (intID - 65535)/255;
	}
}
