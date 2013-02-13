package com.bvcode.ncopter.comms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_gps_raw;
import com.MAVLink.Messages.common.msg_heartbeat;
import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.MainActivity;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.AC1Data.GPSData;
import com.bvcode.ncopter.AC1Data.ProtocolParser;
import com.bvcode.ncopter.AC1Data.RawByte;

/**
 * Communications module. Should be thread safe handles communications between
 * phone and quad parses the strings
 * 
 * @author bart
 * 
 */
public class CommunicationService extends Service {
    
	private NotificationManager mNM;

	ReceiveThread receive = null;
	
	// Messenging
	ArrayList<Messenger> msgCenter = new ArrayList<Messenger>();
    final Messenger mMessenger = new Messenger(new IncomingHandler());
	
    // Constants
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    
    static final int MSG_DEVICE_CONNECTED = 11;
    static final int MSG_DEVICE_DISCONNECTED = 12;
    static final int MSG_CONNECT_DEVICE = 13;
    
    static final int MSG_COPTER_RECEIVED = 20;
    static final int MSG_COPTER_SEND = 21; 
   
    
    private boolean clearToSend = true;
    
	public String lastString = "";
	public int cmdRecvCount;

	private FileWriter fw = null;
	
    ICommunicationModule module = null;
    

	/**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            	case MSG_COPTER_SEND:
            		Bundle b = msg.getData();
	            	byte[] s = b.getByteArray("msgBytes");
	            		            	
	            	if( CommonSettings.isProtocolAC1()){
	            		sendString(ProtocolParser.requestStopDataFlow());
	            	
		            	while( !clearToSend)
							try {
								Thread.sleep(1);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
	            	}
	            	sendString(s);
	            	break;
	            	
                case MSG_REGISTER_CLIENT:
                	synchronized (msgCenter) {
                    	if(!msgCenter.contains(msg.replyTo))
                    		msgCenter.add(msg.replyTo);
						
					}
                	// Intentional drop through when registering!
                case BluetoothModule.MSG_GET_STATUS:
                	module.serviceHandleStatusRequest(msg);
                    
                    break;
                    
                case MSG_UNREGISTER_CLIENT:
                	synchronized (msgCenter) {
                		msgCenter.remove(msg.replyTo);
                		
                	}
                	break;
                   
                case MSG_CONNECT_DEVICE:
                	if( msg.getData().containsKey(module.getDeviceAddressString())){
	                	String address = msg.getData().getString(module.getDeviceAddressString());
	                	if( address.length() != 0){
		                	connect(address);
		                	
	                	}
	    				
                	}
                	
                	break;
                	
                default:
                    super.handleMessage(msg);
                    
            }
        }
    }
	
    /**
     * Sends data out to the device.
     * @param s
     */
	public void sendString( byte[] s){
		
		try {
			if(module.isConnected() && s != null){
				
				if( CommonSettings.isProtocolAC1() )
					clearToSend = false;
				
				module.write(s);
				
				if( CommonSettings.isProtocolAC1() )
					lastString = new String(s);
				
				cmdRecvCount = 0;
				
			}else{
				notifyDeviceDisconnected();
				
			}
		} catch (IOException e) {
			notifyDeviceDisconnected();
			
		}
			
	}

	private Handler mHandler = new Handler();
	Runnable r = new Runnable() {
		@Override
		public void run() {
			disconnect();
			
		}
	};
	
	private class ReceiveThread extends Thread{
		//InputStream in;
		public boolean running;
		private double lastLatitude;
		private double lastLongitude;
		private double lastAltiude;
				
		public ReceiveThread() {
			super();
			running = true;
			//in = i;

		}

		@Override
		public void run() {
			int read = 0;
			byte b[] = new byte[1000];
			long lastRead = System.currentTimeMillis();
			
			boolean received_data;
			while(running){
				try {
					received_data = false;
					
					if(module.available() ){ // might lie to us...
						
						read += module.readByte(b, read, 1);
		    			
						if( read > 0){
							received_data = true;
			    			lastRead = System.currentTimeMillis();
			    			
			    			if( ProtocolParser.passThrough){
			    				// This mode for the CLI. Just pass through whatever we get
			    				RawByte raw = new RawByte();
			    				raw.b = b[0];
			    				read = 0;
			    				notifyNewMessage(0, raw);
			    				
			    			}else{
				    			if( CommonSettings.isProtocolAC1()){
					    			if( read == 1 && b[0] == 13)//eat this at the start??
					    				read = 0;
					    				
					    			if( read > 1 && b[read-1] == 10){
					    				if(read > 2 && b[read-2] == '\r')
					    					b[read-2] = '\0';
					    				
				    					b[read-1] = '\0';
				    					IMAVLinkMessage data = ProtocolParser.parseCopterData(lastString, new String(b,0, read));
				    					
				    					if( data != null)
				    						notifyNewMessage(cmdRecvCount, data);
					    				
				    					cmdRecvCount++;
				    				
					    				read = 0;
																	
									}
				    			}else if (CommonSettings.isProtocolMAVLink()){
				    				// Use the MAVLink protocol
				    				//Log.d("Receiving", "-");
				    				IMAVLinkMessage msg = MAVLink.receivedByte(b[0]);
				    				read = 0; //just use the first element in the buffer, AC1 legacy
				    				
				    				if( msg != null){
				    					if( msg.messageType == msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT){
				    						mHandler.removeCallbacks(r);
				    					    mHandler.postDelayed(r, 5000);
				    							
				    					}
				    					Log.d("received:", "" + msg.messageType);
				    					notifyNewMessage(cmdRecvCount, msg);
				    				}
					    						
				    			}	
			    			}
						}							
		    		}

					if(!received_data){
		    			if( System.currentTimeMillis() - lastRead > 100 ){
		    				if( CommonSettings.isProtocolAC1())
		    					clearToSend = true;
		    				
		    			}		    			
		    		}
						
				} catch (IOException e) {
					disconnect();
				}				
			}
			running = false;
			
		}
		
		private void notifyNewMessage(int cmdRecvCount, IMAVLinkMessage s) {
			
			try {
				
				synchronized (msgCenter) {
					for( Messenger c: msgCenter){
				    	if( c == null)
				    		continue;
				    	
						Message msg = Message.obtain(null, CommunicationService.MSG_COPTER_RECEIVED);
						Bundle data = new Bundle();
						data.putInt("recvCount", cmdRecvCount);
						data.putSerializable("msg", s);
						msg.setData(data);			    	
				    	c.send(msg);
				    	
				    }
			    }
			} catch (RemoteException e) {
				e.printStackTrace();
			
			}		
			
			saveGPS(s);
		}

		private void saveGPS(IMAVLinkMessage s) {
			//TODO clean up this function, combine the common functionality...
			
			if( CommonSettings.isProtocolAC1() ){
				// Record the copter data.
				if(s!=null && s.getClass().equals( GPSData.class)){
					GPSData gd = (GPSData)s;
					
					//How much have we moved
					float[] results = new float[1];
					Location.distanceBetween( // in meters
							lastLatitude, lastLongitude,
							gd.latitude/1000000.0, gd.longitude/1000000.0,
							results);
					
					// get the overall length, including altitude motion.
					results[0] = (float) Math.sqrt( results[0]*results[0] + Math.pow(gd.altitude - lastAltiude, 2));
					        
					// Ensure we have moved no more than 3m.
					if(fw != null && (results[0] > 3)){
						try {
							// Store the values
							lastLatitude = gd.latitude/1000000 ;
							lastLongitude = gd.longitude/1000000 ;
							lastAltiude = gd.altitude;
	
							fw.write( lastLatitude + " " 
									+ lastLongitude + " " 
									+ lastAltiude + "\n");
					
							
						} catch (IOException e) {
							e.printStackTrace();
							
						}				
					}    						
				}
			}else if (CommonSettings.isProtocolMAVLink()){
				if( s.messageType == msg_gps_raw.MAVLINK_MSG_ID_GPS_RAW){	
					msg_gps_raw msg = (msg_gps_raw) s;
					if( msg.fix_type > 1){
						//How much have we moved
						float[] results = new float[1];
						Location.distanceBetween( // in meters
								lastLatitude, lastLongitude,
								msg.lat, msg.lon,
								results);
						
						// get the overall length, including altitude motion.
						results[0] = (float) Math.sqrt( results[0]*results[0] + Math.pow(msg.alt - lastAltiude, 2));
						        
						// Ensure we have moved no more than 3m.
						if(fw != null && (results[0] > 3)){
							// Store the values
							lastLatitude = msg.lat;
							lastLongitude = msg.lon ;
							lastAltiude = msg.alt;
							
							try {
								fw.write( lastLatitude + " " 
										+ lastLongitude + " " 
										+ lastAltiude + "\n");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	};
	
	
	private void notifyDeviceConnected() {
		try {
        	synchronized (msgCenter) {
				for (Messenger c: msgCenter)
					c.send(Message.obtain(null, CommunicationService.MSG_DEVICE_CONNECTED));
        	}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}

	private void notifyDeviceDisconnected() {
		try {
        	synchronized (msgCenter) {
				for (Messenger c: msgCenter)
					c.send(Message.obtain(null, CommunicationService.MSG_DEVICE_DISCONNECTED));
        	}
		} catch (RemoteException e) {
			e.printStackTrace();
		}	
	}
		
	@Override
	public IBinder onBind(Intent intent) {				
		return mMessenger.getBinder();
		
	}
	
    @Override
    public void onCreate() {
    	// which parser to use?
		SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
		
		//Load in the proper comm module.
		if(settings.contains(CommunicationClient.LINK_TYPE)){
	    	String link = settings.getString(CommunicationClient.LINK_TYPE, "");
	    	
	    	if(link.equals(CommonSettings.LINK_BLUE)){
	        	module = new BluetoothModule();
	        	CommonSettings.currentLink = CommonSettings.LINK_BLUETOOTH;
	        	
	    	//}else if(link.equals(ProtocolParser.LINK_USB_) && IProxy.isUSBSupported()){
	    	///	module = new USBModule(this);
	    	//	ProtocolParser.currentLink = ProtocolParser.LINK_USB;
	        	
	    	}else{
	    		
	    		
	    	}
		}else{
    		Log.v("Communication Service", "Defaulting to Bluetooth");
    		module = new BluetoothModule();
    		
    	}		
    	    
		if(settings.contains(CommunicationClient.DEFAULT_ORIENTATION)){
			String address = settings.getString(CommunicationClient.DEFAULT_ORIENTATION, "");
			
	    	if(address.equals(CommonSettings.LANDSCAPE)){
	    		CommonSettings.desiredOrientation = CommonSettings.ORIENTATION_LANDSCAPE;
	    		
	    	}else if(address.equals(CommonSettings.PORTRAIT)){
	    		CommonSettings.desiredOrientation = CommonSettings.ORIENTATION_PORTRAIT;
	    		
	    	}else
	    		CommonSettings.desiredOrientation = CommonSettings.ORIENTATION_DEFAULT;
	    	
			
		}
		
		if(settings.contains(CommunicationClient.ACTIVE_PROTOCOL)){
	    	String address = settings.getString(CommunicationClient.ACTIVE_PROTOCOL, "");
		
	    	if(address.equals(CommonSettings.AC1)){
	    		CommonSettings.currentProtocol = CommonSettings.AC1_PROTOCOL;
	    		
	    	}else if(address.equals(CommonSettings.MAVLink)){
	    		CommonSettings.currentProtocol = CommonSettings.MAVLINK_PROTOCOL;
	    	}

		}
		
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification("Service Activated");
        
        //-------------------------------------
        // Ensure that when the service starts we allow the client to reconnect
        // After a first request, don't bother the user.
        module.setFirstTimeEnableDevice(true);
        module.setFirstTimeListDevices(true);

        //-------------------------------------        
        // Lets make sure we can access the SD card.
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {

		    // Record the data
		    File sdCard = Environment.getExternalStorageDirectory();
		    
		    // Just make sure our subdirectory exists
		    File dir = new File(sdCard.getAbsolutePath() + "/QuadCopter");
		    if(! dir.exists())
		    	dir.mkdir();
		    
		    // And open for writing
		    try {
		    	fw = new FileWriter(new File(sdCard.getAbsolutePath() + "/QuadCopter/GPS_Log_" + System.currentTimeMillis()));
				
			} catch (IOException e) {
				fw = null;
			}
	    }	  
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.local_service_started);
        mNM.cancelAll();

		disconnect();
		
		
		try {			
			if(fw != null)
				fw.close();

		} catch (IOException e) {
			e.printStackTrace();
			
		}		
    }

    public void connect(final String device) {
    	disconnect();
		
		new Thread(){
			public void run(){
				try {
					
					if(receive != null)
						receive.running = false;
					receive = null;

					module.connect(device);
				
					// Start a receive thread.
					receive = new ReceiveThread();
					receive.start();
					
					notifyDeviceConnected();
					
					showNotification("Connected to Bridge");
					
				} catch (IOException e) {
					module.setSocketNull();

					if(receive != null)
						receive.running = false;
					Log.v("Connect Error", e.toString());
					
					notifyDeviceDisconnected();
													
				}		
			}		
		}.start();

	}
    
    public void disconnect(){
    	try {

    		if(receive!=null)
    			receive.running = false;
    		receive = null;
    		        
	        module.disconnect();
			
			if( receive != null)
				receive.running = false;
	        receive = null;
	        
	        mNM.cancel(R.string.local_service_started);
	        mNM.cancelAll();
	        
	         
			notifyDeviceDisconnected();
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    /**
     * Show a notification while this service is running.
     */
    private void showNotification(CharSequence text) {
    	    	
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.main_icon, text, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.local_service_label), text, contentIntent);

        // Send the notification.
        mNM.notify(R.string.local_service_started, notification);
        
    }    	
}
