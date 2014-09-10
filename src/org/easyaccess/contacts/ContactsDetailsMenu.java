/*
	
	Copyright 2013 Caspar Isemer and and Eva Krueger, http://easyaccess.org
	
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

package org.easyaccess.contacts;

import java.util.ArrayList;
import java.util.HashMap;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;
import org.easyaccess.phonedialer.ContactManager;
import org.easyaccess.phonedialer.PhoneDialerApp;
import org.easyaccess.textmessages.TextMessagesComposerApp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Lists the contact's phone number and emails and provides options for various
 * operations such as calling, sending text message, editing the contact etc.
 */
@SuppressLint("DefaultLocale")
public class ContactsDetailsMenu extends EasyAccessActivity {

	/** Declare variables and UI elements **/
	private TextView btnContactName;
	private Button btnEditContactName;
	private String number;
	private String contactId;
	private HashMap<String, ArrayList<String>> contactDetails;
	@SuppressWarnings("unused")
	private GestureDetector gestureDetector;

	/**
	 * Launches the ContactUpdate activity and passes the contact's ID, number
	 * and type of number, to edit the number of the contact.
	 * 
	 * @param id
	 *            The ID of the contact.
	 * @param num
	 *            The number of the contact.
	 * @param numType
	 *            The type of the number.
	 */
	void editNumber(String id, String num, String numType) {
		Intent intent = new Intent(getApplicationContext(), ContactUpdate.class);
		intent.putExtra("id", id);
		intent.putExtra("number", num);
		intent.putExtra("numtype", numType);
		startActivity(intent);
		finish();
	}

	/**
	 * Launches the ContactUpdate activity and passes the contact's ID and email
	 * ID to edit the email ID of the contact.
	 * 
	 * @param id
	 *            The ID of the contact.
	 * @param email
	 *            The email ID of the contact.
	 */
	void editMail(String id, String email) {
		Intent intent = new Intent(getApplicationContext(), ContactUpdate.class);
		intent.putExtra("id", id);
		intent.putExtra("mail", email);
		startActivity(intent);
		finish();
	}

	/**
	 * Attaches onKey listener to the Button passed as a parameter to the
	 * method. If enter key on the keyboard or center key on the keypad is
	 * pressed, the value of the parameter passed is checked and passed to
	 * onSpaceEvent method.
	 * 
	 * @param button
	 *            The button with which the onKeyListener is to be associated.
	 * @param buttonFlag
	 *            If the value of buttonFlag is 1, the contact ID is passed to
	 *            onSpaceEvent method. If the value of buttonFlag is 2 or 3, the
	 *            contact ID, number and type of number are passed to
	 *            onSpaceEvent method. If the value of buttonFlag is 4, the
	 *            contact ID and email ID of the contact are passed to
	 *            onSpaceEvent method.
	 * @param num
	 *            The number of the contact.
	 * @param numType
	 *            The type of number of the contact.
	 * @param email
	 *            The email ID of the contact.
	 */
	void attachKeyListener(final Button button, final int buttonFlag,
			final String num, final String numType, final String email) {
		button.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_SPACE:
						switch (buttonFlag) {
						case 1:
							onSpaceEvent(button,
									ContactsDetailsMenu.this.contactId, button
											.getText().toString(), null, null,
									null);
							break;
						case 2:
						case 3:
							onSpaceEvent(button,
									ContactsDetailsMenu.this.contactId, button
											.getText().toString(), num,
									numType, null);
							break;
						case 4:
							onSpaceEvent(button,
									ContactsDetailsMenu.this.contactId, button
											.getText().toString(), null, null,
									email);
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
	 * Launches the ContactUpdate activity to provide an interface to edit the
	 * value passed to it.
	 * 
	 * @param btn
	 *            The instance of the button that was clicked.
	 * @param id
	 *            The ID of the contact.
	 * @param name
	 *            The name of the contact.
	 * @param number
	 *            The number of the contact.
	 * @param numType
	 *            The type of number of the contact.
	 * @param email
	 *            The email ID of the contact.
	 */

	@SuppressLint("DefaultLocale")
	void onSpaceEvent(Button btn, String id, String name, String number,
			String numType, String email) {
		int flag;
		final int NUMBER = 1;
		final int EMAIL = 2;
		final int NAME = 3;
		if (btn.getText().toString().toLowerCase()
				.startsWith(getResources().getString(R.string.call))
				|| btn.getText()
						.toString()
						.toLowerCase()
						.startsWith(
								getResources().getString(R.string.sendmessage))) {
			// button corresponds to a number
			flag = NUMBER;
		} else if (btn.getText().toString().toLowerCase()
				.startsWith(getResources().getString(R.string.mail))) {
			// button corresponds to an email id
			flag = EMAIL;
		} else {
			// button corresponds to the name
			flag = NAME;
		}
		Intent intent = new Intent(getApplicationContext(), ContactUpdate.class);
		intent.putExtra("id", ContactsDetailsMenu.this.contactDetails.get("id")
				.get(0));
		if (flag == NAME) {
			intent.putExtra("name", ContactsDetailsMenu.this.contactDetails
					.get("name").get(0));
			intent.putExtra("details", ContactsDetailsMenu.this.contactDetails);
		} else if (flag == NUMBER) {
			intent.putExtra("number", number);
			intent.putExtra("numtype", numType);
		} else if (flag == EMAIL) {
			intent.putExtra("mail", email);
		}
		startActivity(intent);
		finish();
	}

	/**
	 * Launches the ContactsOtherOptions activity and passes the ID of the
	 * contact and the number of the contact.
	 */
	void callOtherOptions() {
		Intent intent = new Intent(getApplicationContext(),
				ContactsOtherOptions.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("id", ContactsDetailsMenu.this.contactId);
		intent.putExtra("number", ContactsDetailsMenu.this.number);
		startActivity(intent);
		finish();
	}

	/**
	 * Launches the PhoneDialerApp activity to call the contact.
	 * 
	 * @param view
	 *            The button that was clicked by the user.
	 */
	void callDialer(View view) {
		// pass number to dialer app
		Intent intent = new Intent(getApplicationContext(),
				PhoneDialerApp.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		int positionOfNewLine = (((Button) view).getText().toString())
				.indexOf("\n");
		intent.putExtra("call", (((Button) view).getText().toString()
				.substring(positionOfNewLine)).trim());
		startActivity(intent);
		finish();
	}

	/**
	 * Launches the TextMessagesComposerApp activity to send a text message to
	 * the contact.
	 * 
	 * @param view
	 *            The button that was clicked by the user.
	 */
	void callMessagesApp(View view) {
		// pass number to dialer app
		Intent intent = new Intent(getApplicationContext(),
				TextMessagesComposerApp.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		int positionOfNewLine = (((Button) view).getText().toString())
				.indexOf("\n");
		intent.putExtra("number", (((Button) view).getText().toString()
				.substring(positionOfNewLine)).trim());
		startActivity(intent);
		finish();
	}

	/**
	 * Announces the text passed as a parameter and causes the device to vibrate
	 * for 300 milliseconds. text The text that is to be read aloud.
	 * (non-Javadoc)
	 * 
	 * @see org.easyaccess.EasyAccessActivity#giveFeedback(java.lang.String)
	 */
	public void giveFeedback(String text) {
		// vibrate
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(300);
		// TTS feedback
		TTS.speak(text);
	}

	/**
	 * Create the ContactDetailsMenu activity (non-Javadoc)
	 * 
	 * @see org.easyaccess.EasyAccessActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.contactsdetails);
		super.onCreate(savedInstanceState);

		/** get UI elements **/
		btnContactName = (TextView) findViewById(R.id.txtContactsName);
		btnEditContactName = (Button) findViewById(R.id.btnEditContactName);
		/** get the details of the contact from the number **/
		this.number = getIntent().getExtras().getString("number");
		this.contactId = getIntent().getExtras().getString("id");
		this.contactDetails = new ContactManager(getApplicationContext())
				.getDetailsFromId(this.contactId);
		try {
			btnContactName.setText(this.contactDetails.get("name").get(0));
			btnContactName.setContentDescription(this.contactDetails
					.get("name").get(0).replaceAll(".(?=[0-9])", "$0 "));
			btnEditContactName.setText("Edit "
					+ this.contactDetails.get("name").get(0));
			btnEditContactName.setContentDescription("Edit "
					+ this.contactDetails.get("name").get(0).replaceAll(".(?=[0-9])", "$0 "));

			for (int i = 0; i < contactDetails.get("numbers").size(); i++) {

				final String num = contactDetails.get("numbers").get(i);
				final String numType = contactDetails.get("types").get(i);
				final Button btnCall = new Button(getApplicationContext());
				final Button btnEditNumber = new Button(getApplicationContext());
				btnCall.setText("Call " + numType
						+ Html.fromHtml("<br/>" + num));
				btnCall.setContentDescription("Call " + numType + " " + num.replaceAll(".(?=[0-9])", "$0 "));
				btnCall.setTextColor(getResources().getColor(
						R.drawable.card_textcolor));
				btnCall.setTypeface(Typeface.DEFAULT_BOLD);
				btnCall.setTextSize(Integer.valueOf(getApplicationContext()
						.getResources().getString(R.string.textSize))
						* getApplicationContext().getResources()
								.getDisplayMetrics().density);
				btnCall.setGravity(Gravity.CENTER);
				btnCall.setFocusable(true);
				btnEditNumber.setText("Edit " + numType
						+ Html.fromHtml("<br/>" + num));
				btnEditNumber.setContentDescription("Edit "
						+ btnContactName.getText().toString() + " " + numType
						+ " " + num.replaceAll(".(?=[0-9])", "$0 "));
				btnEditNumber.setTextColor(getResources().getColor(
						R.drawable.card_textcolor));
				btnEditNumber.setTypeface(Typeface.DEFAULT_BOLD);
				btnEditNumber.setTextSize(Integer
						.valueOf(getApplicationContext().getResources()
								.getString(R.string.textSize))
						* getApplicationContext().getResources()
								.getDisplayMetrics().density);
				btnEditNumber.setGravity(Gravity.CENTER);
				btnEditNumber.setFocusable(true);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				btnCall.setLayoutParams(params);
				btnEditNumber.setLayoutParams(params);
				LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
				layout.addView(btnCall);
				layout.addView(btnEditNumber);

				btnCall.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View view, boolean hasFocus) {
						if (hasFocus) {
							giveFeedback(((Button) view).getText().toString());
						}
					}
				});

				btnCall.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						callDialer(view);
					}
				});

				btnCall.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(View view, int keyCode,
							KeyEvent keyEvent) {
						if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
							switch (keyCode) {
							case KeyEvent.KEYCODE_DPAD_CENTER:
							case KeyEvent.KEYCODE_ENTER:
								callDialer(view);
								break;
							}
						}
						return false;
					}
				});

				/** Button to send SMS **/
				final Button btnSms = new Button(getApplicationContext());
				btnSms.setText("Send Message to " + numType
						+ Html.fromHtml("<br/>" + num));
				btnSms.setContentDescription("Send Message to " + numType + " "
						+ num.replaceAll(".(?=[0-9])", "$0 "));
				// btnCall.setBackground(getResources().getDrawable(R.drawable.card));
				btnSms.setTextColor(getResources().getColor(
						R.drawable.card_textcolor));
				btnSms.setTypeface(Typeface.DEFAULT_BOLD);
				btnSms.setTextSize(Integer.valueOf(getApplicationContext()
						.getResources().getString(R.string.textSize))
						* getApplicationContext().getResources()
								.getDisplayMetrics().density);
				btnSms.setGravity(Gravity.CENTER);
				btnSms.setFocusable(true);
				params = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				// params.weight = 1.0f;
				btnCall.setLayoutParams(params);
				layout = (LinearLayout) findViewById(R.id.linearLayout);
				layout.addView(btnSms);

				btnSms.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View view, boolean hasFocus) {
						if (hasFocus) {
							giveFeedback(((Button) view).getText().toString());
						}
					}
				});

				btnSms.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						callMessagesApp(view);
					}
				});

				btnSms.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(View view, int keyCode,
							KeyEvent keyEvent) {
						if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
							switch (keyCode) {
							case KeyEvent.KEYCODE_DPAD_CENTER:
							case KeyEvent.KEYCODE_ENTER:
								callMessagesApp(view);
								break;
							}
						}
						return false;
					}
				});

				btnEditNumber
						.setOnFocusChangeListener(new OnFocusChangeListener() {

							@Override
							public void onFocusChange(View view,
									boolean hasFocus) {
								if (hasFocus) {
									giveFeedback(((Button) view).getText()
											.toString());
								}
							}
						});

				btnEditNumber.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						editNumber(
								ContactsDetailsMenu.this.contactDetails.get(
										"id").get(0), num, numType);
					}
				});

				btnEditNumber.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(View view, int keyCode,
							KeyEvent keyEvent) {
						if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
							switch (keyCode) {
							case KeyEvent.KEYCODE_DPAD_CENTER:
							case KeyEvent.KEYCODE_ENTER:
								editNumber(
										ContactsDetailsMenu.this.contactDetails
												.get("id").get(0), num, numType);
								break;
							}
						}
						return false;
					}
				});
			}

			// display emails
			for (int i = 0; i < contactDetails.get("emails").size(); i++) {
				final String email = contactDetails.get("emails").get(i);
				final Button btnEmail = new Button(getApplicationContext());
				final Button btnEditEmail = new Button(getApplicationContext());

				btnEmail.setText("Email " + Html.fromHtml("<br/>" + email));
				btnEmail.setContentDescription("Email " + email);
				btnEmail.setTextColor(getResources().getColor(
						R.drawable.card_textcolor));
				btnEmail.setTypeface(Typeface.DEFAULT_BOLD);
				btnEmail.setTextSize(Integer.valueOf(getApplicationContext()
						.getResources().getString(R.string.textSize))
						* getApplicationContext().getResources()
								.getDisplayMetrics().density);
				btnEmail.setGravity(Gravity.CENTER);
				btnEmail.setFocusable(true);
				btnEditEmail.setText("Edit " + Html.fromHtml("<br/>" + email));
				btnEditEmail.setContentDescription("Edit " + email);
				btnEditEmail.setTextColor(getResources().getColor(
						R.drawable.card_textcolor));
				btnEditEmail.setTypeface(Typeface.DEFAULT_BOLD);
				btnEditEmail.setTextSize(Integer
						.valueOf(getApplicationContext().getResources()
								.getString(R.string.textSize))
						* getApplicationContext().getResources()
								.getDisplayMetrics().density);
				btnEditEmail.setGravity(Gravity.CENTER);
				btnEditEmail.setFocusable(true);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				btnEmail.setLayoutParams(params);
				btnEditEmail.setLayoutParams(params);
				LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
				layout.addView(btnEmail);
				layout.addView(btnEditEmail);

				btnEmail.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View view, boolean hasFocus) {
						if (hasFocus) {
							giveFeedback(((Button) view).getText().toString());
						}
					}
				});

				btnEditEmail.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						editMail(ContactsDetailsMenu.this.contactId, email);
					}
				});
			}

			// more options
			final Button btnMoreOptions = new Button(getApplicationContext());

			btnMoreOptions.setText("More Options");
			btnMoreOptions.setContentDescription("More Options");
			btnMoreOptions.setTextColor(getResources().getColor(
					R.drawable.card_textcolor));
			btnMoreOptions.setTypeface(Typeface.DEFAULT_BOLD);
			btnMoreOptions.setTextSize(Integer.valueOf(getApplicationContext()
					.getResources().getString(R.string.textSize))
					* getApplicationContext().getResources()
							.getDisplayMetrics().density);
			btnMoreOptions.setGravity(Gravity.CENTER);
			btnMoreOptions.setFocusable(true);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			btnMoreOptions.setLayoutParams(params);
			LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
			layout.addView(btnMoreOptions);

			btnContactName
					.setOnFocusChangeListener(new OnFocusChangeListener() {

						@Override
						public void onFocusChange(View view, boolean hasFocus) {
							if (hasFocus) {
								giveFeedback(((Button) view).getText()
										.toString());
							}
						}
					});

			btnEditContactName.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					Intent intent = new Intent(getApplicationContext(),
							ContactUpdate.class);
					intent.putExtra("id",
							ContactsDetailsMenu.this.contactDetails.get("id")
									.get(0));
					intent.putExtra("name",
							ContactsDetailsMenu.this.contactDetails.get("name")
									.get(0));
					intent.putExtra("details",
							ContactsDetailsMenu.this.contactDetails);
					startActivity(intent);
					finish();
				}
			});

			btnEditContactName.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
					if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
							Intent intent = new Intent(getApplicationContext(),
									ContactUpdate.class);
							intent.putExtra("id",
									ContactsDetailsMenu.this.contactDetails
											.get("id").get(0));
							intent.putExtra("name",
									ContactsDetailsMenu.this.contactDetails
											.get("name").get(0));
							intent.putExtra("details",
									ContactsDetailsMenu.this.contactDetails);
							startActivity(intent);
							finish();
							break;
						}
					}
					return false;
				}
			});

			btnEditContactName
					.setOnFocusChangeListener(new OnFocusChangeListener() {

						@Override
						public void onFocusChange(View view, boolean hasFocus) {
							if (hasFocus) {
								giveFeedback(((Button) view).getText()
										.toString());
							}
						}
					});

			btnMoreOptions
					.setOnFocusChangeListener(new OnFocusChangeListener() {

						@Override
						public void onFocusChange(View view, boolean hasFocus) {
							if (hasFocus) {
								giveFeedback(((Button) view).getText()
										.toString());
							}
						}
					});

			btnMoreOptions.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					callOtherOptions();
				}
			});

			btnMoreOptions.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
					if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
							callOtherOptions();
							break;
						}
					}
					return false;
				}
			});
		} catch (Exception e) {
			finish();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {

		super.onStop();
		if (TTS.isSpeaking()) {
			TTS.stop();
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
		// check if keyboard is connected and accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			TTS.speak(getResources().getString(R.string.contactDetails));
		}
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.contactsdetails);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}
