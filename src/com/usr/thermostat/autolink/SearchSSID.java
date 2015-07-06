package com.usr.thermostat.autolink;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Message;
/**
 * @author 济南有人物联网    刘金启
 */
public class SearchSSID extends Thread {
	private Handler handler;
	private DatagramSocket socket;
	
	private final String IP = "255.255.255.255";
	private final int PORT = 49000;
	
	private boolean receive = true;
	
	public SearchSSID(Handler handler) {
		this.handler = handler;
		init();
	}

	public void init(){
		try {
			socket = new DatagramSocket(null);
			socket.setBroadcast(true);
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(PORT));
		} catch (SocketException e) {
			e.printStackTrace();
			sendErrorMsg("Search Thread init fail");
			return;
		} 
	}
			
	
	public void run() {
		if (socket == null) {
			return;
		}
	
		try {
			byte[] data = new byte[1024];
			DatagramPacket revPacket = new DatagramPacket(data, data.length);
			while (receive) {
				socket.receive(revPacket);
				if(null!=handler){
					byte[] realData = new byte[revPacket.getLength()];
					System.arraycopy(data, 0, realData,0, realData.length);
					Message msg =handler.obtainMessage(Tool.REC_DATA,realData);
					handler.sendMessage(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			socket.close();
		}
	}

	public void close() {
		if (socket == null)
			return;
		socket.close();
	}

	
	private void sendErrorMsg(String info){
		
	}
	
	/**
	 * 发送数据
	 * @param msg
	 */
	public void sendMsg(byte[] msg) {
		if (socket != null) {
			try {
				DatagramPacket sendPacket = new DatagramPacket(msg, msg.length,
						InetAddress.getByName(IP), PORT);
				socket.send(sendPacket);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("发送失败");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("发送失败");
			}

		}
	}
	
	public void setReceive(boolean receive) {
		this.receive = receive;
	}
}
