package com.usr.thermostat;

public class State {

	private double temperature;
	private double setTemperature;
	private int wind;
	private int menu;
	private int switchState;
	private int spinnerSelected;
	private int mID1;
	
	byte windResetByte   = (byte) 0xfc;
	byte switchResetByte = (byte) 0xef;
	byte menuResetByte   = (byte) 0x9f;
	
	public State(){
		
	}
	public State(double temperature, double setTemperature, int wind, int menu,
			int switchState, int spinnerSelected, int mID1) {
		super();
		this.temperature = temperature;
		this.setTemperature = setTemperature;
		this.wind = wind;
		this.menu = menu;
		this.switchState = switchState;
		this.spinnerSelected = spinnerSelected;
		this.mID1 = mID1;
	}

	public State(State state){
		this.menu = state.getMenu();
		this.setTemperature = state.getSetTemperature();
		this.spinnerSelected = state.getSpinnerSelected();
		this.temperature = state.getTemperature();
		this.switchState = state.getSwitchState();
		this.wind = state.getWind();
		this.mID1 = state.getmID1();
	}
	
	public void setStateAsState(State state){
		this.setTemperature  = state.getSetTemperature();
		this.temperature = state.getTemperature();
		this.menu = state.getMenu();
		this.wind  = state.getWind();
		this.mID1 = state.getmID1();
		this.spinnerSelected  = state.getSpinnerSelected();
		this.switchState = state.getSwitchState();

	}
	
	
	public boolean equalto(State state) {
		// TODO Auto-generated method stub
		if (this.menu != state.getMenu())
		{
			return false;
		}
		if (this.wind != state.getWind())
		{
			return false;
		}
		if (this.setTemperature != state.getSetTemperature())
		{
			return false;
		}
//		if (this.temperature != state.getTemperature())
//		{
//			return false;
//		}
//		if (this.spinnerSelected != state.getSpinnerSelected())
//		{
//			return false;
//		}
		if (this.switchState != state.getSwitchState())
		{
			return false;
		}
		
		
		
		return true;
	}




	public byte[] toByteArray(){
		
		/**dataPackage[0] is command
		*  dataPackage[1] is ip0;
		*  dataPackage[2] is ip1;
		*  dataPackage[3] is data0;
		*  dataPackage[4] is  data1;
		*  dataPackage[5] is data 2;
		*  dataPackage[6] is data3;
		*  dataPackage[7] is checkSum;
		*/
		byte[] data = new byte[8];
		data[0] = 0x00;
		data[1] = (byte) this.getSpinnerSelected();
		data[2] = (byte) this.getmID1();
		data[3] = 0x18;
		data[3] = WindDataParse(data[3], this.getWind());
		data[3] = MenuDataParse(data[3], this.getMenu());
		data[3] = SwitchStateParse(data[3], this.getSwitchState());
		data[4] = (byte) 0xfe;
		int temp_initTemperature = (int) (this.getSetTemperature()*2);
		data[5] = (byte) temp_initTemperature;
		data[6] = 0x00;
		Operations.CalcCheckSum(data);
		
		return data;
	}
	
	void byteArrayToState(final byte[] results){
		
		byte ctrlInfo = results[3];
		int  initTempInfo = (int) results[5];
		int  currentTempInfo = (int )results[6];
		
		byte wind = (byte) (ctrlInfo & 0x03);
		byte switchState = (byte) (ctrlInfo & 0x10);
		byte menu = (byte) (ctrlInfo & 0x60);
		
		//parse wind state
		switch(wind){
		case 0x00:
			this.setWind(Operations.WIND_MODE_AUTO);
			break;
		case 0x01:
			this.setWind(Operations.WIND_MODE_HIGH);
			break;
		case 0x02:
			this.setWind(Operations.WIND_MODE_MIDDLE);
			break;
		case 0x03:
			this.setWind(Operations.WIND_MODE_LOW);
			break;
		}
		//menu state
		switch (menu){
		case 0x00:
			this.setMenu(Operations.MENU_MODE_COLD);
			break;
		case 0x20:
			this.setMenu(Operations.MENU_MODE_WARM);
			break;
		case 0x40:
			this.setMenu(Operations.MENU_MODE_VENTILATE);
			break;
		}
		//switch state
		switch (switchState){
		case 0x00:
			this.setSwitchState(MainActivity.SWITCHOFF);
			break;
		case 0x10:
			this.setSwitchState(MainActivity.SWITCHON);
			break;
		}
		//set temperature 
		this.setSetTemperature((double)(initTempInfo*1.0/2.0));;
		//current temperature
		this.setTemperature((double)(currentTempInfo*1.0/2));
	}
	
	byte WindDataParse(byte data, int mode){
		data &= windResetByte;
		switch (mode){
			case Constant.WIND_MODE_AUTO :
				data |= 0x00;
				break;
			case Constant.WIND_MODE_LOW :
				data |= 0x03;
				break;
			case Constant.WIND_MODE_MIDDLE :
				data |= 0x02;
				break;
			case Constant.WIND_MODE_HIGH :
				data |= 0x01;
				break;
			}
		return data;
	}
	
	byte MenuDataParse(byte data, int mode){
		data &= menuResetByte;
				
		switch(mode){
		
		case Constant.MENU_MODE_COLD :
			data |= 0x00;
			break;
		case Constant.MENU_MODE_WARM :
			data |= 0x20;
			break;
		case Constant.MENU_MODE_VENTILATE :
			data |= 0x40;
			break;
		}
		return data;
	}
	
	byte SwitchStateParse(byte data, int state){
		data &= switchResetByte;
		if (state == MainActivity.SWITCHON){
			data |= 0x10;
//			isConnected = true;
//			getTemperatureRequest.start();
		}
		else if (state == MainActivity.SWITCHOFF){
			data |= 0x00;
//			isConnected = false;
		}
		return data;
	}


	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public double getSetTemperature() {
		return setTemperature;
	}

	public void setSetTemperature(double setTemperature) {
		this.setTemperature = setTemperature;
	}

	public int getWind() {
		return wind;
	}

	public void setWind(int wind) {
		this.wind = wind;
	}

	public int getMenu() {
		return menu;
	}

	public void setMenu(int menu) {
		this.menu = menu;
	}

	public int getSwitchState() {
		return switchState;
	}

	public void setSwitchState(int switchState) {
		this.switchState = switchState;
	}

	public int getSpinnerSelected() {
		return spinnerSelected;
	}

	public void setSpinnerSelected(int spinnerSelected) {
		this.spinnerSelected = spinnerSelected;
	}


	public int getmID1() {
		return mID1;
	}


	public void setmID1(int mID1) {
		this.mID1 = mID1;
	}
	
	
	
	
	
}
