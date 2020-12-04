package ke.co.lightspace.yetumobile.activity.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import justtide.BarcodeReader;
import justtide.BarcodeReader.BarcodeResult;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.config.AudioTrackManager;


public class BarCode extends Activity {
	
	protected static final int REFRESH = 0;
	protected static final int REFRESHBUTTON = 1;
	protected static final String TAG = "BarcCode";
	private TextView showDecodeText = null;
	private TextView showStateText = null;
	private Handler mHandler = null;
	private Thread mThread=null;
	private String decodeString = "";
	private Button btnScan;

	BarcodeResult barcodeResult;
	BarcodeReader barcodeReader = BarcodeReader.getInstance();
	private AudioTrackManager audio= new AudioTrackManager();
    private Timer timer;
	@SuppressLint("HandlerLeak")
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.barcode);
		showDecodeText = (TextView)findViewById(R.id.scandata);
		showDecodeText.setMovementMethod(ScrollingMovementMethod.getInstance());

		showStateText = (TextView)super.findViewById(R.id.show_state);
		btnScan = (Button)findViewById(R.id.scan);

		 mHandler = new Handler() {
	        	public void handleMessage(Message msg) {
	        		if (msg.what == REFRESH) { 	 
	        			btnScan.setEnabled(true);
	        			if(barcodeResult.getResult()<0)	        		
	        			{
	        				showStateText.setText(R.string.timeout);
	        				String test = barcodeResult.getText();//"Get Timeout");
	        				System.out.println("result barcode:"+test);
	        			}
	        			else
	        			{	
	        				decodeString = barcodeResult.getText();
	        				showStateText.setText(R.string.ok);	//"Get success");
	        				showDecodeText.setText(decodeString);
	        				System.out.println("result barcode:"+decodeString);
	        				audio.start(3000);
	            			audio.play();
	            			audio.stop();
	        			     
	        			} 
	        			//decodeString = "";
	        		} else if (msg.what == REFRESHBUTTON){
	        			btnScan.setEnabled(true);
	        			showStateText.setText(R.string.ad_page05);	
	        			//showStateText.setText("");	
	        		}
	        		super.handleMessage(msg);  
	        	}
	        };
	        try {
				barcodeReader.open();			
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
	}
	
	  
	
	public void codescan(View v) {

		showDecodeText.setText("");		
			if (mThread == null)
			{
				
				showStateText.setText(R.string.getbarcodes);	//"Getting, Wait 10s");
				mThread = new MyThread();
				mThread.start();
    			btnScan.setEnabled(false);

			}	

	}
	boolean first=true;
	
	
	
	public class MyThread extends Thread {
    	@Override
		public void run() {
    		
    		Log.i(TAG , "scan begin");
    	    try {
    	    	barcodeReader.delay(50); //delay time Nx100mm MAX：100
				barcodeResult = barcodeReader.scan(); //scan
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
    	    Log.i(TAG , "scan end");
    	    Message msg = new Message();
    		msg.what = REFRESH; 
    		mHandler.sendMessage(msg);
    		mThread = null;
    		//barcodeReader.stop();

    	}
    }
	

	  @Override
	    protected void onDestroy() { 
	        Log.e(TAG,"onDestroy 1");
	       
	        // TODO Auto-generated method stub 
		    try {

		    	if(barcodeReader!=null)
				   barcodeReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block 
				e.printStackTrace();
			}
	        Log.e(TAG,"onDestroy");
	        super.onDestroy(); 
	      }
	@Override
	protected void onPause() {
		super.onPause();

		timer = new Timer();
		Log.i("Main", "Invoking logout timer");
		LogOutTimerTask logoutTimeTask = new LogOutTimerTask();
		timer.schedule(logoutTimeTask, 300000); //auto logout in 5 minutes
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (timer != null) {
			timer.cancel();
			Log.i("Main", "cancel timer");
			timer = null;
		}
	}

	private class LogOutTimerTask extends TimerTask {

		@Override
		public void run() {

			//redirect user to login screen
			Intent i = new Intent(getApplicationContext(), Login.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
		}
	}

}
