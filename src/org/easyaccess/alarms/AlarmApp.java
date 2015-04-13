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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class AlarmApp extends EasyAccessActivity {

	/** Create the Alarm activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.alarm);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeAlarm(R.id.txtAlarmsEntry1, "1");
		initializeAlarm(R.id.txtAlarmsEntry2, "2");
		initializeAlarm(R.id.txtAlarmsEntry3, "3");
		initializeAlarm(R.id.txtAlarmsEntry4, "4");
		initializeAlarm(R.id.txtAlarmsEntry5, "5");
	}

	/** Launch the respective Java class, depending on which textview is pressed **/
	private void initializeAlarm(int textviewInt, final String alarmNumber) {
		TextView textview = (TextView) findViewById(textviewInt);
		initializeOnClickAction(alarmNumber, textview);

		String alarmTime = getSharedPreferences("org.easyaccess.alarms", Context.MODE_PRIVATE).getString("alarm" + alarmNumber, "0000d");
		int hourOfDay = Integer.parseInt(alarmTime.substring(0, 2));
		int minute = Integer.parseInt(alarmTime.substring(2, 4));
		String enabled = alarmTime.substring(4, 5);
		String state = "disabled";
		if ("e".equalsIgnoreCase(enabled))
			state = "enabled";
		textview.setText(String.format("Alarm at %02d:%02d (%s)", hourOfDay, minute, state));

		Utils.applyFontColorChanges(getApplicationContext(), textview);
		Utils.applyFontSizeChanges(getApplicationContext(), textview);
		Utils.applyFontTypeChanges(getApplicationContext(), textview);
	}

	private void initializeOnClickAction(final String alarmNumber, TextView textview) {
		textview.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(AlarmApp.this, AlarmChangeApp.class);
				intent.putExtra("alarmNumber", alarmNumber);
				startActivity(intent);
			}
		});
	}

}
