package com.bvcode.ncopter.comms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class BluetoothModule extends ICommunicationModule {
	
	//-----------------------------------------------------------------
	private BluetoothSocket bluetoothSocket = null;
	private OutputStream out;
	private InputStream in;
	
	private static final UUID BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String BLUETOOTH_ADDRESS = "bluetoothDeviceAddress";
        
    // Intent request codes
    private static final int REQUEST_CONNECT_BLUETOOTH_DEVICE = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 3;

    public String getDeviceAddressString(){ 
		return BLUETOOTH_ADDRESS; 
	
    }
	
    public void serviceHandleStatusRequest(Message msg) {
		// Let the other side know the state of the bluetooth.
		try {
			
			int state;
			if(BluetoothAdapter.getDefaultAdapter() == null) // Does not support bluetooth
				state = MSG_NOT_SUPPORTED;
			else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) // bluetooth not enabled
				state = MSG_DEVICE_NOT_ENABLED;
			else if( !isConnected() )
				state = MSG_NOT_CONNECTED_TO_DEVICE;
			else 
				state = MSG_CONNECTED_TO_DEVICE;
				
			msg.replyTo.send( Message.obtain(null, MSG_GET_STATE, state, 0 ) );
			
		} catch (RemoteException e) {
			e.printStackTrace();
			
		}
	}
	
	public int clientHandleState(Messenger mService, Message msg, Activity parent) {
		switch( msg.arg1 ){
		
			case MSG_NOT_SUPPORTED:
				return NOT_SUPPORTED;
				
			case MSG_DEVICE_NOT_ENABLED: // not enabled
				if( firstTimeEnableDevice){
					firstTimeEnableDevice = false;
	    			Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    			parent.startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
				}
				return NOT_ENABLED;
				
			case MSG_NOT_CONNECTED_TO_DEVICE: // Enabled but not connected
				Message ret = listBluetoothDevices(parent);
				if( ret != null){
					try {
					    mService.send(ret);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				return NOT_CONNECTED;
				
			case MSG_CONNECTED_TO_DEVICE: // Connected
				return CONNECTED;
			
		}
		return 0;
	}	
	
	public Message handleOnActivityResult(Activity parent, int requestCode, int resultCode, Intent data){
		switch (requestCode) {
			case REQUEST_ENABLE_BLUETOOTH:
				// When the request to enable Bluetooth returns
				return listBluetoothDevices(parent);
				
			case REQUEST_CONNECT_BLUETOOTH_DEVICE:
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

				// Save the modem for next time
				SharedPreferences settings = parent.getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
				if(!settings.contains(CommunicationClient.DEFAULT_MODEM)){
					SharedPreferences.Editor editor = settings.edit();
				    editor.putString(CommunicationClient.DEFAULT_MODEM, address);
				    editor.commit();
					
				}
				
				// Send the modem selection
				Message msg = Message.obtain(null, CommunicationService.MSG_CONNECT_DEVICE);
				Bundle d = new Bundle();
				d.putString(BLUETOOTH_ADDRESS, address);
				msg.setData(d);
				return msg;
		}
		
		return null;
	}
	

	private Message listBluetoothDevices(Activity parent) {
		if( firstTimeListDevices){
			firstTimeListDevices = false;
			SharedPreferences settings = parent.getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
			
			if(!settings.contains(CommunicationClient.DEFAULT_MODEM)){
		    	parent.startActivityForResult(new Intent(parent, DeviceListActivity.class), REQUEST_CONNECT_BLUETOOTH_DEVICE);
			
			}else{
				String address = settings.getString(CommunicationClient.DEFAULT_MODEM, "");

				Message msg = Message.obtain(null, CommunicationService.MSG_CONNECT_DEVICE);
				Bundle d = new Bundle();
				d.putString(BLUETOOTH_ADDRESS, address);
				msg.setData(d);

				return msg;
			}		
		}
		return null;
	}
	
	public void connect(String address) throws IOException {
		BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
		// Throws an error on reconnect...
		bluetoothSocket = device.createRfcommSocketToServiceRecord(BLUETOOTH_UUID);
		bluetoothSocket.connect();
	
		out = bluetoothSocket.getOutputStream();
		in = bluetoothSocket.getInputStream();
			
	}

	public boolean isConnected(){
    	return bluetoothSocket != null && out != null; 
    	
    }

	public void disconnect() throws IOException {
        
		if( bluetoothSocket != null)
			bluetoothSocket.close();
		bluetoothSocket=null;
		
		if( in != null)
        	in.close();
		in = null;
		
        if( out != null)
        	out.close();
        out = null;

	}
	
	public int write(byte[] b) throws IOException{
		out.write(b);
		return b.length; // assume that all data was written.
		
	}

	public void setSocketNull() {
		bluetoothSocket = null;
		out = null;
		in = null;
		
	}


	@Override
	public int readByte(byte[] b, int offset, int length) throws IOException {
		// Overflow protection. If we reach 1000 bytes, something is wrong.
		if ( offset + in.available() >= b.length )
			offset = 0;
		
		offset += in.read(b, offset, length);

		return offset;
		
	}

	@Override
	public boolean available() throws IOException {
		return (in != null && in.available() > 0);
		
	}
}
