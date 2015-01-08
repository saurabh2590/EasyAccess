/*
	   _           _      _           _     _ 
	  (_)         | |    | |         (_)   | |
	   _ _   _ ___| |_ __| |_ __ ___  _  __| |
	  | | | | / __| __/ _` | '__/ _ \| |/ _` |
	  | | |_| \__ \ || (_| | | | (_) | | (_| |
	  | |\__,_|___/\__\__,_|_|  \___/|_|\__,_|
	 _/ |                                     
	|__/ 
	
	Copyright 2013 Caspar Isemer and and Eva Krueger, http://justdroid.org
	
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

package org.easyaccess.alarms;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.SplashActivity;

public class AlarmSnoozeApp extends EasyAccessActivity {

	/** Create the Alarm Snooze activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.alarm_snooze);
		super.onCreate(savedInstanceState);
		
		/** Find UI elements **/
		Button btnAlarmTurnOff = (Button) findViewById(R.id.btnAlarmTurnOff);

		/** Increase the audio volume so that sounds can be heard **/
        AudioManager audioManager = (AudioManager) getSystemService(SplashActivity.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
        
		/** Playback the alarm.mp3 file **/
        final MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.alarm);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
		
		/** If turn off button is pressed, turn off the alarm alarm **/
		btnAlarmTurnOff.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	// Unload the alarm.mp3 file once finished
				mediaPlayer.stop();
				mediaPlayer.release();
				
				// Unload this alarm snooze activity
				AlarmSnoozeApp.this.finish();
	        }
	    });
		
		/** Put most everything before here **/
	}	
	
}
