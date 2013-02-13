package com.bvcode.ncopter.comms;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;

public abstract class ICommunicationModule {

	public static final int NOT_SUPPORTED = 1;
	public static final int NOT_ENABLED = 2;
	public static final int NOT_CONNECTED = 3;
	public static final int CONNECTED = 4;
	
	public static boolean firstTimeEnableDevice = true;
    public static boolean firstTimeListDevices = true;

	public static final int MSG_GET_STATUS = 3;
	public static final int MSG_GET_STATE = 10;

	static final int MSG_NOT_SUPPORTED = 0;
    static final int MSG_DEVICE_NOT_ENABLED= 1;
    static final int MSG_NOT_CONNECTED_TO_DEVICE = 2;
    static final int MSG_CONNECTED_TO_DEVICE = 3;
        
	abstract Message handleOnActivityResult(Activity parent, int requestCode, int resultCode, Intent data);
	
	abstract int clientHandleState(Messenger mService, Message msg, Activity parent);
	
	abstract void serviceHandleStatusRequest(Message msg);
	
	abstract boolean isConnected();
	
	abstract void setSocketNull();
	
	abstract void connect(String device) throws IOException;
	
	abstract int write(byte[] b) throws IOException;
	abstract int readByte(byte[] b, int offset, int length) throws IOException;
	abstract boolean available() throws IOException;

	abstract void disconnect()  throws IOException;

	abstract String getDeviceAddressString();

	public final void setFirstTimeEnableDevice(boolean b) {
		firstTimeEnableDevice = b;
		
	}

	public final void setFirstTimeListDevices(boolean b) {
		firstTimeListDevices = b;
		
	}
		
}
