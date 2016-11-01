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

package org.easyaccess.simplemenus;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class EntertainmentMenu extends EasyAccessActivity {

	/** Create the Music + Video menu activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.entertainment);
		super.onCreate(savedInstanceState);

		/** Launch respective app, depending on which button is pressed **/
		// setButtonClickUri(R.id.btnMP3Player,
		// "in.co.accessiblenews.gestureplayer");
		Button btnMP3Player = (Button) findViewById(R.id.btnMP3Player);
		btnMP3Player.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Resolved warning of deprecated API use.
				try {
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
						Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					} else {
						Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
						startActivity(intent);
					}
				} catch (Exception e) {
					setButtonClickUri(R.id.btnMP3Player,
							"in.co.accessiblenews.gestureplayer");
				}
			}
		});
		setButtonClickUri(R.id.btnYouTube, "com.google.android.youtube");
		setButtonClickUri(R.id.btnVLCPlayer, "org.videolan.vlc");
		setButtonClickUri(R.id.btnAudioGamesHub, "com.AUT.AudioGameHub");

		/** Put most everything before here **/
	}

	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.entertainmentMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);

		super.onResume();
	}

}
