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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class AlarmChangeApp extends EasyAccessActivity {

	/** Create the Alarm Change App activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.alarm_change);
		super.onCreate(savedInstanceState);

        final String alarmNumber = getIntent().getExtras().getString("alarmNumber");
        final String alarmTime = getSharedPreferences("org.easyaccess.alarms", Context.MODE_PRIVATE).getString("alarm" + alarmNumber, "0000d");

        initializeChangeButton(alarmNumber);
        initializeToggleButton(alarmNumber, alarmTime);
	}

    private void initializeChangeButton(final String alarmNumber) {
        Button changeButton = (Button) findViewById(R.id.btnAlarmChange);
        changeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AlarmChangeApp.this, AlarmSetApp.class);
                intent.putExtra("alarmNumber",alarmNumber);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeToggleButton(final String alarmNumber, String alarmTime) {
        Button toggleButton = (Button) findViewById(R.id.btnAlarmDisable);
        setToggleButtonText(alarmTime, toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleAlarm("alarm"+alarmNumber);
            }
        });
    }

    private void setToggleButtonText(String alarmTime, Button toggleButton) {
        if(isEnabled(alarmTime)) {
            toggleButton.setText("Disable");
        } else {
            toggleButton.setText("Enable");
        }
    }

    private void toggleAlarm(String alarm) {
        SharedPreferences preferences = getSharedPreferences("org.easyaccess.alarms", Context.MODE_PRIVATE);
        String alarmTime = preferences.getString(alarm, "0000d");
        if(isEnabled(alarmTime)) {
            alarmTime = alarmTime.replace('e','d');
        } else {
            alarmTime = alarmTime.replace('d','e');
        }
        setToggleButtonText(alarmTime,(Button) findViewById(R.id.btnAlarmDisable));
        Editor editor = preferences.edit();
        editor.putString(alarm,alarmTime);
        editor.apply();
    }

    private boolean isEnabled(String alarmTime) {
        return alarmTime.substring(4,5).equalsIgnoreCase("e");
    }

	@Override
	public void onResume() {
		super.onResume();
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.alarmChangeMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
    
}
