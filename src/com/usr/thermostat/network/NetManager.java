package com.usr.thermostat.network;

import com.usr.thermostat.MainActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 *   判断网络连接状态
 * @author wjh
 *
 */
public class NetManager
{
	static NetManager s_m = null;
	
	private Context context;
	
	private Handler handler = null;
	
	private NetManager()
	{
		
	}
	
	public void init(Context ctx)
	{
		context = ctx;
	}
	
	public static synchronized NetManager instance()
	{
		if (s_m == null)
		{
			s_m = new NetManager();
		}
		return s_m;
	}
	
	/**
	 * 判断是否有网络连接
	 * @return
	 */
	public boolean isNetworkConnected()
	{
		if (context == null)
		{
			if (handler != null)
			{
				Message msg = new Message();
				msg.what = MainActivity.NETMANAGER_NOTIFY_ERROR;
				handler.sendMessage(msg);
			}
			
			return false;
		}
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null)
		{
			if (handler != null)
			{
				Message msg = new Message();
				msg.what = MainActivity.NETMANAGER_NOTIFY_ERROR;
				handler.sendMessage(msg);
			}
			return false;
		} 
		else
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
			{
				for (int i = 0; i < info.length; i++)
				{
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						if (handler != null)
						{
							Message msg = new Message();
							msg.what = MainActivity.NETMANAGER_NOTIFY_OK;
							handler.sendMessage(msg);
						}
						return true;
					}
				}
			}
		}
		
		if (handler != null)
		{
			Message msg = new Message();
			msg.what = MainActivity.NETMANAGER_NOTIFY_ERROR;
			handler.sendMessage(msg);
		}
		return false;
	}
	/**
	 * 判断WIFI网络是否可用
	 * @return
	 */
	public boolean isWifiConnected()
	{
		if (context != null)
		{
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null)
			{
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}
	/**
	 * 判断MOBILE网络是否可用
	 * @return
	 */
	public boolean isMobileConnected()
	{
		if (context != null)
		{
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null)
			{
				return mMobileNetworkInfo.isAvailable();
			}
		}
		return false;
	}
	
	public int getConnectedType()
	{
		if (context != null)
		{
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable())
			{
				return mNetworkInfo.getType();
			}
		}
		return -1;
	}
	public void release()
	{
		s_m = null;
		context = null;
		handler = null;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	
	
}
