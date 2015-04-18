package com.usr.thermostat;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;

public class CountDownTimer {
	private int countDownMax;
	private boolean initFlag;
	private boolean flag;
	private int countDown;
	private Handler handler;
	private int handlerMsg;
	private Timer timer = new Timer();
	
	

	public CountDownTimer()
	{
		
	}
	public void reSetCountDown()
	{
		this.countDown = this.countDownMax;
		this.flag = this.initFlag;
	}
	
	public void startTimer()
	{
		
		timer.schedule(new CountDownTask(), 0,1 * 1000);
		
	}
	
	class CountDownTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (countDown > 0)
			{
				countDown -= 1;
				flag = initFlag;
				
			}
			else
			{
//				countDown = countDownMax;
				flag = !initFlag;
				Message msg = new Message();
				msg.what = handlerMsg;
				handler.sendMessage(msg);
			}
		}
		
	}
	public void cancleTask()
	{
		this.getTimer().cancel();
	}

	public int getCountDownMax() {
		return countDownMax;
	}
	public void setCountDownMax(int countDownMax) {
		this.countDownMax = countDownMax;
	}
	public boolean isInitFlag() {
		return initFlag;
	}
	public void setInitFlag(boolean initFlag) {
		this.initFlag = initFlag;
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	public int getCountDown() {
		return countDown;
	}
	public void setCountDown(int countDown) {
		this.countDown = countDown;
	}
	public Handler getHandler() {
		return handler;
	}
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	public int getHandlerMsg() {
		return handlerMsg;
	}
	public void setHandlerMsg(int handlerMsg) {
		this.handlerMsg = handlerMsg;
	}
	public Timer getTimer() {
		return timer;
	}
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	
}
