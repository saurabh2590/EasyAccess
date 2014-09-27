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
package org.easyaccess.phonedialer;

import java.lang.reflect.Method;

import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

/**
 * The screen that displays the details of the current call.
 */
public class CallingScreen extends Activity {

	/** Declare UI elements and variables **/
	private String callerDetails;
	private TextView recipientTextView;
	private Button answerButton;
	private BroadcastReceiver bReceiver;
	private GestureDetector gestureDetector;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calling);

		recipientTextView = (TextView) findViewById(R.id.recipientTextView);
		answerButton = (Button) findViewById(R.id.answerButton);

		gestureDetector = new GestureDetector(getApplicationContext(),
				new GestureListener());

		if (getIntent().getExtras() != null) {
			if (Utils.callingDetails != null
					&& Utils.callingDetails.get("name") != null) {
				// Retrieve the name of the recipient and the type of the number
				// from the Bundle
				String name = Utils.callingDetails.get("name");
				String typeOfNumber = Utils.callingDetails.get("type");
				if (getIntent().getExtras().getInt("type", -1) == Utils.OUTGOING) {
					// outgoing call
					callerDetails = "Calling " + name + ": " + typeOfNumber;
					answerButton.setVisibility(View.GONE);
				} else {
					// incoming call
					callerDetails = "Call from " + name + ": " + typeOfNumber;
					answerButton.setVisibility(View.VISIBLE);
				}
			} else if (Utils.callingDetails != null) {
				// Retrieve the name of the recipient and the type of the number
				// from the Bundle
				if (getIntent().getExtras().getInt("type", -1) == Utils.OUTGOING) {
					// outgoing call
					callerDetails = "Calling "
							+ Utils.callingDetails.get("number");
					answerButton.setVisibility(View.GONE);
				} else {
					// incoming call
					callerDetails = "Call from "
							+ Utils.callingDetails.get("number");
					answerButton.setVisibility(View.VISIBLE);
				}
			}
			if (Utils.callingDetails != null) {
				displayCall(callerDetails, Utils.callingDetails.get("number"));
			}
		} else {
			recipientTextView.setText("Error!");
			recipientTextView.setContentDescription("Error");
		}

		answerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// answer call
				Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
				buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
						KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
				getApplicationContext().sendOrderedBroadcast(buttonUp,
						"android.permission.CALL_PRIVILEGED");
			}
		});

		this.bReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Utils.CALL_ENDED)) {
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak(intent.getExtras().getString("message"));
					Toast.makeText(getApplicationContext(),
							intent.getExtras().getString("message"),
							Toast.LENGTH_SHORT).show();
					// check if there is any active call (no. of calls); if so,
					// activate the current call,
					if (Utils.off_hook == 1 || Utils.ringing == 1) {

					} else {
						// All calls ended, finish activity
						finish();
						Utils.off_hook = 0;
					}

				}
			}
		};

	}

	/**
	 * Announces the text, that is the name and type of the contact or the
	 * number being passed as a parameter.
	 * 
	 * @param details
	 *            Consists of the name and type of the contact or the number if
	 *            the number is not stored in the phone.
	 */

	public static void announceCaller(String details) {
		TTS.speak(details);
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
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			// do nothing
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_VOLUME_UP == event.getKeyCode()) {
			if (Utils.ringing == 1) {
				// announce the caller name/number
				announceCaller(this.callerDetails);
				return true;
			} else if (Utils.ringing == 0 && Utils.off_hook == 1) {
				// activate loudspeaker
				Utils.audioManager = (AudioManager) getApplicationContext()
						.getSystemService(Context.AUDIO_SERVICE);
				if (Utils.audioManager.isSpeakerphoneOn() == false) {
					Utils.audioManager.setSpeakerphoneOn(true);
				} else {
					// deactivate loudspeaker
					Utils.audioManager.setSpeakerphoneOn(false);
				}
				return true;
			}
		} else if (KeyEvent.KEYCODE_VOLUME_DOWN == event.getKeyCode()) {
			if (Utils.ringing == 1) {
				// mute the ringtone
				muteRingtone();
				return true;
			} else if (Utils.ringing == 0 && Utils.off_hook == 1) {

				if (Utils.audioManager.isMicrophoneMute() == true) {
					Utils.audioManager.setMicrophoneMute(false);
				} else {
					Utils.audioManager.setMicrophoneMute(true);
				}
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * Stops the ringtone if it is playing.
	 */
	public static void muteRingtone() {
		if (Utils.ringtone.isPlaying()) {
			// mute
			Utils.ringtone.stop();
		}
	}

	/**
	 * Displays the details of the call.
	 * 
	 * @param details
	 *            Consists of the call details.
	 * @param number
	 *            Stores the number associated with the call.
	 */

	public void displayCall(String details, String number) {
		recipientTextView.setText(details);
		recipientTextView.setContentDescription(details.replaceAll(".(?=[0-9])", "$0 "));

		// store outgoing call details
		if (!Utils.numbers.contains(number)) {
			Utils.numbers.add(number);
			Utils.callers.add(callerDetails);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver(
				(this.bReceiver), new IntentFilter(Utils.CALL_ENDED));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return gestureDetector.onTouchEvent(e);
	}

	/**
	 * Class to detect gestures on the Calling Screen. 
	 */
	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.GestureDetector.SimpleOnGestureListener#onDown(android
		 * .view.MotionEvent)
		 */
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		// event when double tap occurs
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.GestureDetector.SimpleOnGestureListener#onDoubleTap(
		 * android.view.MotionEvent)
		 */
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			// user wants to type a number
			Intent intent = new Intent(getBaseContext(), PhoneDialerApp.class);
			intent.putExtra("flag", 1);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.GestureDetector.SimpleOnGestureListener#onLongPress(
		 * android.view.MotionEvent)
		 */
		@Override
		public void onLongPress(MotionEvent e) {
			// make a new call
			Intent intent = new Intent(getBaseContext(), PhoneDialerApp.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		if (Utils.off_hook == 0 && Utils.ringing == 0)
			finish();
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.calling);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}