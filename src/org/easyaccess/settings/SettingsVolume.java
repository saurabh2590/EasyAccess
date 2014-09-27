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

import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class SettingsVolume extends EasyAccessActivity {
	/**
	* The Volume option in easyaccess Settings lists the options for various levels of volume. The
	* options are Softest, Softer, Soft, Normal, Louder, Loudest.
	*/
	
	/** Declare variables and UI elements **/
	private RadioGroup rgVolume;
	
	/**
 	* Attaches onFocusChange listener to the TextView passed as a parameter to the method, to track
 	* the change in focus of the TextView. If the TextView receives focus, pass the content 
 	* description of the TextView to giveFeedback().
 	* @param textView This is an instance of TextView.
 	*/
	void attachListenerToTextView(TextView textView) {
		textView.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(hasFocus) {
					giveFeedback(((TextView)view).getText().toString());
				}
			}
		});
	}
	
	/** 
	* Attaches onCheckedChange listener to the RadioGroup displaying all the volume levels.
	* Based on the radio button selected, the media volume of the phone is set accordingly and a 
	* TTS feedback is given to the user, with the selected volume.	
	**/
	void attachListenerToRadioGroup() {
		rgVolume.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup rg, int radioButtonId) {
				switch(radioButtonId) {
				case R.id.radioSoftest:
					Utils.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 3, 
							AudioManager.FLAG_PLAY_SOUND);
					TTS.speak(((RadioButton)findViewById(radioButtonId)).getText().toString());
					break;
				case R.id.radioSofter:
					Utils.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 
							AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
					TTS.speak(((RadioButton)findViewById(radioButtonId)).getText().toString());
					break;
				case R.id.radioSoft:
					Utils.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 7, 
							AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
					TTS.speak(((RadioButton)findViewById(radioButtonId)).getText().toString());
					break;
				case R.id.radioNormal:
					Utils.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 9, 
							AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
					TTS.speak(((RadioButton)findViewById(radioButtonId)).getText().toString());
					break;
				case R.id.radioLoud:
					Utils.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 11, 
							AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
					TTS.speak(((RadioButton)findViewById(radioButtonId)).getText().toString());
					break;
				case R.id.radioLouder:
					Utils.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 13, 
							AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
					TTS.speak(((RadioButton)findViewById(radioButtonId)).getText().toString());
					break;
				case R.id.radioLoudest:
					Utils.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
							Utils.audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 
							AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
					TTS.speak(((RadioButton)findViewById(radioButtonId)).getText().toString());
					break;
				}
			}
		});
	}
	
	/** Creates the SettingsVolume activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.settingsvolume);
		super.onCreate(savedInstanceState);
		
		Utils.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		/** Find UI elements **/
		rgVolume = (RadioGroup) findViewById(R.id.rgVolume);
		
		attachListener((RadioButton) findViewById(R.id.radioLoud));
		attachListener((RadioButton) findViewById(R.id.radioLouder));
		attachListener((RadioButton) findViewById(R.id.radioLoudest));
		attachListener((RadioButton) findViewById(R.id.radioNormal));
		attachListener((RadioButton) findViewById(R.id.radioSoft));
		attachListener((RadioButton) findViewById(R.id.radioSofter));
		attachListener((RadioButton) findViewById(R.id.radioSoftest));
		
		/** attach OnCheckedChangeListener to the RadioGroup **/
		attachListenerToRadioGroup();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//check if keyboard is connected and accessibility services are disabled
    	if(!Utils.isAccessibilityEnabled(getApplicationContext()) &&
    			getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
    		TTS.speak(getResources().getString(R.string.settingsVolume));
    	}
		/** Display the current media volume **/
		if(Utils.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 3) {
			((RadioButton)findViewById(R.id.radioSoftest)).setChecked(true);
		}
		else if(Utils.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 5) {
			((RadioButton)findViewById(R.id.radioSofter)).setChecked(true);
		}
		else if(Utils.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 7) {
			((RadioButton)findViewById(R.id.radioSoft)).setChecked(true);
		}
		else if(Utils.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 9) {
			((RadioButton)findViewById(R.id.radioNormal)).setChecked(true);
		}
		else if(Utils.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 11) {
			((RadioButton)findViewById(R.id.radioLoud)).setChecked(true);
		}
		else if(Utils.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 13) {
			((RadioButton)findViewById(R.id.radioLouder)).setChecked(true);
		}
		else if(Utils.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 
				Utils.audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
			((RadioButton)findViewById(R.id.radioLoudest)).setChecked(true);
		}
		
		/** Apply the selected font color, font size and font type to the activity **/
		LinearLayout layout = (LinearLayout) findViewById(R.id.settingsfont);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);  
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}
