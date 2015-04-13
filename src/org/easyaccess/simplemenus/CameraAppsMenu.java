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

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class CameraAppsMenu extends EasyAccessActivity {

	/** Create the Camera Apps menu activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.camera);
		super.onCreate(savedInstanceState);

		/** Launch respective app, depending on which button is pressed **/
		setButtonClickIntent(R.id.btnPhoneCamera, MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
		setButtonClickUri(R.id.btnGoogleGoggles, "com.google.android.apps.unveil");
		setButtonClickUri(R.id.btnOCRScanner, "com.smartmobilesoftware.mobileocrfree");
		setButtonClickUri(R.id.btnColorIdentifier, "com.loomatix.colorgrab");
		setButtonClickUri(R.id.btnMoneyIdentifier, "com.ndu.mobile.darwinwallet");

		/** Find UI elements **/
		Button btnLightDetector = (Button) findViewById(R.id.btnLightDetector);

		/** If Light Detector button is pressed on keypad, launch TBD if installed; otherwise, offer download from Play store **/
		btnLightDetector.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Notify user that we do not know a good light detection app yet
				// In future: Open some light detection app once available
				Context context = getApplicationContext();
				CharSequence text = "We did not find a good light detection app yet, unfortunately!";
				Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			}
		});

		/** Put most everything before here **/
	}

	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.cameraAppsMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);

		super.onResume();
	}

}
