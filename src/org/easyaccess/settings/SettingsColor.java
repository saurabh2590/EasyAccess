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
import org.easyaccess.contacts.SpinnerAdapter;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * SettingsColor allows the user to select the text color and background color
 * for all the screens in the app
 **/
public class SettingsColor extends EasyAccessActivity {
	/**
	 * The Color option in easyaccess Settings lists the options for setting the
	 * text color and the background color of the app.
	 */

	/** Declare variables and UI elements **/
	private Spinner spinnerFg, spinnerBg;
	private SpinnerAdapter adapter;
	private TextView txtForeground, txtBackground, txtPreview;
	private Button btnReset;
	private Editor editor;
	private SharedPreferences preferences;
	private int currentSelectionFg = -1, currentSelectionBg = -1;
	private String msg;
	private static int flag = 0;

	/**
	 * Saves the selected text color and background color in SharedPreferences.
	 * 
	 * @param spinner
	 *            This is an instance of Spinner.
	 * @param position
	 *            This is the position of the spinner item that was selected by
	 *            the user.
	 */
	@SuppressLint("DefaultLocale")
	void applyColor(Spinner spinner, int position) {
		String color = getResources().getStringArray(R.array.colors)[position];
		int resId = getResources().getIdentifier(color.toLowerCase(), "color",
				SettingsColor.this.getPackageName());

		switch (spinner.getId()) {
		case R.id.fcolors:
			// spinner associated with text colors was passed as parameter
			// save in SharedPreferences
			preferences = getSharedPreferences(
					getResources().getString(R.string.color), 0);
			editor = preferences.edit();
			editor.putInt("fgcolor", resId);
			txtPreview.setTextColor(getResources().getColor(resId));
			if (editor.commit()) {
				// check if keyboard is connected and accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(spinner.getSelectedItem().toString()
							+ " "
							+ getResources().getString(
									R.string.appliedFgColorSuccess));
				Toast.makeText(
						getApplicationContext(),
						spinner.getSelectedItem().toString()
								+ " "
								+ getResources().getString(
										R.string.appliedFgColorSuccess),
						Toast.LENGTH_SHORT).show();
			} else {
				// check if keyboard is connected and accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(spinner.getSelectedItem().toString()
							+ " "
							+ getResources().getString(
									R.string.appliedFgColorFailure));
				Toast.makeText(
						getApplicationContext(),
						spinner.getSelectedItem().toString()
								+ " "
								+ getResources().getString(
										R.string.appliedFgColorFailure),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.bcolors:
			// spinner associated with background colors was passed as parameter
			// save in SharedPreferences
			preferences = getSharedPreferences(
					getResources().getString(R.string.color), 0);
			editor = preferences.edit();
			editor.putInt("bgcolor", resId);
			txtPreview.setBackgroundColor(getResources().getColor(resId));
			if (editor.commit()) {
				// check if keyboard is connected and accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(spinner.getSelectedItem().toString()
							+ " "
							+ getResources().getString(
									R.string.appliedBgColorSuccess));
				Toast.makeText(
						getApplicationContext(),
						spinner.getSelectedItem().toString()
								+ " "
								+ getResources().getString(
										R.string.appliedBgColorSuccess),
						Toast.LENGTH_SHORT).show();
			} else {
				// check if keyboard is connected and accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(spinner.getSelectedItem().toString()
							+ " "
							+ getResources().getString(
									R.string.appliedBgColorFailure));
				Toast.makeText(
						getApplicationContext(),
						spinner.getSelectedItem().toString()
								+ " "
								+ getResources().getString(
										R.string.appliedBgColorFailure),
						Toast.LENGTH_SHORT).show();
			}
			break;
		}
		LinearLayout layout = (LinearLayout) findViewById(R.id.settingscolor);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
	}

	/**
	 * Attaches onKey listener to the Button passed as a parameter to the
	 * method. If enter key on the keyboard or center key on the keypad is
	 * pressed, it resets the text color and the background color of the app to
	 * their default values.
	 * 
	 * @param button
	 *            This is an instance of Button
	 */
	void attachKeyListener(final Button button) {
		button.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						reset();
						break;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Attaches onFocusChange listener to the TextView passed as parameter to
	 * the method. If a keyboard is connected to the device, a TTS feedback
	 * would be given to the user informing him/her about the text on the
	 * TextView that received focus.
	 * 
	 * @param textView
	 *            This is an instance of TextView.
	 */
	void attachListenerToTextView(TextView textView) {
		textView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					giveFeedback(((TextView) view).getText().toString());
				}
			}
		});
	}

	/**
	 * Clears the text color and background color from SharedPreferences and
	 * resets the text color and the background color of the app to their
	 * default values.
	 */
	void reset() {
		SettingsColor.this.preferences = getSharedPreferences(getResources()
				.getString(R.string.color), 0);
		SettingsColor.flag = 0;
		editor = SettingsColor.this.preferences.edit();
		editor.putInt("fgcolor", -1);
		editor.putInt("bgcolor", -1);
		editor.commit();

		spinnerFg.setSelection(0);
		spinnerBg.setSelection(1);

		LinearLayout layout = (LinearLayout) findViewById(R.id.settingscolor);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		// check if keyboard is connected and accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
			TTS.speak(getResources().getString(R.string.resetColorsSuccess));
		Toast.makeText(getApplicationContext(),
				getResources().getString(R.string.resetColorsSuccess),
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * Attaches onItemSelected listener to the Spinner passed as a parameter to
	 * the method.
	 * 
	 * @param spinner
	 *            This is an instance of Spinner. The selected color is applied
	 *            when an item of the spinner is selected.
	 */
	void attachListenerToSpinnerItem(final Spinner spinner) {

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@SuppressLint("DefaultLocale")
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view,
					int position, long arg3) {
				// flag value is used so that the color is not applied when the
				// activity is started.
				// OnItemSelected is called after the activity is created.
				if (flag > 1) {
					applyColor(spinner, position);
				} else {
					LinearLayout layout = (LinearLayout) findViewById(R.id.settingscolor);
					Utils.applyFontColorChanges(getApplicationContext(), layout);
					flag++;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	/**
	 * Retrieves the position of the color in the spinner based on its value in
	 * color.xml.
	 * 
	 * @param colorValue
	 *            . This is the integer value of the color specified in
	 *            color.xml.
	 **/
	int getColorIndex(int colorValue) {
		switch (colorValue) {
		case R.color.black:
		default:
			return 0;
		case R.color.white:
			return 1;
		case R.color.blue:
			return 2;
		case R.color.cyan:
			return 3;
		case R.color.green:
			return 4;
		case R.color.grey:
			return 5;
		case R.color.magenta:
			return 6;
		case R.color.red:
			return 7;
		case R.color.yellow:
			return 8;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// check if keyboard is connected and accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			TTS.speak(getResources().getString(R.string.settingsColor));
		}

		// initialize the flag when the activity comes to the foreground
		SettingsColor.flag = 0;
		// Apply the selected font color, font size and font type to the
		// activity
		LinearLayout layout = (LinearLayout) findViewById(R.id.settingscolor);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);

		int fgColorValue = getSharedPreferences(
				getResources().getString(R.string.color), 0).getInt("fgcolor",
				-1);
		int bgColorValue = getSharedPreferences(
				getResources().getString(R.string.color), 0).getInt("bgcolor",
				-1);
		// check if keyboard is connected or accessibility services are enabled
		if (Utils.isAccessibilityEnabled(getApplicationContext())
				|| getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			msg = " ";
			if (fgColorValue == -1) {
				msg = getString(R.string.foreground_color_black);
			} else {
				msg += " " + getString(R.string.foreground_color) + " "
						+ Utils.colorNames.get(getColorIndex(fgColorValue));
			}
			if (bgColorValue == -1) {
				msg += ", " + getString(R.string.backgroud_color_white);
			} else {
				msg += " " + getString(R.string.background_color) + " "
						+ Utils.colorNames.get(getColorIndex(bgColorValue));
			}
			new java.util.Timer().schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					TTS.speak(msg);
				}
			}, 1000);
		}

		// Display the selected colors in the corresponding spinners
		if (fgColorValue == -1) {
			fgColorValue = 0;
			spinnerFg.setSelection(fgColorValue);
		} else {
			spinnerFg.setSelection(getColorIndex(fgColorValue));
		}
		if (bgColorValue == -1) {
			bgColorValue = 1;
			spinnerBg.setSelection(bgColorValue);
		} else {
			spinnerBg.setSelection(getColorIndex(bgColorValue));
		}
	}

	/** Creates the SettingsColor activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.settingscolor);
		super.onCreate(savedInstanceState);
		// Find UI elements
		spinnerFg = (Spinner) findViewById(R.id.fcolors);
		spinnerBg = (Spinner) findViewById(R.id.bcolors);
		txtPreview = (TextView) findViewById(R.id.txtPreview);
		txtForeground = (TextView) findViewById(R.id.txtForeground);
		txtBackground = (TextView) findViewById(R.id.txtBackground);
		btnReset = (Button) findViewById(R.id.btnApplyColors);

		adapter = new SpinnerAdapter(getApplicationContext(), Utils.colorNames);
		spinnerFg.setAdapter(adapter);
		adapter = new SpinnerAdapter(getApplicationContext(), Utils.colorNames);
		spinnerBg.setAdapter(adapter);
		spinnerFg.setSelection(0);
		spinnerBg.setSelection(1);
		// Attach onFocusChanged listener to both the Spinners
		attachListenerToSpinner(spinnerFg);
		attachListenerToSpinner(spinnerBg);

		// Attach onFocusChanged listener to the Text Views
		attachListenerToTextView(txtForeground);
		attachListenerToTextView(txtBackground);

		// Attach onItemSelected listener to both the Spinners
		attachListenerToSpinnerItem(spinnerFg);
		attachListenerToSpinnerItem(spinnerBg);

		// Attach onFocusChanged listener to reset button
		attachListener(btnReset);
		// Attach onKey listener to reset button
		attachKeyListener(btnReset);

		// Attach onClick listener to reset button
		btnReset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				reset();
			}
		});

		// Attach onKey listener to the spinner containing text colors
		spinnerFg.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				Spinner spinner = (Spinner) view;
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_DOWN:
						currentSelectionFg++;
						if (currentSelectionFg == spinner.getCount()) {
							currentSelectionFg = 0;
						}
						giveFeedback(spinnerFg.getItemAtPosition(
								currentSelectionFg).toString());
						spinner.setSelection(currentSelectionFg);
						break;
					case KeyEvent.KEYCODE_DPAD_UP:
						currentSelectionFg--;
						if (currentSelectionFg <= -1) {
							currentSelectionFg = spinner.getChildCount() - 1;
						}
						giveFeedback(spinnerFg.getItemAtPosition(
								currentSelectionFg).toString());
						spinner.setSelection(currentSelectionFg);
						break;
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						if (currentSelectionBg != -1)
							applyColor(spinnerFg, currentSelectionFg);
						break;
					}
				}
				return false;
			}
		});

		// Attach onKey listener to the spinner containing background colors
		spinnerBg.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				Spinner spinner = (Spinner) view;
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_DOWN:
						currentSelectionBg++;
						if (currentSelectionBg == spinner.getCount()) {
							currentSelectionBg = 0;
						}
						giveFeedback(spinnerBg.getItemAtPosition(
								currentSelectionBg).toString());
						spinner.setSelection(currentSelectionBg);
						break;
					case KeyEvent.KEYCODE_DPAD_UP:
						currentSelectionBg--;
						if (currentSelectionBg <= -1) {
							currentSelectionBg = spinner.getCount() - 1;
						}
						giveFeedback(spinnerBg.getItemAtPosition(
								currentSelectionBg).toString());
						spinner.setSelection(currentSelectionBg);
						break;
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						if (currentSelectionBg != -1)
							applyColor(spinnerBg, currentSelectionBg);
						break;
					}
				}
				return false;
			}
		});
	}
}