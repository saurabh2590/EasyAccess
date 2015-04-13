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
import android.widget.LinearLayout;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class BooksNewspapersMenu extends EasyAccessActivity {
	
	/** Create the Books + Newspapers activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.booksnewspapers);
		super.onCreate(savedInstanceState);
		
		/** Launch respective app, depending on which button is pressed **/
		setButtonClickUri(R.id.btnDarwinReader, "com.ndu.mobile.daisy.free");
		setButtonClickUri(R.id.btnIdealGroupReader, "org.easyaccess.epubreader");
		setButtonClickUri(R.id.btnBookshareGoRead, "org.benetech.android");
		setButtonClickUri(R.id.btnGooglePlayBooks, "com.google.android.apps.books");
		setButtonClickUri(R.id.btnGoogleNewsstand, "com.google.android.apps.magazines");
		setButtonClickUri(R.id.btnGoogleDocs, "com.google.android.apps.docs.editors.docs");
		/** Put most everything before here **/
	}
	
	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.booksNewsPaperMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
		
		super.onResume();
	}
	
	
}
