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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;
import org.easyaccess.phonedialer.ContactManager;
import org.easyaccess.phonedialer.PhoneDialerApp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that loads the text messages from a number or a contact and provides
 * options such as delete message, delete all messages, call and reply.
 **/

public class TextMessagesViewerApp extends EasyAccessActivity {

	private Button btnReply, btnCall, btnDeleteThread;
	private final static int INBOX = 1;
	private final static int SENT = 2;
	private Cursor cursor;
	private Runnable runnable;
	private Handler handler;
	private HashMap<String, ArrayList<String>> records;
	private ArrayList<String> dateArrayList;
	private String address;
	private ProgressBar progressBar;

	/**
	 * Launches the PhoneDialerApp activity and passes the number to be called.
	 */
	void call() {
		// pass number to dialer app
		Intent intent = new Intent(getApplicationContext(),
				PhoneDialerApp.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("call", TextMessagesViewerApp.this.address);
		startActivity(intent);
		finish();
	}

	/**
	 * Launches the TextMessagesComposerApp activity and passes the number of
	 * the recipient, and the name and type of number if the recipient is
	 * storead as a contact on the device.
	 */
	void reply() {
		Intent intent = new Intent(getApplicationContext(),
				TextMessagesComposerApp.class);
		intent.putExtra("number", TextMessagesViewerApp.this.address);
		HashMap<String, String> map = new ContactManager(
				getApplicationContext())
				.getNameFromNumber(TextMessagesViewerApp.this.address);
		if (map.get("name") != null) {
			intent.putExtra("name", map.get("name"));
			intent.putExtra("type", map.get("type"));
		}
		startActivity(intent);
	}

	/**
	 * Attaches onKeyListener to the button passed as a parameter to the method.
	 * If enter key on the keyboard or center key on the keypad is pressed, the
	 * value of the parameter passed is checked.
	 * 
	 * @param button
	 *            The button with which the onKeyListener is to be associated.
	 * @param buttonFlag
	 *            If the value of buttonFlag is 1, the method used to reply to
	 *            the sender is called. If the value of buttonFlag is 2, the
	 *            method used to call the sender is called. If the value of
	 *            buttonFlag is 3, the user is asked if he would like to delete
	 *            all them messages from the sender.
	 */
	void attachKeyListener(Button button, final int buttonFlag) {
		button.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						switch (buttonFlag) {
						case 1:
							reply();
							break;
						case 2:
							call();
							break;
						case 3:
							confirmDelete(
									"Are your sure you want to delete all the messages from "
											+ "this number?", 1, null);
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
	 * Displays an alert dialog to confirm that the message/s is/are to be
	 * deleted.
	 * 
	 * @param message
	 *            The message to be displayed to the user in the alert dialog.
	 * @param deleteAll
	 *            Indicates whether all the messages from the sender should be
	 *            deleted.
	 * @param dateTimestamp
	 *            The time at which the message was sent.
	 */
	private void confirmDelete(String message, final int deleteAll,
			final String dateTimestamp) {
		if (TTS.isSpeaking())
			TTS.stop();
		// giveFeedback(message);
		AlertDialog confirmBox = new AlertDialog.Builder(this)
				// set message, title, and icon
				.setTitle(getResources().getString(R.string.btnTextMsgsDelete))
				.setMessage(message)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {
								boolean success;
								if (deleteAll == 1) {
									success = deleteThread();
									if (success) {
										// check if keyboard is connected but
										// accessibility services are disabled
										if (!Utils
												.isAccessibilityEnabled(getApplicationContext())
												&& getResources()
														.getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
											TTS.speak(getResources()
													.getString(
															R.string.deleteThreadSuccess));
										Toast.makeText(
												getApplicationContext(),
												getResources()
														.getString(
																R.string.deleteThreadSuccess),
												Toast.LENGTH_SHORT).show();
										// reload activity
										finish();
										Intent intent = new Intent(
												getApplicationContext(),
												TextMessagesApp.class);
										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
												| Intent.FLAG_ACTIVITY_NEW_TASK);
										startActivity(intent);
									} else {
										// check if keyboard is connected but
										// accessibility services are disabled
										if (!Utils
												.isAccessibilityEnabled(getApplicationContext())
												&& getResources()
														.getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
											TTS.speak(getResources()
													.getString(
															R.string.deleteThreadFailure));
										Toast.makeText(
												getApplicationContext(),
												getResources()
														.getString(
																R.string.deleteThreadFailure),
												Toast.LENGTH_SHORT).show();
									}
								} else {
									success = deleteMessage(dateTimestamp);
									if (success) {
										// check if keyboard is connected but
										// accessibility services are disabled
										if (!Utils
												.isAccessibilityEnabled(getApplicationContext())
												&& getResources()
														.getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
											TTS.speak(getResources()
													.getString(
															R.string.deleteMessageSuccess));
										Toast.makeText(
												getApplicationContext(),
												getResources()
														.getString(
																R.string.deleteMessageSuccess),
												Toast.LENGTH_SHORT).show();
										// reload activity
										finish();
										Intent intent = new Intent(
												getApplicationContext(),
												TextMessagesViewerApp.class);
										intent.putExtra(
												"address",
												TextMessagesViewerApp.this.address);
										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
												| Intent.FLAG_ACTIVITY_NEW_TASK);
										startActivity(intent);
									} else {
										// check if keyboard is connected but
										// accessibility services are disabled
										if (!Utils
												.isAccessibilityEnabled(getApplicationContext())
												&& getResources()
														.getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
											TTS.speak(getResources()
													.getString(
															R.string.deleteMessageFailure));
										Toast.makeText(
												getApplicationContext(),
												getResources()
														.getString(
																R.string.deleteMessageFailure),
												Toast.LENGTH_SHORT).show();
									}
								}
								dialog.dismiss();
							}
						})

				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();

					}
				}).create();
		confirmBox.show();
	}

	/**
	 * Deletes all the messages from the sender.
	 **/
	boolean deleteThread() {
		Uri deleteUri = Uri.parse("content://sms");
		if (getContentResolver().delete(deleteUri, "address=?",
				new String[] { this.address }) != 0) {
			this.dateArrayList.clear();
			return true;
		}
		return false;
	}

	/**
	 * Deletes the message that based on the timestamp at which the message was
	 * sent by the sender.
	 * 
	 * @param dateTimestamp
	 *            The timestamp at which the message was sent.
	 **/
	boolean deleteMessage(String dateTimestamp) {
		Uri deleteUri = Uri.parse("content://sms");

		if (getContentResolver().delete(deleteUri, "address=? AND date = ?",
				new String[] { this.address, dateTimestamp }) != 0)
			return true;
		return false;
	}

	/**
	 * Marks the message as read.
	 * 
	 * @param dateTimestamp
	 *            The timestamp that identifies the message from the sender in
	 *            the inbox.
	 */
	private void markMessageRead(String dateTimestamp) {
		ContentValues values = new ContentValues();
		values.put("read", true);
		getContentResolver().update(Uri.parse("content://sms/inbox"), values,
				"address=?" + " AND date = ?",
				new String[] { this.address, dateTimestamp });
	}

	/**
	 * Attaches onFocusListener to the TextView passed as a parameter. The text
	 * specified in the TextView is sent to giveFeedback method.
	 * 
	 * @param textView
	 *            The TextView with which onFocusChange listener is to be
	 *            associated.
	 **/
	void attachListenerToTextView(TextView textView) {
		final String text = textView.getText().toString();
		textView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					giveFeedback(text);
				}
			}
		});
	}

	/**
	 * Executes a thread to retrieve the messages associated with the number
	 * passed as a parameter.
	 * 
	 * @param address
	 *            The number of the sender/recipient.
	 **/
	void runThread(final String address) {
		runnable = new Runnable() {
			@Override
			public void run() {
				getMessages(address);
			}
		};
		new Thread(runnable).start();
	}

	/**
	 * Retrieves the messages associated with the number passed as a parameter.
	 * 
	 * @param address
	 *            The number of the sender/recipient.
	 **/
	@SuppressLint("SimpleDateFormat")
	void getMessages(String address) {
		ArrayList<String> values;
		Message message = new Message();
		Bundle bundle = new Bundle();
		String uri = "content://sms";
		this.cursor = getContentResolver().query(Uri.parse(uri),
				new String[] { "subject", "body", "type", "date" },
				"address = ?", new String[] { address }, null);
		if (this.cursor.getCount() != 0) {
			this.cursor.moveToFirst();
		} else {
			Intent intentObject = new Intent(getApplicationContext(),
					TextMessagesApp.class);
			intentObject.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(intentObject);
			finish();
		}
		do {
			values = new ArrayList<String>();
			try {
				String date = this.cursor.getString(this.cursor
						.getColumnIndex("date"));
				values.add(this.cursor.getString(this.cursor
						.getColumnIndex("subject")));
				values.add(this.cursor.getString(this.cursor
						.getColumnIndex("body")));
				if (cursor.getString(cursor.getColumnIndex("type"))
						.equalsIgnoreCase("1")) {
					// sms received
					values.add(Integer.toString(INBOX));
				} else if (cursor.getString(cursor.getColumnIndex("type"))
						.equalsIgnoreCase("2")) {
					values.add(Integer.toString(SENT));
				}
				records.put(date, values);
			} catch (Exception e) {
				continue;
			}
		} while (this.cursor.moveToNext()
				&& this.cursor.getPosition() != this.cursor.getCount());
		if (cursor.getCount() > 0)
			bundle.putInt("status", 1);
		else
			bundle.putInt("status", 0);
		message.setData(bundle);
		handler.sendMessage(message);
	}

	/**
	 * Sorts the HashMap passed as a parameter in reverse order of date and
	 * time, and displays the message along with a button that will provide the
	 * facility to delete the message.
	 * 
	 * @param hashMap
	 *            the HashMap that is to be sorted.
	 **/
	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings({ "unchecked", "deprecation" })
	void sort(@SuppressWarnings("rawtypes") HashMap hashMap) {

		Map<Integer, String> map = new TreeMap<Integer, String>(
				Collections.reverseOrder());
		map.putAll(hashMap);
		@SuppressWarnings("rawtypes")
		Set set = map.entrySet();
		@SuppressWarnings("rawtypes")
		Iterator iterator = set.iterator();
		while (iterator.hasNext()) {
			@SuppressWarnings("rawtypes")
			final Map.Entry me = (Map.Entry) iterator.next();

			this.dateArrayList.add(me.getKey().toString());
			progressBar.setVisibility(View.GONE);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			// create TextViews
			final TextView txtMessage = new TextView(getApplicationContext());
			// create Button
			final Button btnDelete = new Button(getApplicationContext());
			btnDelete.setText(getResources().getString(R.string.btnDelete));
			btnDelete.setContentDescription(getResources().getString(
					R.string.btnDelete));
			// display subject, body, date, type of message(sent or received)
			String text = "";
			if (records.get(me.getKey()).get(0) == null)
				text = "";
			else
				text = records.get(me.getKey()).get(0) + Html.fromHtml("<br/>");
			text += records.get(me.getKey()).get(1);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"d MMMM yyyy' 'HH:MM:ss");
			Date dateTemp = new Date(Long.valueOf(me.getKey().toString()));
			String date = simpleDateFormat.format(dateTemp);
			markMessageRead(me.getKey().toString());
			if (records.get(me.getKey()).get(2) == Integer.toString(INBOX)) {
				text += Html.fromHtml("<br/>") + "Received on " + date;
				txtMessage.setGravity(Gravity.LEFT);
			} else {
				text += Html.fromHtml("<br/>") + "Sent on " + date;
				txtMessage.setGravity(Gravity.RIGHT);
			}
			txtMessage.setWidth(params.width / 3);
			txtMessage.setText(text);
			txtMessage.setContentDescription(text);

			txtMessage.setTextColor(getResources().getColor(
					R.color.card_textcolor_regular));

			txtMessage.setTextSize(Integer.valueOf(getApplicationContext()
					.getResources().getString(R.string.textSize))
					* getApplicationContext().getResources()
							.getDisplayMetrics().density);
			SharedPreferences preferences = getApplicationContext()
					.getSharedPreferences(
							getApplicationContext().getResources().getString(
									R.string.color), 0);
			int bgColor = preferences.getInt("bgcolor", 0);
			int fgColor = preferences.getInt("fgcolor", 0);
			try {
				if (bgColor != 0) {
					getApplicationContext().getResources().getResourceName(
							bgColor);
					bgColor = getApplicationContext().getResources().getColor(
							bgColor);
					txtMessage.setBackgroundColor(bgColor);
					btnDelete.setBackgroundColor(bgColor);
				} else {
					txtMessage.setBackgroundDrawable(null);
					btnDelete.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.card));
				}
			} catch (NotFoundException nfe) {
				txtMessage.setBackgroundDrawable(null);
				btnDelete.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.card));
			}
			try {
				getApplicationContext().getResources().getResourceName(fgColor);
				fgColor = getApplicationContext().getResources().getColor(
						fgColor);
			} catch (NotFoundException nfe) {
				fgColor = getApplicationContext().getResources().getColor(
						R.color.card_textcolor_regular);
			}
			txtMessage.setTextColor(fgColor);
			btnDelete.setTextColor(fgColor);

			preferences = getApplicationContext().getSharedPreferences(
					getApplicationContext().getResources().getString(
							R.string.fonttype), 0);
			if (preferences.getInt("typeface", -1) != -1) {
				switch (preferences.getInt("typeface", -1)) {
				case Utils.NONE:
					txtMessage.setTypeface(null, Typeface.NORMAL);
					btnDelete.setTypeface(null, Typeface.BOLD);
					break;
				case Utils.SERIF:
					txtMessage.setTypeface(Typeface.SERIF);
					btnDelete.setTypeface(Typeface.SERIF);
					break;
				case Utils.MONOSPACE:
					txtMessage.setTypeface(Typeface.MONOSPACE);
					btnDelete.setTypeface(Typeface.MONOSPACE);
					break;
				}
			} else {
				txtMessage.setTypeface(null, Typeface.NORMAL);
				btnDelete.setTypeface(null, Typeface.BOLD);
			}

			preferences = getApplicationContext().getSharedPreferences(
					getApplicationContext().getResources().getString(
							R.string.size), 0);
			if (preferences.getFloat("size", 0) != 0) {
				float fontSize = preferences.getFloat("size", 0);
				txtMessage.setTextSize(fontSize);
				btnDelete.setTextSize(fontSize);
			} else {
				txtMessage.setTextSize(Integer.valueOf(getApplicationContext()
						.getResources().getString(R.string.textSize))
						* getApplicationContext().getResources()
								.getDisplayMetrics().density);
				btnDelete.setTextSize(Integer.valueOf(getApplicationContext()
						.getResources().getString(R.string.textSize))
						* getApplicationContext().getResources()
								.getDisplayMetrics().density);
			}

			params.setMargins(0, 50, 0, 20);
			txtMessage.setFocusable(true);
			btnDelete.setFocusable(true);
			txtMessage.setLayoutParams(params);
			btnDelete.setPadding(0, 10, 0, 20);
			txtMessage.setLayoutParams(params);

			attachListenerToTextView(txtMessage);
			attachListener(btnDelete);

			// to delete the message
			btnDelete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					confirmDelete(
							"Are you sure you want to delete this message?", 0,
							me.getKey().toString());
				}
			});

			LinearLayout layout = (LinearLayout) findViewById(R.id.textLinearLayout);
			layout.addView(txtMessage);
			layout.addView(btnDelete);
		}
	}

	/** Create the Text Messages Viewer activity **/
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.textmessages_viewer);
		super.onCreate(savedInstanceState);

		/** Find UI elements **/
		btnReply = (Button) findViewById(R.id.btnTextMsgsReply);
		btnCall = (Button) findViewById(R.id.btnTextMsgsCall);
		btnDeleteThread = (Button) findViewById(R.id.btnTextMsgsDeleteThread);
		// get all contacts and pass to the list adapter
		progressBar = (ProgressBar) findViewById(R.id.progressBarMessages);
		progressBar.setVisibility(View.VISIBLE);

		/** attach onFocus and onTouch listeners **/
		attachListener(btnReply);
		attachListener(btnCall);
		attachListener(btnDeleteThread);

		this.address = getIntent().getExtras().getString("address");
		this.records = new HashMap<String, ArrayList<String>>();
		this.dateArrayList = new ArrayList<String>();

		attachKeyListener(btnReply, 1);

		/**
		 * If Reply button is pressed, open the Text Messages Composer to write
		 * a reply to this text message
		 **/
		btnReply.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				reply();
			}
		});

		attachKeyListener(btnCall, 2);

		/** If Call button is pressed, call the sender of the text message **/
		btnCall.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				call();
			}
		});

		attachKeyListener(btnDeleteThread, 3);

		/**
		 * If Delete button is pressed, bring up Yes/No dialog to confirm to
		 * delete this message
		 **/
		btnDeleteThread.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				confirmDelete(
						"Are your sure you want to delete all the messages from this number?",
						1, null);
			}
		});

		handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				Bundle bundle = message.getData();
				if (bundle.getInt("status") == 1) {
					// sort records on date
					sort(records);
				} else {
					// no messages from this number
				}
			}
		};
	}

	@Override
	public void onResume() {
		super.onResume();
		if (this.address != null) {
			// check if keyboard is connected but accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getResources().getString(R.string.conversation)
						+ ", Loading messages");
			Toast.makeText(getApplicationContext(), "Loading messages",
					Toast.LENGTH_SHORT).show();
			runThread(this.address);
		}

		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.textmessagesviewer);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}