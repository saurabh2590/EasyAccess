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

import android.os.Bundle;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;

public class SocialNetworksMenu extends EasyAccessActivity {

	/** Create the Social Networks menu activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.socialnetworks);
		super.onCreate(savedInstanceState);
		
		/** Launch respective app, depending on which button is pressed **/
		setButtonClickUri(R.id.btnWhatsApp, "com.whatsapp");
		setButtonClickUri(R.id.btnFacebookApp, "com.facebook.katana");
		setButtonClickUri(R.id.btnFacebookMessenger, "com.facebook.orca");
		setButtonClickUri(R.id.btnGooglePlus, "com.google.android.apps.plus");
		setButtonClickUri(R.id.btnSkype, "com.skype.raider");
		setButtonClickUri(R.id.btnViber, "com.viber.voip");
		
		/** Put most everything before here **/
	}	
	
}
