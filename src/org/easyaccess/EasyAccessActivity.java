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

import org.easyaccess.settings.ScreenCurtainFunctions;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EasyAccessActivity extends Activity implements KeyListener {

	/** Declare variables used for the Screen Curtain feature **/
	protected View curtainView;
	protected boolean curtainSet = false;

	/**
	 * Attaches onFocusChangeListener to the button passed as a parameter. When the button receives focus, giveFeedback method is called, that reads aloud the
	 * string passed to it as a parameter.
	 * 
	 * @param button is an instance of Button.
	 */
	protected void attachListener(Button button) {
		final String text = button.getText().toString();

		button.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					giveFeedback(text);
				}
			}
		});
	}

	/** Launch the respective Java class, depending on which button is pressed **/
	protected void setButtonClickActivity(int buttonInt, final Context ctx, final Class<?> cls) {
		Button button = (Button) findViewById(buttonInt);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ctx, cls);
				startActivity(intent);
			}
		});
	}

	/** Launch the respective Android intent, depending on which button is pressed
	 * @param buttonInt
	 * @param intentTarget**/
	protected void setButtonClickIntent(String buttonInt, final String intentTarget) {
		Button button = (Button) findViewById(Integer.parseInt(buttonInt));
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(intentTarget);
				startActivity(intent);
			}
		});
	}



	/** Launch the respective Android app, depending on which button is pressed **/
	protected void setButtonClickUri(int buttonInt, final String uriTarget) {
		Button button = (Button) findViewById(buttonInt);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				launchOrDownloadFromActivity(uriTarget);
			}
		});
	}

	/** Launch installed Android app or download from Google Play Store if missing **/
	void launchOrDownloadFromActivity(String uriTarget) {
		Intent intent = getPackageManager().getLaunchIntentForPackage(uriTarget);
		if (intent != null) {
			// Start installed app
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} else {
			// If app is not installed, bring user to the Play Store
			intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(Uri.parse("market://details?id=" + uriTarget));

			// Error handling in case Play Store cannot be launched
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Context context = getApplicationContext();
				CharSequence text = "Unable to launch the Google Play Store!";
				Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Turns off the screen curtain functionality if it is on.
	 */
	protected void turnOffScreenCurtain() {
		WindowManager windowManager = getWindowManager();
		ScreenCurtainFunctions appState = ((ScreenCurtainFunctions) getApplicationContext());
		if (appState.getState()) {
			windowManager.removeView(curtainView);
			curtainSet = false;
			appState.setState(false);
		}
	}

	/**
	 * Attachs onFocusChangeListener to the spinner passed as a parameter. When the spinner receives focus, giveFeedback method is called, that reads aloud the
	 * string passed to it as a parameter.
	 * 
	 * @param spinner is an instance of Spinner.
	 */
	protected void attachListenerToSpinner(Spinner spinner) {
		final String text = spinner.getContentDescription().toString();
		spinner.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					giveFeedback(text);
				}
			}
		});
	}

	/**
	 * Announces the text passed as a parameter, and causes the device to vibrate for 300 milliseconds.
	 * 
	 * @param text The text that is to be read aloud.
	 */
	public void giveFeedback(String text) {
		// vibrate
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(300);
		// TTS feedback
		if (!TTS.isSpeaking())
			TTS.speak(text);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// hide the action bar
		if (getActionBar() != null) {
			getActionBar().setDisplayShowHomeEnabled(false);
			getActionBar().setDisplayShowTitleEnabled(false);
		}

		// Find easyaccess-specific Back and Home buttons
		Button btnNavigationBack = (Button) findViewById(R.id.btnNavigationBack);
		Button btnNavigationHome = (Button) findViewById(R.id.btnNavigationHome);

		// If Back navigation button is pressed, go back to previous activity
		btnNavigationBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				turnOffScreenCurtain();
				finish();
			}
		});

		// If Home navigation button is pressed, go back to previous activity
		btnNavigationHome.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				turnOffScreenCurtain();
				finish();
				Intent intent = new Intent(getApplicationContext(), SwipingUtils.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				startActivity(intent);
			}
		});

		OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					TTS.speak(((TextView) view).getText().toString());
				}
			}
		};

		// Attach onFocusChange listener to back and home buttons
		btnNavigationBack.setOnFocusChangeListener(focusChangeListener);
		btnNavigationHome.setOnFocusChangeListener(focusChangeListener);
	}

	@Override
	public void clearMetaKeyState(View arg0, Editable arg1, int arg2) {

	}

	@Override
	public int getInputType() {
		return 0;
	}

	@Override
	public boolean onKeyDown(View view, Editable arg1, int keyCode, KeyEvent keyEvent) {
		return false;
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	/**
	 * Back and Home button functionalities for all the activities that extend easyaccessActivity
	 **/
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
			// go to the previous screen
			// check if keyboard is connected and accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext()) && getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getString(R.string.btnNavigationBack));
			turnOffScreenCurtain();
			finish();
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_F1) {
			// go to the home screen
			// check if keyboard is connected and accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext()) && getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getString(R.string.btnNavigationHome));
			turnOffScreenCurtain();
			finish();
			Intent intent = new Intent(getApplicationContext(), SwipingUtils.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			startActivity(intent);
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyOther(View arg0, Editable arg1, KeyEvent arg2) {
		return false;
	}

	@Override
	public boolean onKeyUp(View view, Editable arg1, int keyCode, KeyEvent keyEvent) {
		return false;
	}

}