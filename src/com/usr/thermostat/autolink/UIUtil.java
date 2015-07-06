package com.usr.thermostat.autolink;

import android.content.Context;
import android.widget.Toast;

public class UIUtil {
    public static void toastShow(Context context,String string){
    	Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }
    
    public static void toastShow(Context context,int resId){
    	Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }
}
