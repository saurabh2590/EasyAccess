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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;

public class AlarmApp extends EasyAccessActivity {

	/** Create the Alarm activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.alarm);
		super.onCreate(savedInstanceState);
		
		/** Find UI elements **/
		Button btnSetNewAlarm = (Button) findViewById(R.id.btnSetNewAlarm);
		TextView txtAlarmsEntry1 = (TextView) findViewById(R.id.txtAlarmsEntry1);
		TextView txtAlarmsEntry2 = (TextView) findViewById(R.id.txtAlarmsEntry2);
		TextView txtAlarmsEntry3 = (TextView) findViewById(R.id.txtAlarmsEntry3);
		TextView txtAlarmsEntry4 = (TextView) findViewById(R.id.txtAlarmsEntry4);
		TextView txtAlarmsEntry5 = (TextView) findViewById(R.id.txtAlarmsEntry5);
	    
		/** If Set New Alarm button is pressed on keypad, launch the custom justdroid alarm clock **/
	    setButtonClickActivity(R.id.btnSetNewAlarm, AlarmApp.this, AlarmSetApp.class);
		
		/** If 1st Alarm text is pressed launch the change/disable/delete view for the alarm **/
		txtAlarmsEntry1.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	setTextviewClickActivity(R.id.txtAlarmsEntry1, AlarmApp.this, AlarmChangeApp.class);
	        }
	    });	
		
		/** If 2nd Alarm text is pressed launch the change/disable/delete view for the alarm **/
		txtAlarmsEntry2.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	setTextviewClickActivity(R.id.txtAlarmsEntry2, AlarmApp.this, AlarmChangeApp.class);
	        }
	    });	
		
		/** If 3rd Alarm text is pressed launch the change/disable/delete view for the alarm **/
		txtAlarmsEntry3.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	setTextviewClickActivity(R.id.txtAlarmsEntry3, AlarmApp.this, AlarmChangeApp.class);
	        }
	    });	
		
		/** If 4th Alarm text is pressed launch the change/disable/delete view for the alarm **/
		txtAlarmsEntry4.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	setTextviewClickActivity(R.id.txtAlarmsEntry4, AlarmApp.this, AlarmChangeApp.class);
	        }
	    });	
		
		/** If 5th Alarm text is pressed launch the change/disable/delete view for the alarm **/
		txtAlarmsEntry5.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	setTextviewClickActivity(R.id.txtAlarmsEntry5, AlarmApp.this, AlarmChangeApp.class);
	        }
	    });			
		
		/** Put most everything before here **/
	}
	
}
