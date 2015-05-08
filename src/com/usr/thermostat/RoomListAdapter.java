package com.usr.thermostat;

import java.util.List;

import com.usr.thermostat.Utils.CalculationUtils;
import com.usr.thermostat.beans.RoomItemInfo;
import com.usr.thermostat.network.NetManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RoomListAdapter extends BaseAdapter {
	private Context context;
	private List<RoomItemInfo> list = null;
	private ViewHolder holder;
	private int currentPos;
	
	public RoomListAdapter(Context ctx, List<RoomItemInfo> l)
	{
		this.context = ctx;
		this.list = l;
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
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (position == list.size()-1)
		{
			holder.rl_itemBg.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.room_add));
			holder.tv_temp.setVisibility(View.GONE);
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
			holder.tv_temp.setText(list.get(position).getName());
			
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
					return false;
				}
			};
			
			OnClickListener intentOperationListener = new OnClickListener() {
				private int pos = currentPos;
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					NetManager.instance().init(context);
					String registID = list.get(pos).getRegistid();
					int int_registID = CalculationUtils.calcRegistID(registID);
					
					if (Operations.GetOperation().Connect(int_registID))
					{
						NetManager.instance().release();
						Intent _intent = new Intent(context,MainActivity.class);
						_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
						context.startActivity(_intent);
					}
					else {
						Toast.makeText(context, "connect failed !", Toast.LENGTH_SHORT).show();
					}
					
					
				}
			};
			
			holder.rl_itemBg.setOnClickListener(intentOperationListener);
			holder.tv_temp.setOnClickListener(intentOperationListener);
			holder.rl_itemBg.setOnLongClickListener(editListener);
			holder.tv_temp.setOnLongClickListener(editListener);
			
		}
		
		
		
		return convertView;
	}
	
	class ViewHolder 
	{
		TextView tv_temp;
		RelativeLayout rl_itemBg;
	}

}
