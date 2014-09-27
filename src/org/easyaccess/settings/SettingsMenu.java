/*
	
	Copyright 2014 Caspar Isemer, Eva Krueger and IDEAL Group Inc.(http://www.ideal-group.org), http://easyaccess.org
	
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

package org.easyaccess.settings;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class SettingsMenu extends EasyAccessActivity {
	/**
	* The Settings menu in easyaccess displays all the options for modifying the settings of easyaccess.
	*/
	
	/** Declare all the Buttons used in the activity **/
	private Button btnSettingsScreenCurtain, btnSettingsColor, btnSettingsFont, btnSettingsVolume, 
	btnSettingsAndroid, btnAbout;
	private int screenCurtainFlag;
	
	/** 
	* Enables or disables the screen curtain system-wide depending on current state.
	**/
	void addScreenCurtain() {
		WindowManager windowManager = getWindowManager();
		LayoutInflater inflater = getLayoutInflater();
		LayoutParamsAndViewUtils layoutParamsAndView = ScreenCurtainFunctions.prepareForCurtainCheck(inflater);

    	ScreenCurtainFunctions appState = ((ScreenCurtainFunctions) getApplicationContext());
    	if(appState.getState()) {
    		windowManager.removeView(curtainView);
    		curtainSet = false;
    		appState.setState(false);
    		screenCurtainFlag = 0;
    	} else {
    		curtainView = layoutParamsAndView.getView();
    		windowManager.addView(curtainView, layoutParamsAndView.getLayoutParams());
    		curtainSet = true;
    		appState.setState(true);
    	}
	}
	
	@Override
	public void onBackPressed() {
		turnOffScreenCurtain();
		finish();
	}
	
	/** Launches the Font Settings menu. **/
	void startSettingsFont() {
		Intent intent = new Intent(getApplicationContext(), SettingsFont.class);
    	startActivity(intent);
	}
	
	/** Launches the Color Settings menu. **/
	void startSettingsColor() {
		Intent intent = new Intent(getApplicationContext(), SettingsColor.class);
    	startActivity(intent);
	}
	
	/** Launches the Volume Settings menu. **/
	void startSettingsVolume() {
		Intent intent = new Intent(getApplicationContext(), SettingsVolume.class);
    	startActivity(intent);
	}
	
	/** Launches the regular Android Settings. **/
	void startSettingsAndroid() {
		Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);           
        startActivity(intent);
	}
	
	/** Launches the About easyaccess activity. **/
	void startSettingsAbout() {
		Intent intent = new Intent(SettingsMenu.this, AboutActivity.class);
        startActivity(intent);
	}
	
	/** Creates the SettingsMenu activity. **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.settings);
		super.onCreate(savedInstanceState);
		
		/** Find UI elements **/
		btnSettingsScreenCurtain = (Button) findViewById(R.id.btnSettingsScreenCurtain);
		btnSettingsColor = (Button) findViewById(R.id.btnSettingsColor);
		btnSettingsFont = (Button) findViewById(R.id.btnSettingsFont);
		btnSettingsVolume = (Button) findViewById(R.id.btnSettingsVolume);
		btnSettingsAndroid = (Button) findViewById(R.id.btnSettingsAndroid);
		btnAbout = (Button) findViewById(R.id.btnAbout);
		
		screenCurtainFlag = 0;
		
		/** If Screen Curtain button is pressed, call addScreenCurtain method **/
				btnSettingsScreenCurtain.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	screenCurtainFlag = 1;
	        	addScreenCurtain();
	        }
	    });
		
		/** If Color Settings button is pressed, launch the Color Settings menu **/
		btnSettingsColor.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	if(screenCurtainFlag == 1) {
	        		addScreenCurtain();
	        	}
	        	else {
	        		startSettingsColor();
	        	}
	        }
	    });
		
		/** If Font Settings button is pressed, launch the Font Settings menu **/
		btnSettingsFont.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	if(screenCurtainFlag == 1) {
	        		addScreenCurtain();
	        	}
	        	else {
	        		startSettingsFont();
	        	}
	        }
	    });	
		
		/** If Volume Settings button is pressed, launch the Volume Settings menu **/
		btnSettingsVolume.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	if(screenCurtainFlag == 1) {
	        		addScreenCurtain();
	        	}
	        	else {
	        		startSettingsVolume();
	        	}
	        }
	    });		
		
		/** If Android Settings button is pressed, launch the regular Android settings **/
		btnSettingsAndroid.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	if(screenCurtainFlag == 1) {
	        		addScreenCurtain();
	        	}
	        	else {
	        		startSettingsAndroid();
	        	}
	        }
	    });
		
		/** If About easyaccess button is pressed, launch the About easyaccess activity **/
		btnAbout.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	if(screenCurtainFlag == 1) {
	        		addScreenCurtain();
	        	}
	        	else {
	        		startSettingsAbout();
	        	}
	        }
	    });
		
		/** Attach onFocusChange listener to the buttons **/
		attachListener(btnSettingsScreenCurtain);
		attachListener(btnSettingsColor);
		attachListener(btnSettingsFont);
		attachListener(btnSettingsVolume);
		attachListener(btnSettingsAndroid);
		attachListener(btnAbout);
	}	

	@Override
	public void onResume() {
		super.onResume();
		//check if keyboard is connected and accessibility services are disabled
    	if(!Utils.isAccessibilityEnabled(getApplicationContext()) &&
    			getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
    		TTS.speak(getResources().getString(R.string.settings));
    	}
		//get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.settings);
		/** Apply the selected font color, font size and font type to the activity **/
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}
