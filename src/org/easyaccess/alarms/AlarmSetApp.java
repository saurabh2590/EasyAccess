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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;

import java.util.Calendar;


public class AlarmSetApp extends EasyAccessActivity {
	
	/** Declare variables and UI elements **/
	String strAlarmTimeUserInput = "";
	TextView txtAlarmTimeUserInput;
	
	/** Function to dial a phone number on button click **/
	void appendAlarmTimeUserInput(int buttonInt, final int vibrateLength, final String strDialDigit) {
		Button button = (Button) findViewById(buttonInt);
		button.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    	    vibrator.vibrate(vibrateLength);
	            strAlarmTimeUserInput = strAlarmTimeUserInput + strDialDigit;
	            txtAlarmTimeUserInput.setText(strAlarmTimeUserInput);
	        }
	    });
	} 	
	
	/** Create the Alarm Set App activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.alarm_set);
		super.onCreate(savedInstanceState);
		
		/** Find UI elements **/
		txtAlarmTimeUserInput = (TextView) findViewById(R.id.txtAlarmTimeUserInput);
		Button btnKeypadBackspace = (Button) findViewById(R.id.btnKeypadBackspace);
		Button btnAlarmSet = (Button) findViewById(R.id.btnAlarmSet);
	    
		/** If "X" is pressed on keypad, append "X" to the set time **/
		appendAlarmTimeUserInput(R.id.btnKeypad1, 100, "1");
		appendAlarmTimeUserInput(R.id.btnKeypad2, 150, "2");
		appendAlarmTimeUserInput(R.id.btnKeypad3, 200, "3");
		appendAlarmTimeUserInput(R.id.btnKeypad4, 250, "4");
		appendAlarmTimeUserInput(R.id.btnKeypad5, 300, "5");
		appendAlarmTimeUserInput(R.id.btnKeypad6, 350, "6");
		appendAlarmTimeUserInput(R.id.btnKeypad7, 400, "7");
		appendAlarmTimeUserInput(R.id.btnKeypad8, 450, "8");
		appendAlarmTimeUserInput(R.id.btnKeypad9, 500, "9");
		appendAlarmTimeUserInput(R.id.btnKeypad0, 550, "0");
		
		/** If backspace is pressed on keypad, trim the right-most digit off the set time **/
		btnKeypadBackspace.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	if(strAlarmTimeUserInput != null && !strAlarmTimeUserInput.isEmpty()) {
	            	strAlarmTimeUserInput = strAlarmTimeUserInput.substring(0, strAlarmTimeUserInput.length() - 1);
	            	txtAlarmTimeUserInput.setText(strAlarmTimeUserInput);
	        	}
	        }
	    });
		
		/** If set alarm button is pressed, set the alarm **/
		btnAlarmSet.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
                if(strAlarmTimeUserInput != null && !strAlarmTimeUserInput.isEmpty()) {
                	// Parse user input
                    int hourOfDay = Integer.parseInt(strAlarmTimeUserInput.substring(0, 2));
                    int minute = Integer.parseInt(strAlarmTimeUserInput.substring(2, 4));
                    setAlarm(hourOfDay, minute);
                    
                    // Unload this alarm set activity
    				AlarmSetApp.this.finish();
                }
	        }
	    });
				
		/** Put most everything before here **/
	}

    private void setAlarm(int hourOfDay, int minute) {

        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();

        calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calSet.set(Calendar.MINUTE, minute);
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);

        if (calSet.compareTo(calNow) <= 0) {
            // Today Set time passed, count to tomorrow
            calSet.add(Calendar.DATE, 1);
        }

        setAlarmManager(calSet);
    }


    private void setAlarmManager(Calendar targetCal) {

        Log.d("alarm", "Alarm is set " + targetCal.getTime() + "\n" + "***\n");

        Intent intent = new Intent(AlarmSetApp.this, AlarmSnoozeApp.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(AlarmSetApp.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }


}
