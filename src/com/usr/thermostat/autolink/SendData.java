package com.usr.thermostat.autolink;
/*package com.usr.autolink;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.Handler;
*//**
 * @author 济南有人物联网    刘金启
 *//*
public class SendData extends Thread {
	private final int PORT = 48899;
	private DatagramSocket socket;
	// 是否接收消息
	private boolean receive = true;
	private String ip;

	private Handler handler;
	
	public SendData(String ip,Handler handler) {
		this.ip = ip;
		this.handler = handler;
		initSocket();
	}

	private void initSocket() {
		try {
			socket = new DatagramSocket(PORT);
			this.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	//+ok命令，连接设备
	public byte[] connectData() {
		String conntr = "+ok";
		byte[] data = conntr.getBytes();
		return data;
	}

	*//**
	 * 发送数据
	 * @param msg
	 *//*
	public void sendMsg(byte[] msg) {
		if (socket != null) {
			try {
				DatagramPacket sendPacket = new DatagramPacket(msg, msg.length,
						InetAddress.getByName(ip), PORT);
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

	public void run() {
		try {
			byte[] data = new byte[128];
			DatagramPacket revPacket = new DatagramPacket(data, data.length);
			while (receive) {
				socket.receive(revPacket);
				if(null!=handler){
					Message msg =handler.obtainMessage(MainActivity.RECEIVE_INFO, new String(data,"utf-8").trim());
					handler.sendMessage(msg);
				}
				System.out.println(new String(data,"utf-8").trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setReceive(boolean receive) {
		this.receive = receive;
	}

	*//**
	 * 在新的线程里，每隔40秒发送一次AT+W指令，保持连接
	 *//*
	public void keepLive() {
		new Thread() {
			public void run() {
				while (receive) {
					System.out.println("SendData---------->keepLive");
					String keepLiveStr = "AT+W\r\n";
					byte[] data = keepLiveStr.getBytes();
					sendMsg(data);
					try {
						Thread.sleep(40*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	*//**
	 * 断开连接指令
	 * @return
	 *//*
	public byte[] breakData() {
		String keepLiveStr = "AT+Q\r\n";
		byte[] data = keepLiveStr.getBytes();
		return data;
	}

	*//**
	 * 断开连接
	 *//*
	public void breakConnect() {
		receive = false;
		if (socket == null)
			return;
		socket.close();
	}
}
*/