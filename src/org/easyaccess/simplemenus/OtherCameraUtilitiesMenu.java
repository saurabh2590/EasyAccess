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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class OtherCameraUtilitiesMenu extends EasyAccessActivity {

	/** Create the Camera Apps menu activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.othercamerautilities);
		super.onCreate(savedInstanceState);

		/** Launch respective app, depending on which button is pressed **/
		setButtonClickUri(R.id.btnMoneyIdentifier, "com.msearcher.camfind");
		setButtonClickUri(R.id.btnColorIdentifier, "vOICe.vOICe");
		setButtonClickUri(R.id.btnLightDetector, "com.visionandroid.apps.motionsensor");
		setButtonClickUri(R.id.btnTapTapSee, "com.msearcher.taptapsee.android");

		/** Put most everything before here **/
	}

	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.otherCameraUtilitiesMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);

		super.onResume();
	}

}
