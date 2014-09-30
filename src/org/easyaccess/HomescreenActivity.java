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
package org.easyaccess;

import org.easyaccess.calllog.CallLogApp;
import org.easyaccess.contacts.ContactsApp;
import org.easyaccess.phonedialer.PhoneDialerApp;
import org.easyaccess.settings.SettingsMenu;
import org.easyaccess.status.StatusApp;
import org.easyaccess.textmessages.TextMessagesApp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class HomescreenActivity extends EasyAccessFragment implements
		KeyListener {
	/**
	 * The HomeScreenActivity displays the options available in the app.
	 */

	/** Declare variables and UI elements **/
	private View view;

	void startNewActivity(@SuppressWarnings("rawtypes") Class className) {
		Intent intent = new Intent(getActivity().getApplicationContext(),
				className);
		startActivity(intent);
	}

	/**
	 * Attaches onClick, onFocus and Key listener to the button passed as a
	 * paramater, and invokes startNewActivity method when the button is
	 * clicked. The startNewActivity method takes as parameter, the class of the
	 * activity to be launched. When the button receives focus, giveFeedback
	 * method is invoked, that reads aloud the text on the button.
	 * 
	 * @param button
	 *            The button with which the listeners are to be associated.
	 * @param className
	 *            The class of the activity to be launched.
	 */
	void attachListener(final Button button,
			@SuppressWarnings("rawtypes") final Class className) {

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				startNewActivity(className);
			}
		});

		button.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus)
					giveFeedback(button.getText().toString());
			}
		});

		button.setKeyListener(HomescreenActivity.this);
	}

	/**
	 * Announces the text passed as a parameter, and causes the device to
	 * vibrate for 300 milliseconds.
	 * 
	 * @param text
	 *            The text that is to be read aloud.
	 */
	void giveFeedback(String text) {
		// vibrate
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);
		vibrator.vibrate(300);
		// TTS feedback
		if (!TTS.isSpeaking()) {
			TTS.speak(text);
		}
	}

	/** Create the Main activity showing home screen #1 **/
	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.homescreen1, container, false);
		this.view = v;

		/**
		 * Launch respective easyaccess app, depending on which button is
		 * pressed
		 **/
		attachListener((Button) v.findViewById(R.id.btnPhoneDialer),
				PhoneDialerApp.class);
		attachListener((Button) v.findViewById(R.id.btnCallLog),
				CallLogApp.class);
		attachListener((Button) v.findViewById(R.id.btnTextMessages),
				TextMessagesApp.class);
		attachListener((Button) v.findViewById(R.id.btnContacts),
				ContactsApp.class);
		attachListener((Button) v.findViewById(R.id.btnStatus), StatusApp.class);
		attachListener((Button) v.findViewById(R.id.btnSettings),
				SettingsMenu.class);

		/** Put most everything before here **/
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		// get the root layout
		LinearLayout layout = (LinearLayout) this.view
				.findViewById(R.id.homescreen1);
		Utils.applyFontColorChanges(this.view.getContext(), layout);
		Utils.applyFontSizeChanges(this.view.getContext(), layout);
		Utils.applyFontTypeChanges(this.view.getContext(), layout);
	}

	@Override
	public boolean onKeyUp(View view, Editable arg1, int keyCode,
			KeyEvent keyEvent) {
		int shift = 0;
		switch (keyCode) {
		case KeyEvent.KEYCODE_TAB:
			if (shift == 1) {
				if (view.getNextFocusLeftId() != -1) {
					getActivity().findViewById(view.getNextFocusLeftId())
							.requestFocus();
				} else if (view.getNextFocusUpId() != -1) {
					getActivity().findViewById(view.getNextFocusUpId())
							.requestFocus();
				}
			}
			break;
		}
		return false;
	}

	@Override
	public void clearMetaKeyState(View view, Editable content, int states) {

	}

	@Override
	public int getInputType() {
		return 0;
	}

	@Override
	public boolean onKeyDown(View view, Editable text, int keyCode,
			KeyEvent keyEvent) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DEL:// go to the previous screen
			// check if keyboard is connected and accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getActivity()
					.getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak("Back");
			getActivity().finish();
			break;
		case KeyEvent.KEYCODE_F1:// go to the home screen
			// check if keyboard is connected and accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getActivity()
					.getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak("Home");
			getActivity().finish();
			Intent intent = new Intent(getActivity().getApplicationContext(),
					SwipingUtils.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			startActivity(intent);
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			switch (view.getId()) {
			case R.id.btnPhoneDialer:
				startNewActivity(PhoneDialerApp.class);
				break;
			case R.id.btnCallLog:
				startNewActivity(CallLogApp.class);
				break;
			case R.id.btnTextMessages:
				startNewActivity(TextMessagesApp.class);
				break;
			case R.id.btnContacts:
				startNewActivity(ContactsApp.class);
				break;
			case R.id.btnStatus:
				startNewActivity(StatusApp.class);
				break;
			case R.id.btnSettings:
				startNewActivity(SettingsMenu.class);
				break;
			}
			break;
		}
		return false;
	}

	@Override
	public boolean onKeyOther(View view, Editable text, KeyEvent event) {
		return false;
	}
}
