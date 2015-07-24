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
package org.easyaccess.textmessages;

import org.easyaccess.R;
import org.easyaccess.SwipingUtils;
import org.easyaccess.TTS;
import org.easyaccess.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that is used to write a text message and send to the selected
 * recipient.
 **/

public class TextMessagesComposerApp extends Activity implements KeyListener {

	private Button btnTextMsgsSend;
	private TextView txtRecipient;
	private EditText editMessage;
	private String name, number, type;
	private BroadcastReceiver statusReceiver;

	private static final String SENT = "1";
	private static final String DELIVERED = "2";
	private int deletedFlag = 0;

	/**
	 * Attaches onKey listener to the button passed as a parameter to the
	 * method. If enter key on the keyboard or center key on the keypad is
	 * pressed, sendMessage method is called, which will be used to send the
	 * text message to the recipient.
	 * 
	 * @param button
	 *            The button with which the onKey listener is to be associated.
	 */
	void attachKeyListener(Button button) {
		button.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						sendMessage();
						break;
					}
				}
				return false;
			}
		});
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (!editMessage.hasFocus()) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					// go to the previous screen
					// check if keyboard is connected and accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak(getString(R.string.btnNavigationBack));
					finish();
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_F1) {
					// go to the home screen
					// check if keyboard is connected and accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak(getString(R.string.btnNavigationHome));
					finish();
					Intent intent = new Intent(getApplicationContext(),
							SwipingUtils.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
					startActivity(intent);
				} else
					return super.dispatchKeyEvent(event);
			} else {
				if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					deletedFlag = 1;
					String editMessageText = editMessage.getText().toString();
					if (editMessageText.length() != 0) {
						// check if keyboard is connected and accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
							if (editMessageText.substring(
									editMessageText.length() - 1,
									editMessageText.length()).matches(
									"-?\\d+(\\.\\d+)?")) {
								TTS.speak("Deleted "
										+ editMessageText.substring(
												editMessageText.length() - 1,
												editMessageText.length())
										+ ". "
										+ TTS.readNumber(editMessageText
												.substring(0, editMessageText
														.length() - 1)));
							} else {
								TTS.speak("Deleted "
										+ editMessageText.substring(
												editMessageText.length() - 1,
												editMessageText.length())
										+ ". "
										+ editMessageText.substring(0,
												editMessageText.length() - 1));
							}
						}
						editMessage.setText(editMessageText.substring(0,
								editMessageText.length() - 1));
						
						editMessage.setSelection(editMessageText.length()-1);
//						editMessage.setSelection(editMessageText.length()-1,
//								editMessageText.length());
//						
						
						return false;
					} else {
						// check if keyboard is connected and accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							TTS.speak(getString(R.string.btnNavigationBack));
						finish();
					}
				} else {
					return super.dispatchKeyEvent(event);
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}

	/** Create the Text Messages Composer activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.textmessages_composer);
		super.onCreate(savedInstanceState);

		statusReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					TTS.stop();
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						Utils.giveFeedback(getApplicationContext(),
								getString(R.string.smsDelivered));
					Toast.makeText(getApplicationContext(),
							getString(R.string.smsDelivered),
							Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					TTS.stop();
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						Utils.giveFeedback(getApplicationContext(),
								getString(R.string.smsNotDelivered));
					Toast.makeText(getApplicationContext(),
							getString(R.string.smsNotDelivered),
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};

		// Find UI elements
		btnTextMsgsSend = (Button) findViewById(R.id.btnTextMsgsSend);
		txtRecipient = (TextView) findViewById(R.id.inputTextMessagesRecipient);
		editMessage = (EditText) findViewById(R.id.inputTextMessagesTypedMessage);

		// Find easyaccess-specific Back and Home buttons
		Button btnNavigationBack = (Button) findViewById(R.id.btnNavigationBack);
		Button btnNavigationHome = (Button) findViewById(R.id.btnNavigationHome);

		// If Back navigation button is pressed, go back to previous activity
		btnNavigationBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		// If Home navigation button is pressed, go back to previous activity
		btnNavigationHome.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(getApplicationContext(),
						SwipingUtils.class);
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

		Utils.attachListener(getApplicationContext(), btnTextMsgsSend);

		if (getIntent().hasExtra("name")) {
			this.name = getIntent().getExtras().getString("name");
			this.type = getIntent().getExtras().getString("type");
			txtRecipient.setText(this.name + " " + this.type);
			txtRecipient.setContentDescription(this.name.replaceAll(
					".(?=[0-9])", "$0 ") + " " + this.type);
		} else if (getIntent().hasExtra("number")) {
			this.number = getIntent().getExtras().getString("number");
			if (txtRecipient.getText().toString().trim().equals(""))
				txtRecipient.setText(this.number);
			txtRecipient.setContentDescription(this.number.replaceAll(
					".(?=[0-9])", "$0 "));
		}

		// If Send button is pressed, send the text message to the selected
		// recipient
		btnTextMsgsSend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				sendMessage();
			}
		});

		editMessage.addTextChangedListener(new TextWatcher() {

			@SuppressLint("DefaultLocale")
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				if (deletedFlag != 1) {
					if (cs.length() > 0) {
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
							if (cs.toString()
									.substring(cs.length() - 1, cs.length())
									.matches("(?![@',&] )\\p{Punct}")) {
								if (editMessage.getText().toString()
										.matches("-?\\d+(\\.\\d+)?")) {
									TTS.readNumber(editMessage.getText()
											.toString());
								} else {
									TTS.speak(editMessage.getText().toString());
								}
							} else {
								TTS.speak(cs.toString().substring(
										cs.length() - 1, cs.length()));
							}
						}
					}
				} else {
					deletedFlag = 0;
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		});
	}

	/**
	 * Sends the text message to the the recipient. Informs the user about the
	 * status, whether the message was sent or not.
	 */
	void sendMessage() {
		TTS.stop();
		// check if keyboard is connected but accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
			Utils.giveFeedback(getApplicationContext(),
					getString(R.string.sendingSms));
		Toast.makeText(getApplicationContext(), getString(R.string.sendingSms),
				Toast.LENGTH_SHORT).show();
		try {

			PendingIntent sentPI = PendingIntent.getBroadcast(
					getApplicationContext(), 0, new Intent(SENT), 0);
			PendingIntent deliveredPI = PendingIntent.getBroadcast(
					getApplicationContext(), 0, new Intent(DELIVERED), 0);
			// ---when the SMS has been sent---
			registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						TTS.stop();
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							Utils.giveFeedback(getApplicationContext(),
									getString(R.string.sentSms));
						Toast.makeText(getApplicationContext(),
								getString(R.string.sentSms), Toast.LENGTH_SHORT)
								.show();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						TTS.stop();
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							Utils.giveFeedback(getApplicationContext(),
									getString(R.string.noService));
						Toast.makeText(getApplicationContext(),
								getString(R.string.noService),
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						TTS.stop();
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							Utils.giveFeedback(getApplicationContext(),
									getString(R.string.radioOff));
						Toast.makeText(getApplicationContext(),
								getString(R.string.radioOff),
								Toast.LENGTH_SHORT).show();
						break;
					default:
						TTS.stop();
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							Utils.giveFeedback(getApplicationContext(),
									getString(R.string.smsNotSent));
						Toast.makeText(getApplicationContext(),
								getString(R.string.smsNotSent),
								Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}, new IntentFilter(SENT));
			// ---when the SMS has been delivered---
			registerReceiver(statusReceiver, new IntentFilter(DELIVERED));
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(TextMessagesComposerApp.this.number, null,
					editMessage.getText().toString(), sentPI, deliveredPI);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		// check if keyboard is connected and accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			TTS.speak(getString(R.string.composeTextMessage));
		}
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.textmessagescomposer);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}

	@Override
	public void clearMetaKeyState(View arg0, Editable arg1, int arg2) {
	}

	@Override
	public int getInputType() {
		return 0;
	}

	@Override
	public boolean onKeyDown(View arg0, Editable arg1, int arg2, KeyEvent arg3) {
		return false;
	}

	@Override
	public boolean onKeyOther(View arg0, Editable arg1, KeyEvent arg2) {
		return false;
	}

	@Override
	public boolean onKeyUp(View arg0, Editable arg1, int arg2, KeyEvent arg3) {
		return false;
	}
}
