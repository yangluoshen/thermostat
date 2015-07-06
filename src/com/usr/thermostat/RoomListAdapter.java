package com.usr.thermostat;

import java.util.LinkedList;
import java.util.List;

import com.usr.thermostat.Utils.CalculationUtils;
import com.usr.thermostat.Utils.CommonUtils;
import com.usr.thermostat.beans.RoomItemInfo;
import com.usr.thermostat.network.NetManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract.Constants;
import android.text.NoCopySpan.Concrete;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RoomListAdapter extends BaseAdapter {
	private Context context;
	private List<RoomItemInfo> list = null;
	private ViewHolder holder;
	private int currentPos;
	private LinkedList<ImageView> imageviewList = new LinkedList<ImageView>();
//	private Handler handler;
	
	public RoomListAdapter(Context ctx, List<RoomItemInfo> l)
	{
		this.context = ctx;
		this.list = l;
//		this.handler = handler;
		
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return this.list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		currentPos = position;
		
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.room_item, null);
			holder.tv_temp = (TextView) convertView.findViewById(R.id.room_temp_tv);
			holder.rl_itemBg = (RelativeLayout) convertView.findViewById(R.id.room_item_bg_rl);
			holder.iv_roomWind = (ImageView) convertView.findViewById(R.id.room_item_wind);
			imageviewList.add(holder.iv_roomWind);
			holder.tv_name = (TextView) convertView.findViewById(R.id.room_item_name);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (position == list.size()-1)
		{
			//将gridView 最后一个元素设置为添加按钮
			holder.rl_itemBg.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.add_room_sel));
			holder.tv_temp.setVisibility(View.GONE);
			holder.iv_roomWind.setVisibility(View.GONE);
			holder.tv_name.setText("ADD");
			
			holder.rl_itemBg.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent _intent = new Intent(context, ConnectActivity.class);
					_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
					Bundle _bundle = new Bundle();
					_bundle.putString("mode",Constant.INTENT_MODE_ADD);
					_intent.putExtras(_bundle);
					context.startActivity(_intent);
					
				}
			});
			
		}
		else
		{
			//如果当前元素不是最后一个的话，则为房间按钮
//			String key = ""+list.get(position).getId();
			int isOperated = list.get(position).getIsoperated();
			
			//设置房间名称
			holder.tv_name.setText(list.get(position).getName());
			
			//设置房间图标背景色：将当前的房间的mode设置房间图标背景色
			int background = 0;
			//判断此房间是否被设置过
			if (isOperated == 1)
			{
				int mode = list.get(position).getMode();
				
				if (mode == Operations.MENU_MODE_VENTILATE)
				{
					background = R.drawable.green_sel;
				}
				else if (mode == Operations.MENU_MODE_WARM)
				{
					background = R.drawable.orange;
				}
				else 
				{
					background = R.drawable.blue_sel;
				}
			}
			else
			{
				background = R.drawable.black;
			}
			
			holder.rl_itemBg.setBackgroundDrawable(context.getResources().getDrawable(background));
			//设置风速图标：根据房间风速，设置风速图标
			int windType = 0;
			
			//先判断此房间是否被设置过
			if(isOperated == 1)
			{
				int wind = list.get(position).getWind();
				
				if (wind == Operations.WIND_MODE_LOW)
				{
					windType = R.drawable.wind1;
				}
				else if (wind == Operations.WIND_MODE_MIDDLE)
				{
					windType = R.drawable.wind2;
				}
				else if (wind == Operations.WIND_MODE_HIGH)
				{
					windType = R.drawable.wind3;
				}
				else 
				{
					windType = R.drawable.windauto;
				}
//				holder.iv_roomWind.setVisibility(View.VISIBLE);
				holder.iv_roomWind.setBackgroundDrawable(context.getResources().getDrawable(windType));
				holder.iv_roomWind.setVisibility(View.VISIBLE);
			}
			else 
			{
				//若没有被设置过，则不显示
				holder.iv_roomWind.setVisibility(View.INVISIBLE);
			}
			
			
			//设置房间设置温度
			//先判断此房间是否被设置过
			String setTemp = "0.0";
			if(isOperated == 1)
			{
				setTemp = list.get(position).getSettemp();
			}
			
			holder.tv_temp.setText(""+setTemp);
			//房间按钮的长按事件， 长按跳转到编辑界面
			OnLongClickListener editListener = new OnLongClickListener() {
				private int pos = currentPos;
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					
					Intent _intent = new Intent(context, ConnectActivity.class);
					_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
					Bundle _bundle = new Bundle();
					_bundle.putString("mode",Constant.INTENT_MODE_MODIFY);
					_bundle.putInt("id", list.get(pos).getId());
					_bundle.putString("room", list.get(pos).getName());
					_bundle.putString("registid", list.get(pos).getRegistid());
					_intent.putExtras(_bundle);
					context.startActivity(_intent);
//					releaseAllImageView();
					return false;
				}
			};
			//房间按钮的点击事件，单击进入操作界面
			OnClickListener intentOperationListener = new OnClickListener() {
				private int pos = currentPos;
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					NetManager.instance().init(context);
					String registID = list.get(pos).getRegistid();
					int int_registID = CalculationUtils.calcRegistID(registID);
					
					GlobalData.Instance().setCurrentRoomID(list.get(pos).getId());
//					Operations.GetOperation().setHandler(handler);
//					releaseAllImageView();
					Operations.GetOperation().Connect(int_registID);
					
//					if ()
//					{
////						NetManager.instance().release();
//////						//跳转到mainactivity前的准备
//////						CommonUtils.readyIntentMain(int_registID);
////						
////						Intent _intent = new Intent(context, MainActivity.class);
////						_intent.putExtra("id", list.get(pos).getId());
////						_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
////						context.startActivity(_intent);
//					}
//					else {
//						Toast.makeText(context, "connect failed !", Toast.LENGTH_SHORT).show();
//					}
					
					
				}
			};
			
			holder.rl_itemBg.setOnClickListener(intentOperationListener);
			holder.tv_temp.setOnClickListener(intentOperationListener);
			holder.rl_itemBg.setOnLongClickListener(editListener);
			holder.tv_temp.setOnLongClickListener(editListener);
			
		}
		
		return convertView;
	}
	
	void releaseImageView(ImageView imageview)
	{
		Drawable d = imageview.getDrawable();  
		if (d != null) d.setCallback(null);  
		imageview.setImageDrawable(null);  
		imageview.setBackgroundDrawable(null);
	}
	void releaseAllImageView()
	{
		for (ImageView iv : imageviewList)
		{
			releaseImageView(iv);
		}
	}
	
	
	class ViewHolder 
	{
		TextView tv_temp;
		RelativeLayout rl_itemBg;
		ImageView iv_roomWind;
		TextView tv_name;
	}

}
