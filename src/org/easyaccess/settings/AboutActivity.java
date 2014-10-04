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

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * AboutActivity displays the details about easyaccess, the people involved and
 * the supporters of the project
 **/

public class AboutActivity extends EasyAccessActivity {
	/** Declare all the TextViews used in the activity **/
	TextView txtHeader, txtMission, txtSignature, txtHeaderResearch,
			txtFactFinding, txtFocusGroup, txtHeaderSupporters,
			txtCertificates, txtDonors;

	/**
	 * Attach onFocusChangeListener to the TextView passed as parameter to the
	 * method. If a keyboard is connected to the device, a TTS feedback would be
	 * given to the user informing him/her about the text on the TextView that
	 * received focus.
	 * 
	 * @param textView
	 *            is an instance of TextView.
	 */
	void attachListenerToTextView(final TextView textView) {
		textView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						giveFeedback(textView.getText().toString());
				}
			}
		});
	}

	/** Create the About activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.about);
		super.onCreate(savedInstanceState);

		// Find UI elements
		txtHeader = (TextView) findViewById(R.id.txtAboutHeaderMission);
		txtMission = (TextView) findViewById(R.id.txtAboutMission);
		txtSignature = (TextView) findViewById(R.id.txtAboutSignature);
		txtHeaderResearch = (TextView) findViewById(R.id.txtAboutHeaderResearch);
		txtFactFinding = (TextView) findViewById(R.id.txtAboutFactFinding);
		txtFocusGroup = (TextView) findViewById(R.id.txtAboutFocusGroup);
		txtHeaderSupporters = (TextView) findViewById(R.id.txtAboutHeaderSupporters);
		txtCertificates = (TextView) findViewById(R.id.txtAboutCertificates);
		txtDonors = (TextView) findViewById(R.id.txtAboutDonors);

		// Attach the onFocusChangedListener to each of the TextViews
		attachListenerToTextView(txtHeader);
		attachListenerToTextView(txtMission);
		attachListenerToTextView(txtSignature);
		attachListenerToTextView(txtHeaderResearch);
		attachListenerToTextView(txtFactFinding);
		attachListenerToTextView(txtFocusGroup);
		attachListenerToTextView(txtHeaderSupporters);
		attachListenerToTextView(txtCertificates);
		attachListenerToTextView(txtDonors);
	}

	@Override
	public void onResume() {
		super.onResume();
		// check if keyboard is connected and accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			TTS.speak(getString(R.string.about));
		}
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.about);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}
