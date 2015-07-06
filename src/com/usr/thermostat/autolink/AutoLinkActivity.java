package com.usr.thermostat.autolink;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.usr.thermostat.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * @author usr_liujinqi
 */
public class AutoLinkActivity extends Activity implements OnClickListener{
    private EditText etSsid;
    private EditText etPasd;
    private WifiManager.MulticastLock lock;
    private SearchSSID searchSSID;
    private SendMsgThread smt;
    
    private ProgressDialog dialog ;
    
    public final int RESQEST_SSID_LIST = 1;
    
   //获得ssid列表指令   
  	private final byte[] searchCode= new byte[]{(byte) 0xff,0x00,0x01,0x01,0x02};
  	
  	private Handler handler = new Handler(){
  		public void handleMessage(Message msg) {
  			switch (msg.what) {
			case Tool.REC_DATA://解析接收到的数据
				 byte[] data = (byte[]) msg.obj;
				 Tool.bytesToHexString(data);
				 decodeData(data);
				break;

			default:
				break;
			}
  		}
  	};
  	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.autolink);
		
		WifiManager manager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		lock = manager.createMulticastLock("fawifi");
		lock.acquire();
		
		etSsid = (EditText) findViewById(R.id.et_ssid);
		etPasd = (EditText) findViewById(R.id.et_pasd);
		
		dialog = new ProgressDialog(this);
		dialog.setMessage("Search...");
		
		searchSSID = new SearchSSID(handler);
		searchSSID.start();
		
		smt = new SendMsgThread(searchSSID);
		smt.start();
	}

	@Override
	public void onClick(View v) {
		
		if(v.getId() == R.id.btn_search){
			dialog.show();
			//发送收索SSID指令
			smt.putMsg(searchCode);
		}
		
		
		if(v.getId() == R.id.btn_ok){
	       String ssid = etSsid.getText().toString();
	       String pasd = etPasd.getText().toString();
	       if(TextUtils.isEmpty(ssid)){
	    	   UIUtil.toastShow(this,"please input ssid");
	    	   return;
	       }
	       
	       if(TextUtils.isEmpty(pasd)){
	    	   UIUtil.toastShow(this,"please input pasd");
	    	   return;
	       }
	       
	       byte[] data = Tool.generate_02_data(ssid, pasd, 0);
	       smt.putMsg(data);
		}
	}
	
	
	
	
	/**
	 * 解析数据
	 * @param data
	 */
	private void decodeData(byte[] data){
		if((data[0]&0xff) != 0xff)//如果接收到的数据不是0xff开头,那么丢弃
			return ;
		switch (data[3]&0xff) {
		case 0x81://解析返回列表指令
			dialog.dismiss();
			ArrayList<Item> ssids = Tool.decode_81_data(data);
			if(ssids.size() != 0){
				Intent intent = new Intent(AutoLinkActivity.this,SsidListAct.class);
				intent.putExtra("ssids", ssids);
				startActivityForResult(intent, RESQEST_SSID_LIST);
			}
			break;
		case 0x82://返回校验结果
			int[] values = Tool.decode_82_data(data);
			if(values[0] == 0)
				UIUtil.toastShow(this,R.string.no_ssid);
			else if(values[1] == 0)
				UIUtil.toastShow(this,R.string.error_pasd_length);
			else if(values[0] == 1 && values[1] == 1)
				UIUtil.toastShow(this,R.string.confing_end);
			break;
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == RESQEST_SSID_LIST && data != null){
			String ssid = data.getStringExtra("ssid");
			etSsid.setText(ssid);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//退出处理
		lock.release();
		smt.setSend(false);
		searchSSID.setReceive(false);
		searchSSID.close();
	}
	
	
	/**
	 * 发送消息的队列，每次发送数据时，只需要调用putMsg(byte[] data)方法
	 * @author usr_liujinqi
	 *
	 */
	private class SendMsgThread extends Thread {
		// 发送消息的队列
		private Queue<byte[]> sendMsgQuene = new LinkedList<byte[]>();
		// 是否发送消息
		private boolean send = true;

		private SearchSSID ss;
		
		public SendMsgThread(SearchSSID ss){
			this.ss = ss;
		}
		
		public synchronized void putMsg(byte[] msg) {
			// 唤醒线程
			if (sendMsgQuene.size() == 0)
				notify();
			sendMsgQuene.offer(msg);
		}

		public void run() {
			synchronized (this) {
				while (send) {
					// 当队列里的消息发送完毕后，线程等待
					while (sendMsgQuene.size() > 0) {
						byte[] msg = sendMsgQuene.poll();
						if (ss != null)
							ss.sendMsg(msg);
					}
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		public void setSend(boolean send) {
			this.send = send;
		}
	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		super.onBackPressed();
	}
	
	
}
