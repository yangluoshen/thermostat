package com.usr.thermostat.network;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class NetworkDetectorService extends Service{
	private Binder binder = new MyBinder();
	private NetworkReceiver mReceiver = new NetworkReceiver();
	private static GetConnectState onGetConnectState;
	private static boolean isConnected = true;
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
//		super.onCreate();
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mReceiver, mFilter);
	}

	public static class NetworkReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action  = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
				Timer timer = new Timer();
				timer.schedule(new QunXTask(context),0, 3*1000);
			}
			
		}
		
	}
	public interface GetConnectState{
		/**whenever network changed, broadcast network state through this method
		 * this interface instanced in activities
		 */
		public void GetState(boolean isConnected);
	}
	
	public void setOnGetConnectState(GetConnectState onGetConnectState){
		this.onGetConnectState = onGetConnectState;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}
	
	public class MyBinder extends Binder{
		public NetworkDetectorService getService(){
			return NetworkDetectorService.this;
		}
	}
	
	public static class QunXTask extends TimerTask
	{
		private Context context;

		public QunXTask(Context context)
		{
			this.context = context;
		}

		@Override
		public void run()
		{
			if (isNetworkConnected(context) || isWifiConnected(context))
			{
				isConnected = true;
			}
			else
			{
				isConnected = false;
			}
			if (onGetConnectState != null)
			{
				onGetConnectState.GetState(isConnected); // 通知网络状态改变
//				Log.i("mylog", "通知网络状态改变:" + isConnected);
			}
		}

		/*
		 * 判断是3G否有网络连接
		 */
		private boolean isNetworkConnected(Context context)
		{
			if (context != null)
			{
				ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//				NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
//				if (mNetworkInfo != null)
//				{
//					return mNetworkInfo.isAvailable();
//				}
				return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
			}
			return false;
		}

		/*
		 * 判断是否有wifi连接
		 */
		private boolean isWifiConnected(Context context)
		{
			if (context != null)
			{
				ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//				NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//				if (mWiFiNetworkInfo != null)
//				{
//					return mWiFiNetworkInfo.isAvailable();
//				}
				return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
			}
			return false;
		}
	}
	
	
	

}
