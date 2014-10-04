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
import java.util.Date;
import java.util.HashMap;

import org.easyaccess.CommonAdapter;
import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;
import org.easyaccess.phonedialer.ContactManager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.text.Html;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class TextMessagesApp extends EasyAccessActivity implements
		OnClickListener {

	/**
	 * The Text Messages app is used to view and compose text messages.
	 */

	/** Declare variables and UI elements **/
	private Button btnInbox, btnSent, btnCompose;
	private ListView messageListView;
	private Runnable runnable;
	private Cursor cursor;
	private Handler handler;
	private CommonAdapter adapter;
	private ArrayList<HashMap<String, String>> records;
	private ArrayList<String> numbers;
	private int currentSelection = -1;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	private final static int INBOX = 1;
	private final static int SENT = 2;
	private int typeOfMessage, i, oldPosition;

	/**
	 * Sets the type of message to inbox and calls the thread to fetch text
	 * messages from the inbox.
	 */
	void loadInboxMessages() {
		TextMessagesApp.this.typeOfMessage = INBOX;
		if (TextMessagesApp.this.cursor != null) {
			TextMessagesApp.this.cursor.close();
			TextMessagesApp.this.cursor = null;
		}
		runThread(TextMessagesApp.this.typeOfMessage);
	}

	/**
	 * Sets the type of message to sent and calls the thread to fetch the sent
	 * text messages.
	 */
	void loadSentMessages() {
		TextMessagesApp.this.typeOfMessage = SENT;
		if (TextMessagesApp.this.cursor != null) {
			TextMessagesApp.this.cursor.close();
			TextMessagesApp.this.cursor = null;
		}
		runThread(TextMessagesApp.this.typeOfMessage);
	}

	/**
	 * Launches the TextMessagesCompsoerRecipientApp in order to choose the
	 * contact or type the number to which the text message is to be sent.
	 */
	void compose() {
		Intent intent = new Intent(getApplicationContext(),
				TextMessagesComposerRecipientApp.class);
		startActivity(intent);
	}

	/**
	 * Launches the TextMessagesViewerApp to load all the messages sent to or
	 * received from the number passed as parameter.
	 * 
	 * @param number
	 *            The number whose messages are to be displayed.
	 */
	void startNewActivity(String number) {
		Intent intent = new Intent(getApplicationContext(),
				TextMessagesViewerApp.class);
		intent.putExtra("address", number);
		startActivity(intent);
	}

	/**
	 * Attaches onKeyListener to the Button passed as a parameter to the method.
	 * The method corresponding to the button on which the enter key of the
	 * keyboard or the center key of the keypad is pressed.
	 * 
	 * @param button
	 *            The button with which the onKeyListener is to be associated.
	 * @param buttonFlag
	 *            If the value of buttonFlag is 1, the messages in the inbox are
	 *            loaded. If the value of buttonFlag is 2, the messages that are
	 *            sent are loaded. If the value of buttonFlag is 3, the activity
	 *            required to compose a text message is launched.
	 */
	void attachKeyListener(final Button button, final int buttonFlag) {
		button.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						switch (buttonFlag) {
						case 1:
							loadInboxMessages();
							break;
						case 2:
							loadSentMessages();
							break;
						case 3:
							compose();
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
	 * Executes a thread to retrieve the text messages corresponding to the type
	 * 
	 * @param typeOfMessage
	 *            The integer that denotes whether the messages in the inbox or
	 *            the sent items are to loaded. It can hold two values: INBOX,
	 *            which corresponds to the value 1, or SENT, which corresponds
	 *            to the value 2.
	 **/
	void runThread(final int typeOfMessage) {
		runnable = new Runnable() {
			@Override
			public void run() {
				getMessages(typeOfMessage);
			}
		};
		new Thread(runnable).start();
	}

	/**
	 * Retrieves the messages using the corresponding uri based on the type of
	 * message that is passed as a parameter.
	 * 
	 * @param typeOfMessage
	 *            The integer that denotes whether the messages in the inbox or
	 *            the sent items are to loaded. It can hold two values: INBOX,
	 *            which corresponds to the value 1, or SENT, which corresponds
	 *            to the value 2.
	 **/
	@SuppressLint("SimpleDateFormat")
	void getMessages(int typeOfMessage) {
		Bundle senderDetails;
		Message message = new Message();
		Bundle bundle = new Bundle();
		String uri = "";
		TextMessagesApp.this.numbers = new ArrayList<String>();
		switch (typeOfMessage) {
		case INBOX:
			uri = "content://sms/inbox";
			break;
		case SENT:
			uri = "content://sms/sent";
			break;
		}
		if (this.cursor == null) {
			this.cursor = getContentResolver().query(
					Uri.parse(uri),
					new String[] { "DISTINCT address", "date", "read",
							"subject", "body" },
					"address IS NOT NULL) GROUP BY (address", null, null);
			if (this.cursor.getCount() > 0) {
				this.cursor.moveToFirst();
				TextMessagesApp.this.records = new ArrayList<HashMap<String, String>>();
			}
		} else {
			if (this.cursor.getPosition() != 0)
				this.cursor.moveToNext();
		}
		if (this.cursor.getCount() > 0
				&& this.cursor.getPosition() < this.cursor.getCount()) {
			i = 0;
			do {
				senderDetails = new Bundle();
				senderDetails.putString("address", this.cursor
						.getString(this.cursor.getColumnIndex("address")));

				TextMessagesApp.this.numbers.add(senderDetails
						.getString("address"));

				Date date = new Date(Long.valueOf(this.cursor
						.getString(this.cursor.getColumnIndex("date"))));
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
						"d MMMM yyyy' 'HH:MM:ss");
				senderDetails.putString("date", Html.fromHtml("<br/>")
						+ simpleDateFormat.format(date) + " ");
				senderDetails.putString("read",
						cursor.getString(this.cursor.getColumnIndex("read")));
				senderDetails.putString("subject", this.cursor
						.getString(this.cursor.getColumnIndex("subject")));
				senderDetails.putString("body", this.cursor
						.getString(this.cursor.getColumnIndex("body")));
				bundle.putBundle(Integer.toString(i), senderDetails);
				i++;
			} while (i < 5 && this.cursor.moveToNext()
					&& this.cursor.getPosition() < this.cursor.getCount());
			message.setData(bundle);
		}
		handler.sendMessage(message);
	}

	/** Create the Text Messages activity **/
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.textmessages);
		super.onCreate(savedInstanceState);

		/** Find UI elements **/
		btnInbox = (Button) findViewById(R.id.tabTextMsgsInbox);
		btnSent = (Button) findViewById(R.id.tabTextMsgsSent);
		btnCompose = (Button) findViewById(R.id.tabTextMsgsWrite);

		messageListView = (ListView) findViewById(R.id.lstMessages);

		attachListener(btnInbox);
		attachListener(btnSent);
		attachListener(btnCompose);

		// check if TextMEssagesApp is the default SMS app for the device
		checkIfDefault();

		this.records = new ArrayList<HashMap<String, String>>();
		this.numbers = new ArrayList<String>();
		this.cursor = null;
		this.typeOfMessage = INBOX;
		this.oldPosition = 0;

		// load the list
		runThread(this.typeOfMessage);
		// Gesture detection
		gestureDetector = new GestureDetector(this, new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};

		messageListView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				ListView lstView = (ListView) view;
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
						startNewActivity(TextMessagesApp.this.numbers
								.get(currentSelection));
						break;
					case KeyEvent.KEYCODE_DPAD_DOWN:
						currentSelection++;
						if (currentSelection == lstView.getCount()) {
							currentSelection = 0;
						}
						giveFeedback(messageListView.getItemAtPosition(
								currentSelection).toString());
						messageListView.setSelection(currentSelection);
						break;
					case KeyEvent.KEYCODE_DPAD_UP:
						currentSelection--;
						if (currentSelection == -1) {
							currentSelection = lstView.getCount() - 1;
						} else {
							giveFeedback(messageListView.getItemAtPosition(
									currentSelection).toString());
							messageListView.setSelection(currentSelection);
						}
						break;
					}
				}
				return false;
			}
		});

		messageListView.setOnTouchListener(gestureListener);
		messageListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				startNewActivity(TextMessagesApp.this.numbers.get(position));
			}
		});

		attachKeyListener(btnInbox, 1);
		// If Inbox tab is pressed, bring up the text messages inbox
		btnInbox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				loadInboxMessages();
			}
		});

		attachKeyListener(btnSent, 2);
		// If Sent tab is pressed, bring up the sent text messages
		btnSent.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				loadSentMessages();
			}
		});

		attachKeyListener(btnCompose, 3);
		// If Write tab is pressed, launch the easyaccess Text Messages Composer
		// app
		btnCompose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				compose();
			}
		});

		handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				ArrayList<String> strRecords = new ArrayList<String>();
				HashMap<String, String> record;
				Bundle bundle = message.getData();
				if (bundle.size() < 1) {
					// empty messages
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak(getString(R.string.emptymessages));
					Toast.makeText(getApplicationContext(),
							getString(R.string.emptymessages),
							Toast.LENGTH_SHORT).show();
				} else {
					for (int i = 0; i < bundle.size(); i++) {
						Bundle tempBundle = bundle.getBundle(Integer
								.toString(i));
						record = new HashMap<String, String>();
						String read = getString(R.string.unread), subject = " ";
						// address, date, read, subject, body
						if (TextMessagesApp.this.typeOfMessage != SENT) {
							// sent messages are always read
							if (tempBundle.containsKey("read")
									&& tempBundle.getString("read").equals("1")) {
								record.put("read", "1");
								read = getString(R.string.read);
							} else {
								record.put("read", "0");
							}
						} else {
							read = "";
						}
						if (tempBundle.getString("subject") != null) {
							subject = tempBundle.getString("subject");
							record.put("subject", subject);
						} else {
							record.put("subject", "");
						}
						HashMap<String, String> map = (new ContactManager(
								getApplicationContext())
								.getNameFromNumber(tempBundle
										.getString("address")));
						if (map.size() == 1) {
							// number does not correspond to a name
							strRecords.add(tempBundle.getString("address")
									+ " " + tempBundle.getString("date")
									+ subject + read);
						} else {
							record.put("name", tempBundle.getString("name"));
							strRecords.add(map.get("name") + " "
									+ map.get("type") + " "
									+ tempBundle.getString("date") + subject
									+ read);
						}
						record.put("number", tempBundle.getString("address"));
						record.put("date", tempBundle.getString("date"));
						record.put("read", tempBundle.getString("read"));
						record.put("subject", tempBundle.getString("subject"));
						record.put("body", tempBundle.getString("body"));
						TextMessagesApp.this.records.add(record);
					}
				}
				adapter = new CommonAdapter(getApplicationContext(),
						strRecords, 3);
				messageListView.setAdapter(adapter);
				messageListView.setVisibility(View.VISIBLE);
			}
		};
	}

	@Override
	public void onResume() {
		super.onResume();
		// check if keyboard is connected and accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			TTS.speak(getString(R.string.textMessages));
		}
		if (this.cursor != null) {
			this.cursor.moveToFirst();
			// refresh the list
			runThread(INBOX);
		}
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.textmessages);
		// get the values in SharedPreferences
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}

	@TargetApi(19)
	public void checkIfDefault() {
		final String myPackageName = getPackageName();
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
				// App is not default.
				// Show the "not currently set as the default SMS app" dialog
				Intent intent = new Intent(Sms.Intents.ACTION_CHANGE_DEFAULT);
				intent.putExtra(Sms.Intents.EXTRA_PACKAGE_NAME,
						getApplicationContext().getPackageName());
				startActivity(intent);
			}
		}
	}

	/** Class to detect gestures */
	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// get next 5 messages
					if (TextMessagesApp.this.cursor.getPosition() == TextMessagesApp.this.cursor
							.getCount()) {
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							giveFeedback(getString(R.string.reachedEnd));
						Toast.makeText(getApplicationContext(),
								getString(R.string.reachedEnd),
								Toast.LENGTH_SHORT).show();
					} else {
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							giveFeedback(getString(R.string.next));
						Toast.makeText(getApplicationContext(),
								getString(R.string.next), Toast.LENGTH_SHORT)
								.show();
						runThread(TextMessagesApp.this.typeOfMessage);
					}
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// get previous 5 messages
					int positionDifference = ((5 + i) == 10) ? 10 : (5 + i + 1);
					TextMessagesApp.this.cursor
							.moveToPosition(TextMessagesApp.this.cursor
									.getPosition() - positionDifference);
					if ((TextMessagesApp.this.cursor.getPosition() <= -1)
							&& oldPosition <= -1) {
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							giveFeedback(getString(R.string.reachedBeginning));
						Toast.makeText(getApplicationContext(),
								getString(R.string.reachedBeginning),
								Toast.LENGTH_SHORT).show();
					} else {
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							giveFeedback(getString(R.string.previous));
						Toast.makeText(getApplicationContext(),
								getString(R.string.previous),
								Toast.LENGTH_SHORT).show();
						runThread(TextMessagesApp.this.typeOfMessage);
					}
					oldPosition = TextMessagesApp.this.cursor.getPosition();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

	}

	@Override
	public void onClick(View view) {
	}
}
