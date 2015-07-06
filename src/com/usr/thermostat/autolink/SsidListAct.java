package com.usr.thermostat.autolink;

import java.util.ArrayList;

import com.usr.thermostat.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SsidListAct extends Activity {
	private ArrayList<Item> ssids;
	private ListView lv;
    @SuppressWarnings("unchecked")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ssid_list);
    	
    	ssids = (ArrayList<Item>) getIntent().getSerializableExtra("ssids");
    	lv = (ListView) findViewById(R.id.lv_ssid);
    	ItemAdapter adapter = new ItemAdapter(this, ssids);
    	lv.setAdapter(adapter);
    	
    	
    	lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Item ssid = ssids.get(position);
				System.out.println("onClick-------------->ssid:"+ssid.getName()+" dbm:"+ssid.getDbm());
				Intent data = new Intent();
				data.putExtra("ssid", ssid.getName());
				setResult(RESULT_OK, data);
				finish();
			}
		});
    }
}
