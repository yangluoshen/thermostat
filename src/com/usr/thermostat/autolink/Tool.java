package com.usr.thermostat.autolink;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Tool {
	public static final int REC_DATA = 0x01;
	
	/**
	 * 校验数据对不对
	 * @param data
	 * @return
	 */
	public static boolean checkData(byte[] data){
		System.out.println("data---->"+bytesToHexString(data));
		int sum = 0;
	    for(int i=1;i<data.length-1;i++){
	    	sum += data[i];
	    }
	    System.out.println("sum&0xff---------->"+(sum & 0xff));
	    return (sum&0xff) == data[data.length-1];
	}

	
	/**
	 * 解析返回SSID列表指令
	 */
	public static ArrayList<Item> decode_81_data(byte[] data) {
		ArrayList<Item> items = new ArrayList<Item>();
		byte[] ssidData = new byte[data.length - 6];
		System.arraycopy(data, 5, ssidData, 0, ssidData.length);
		int last = 0 ;
		for(int i=0;i<ssidData.length-1;i++){
			//每次2个字节的遍历数组，是否是0d0a(\r\n)
			byte[] two = new byte[2];
			two[0] = ssidData[i] ;
			two[1] = ssidData[i+1];
			String zdza = new String(two);
			if(zdza.equals("\r\n")){//如果是0d0a那么根据协议提取出名字和信号强度
				Item item = new Item();
				byte[] name = new byte[i-2-last];//跳过信号强度2个字节
				System.arraycopy(ssidData,last,name,0,name.length);
                item.setName(new String(name).trim());
                int dbm = ssidData[i-1]&0xff;//解析信号强度
                item.setDbm(dbm);
                items.add(item);
                last = i+2;//改变last的值，赋值为下一个代表ssid字节的开始索引
			}
		}
		
		return items;
	}

	public static int[] decode_82_data(byte[] data) {
		int[] values = new int[2];
		values[0] = data[4] & 0xff;
		values[1] = data[5] & 0xff;
		return values;
	}

	/**
	 * 生产设定ssid及密码指令
	 * 
	 * @param ssid
	 * @param pasd
	 * @param index
	 *            ssid所在的序列 暂时为起作用，赋0即可
	 * @return
	 */
	public static byte[] generate_02_data(String ssid, String pasd, int index) {
		try {
			String str = ssid + "\r\n" + pasd;
			byte[] strBytes = str.getBytes("utf-8");
			byte[] data = new byte[1 + 1 + strBytes.length];
			data[0] = 0x02;
			data[1] = (byte) (index & 0xff);
			System.arraycopy(strBytes, 0, data, 2, strBytes.length);
			return generateCmd(data);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据所给的关键字生产指令
	 * 
	 * @return
	 */
	public static byte[] generateCmd(byte[] key) {
		// 根据协议：包头1 + 长度 2 + 命令 + 参数+ 校验1
		int length = 4 + key.length;
		byte[] cmd = new byte[length];
		cmd[0] = (byte) 0xff;

		byte[] lengthBytes = int2byte(key.length);
		cmd[1] = lengthBytes[1];
		cmd[2] = lengthBytes[0];

		// 校验位累加
		cmd[length - 1] = (byte) (cmd[1] + cmd[2]);
		for (int i = 0; i < key.length; i++) {
			cmd[i + 3] = key[i];
			cmd[length - 1] += key[i];
		}
		return cmd;
	}

	/**
	 * int 转byte[]
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] int2byte(int res) {
		byte[] targets = new byte[4];
		targets[0] = (byte) (res & 0xff);// 最低位
		targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
		targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
		targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
		return targets;
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv+" ");
		}
		return stringBuilder.toString();
	}

	public static int byteToInt2(byte[] b) {

		int mask = 0xff;
		int temp = 0;
		int n = 0;
		for (int i = 0; i < 4; i++) {
			n <<= 8;
			temp = b[i] & mask;
			n |= temp;
		}
		return n;
	}
}
