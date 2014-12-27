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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
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

public class SettingsFont extends EasyAccessActivity {
	/**
	 * The Font option in easyaccess allows the user to set a font size and
	 * select the font type used in the app.
	 */

	/** Declare variables and UI elements **/
	private Spinner spinnerFontType;
	private SpinnerAdapter adapter;
	private TextView txtPreview, txtFontSize, txtFontType;
	private Button btnIncrease, btnDecrease, btnReset;
	private TextView txtNumber;
	private SharedPreferences preferences;
	private Editor editor;
	private int currentSelection = -1, flag = 0;

	/**
	 * Attaches onClick listener to the Button passed as a parameter to the
	 * method. The method corresponding to the button being clicked is called.
	 * 
	 * @param button
	 *            This is an instance of Button.
	 */
	void attachOnClickListenerToButton(Button button) {
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				switch (view.getId()) {
				case R.id.btnApplyFont:
					reset();
					break;
				case R.id.btnIncrease:
					increase();
					break;
				case R.id.btnDecrease:
					decrease();
					break;
				}
			}
		});
	}

	/**
	 * Attaches onKey listener to the Button passed as a parameter to the
	 * method. The method corresponding to the button on which the enter key of
	 * the keyboard or the center key of the keypad is pressed.
	 * 
	 * @param button
	 *            This is an instance of Button.
	 */
	void attachKeyListener(final Button button) {
		button.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						switch (view.getId()) {
						case R.id.btnApplyFont:
							reset();
							break;
						case R.id.btnIncrease:
							increase();
							break;
						case R.id.btnDecrease:
							decrease();
							break;
						}
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
	 * Attaches onItemSelected listener and onKey listener to the Spinner passed
	 * as a parameter to the method. If an item is selected from the spinner,
	 * the selected font type is saved to SharedPreferences. KeyListener is used
	 * to shift the focus in the spinner items using the up and down arrow keys
	 * on the keyboard or the keypad.
	 * 
	 * @param spinner
	 *            This is an instance of Spinner.
	 */
	void attachListenerToSpinnerItem(final Spinner spinner) {

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@SuppressLint("DefaultLocale")
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view,
					int position, long arg3) {
				// flag value is used so that the font type is not applied when
				// the activity is started.
				// OnItemSelected is called after the activity is created.
				if (flag == 1) {
					String typeface = Integer.toString(position);
					// save in SharedPreferences
					preferences = getSharedPreferences(getResources()
							.getString(R.string.fonttype), 0);
					if (typeface
							.equals(getResources().getString(R.string.none))) {
						txtPreview.setTypeface(Typeface.create("default",
								Typeface.NORMAL));
					} else if (typeface.equals(getResources().getString(
							R.string.serif))) {
						txtPreview.setTypeface(Typeface.SERIF);
					} else if (typeface.equals(getResources().getString(
							R.string.monospace))) {
						txtPreview.setTypeface(Typeface.MONOSPACE);
					}
					editor = preferences.edit();
					editor.putInt("typeface", position);
					if (editor.commit()) {
						// check if keyboard is connected and accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							TTS.speak(spinner.getSelectedItem().toString()
									+ " "
									+ getResources().getString(
											R.string.appliedTypeSuccess));
						Toast.makeText(
								getApplicationContext(),
								spinner.getSelectedItem().toString()
										+ " "
										+ getResources().getString(
												R.string.appliedTypeSuccess),
								Toast.LENGTH_SHORT).show();
					} else {
						// check if keyboard is connected and accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							TTS.speak(spinner.getSelectedItem().toString()
									+ " "
									+ getResources().getString(
											R.string.appliedTypeFailure));
						Toast.makeText(
								getApplicationContext(),
								spinner.getSelectedItem().toString()
										+ " "
										+ getResources().getString(
												R.string.appliedTypeFailure),
								Toast.LENGTH_SHORT).show();
					}
					LinearLayout layout = (LinearLayout) findViewById(R.id.settingsfont);
					Utils.applyFontTypeChanges(getApplicationContext(), layout);
				} else {
					flag++;
					LinearLayout layout = (LinearLayout) findViewById(R.id.settingsfont);
					Utils.applyFontTypeChanges(getApplicationContext(), layout);
					Utils.applyFontSizeChanges(getApplicationContext(), layout);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		spinner.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				Spinner lstView = (Spinner) view;
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						if (currentSelection != -1) {
							String typeface = Integer
									.toString(currentSelection);
							// save in SharedPreferences
							preferences = getSharedPreferences(getResources()
									.getString(R.string.fonttype), 0);
							if (typeface.equals(getResources().getString(
									R.string.none))) {
								txtPreview.setTypeface(Typeface.create(
										"default", Typeface.NORMAL));
							} else if (typeface.equals(getResources()
									.getString(R.string.serif))) {
								txtPreview.setTypeface(Typeface.SERIF);
							} else if (typeface.equals(getResources()
									.getString(R.string.monospace))) {
								txtPreview.setTypeface(Typeface.MONOSPACE);
							}
							editor = preferences.edit();
							editor.putInt("typeface", currentSelection);
							if (editor.commit()) {
								// check if keyboard is connected and
								// accessibility services are disabled
								if (!Utils
										.isAccessibilityEnabled(getApplicationContext())
										&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
									TTS.speak(lstView.getSelectedItem()
											.toString()
											+ " "
											+ getResources()
													.getString(
															R.string.appliedTypeSuccess));
								Toast.makeText(
										getApplicationContext(),
										lstView.getSelectedItem().toString()
												+ " "
												+ getResources()
														.getString(
																R.string.appliedTypeSuccess),
										Toast.LENGTH_SHORT).show();
							} else {
								// check if keyboard is connected and
								// accessibility services are disabled
								if (!Utils
										.isAccessibilityEnabled(getApplicationContext())
										&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
									TTS.speak(lstView.getSelectedItem()
											.toString()
											+ " "
											+ getResources()
													.getString(
															R.string.appliedTypeFailure));
								Toast.makeText(
										getApplicationContext(),
										lstView.getSelectedItem().toString()
												+ " "
												+ getResources()
														.getString(
																R.string.appliedTypeFailure),
										Toast.LENGTH_SHORT).show();
							}
						}
						break;
					case KeyEvent.KEYCODE_DPAD_DOWN:
						currentSelection++;
						if (currentSelection == lstView.getCount()) {
							currentSelection = 0;
						} else {
							giveFeedback(spinner.getItemAtPosition(
									currentSelection).toString());
							lstView.setSelection(currentSelection);
						}
						break;
					case KeyEvent.KEYCODE_DPAD_UP:
						currentSelection--;
						if (currentSelection <= -1) {
							currentSelection = lstView.getCount() - 1;
						} else {
							giveFeedback(lstView.getItemAtPosition(
									currentSelection).toString());
							lstView.setSelection(currentSelection);
						}
						break;
					}
				}
				return false;
			}
		});
	}

	/** Reduces the font size by 1 and saves the value in SharedPreferences **/
	void decrease() {
		int number = Integer.valueOf(txtNumber.getText().toString());
		// check for lower limit of font size
		if (number - 1 >= Integer.valueOf(getResources().getString(
				R.string.lowerLimit))) {
			txtNumber.setText(Integer.toString(number - 1));
			txtNumber.setContentDescription(Integer.toString(number - 1));
			txtPreview.setTextSize(Float
					.valueOf(txtNumber.getText().toString()));
			// save in SharedPreferences
			preferences = getSharedPreferences(
					getResources().getString(R.string.size), 0);
			editor = preferences.edit();
			editor.putFloat("size",
					Float.valueOf(txtNumber.getText().toString()));
			if (editor.commit()) {
				LinearLayout layout = (LinearLayout) findViewById(R.id.settingsfont);
				Utils.applyFontSizeChanges(getApplicationContext(), layout);
				// check if keyboard is connected and accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(txtNumber.getText().toString()
							+ " "
							+ getResources().getString(
									R.string.appliedFontSizeSuccess));
				Toast.makeText(
						getApplicationContext(),
						txtNumber.getText().toString()
								+ " "
								+ getResources().getString(
										R.string.appliedFontSizeSuccess),
						Toast.LENGTH_SHORT).show();
			} else {
				// check if keyboard is connected and accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(txtNumber.getText().toString()
							+ " "
							+ getResources().getString(
									R.string.appliedFontSizeFailure));
				Toast.makeText(
						getApplicationContext(),
						txtNumber.getText().toString()
								+ " "
								+ getResources().getString(
										R.string.appliedFontSizeFailure),
						Toast.LENGTH_SHORT).show();
			}
		} else {
			// check if keyboard is connected and accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getResources().getString(R.string.reachedLowerLimit));
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.reachedLowerLimit),
					Toast.LENGTH_SHORT).show();
		}
	}

	/** Increases the font size by 1 and saves the value in SharedPreferences **/
	void increase() {
		int number = Integer.valueOf(txtNumber.getText().toString());
		// check for upper limit of font size
		if (number + 1 <= Integer.valueOf(getResources().getString(
				R.string.maxLimit))) {
			txtNumber.setText(Integer.toString(number + 1));
			txtNumber.setContentDescription(Integer.toString(number + 1));
			txtPreview.setTextSize(Float
					.valueOf(txtNumber.getText().toString()));
			// save in SharedPreferences
			preferences = getSharedPreferences(
					getResources().getString(R.string.size), 0);
			editor = preferences.edit();
			editor.putFloat("size",
					Float.valueOf(txtNumber.getText().toString()));
			if (editor.commit()) {
				LinearLayout layout = (LinearLayout) findViewById(R.id.settingsfont);
				Utils.applyFontSizeChanges(getApplicationContext(), layout);
				// check if keyboard is connected and accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(txtNumber.getText().toString()
							+ " "
							+ getResources().getString(
									R.string.appliedFontSizeSuccess));
				Toast.makeText(
						getApplicationContext(),
						txtNumber.getText().toString()
								+ " "
								+ getResources().getString(
										R.string.appliedFontSizeSuccess),
						Toast.LENGTH_SHORT).show();
			} else {
				// check if keyboard is connected and accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(txtNumber.getText().toString()
							+ " "
							+ getResources().getString(
									R.string.appliedFontSizeFailure));
				Toast.makeText(
						getApplicationContext(),
						txtNumber.getText().toString()
								+ " "
								+ getResources().getString(
										R.string.appliedFontSizeFailure),
						Toast.LENGTH_SHORT).show();
			}
		} else {
			// check if keyboard is connected and accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getResources().getString(R.string.reachedMaxLimit));
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.reachedMaxLimit),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Clears the font size and type values from SharedPreferences and resets
	 * the font size and type used across the app to their default values.
	 */
	void reset() {
		SettingsFont.this.preferences = getSharedPreferences(getResources()
				.getString(R.string.fonttype), 0);
		editor = SettingsFont.this.preferences.edit();
		editor.clear();
		editor.commit();
		SettingsFont.this.preferences = getSharedPreferences(getResources()
				.getString(R.string.size), 0);
		editor = SettingsFont.this.preferences.edit();
		editor.putFloat("size", Float.valueOf(getResources().getString(
				R.string.defaultFontSize)));
		editor.commit();
		spinnerFontType.setSelection(0);
		txtNumber.setText(Float.toString(getSharedPreferences(
				getResources().getString(R.string.size), 0).getFloat(
				"size",
				Float.valueOf(getResources()
						.getString(R.string.defaultFontSize)))));
		txtNumber.setContentDescription(Float.toString(getSharedPreferences(
				getResources().getString(R.string.size), 0).getFloat(
				"size",
				Float.valueOf(getResources()
						.getString(R.string.defaultFontSize)))));
		LinearLayout layout = (LinearLayout) findViewById(R.id.settingsfont);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		// check if keyboard is connected and accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
			TTS.speak(getResources().getString(R.string.resetFontSuccess));
		Toast.makeText(getApplicationContext(),
				getResources().getString(R.string.resetFontSuccess),
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * Announces the text that is passed as a parameter and causes the device to
	 * vibrate for 300 milliseconds.
	 * 
	 * @param text
	 *            This is the string that is to be read aloud.
	 */
	public void giveFeedback(String text) {
		if (text.trim().equals(getResources().getString(R.string.minus)))
			text = getResources().getString(R.string.minusDescription);
		// vibrate
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(300);
		// TTS feedback
		if (!TTS.isSpeaking())
			TTS.speak(text);
	}

	/** Creates the SettingsFont activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.settingsfont);
		super.onCreate(savedInstanceState);

		// find UI elements
		txtPreview = (TextView) findViewById(R.id.txtPreview);
		txtFontType = (TextView) findViewById(R.id.txtFontType);
		btnIncrease = (Button) findViewById(R.id.btnIncrease);
		btnDecrease = (Button) findViewById(R.id.btnDecrease);
		btnReset = (Button) findViewById(R.id.btnApplyFont);
        txtFontSize = (TextView) findViewById(R.id.txtSettingsFontHeaderSize);
		txtNumber = (TextView) findViewById(R.id.txtNumber);
		spinnerFontType = (Spinner) findViewById(R.id.fontType);
		adapter = new SpinnerAdapter(getApplicationContext(), Utils.fontType);
		spinnerFontType.setAdapter(adapter);

		// Attach onFocusChanged listener to both the TextViews
		attachListenerToTextView(txtFontSize);
		attachListenerToTextView(txtNumber);
		attachListenerToTextView(txtFontType);

		// Attach onFocusChanged listener to all the buttons
		attachListener(btnIncrease);
		attachListener(btnDecrease);
		attachListener(btnReset);

		// Attach onClick listener to all the buttons
		attachOnClickListenerToButton(btnReset);
		attachOnClickListenerToButton(btnIncrease);
		attachOnClickListenerToButton(btnDecrease);

		// Attach onItemSelected listener to both the Spinners
		attachListenerToSpinnerItem(spinnerFontType);
		attachListenerToSpinner(spinnerFontType);

		// Attach OnKey listener to increase button, decrease button and reset
		// button
		attachKeyListener(btnReset);
		attachKeyListener(btnIncrease);
		attachKeyListener(btnDecrease);
	}

	@Override
	public void onResume() {
		super.onResume();
		// check if keyboard is connected and accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			TTS.speak(getResources().getString(R.string.settingsFont));
		}
		/**
		 * Display the selected text size and text type in the TextView and the
		 * Spinner, respectively
		 **/
		txtNumber.setText(Float.toString(getSharedPreferences(
				getResources().getString(R.string.size), 0).getFloat(
				"size",
				Float.valueOf(getResources()
						.getString(R.string.defaultFontSize)))));
		txtNumber.setContentDescription(Float.toString(getSharedPreferences(
				getResources().getString(R.string.size), 0).getFloat(
				"size",
				Float.valueOf(getResources()
						.getString(R.string.defaultFontSize)))));
		spinnerFontType.setSelection(getSharedPreferences(
				getResources().getString(R.string.fonttype), 0).getInt(
				"typeface", 0));

		// check if keyboard is connected or accessibility services are enabled
		if (Utils.isAccessibilityEnabled(getApplicationContext())
				|| getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			new java.util.Timer().schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					TTS.speak(getString(R.string.fontSize)
							+ " "
							+ Float.toString(getSharedPreferences(
									getResources().getString(R.string.size), 0)
									.getFloat(
											"size",
											Float.valueOf(getResources()
													.getString(
															R.string.defaultFontSize))))
							+ ", "
							+ getString(R.string.fontType)
							+ " "
							+ Utils.fontType
									.get(getSharedPreferences(
											getResources().getString(
													R.string.fonttype), 0)
											.getInt("typeface", 0)));
				}
			}, 1000);
		}

		// Apply the selected font color, font size and font type to the
		// activity

		LinearLayout layout = (LinearLayout) findViewById(R.id.settingsfont);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}
