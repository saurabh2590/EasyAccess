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

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

import android.os.Bundle;
import android.widget.LinearLayout;

public class DailyUtilities extends EasyAccessActivity {

	/** Create the Music + Video menu activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.dailyutilities);
		super.onCreate(savedInstanceState);
		
		/** Launch respective app, depending on which button is pressed **/
		setButtonClickActivity(R.id.btnAlarm, DailyUtilities.this, AlarmApp.class);
		setButtonClickUri(R.id.btnSimpleAlarmClock, "ar.com.basejuegos.simplealarm");
		setButtonClickUri(R.id.btnCalendar, "com.google.android.calendar");
		setButtonClickUri(R.id.btnNoteTakerTodo, "com.google.android.keep");
		setButtonClickUri(R.id.btnVoiceRecorder, "com.stereomatch.amazing.audio.voice.recorder");
		setButtonClickUri(R.id.btnCalculator, "com.google.android.calculator");
		setButtonClickUri(R.id.btnEnglishHindiDictionary, "HinKhoj.Dictionary");
		/** Put most everything before here **/
	}	
	
	@Override
	public void onResume() {
		super.onResume();
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.dailyUtilitiesMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
	
	
}
