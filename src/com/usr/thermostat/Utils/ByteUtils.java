package com.usr.thermostat.Utils;

public class ByteUtils {

	public ByteUtils(){
		
	}
	
	/**
	 * convert a byte to a char
	 * @param b
	 * @return
	 */
	static public char findHex(byte b) {
		int t = new Byte(b).intValue();
		t = t < 0 ? t + 16 : t;
		if ((0 <= t) &&(t <= 9)) {
		return (char)(t + '0');
		}
		return (char)(t-10+'A');
	}
	
	/**
	 * convert a byte[] to String
	 * @param bytes
	 * @return
	 */
	public StringBuffer Bytes2String(byte[] bytes){
		byte maskHigh = (byte) 0xf0;
		byte maskLow  = (byte) 0x0f;
		
		StringBuffer buf = new StringBuffer();
		buf.append("data above means: ");
		for (byte b : bytes){
			byte high, low;
			high = (byte) ((b & maskHigh)>>4);
			low  = (byte) ((b & maskLow));
			buf.append(findHex(high));
			buf.append(findHex(low));
			buf.append(" ");
		}
		
		return buf;
	}
	
	/**
	 * convert a byte[] to a chars[]
	 * @param bytes
	 * @return
	 */
	char[] Bytes2Chars(byte[] bytes){
		char[] chars = new char[bytes.length];
		
		for (int i=0; i<bytes.length; i++){
			if ( (bytes[i] & 0x80) == 0){
				chars[i] = (char)bytes[i];
			}else{
				chars[i] = (char)bytes[i];
				chars[i] &= 0x00ff;
			}
		}
		return chars;
	}
}
