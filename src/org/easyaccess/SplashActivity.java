/*

	Copyright 2013 Caspar Isemer and and Eva Krueger, http://easyaccess.org
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
		http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License. 
*/

package org.easyaccess;

import org.easyaccess.phonedialer.CallStateService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class SplashActivity extends Activity implements OnInitListener{

	/** Create the Splash screen on easyaccess startup **/
	
	// Declare constants and variables
	private static String TAG = SplashActivity.class.getName();
	private static long SLEEP_TIME = 3600;

	private TextToSpeech tts;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Start call state changed service
		Intent bootIntent = new Intent(getApplicationContext(), CallStateService.class);
	    bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    startService(bootIntent);
		
		// Display splash screen without title and notification bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	    		WindowManager.LayoutParams.FLAG_FULLSCREEN);	    
	    setContentView(R.layout.splash);
	    
	    //check for TTS
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, 0);
        
	    if(Utils.INIT != 1) {	    
		    // Vibrate for 1000 milliseconds
		    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		    vibrator.vibrate(700);
		 
		    // Start timer and launch main activity
		    IntentLauncher launcher = new IntentLauncher();
		    launcher.start();
		    Utils.INIT = 1;
	    }
	    else {
	    	Intent intent = new Intent(SplashActivity.this, SwipingUtils.class);
			SplashActivity.this.startActivity(intent);
			SplashActivity.this.finish();
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (requestCode == 0)
	        {
	        	if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
	        	{
	    	       //success
	        		try
	        		{
	        			tts = new TextToSpeech(getApplicationContext(), this);
	        			TTS.setObject(tts);
	        		}
	        		catch(Exception e)
	        		{
	        			e.printStackTrace();
	        		}
	            }
	        	else
	        	{
	        		//install the TTS data
	        		Intent installIntent = new Intent();
	        	 	installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	        		startActivity(installIntent);
	        	}
	        }
	}
	
	@Override
	public void onInit(int status) {
    	if(status == TextToSpeech.ERROR) {
    		Toast.makeText(getApplicationContext(), this.getResources().getString(R.string.ttsError), Toast.LENGTH_LONG).show();
    	}
    }
	 
	private class IntentLauncher extends Thread {
		/** Launches SwipingUtils activity after displaying the splash screen */
		@Override
		public void run() {
			try {
				// Sleeping
	            Thread.sleep(SLEEP_TIME);
				// Start main activity
				Intent intent = new Intent(SplashActivity.this, SwipingUtils.class);
				SplashActivity.this.startActivity(intent);
				SplashActivity.this.finish();	            
	         } catch (Exception e) {
	        	// Display the error in the LogCat window
	            Log.e(TAG, e.getMessage());
	            // ImageView so that TalkBack can read it out loud
	            ImageView imageView = (ImageView)findViewById(R.id.splash);
	            imageView.setContentDescription(getString(R.string.txteasyaccessActivated));
	         }
		}
	}
}
