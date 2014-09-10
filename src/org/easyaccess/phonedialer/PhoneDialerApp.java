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

package org.easyaccess.phonedialer;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

/**
 * The PhoneDialer option in JustDroid allows the user to make and receive calls.
 */

public class PhoneDialerApp extends EasyAccessActivity {

	/** Declare variables and UI elements **/
	private TelephonyManager telManager;
	private Button btnKeypad1, btnCall;
	private CallManager callManager;
	private ContactManager contactManager;
	private ConnectivityManager cm;
	private NetworkInfo networkInfo;
	private HashMap<String, String> callingDetails;
	private String strDialNumber = "";
	private TextView txtDialNumber;

	/**
	 * Retrieves the voicemail number associated with the digit pressed by the
	 * user. The number is displayed in the TextView.
	 */

	void callVoiceMail() {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(800);
		telManager = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telManager.getVoiceMailNumber() != null) {
			txtDialNumber.setText(telManager.getVoiceMailNumber());
			txtDialNumber.setContentDescription(telManager.getVoiceMailNumber().
					replaceAll(".(?=[0-9])", "$0 "));
			strDialNumber = txtDialNumber.getText().toString();
		}
	}

	/**
	 * Displays the digit pressed by the user in the TextView. The device
	 * vibrates every time the user presses a digit.
	 * 
	 * @param buttonInt
	 *            This is the id of the button that was pressed.
	 * @param vibrateLength
	 *            This is the number of milliseconds for which the device should
	 *            vibrate.
	 * @param strDialDigit
	 *            This is the digit that should be displayed in the TextView.
	 */

	void appendDialNumber(int buttonInt, final int vibrateLength,
			final String strDialDigit) {
		Button button = (Button) findViewById(buttonInt);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// check if keyboard is connected or accessibility services are
				// enabled
				if (Utils.isAccessibilityEnabled(getApplicationContext())
						|| getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
					TTS.speak(strDialDigit);
				}
				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(vibrateLength);
				strDialNumber = strDialNumber + strDialDigit;
				txtDialNumber.setText(strDialNumber);
				txtDialNumber.setContentDescription(strDialNumber.replaceAll(".(?=[0-9])", "$0 "));
			}
		});
	}

	/**
	 * Attaches OnFocusChangeListener to all the buttons and the TextView. The
	 * text on the view passed as the parameter is read aloud.
	 * 
	 * @param viewInt
	 *            This is the id of the view with which the
	 *            onFocusChangeListener is to be associated.
	 */

	void attachOnFocusChangeListener(int viewInt) {
		Button button = null;
		TextView textView = null;
		if (viewInt == R.id.txtDialNumber) {
			textView = (TextView) findViewById(viewInt);
			textView.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View view, boolean hasFocus) {
					if (hasFocus) {
						if (!(((TextView) view).getText().toString().equals(""))) {
							TTS.speak(TTS.readNumber(((TextView) view)
									.getText().toString()));
						}
						else {
							TTS.speak(((TextView) view).getContentDescription().toString());
						}
					}
				}
			});
		} else {
			button = (Button) findViewById(viewInt);
			button.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View view, boolean hasFocus) {
					if (hasFocus) {
						if (((Button) view).getText().toString().equals("<")) {
							TTS.speak(getResources().getString(R.string.delete));
						} else {
							TTS.speak(((Button) view).getText().toString());
						}
					}
				}
			});
		}
	}

	/**
	 * Calls the method to make the call if the user entered a number.
	 */
	void checkAndMakeCall() {
		if (strDialNumber != null && !strDialNumber.isEmpty()) {
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			long pattern[] = new long[] { 0, 200, 100, 200 };
			vibrator.vibrate(pattern, -1);
			makeCall();
		}
	}

	/**
	 * Checks the SIM card and network state, and makes the call, that is,
	 * passes the number to the default dialer app. A TTS feedback is given to
	 * the user informing him/her about the status of the operation.
	 */

	void makeCall() {
		// check SIM card availability if network is available
		if (callManager.getSimState().equals(
				getApplicationContext().getResources().getString(
						R.string.sim_ready))) {
			callManager.setNumber(txtDialNumber.getText().toString());

			// check Network availability
			if (callManager.getServiceState().equals(
					getApplicationContext().getResources().getString(
							R.string.state_in_service))) {
				// get details of the number
				callingDetails = contactManager.getNameFromNumber(txtDialNumber
						.getText().toString());

				// Check if there is any existing call
				if (Utils.off_hook == 1) {
					announceCall(callingDetails, 1);
				}

				// pass the details to the Calling Activity
				// make call
				announceCall(callingDetails, 0);
				Utils.callingDetails = callingDetails;
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
						+ txtDialNumber.getText()));
				startActivity(intent);
				if (getIntent().getExtras() != null
						&& getIntent().getExtras().getString("call") != null) {
					finish();
				}
			} else {
				// Inform user through TTS about the network status
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(callManager.getServiceState());
				Toast.makeText(getApplicationContext(),
						callManager.getServiceState(), Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			// Inform user through TTS about the SIM/network status
			if (callManager.getSimState().equals(
					getApplicationContext().getResources().getString(
							R.string.service_unknown_reason))) {
				Toast.makeText(getApplicationContext(),
						callManager.getSimState(), Toast.LENGTH_SHORT).show();
				TTS.stop();
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(callManager.getSimState());
			} else {
				Toast.makeText(getApplicationContext(),
						callManager.getSimState(), Toast.LENGTH_SHORT).show();
				TTS.stop();
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(callManager.getSimState());
			}
		}
	}

	/**
	 * Announces the name of the contact or the number being called if
	 * accessibility services are enabled, or a keyboard is connected to the
	 * phone.
	 * 
	 * @param details
	 *            This is a HashMap that consists of the name and type of the
	 *            contact if it is stored in the phone.
	 * @param activeCall
	 *            If this value is 1, it indicates that there is an active call.
	 */

	void announceCall(HashMap<String, String> details, int activeCall) {
		if (activeCall == 1) {
			if (details.get("name") != null) {
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak("Putting the current call on hold and calling "
							+ details.get("name") + " " + details.get("type"));
				Toast.makeText(
						getApplicationContext(),
						"Putting the current call on hold and calling "
								+ details.get("name") + " "
								+ details.get("type"), Toast.LENGTH_SHORT)
						.show();
			} else {
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak("Putting the current call on hold and calling "
							+ TTS.readNumber(txtDialNumber.getText().toString()));
				Toast.makeText(
						getApplicationContext(),
						"Putting the current call on hold and calling "
								+ txtDialNumber.getText(), Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			if (details.get("name") != null) {
				// check if keyboard is connected or accessibility services are
				// disabled
				if (Utils.isAccessibilityEnabled(getApplicationContext())
						|| getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak("Calling " + details.get("name") + " "
							+ details.get("type"));
				Toast.makeText(
						getApplicationContext(),
						"Calling " + details.get("name") + " "
								+ details.get("type"), Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getApplicationContext(),
						"Calling " + txtDialNumber.getText().toString(),
						Toast.LENGTH_SHORT).show();
				// check if keyboard is connected or accessibility services are
				// disabled
				if (Utils.isAccessibilityEnabled(getApplicationContext())
						|| getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak("Calling "
							+ TTS.readNumber(txtDialNumber.getText().toString()));
			}
		}
	}

	/**
	 * Create the Phone Dialer activity (non-Javadoc)
	 * 
	 * @see org.easyaccess.EasyAccessActivity#onCreate(android.os.Bundle)
	 */
	@SuppressLint("CutPasteId")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.phonedialer);
		super.onCreate(savedInstanceState);

		/** Find UI elements **/
		txtDialNumber = (TextView) findViewById(R.id.txtDialNumber);
		Button btnKeypadBackspace = (Button) findViewById(R.id.btnKeypadBackspace);
		Button btnCallHangup = (Button) findViewById(R.id.btnCallHangup);
		btnKeypad1 = (Button) findViewById(R.id.btnKeypad1);
		btnCall = (Button) findViewById(R.id.btnCallHangup);

		attachOnFocusChangeListener(R.id.btnCallHangup);

		/*
		 * btnCall.setOnTouchListener(new OnTouchListener() {
		 * 
		 * @Override public boolean onTouch(View view, MotionEvent arg1) {
		 * Vibrator vibrator = (Vibrator)
		 * getSystemService(Context.VIBRATOR_SERVICE); vibrator.vibrate(200);
		 * TTS.speak(((Button)view).getText().toString()); return false; } });
		 */

		// if we came here from the calling screen, the user wants to type a
		// digit
		if (getIntent().getExtras() != null
				&& getIntent().getExtras().getInt("flag", 0) == 1) {
			// do not display the call button
			btnCall.setVisibility(View.GONE);
		} else {
			// display the call button
			btnCall.setVisibility(View.VISIBLE);
		}

		callManager = new CallManager(getApplicationContext());
		contactManager = new ContactManager(getApplicationContext());
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		networkInfo = cm.getActiveNetworkInfo();
		Intent intent = new Intent(getApplicationContext(),
				CallStateService.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startService(intent);

		// If roaming, inform the user
		if (networkInfo != null) {
			if (networkInfo.isRoaming()) {
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak("Please note that roaming is activated");
				Toast.makeText(getApplicationContext(),
						"Please not that roaming is activated",
						Toast.LENGTH_SHORT).show();
			}/*
			 * else { Toast.makeText(getApplicationContext(),
			 * "You are in your home network", Toast.LENGTH_SHORT).show(); }
			 */
		}

		// if we came here from the Contacts app, make a call
		if (getIntent().getExtras() != null
				&& getIntent().getExtras().getString("call") != null) {
			txtDialNumber.setText(getIntent().getExtras().getString("call"));
			txtDialNumber.setContentDescription(getIntent().getExtras().getString("call").
					replaceAll(".(?=[0-9])", "$0 "));
			makeCall();
		}

		/** If "X" is pressed on keypad, append "X" to the dialed number **/
		appendDialNumber(R.id.btnKeypad1, 100, "1");
		appendDialNumber(R.id.btnKeypad2, 150, "2");
		appendDialNumber(R.id.btnKeypad3, 200, "3");
		appendDialNumber(R.id.btnKeypad4, 250, "4");
		appendDialNumber(R.id.btnKeypad5, 300, "5");
		appendDialNumber(R.id.btnKeypad6, 350, "6");
		appendDialNumber(R.id.btnKeypad7, 400, "7");
		appendDialNumber(R.id.btnKeypad8, 450, "8");
		appendDialNumber(R.id.btnKeypad9, 500, "9");
		appendDialNumber(R.id.btnKeypad0, 550, "0");
		appendDialNumber(R.id.btnKeypadStar, 800, "*");
		appendDialNumber(R.id.btnKeypadHash, 850, "#");

		attachOnFocusChangeListener(R.id.btnKeypad1);
		attachOnFocusChangeListener(R.id.btnKeypad2);
		attachOnFocusChangeListener(R.id.btnKeypad3);
		attachOnFocusChangeListener(R.id.btnKeypad4);
		attachOnFocusChangeListener(R.id.btnKeypad5);
		attachOnFocusChangeListener(R.id.btnKeypad6);
		attachOnFocusChangeListener(R.id.btnKeypad7);
		attachOnFocusChangeListener(R.id.btnKeypad8);
		attachOnFocusChangeListener(R.id.btnKeypad9);
		attachOnFocusChangeListener(R.id.btnKeypad0);
		attachOnFocusChangeListener(R.id.btnKeypadStar);
		attachOnFocusChangeListener(R.id.btnKeypadHash);
		attachOnFocusChangeListener(R.id.txtDialNumber);
		attachOnFocusChangeListener(R.id.btnKeypadBackspace);
		attachOnFocusChangeListener(R.id.btnCallHangup);

		/**
		 * If backspace is pressed on keypad, trim the right-most digit off the
		 * dialled number
		 **/
		btnKeypadBackspace.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (strDialNumber != null && !strDialNumber.isEmpty()) {
					// TTS.speak(getResources().getString(R.string.btnKeypadBackspaceTalkBackFriendly));
					Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vibrator.vibrate(800);
					// check if keyboard is connected or accessibility services
					// are enabled
					if (Utils.isAccessibilityEnabled(getApplicationContext())
							|| getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
						// announce the deleted digit and the remaining number
						TTS.speak(getResources().getString(R.string.deleted)
								+ " "
								+ strDialNumber.charAt(strDialNumber.length() - 1)
								+ ", "
								+ TTS.readNumber(strDialNumber.substring(0,
										strDialNumber.length() - 1)));
					}
					strDialNumber = strDialNumber.substring(0,
							strDialNumber.length() - 1);
					txtDialNumber.setText(strDialNumber);
					txtDialNumber.setContentDescription(strDialNumber.replaceAll(".(?=[0-9])", "$0 "));
				}
			}
		});

		/**
		 * get voicemail number when the button with the digit 1 is long pressed
		 **/
		btnKeypad1.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				callVoiceMail();
				return false;
			}
		});

		btnKeypad1.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						callVoiceMail();
						break;
					}
				}
				return false;
			}
		});

		/*
		 * txtDialNumber.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View view) {
		 * if(!(((TextView)view).getText().toString().equals("")))
		 * TTS.speak(TTS.readNumber(((TextView)view).getText().toString())); }
		 * });
		 */

		/**
		 * If call/hang up button is pressed, dial the number or hang up the
		 * call
		 **/
		btnCallHangup.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				checkAndMakeCall();
			}
		});

		btnCallHangup.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						checkAndMakeCall();
						break;
					}
				}
				return false;
			}
		});
		/** Put most everything before here **/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		// check if keyboard is connected and accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			TTS.speak(getResources().getString(R.string.phoneDialer));
		}
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.phonedialer);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// long press of power button will end the call
		if (KeyEvent.KEYCODE_POWER == event.getKeyCode()) {
			TelephonyManager telephony = (TelephonyManager) getApplicationContext()
					.getSystemService(Context.TELEPHONY_SERVICE);
			try {
				Class<?> c = Class.forName(telephony.getClass().getName());
				Method m = c.getDeclaredMethod("getITelephony");
				m.setAccessible(true);
				ITelephony telephonyService = (ITelephony) m.invoke(telephony);
				telephonyService.endCall();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Announces the name of the contact or the number being called.
	 * 
	 * @param details
	 *            This is a HashMap that consists of the name and type of the
	 *            contact if it is stored in the phone.
	 * @param activeCall
	 *            If this value is 1, it indicates that there is an active call.
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_VOLUME_UP == event.getKeyCode()) {
			if (Utils.ringing == 0 && Utils.off_hook == 1) {
				// activate loudspeaker
				Utils.audioManager = (AudioManager) getApplicationContext()
						.getSystemService(Context.AUDIO_SERVICE);
				Utils.audioManager.setSpeakerphoneOn(true);
				return true;
			}
		} else if (KeyEvent.KEYCODE_VOLUME_DOWN == event.getKeyCode()) {
			if (Utils.ringing == 0 && Utils.off_hook == 1) {
				// deactivate loudspeaker if activated
				if (Utils.audioManager.isSpeakerphoneOn()) {
					Utils.audioManager.setSpeakerphoneOn(false);
				}
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
}
