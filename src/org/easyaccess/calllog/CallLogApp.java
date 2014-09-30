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
package org.easyaccess.calllog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.easyaccess.CommonAdapter;
import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

/**
 * The Call Log App activity is used for listing out the outgoing, incoming and
 * missed calls and the total duration of the calls that have been received and
 * dialed.
 */

public class CallLogApp extends EasyAccessActivity {

	/** Declare variables and UI elements **/
	private ListView callLogsListView;
	private Button tabCallsDialled, tabCallsReceived, tabCallsMissed,
			tabCallsAll, tabTime;
	private Runnable runnable;
	private Handler handler;
	private int currentSelection = 0;
	private final int TYPE_DIALED = 1;
	private final int TYPE_RECEIVED = 2;
	private final int TYPE_MISSED = 3;
	private final int TYPE_ALL = 4;
	private final int TYPE_TIME = 5;
	private CommonAdapter callLogsAdapter;
	// private GridView gridView;
	private ArrayList<HashMap<String, String>> records;
	static String[] topBar;

	/**
	 * Launches the CallLogOptions activity and passes the name, number and id
	 * of the contact.
	 * 
	 * @param position
	 *            The position of the item that was selected in the ListView.
	 */
	void startNewActivity(int position) {
		if (CallLogApp.this.records.size() != 0) {
			Intent intent = new Intent(getApplicationContext(),
					CallLogOptions.class);
			intent.putExtra("name",
					((HashMap<String, String>) (CallLogApp.this.records
							.get(position))).get("name").toString());
			intent.putExtra("number",
					((HashMap<String, String>) (CallLogApp.this.records
							.get(position))).get("number").toString());
			intent.putExtra("id",
					((HashMap<String, String>) (CallLogApp.this.records
							.get(position))).get("id").toString());
			startActivity(intent);
			finish();
		}
	}

	/**
	 * Executes a thread to retrieve call logs.
	 * 
	 * @param typeOfLog
	 *            Determines whether incoming or outgoing or all the logs are to
	 *            be displayed.
	 **/
	void runThread(final int typeOfLog) {
		runnable = new Runnable() {
			@Override
			public void run() {
				getCallLogs(typeOfLog);
			}
		};
		new Thread(runnable).start();
	}

	@SuppressLint("SimpleDateFormat")
	/** 
	 * Retrieves the call logs.
	 * @param type Determines whether incoming or outgoing or all the logs are to be displayed.
	 **/
	void getCallLogs(int type) {
		Cursor cursor = null;
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putInt("type", type);
		switch (type) {
		case TYPE_DIALED:
			cursor = getContentResolver().query(
					Uri.parse("content://call_log/calls"), null,
					"type = " + CallLog.Calls.OUTGOING_TYPE, null, "date DESC");
			break;
		case TYPE_RECEIVED:
			cursor = getContentResolver().query(
					Uri.parse("content://call_log/calls"), null,
					"type = " + CallLog.Calls.INCOMING_TYPE, null, "date DESC");
			break;
		case TYPE_MISSED:
			cursor = getContentResolver().query(
					Uri.parse("content://call_log/calls"), null,
					"type = " + CallLog.Calls.MISSED_TYPE, null, "date DESC");
			break;
		case TYPE_ALL:
			cursor = getContentResolver().query(
					Uri.parse("content://call_log/calls"), null, null, null,
					"date DESC");
			break;
		case TYPE_TIME:
			cursor = getContentResolver().query(
					Uri.parse("content://call_log/calls"),
					new String[] { CallLog.Calls.DURATION },
					"type = " + CallLog.Calls.OUTGOING_TYPE, null, null);
			long totalOutgoingTime = 0,
			totalIncomingTime = 0;
			while (cursor.moveToNext()) {
				totalOutgoingTime += cursor.getLong(cursor
						.getColumnIndex(CallLog.Calls.DURATION));
			}
			cursor = getContentResolver().query(
					Uri.parse("content://call_log/calls"),
					new String[] { CallLog.Calls.DURATION },
					"type = " + CallLog.Calls.INCOMING_TYPE, null, null);
			while (cursor.moveToNext()) {
				totalIncomingTime += cursor.getLong(cursor
						.getColumnIndex(CallLog.Calls.DURATION));
			}
			bundle.putLong("outtime", totalOutgoingTime);
			bundle.putLong("intime", totalIncomingTime);
			message.setData(bundle);
			cursor.close();
			cursor = null;
			break;
		}
		int i = 0;
		if (cursor != null && cursor.getCount() == 0) {
			message = new Message();
			message.setData(null);
		} else if (cursor != null) {
			while (cursor.moveToNext()) {
				Bundle values = new Bundle();
				if (cursor.getString(cursor
						.getColumnIndex(CallLog.Calls.CACHED_NAME)) != null)
					values.putString(
							"name",
							cursor.getString(cursor
									.getColumnIndex(CallLog.Calls.CACHED_NAME))
									+ " ");
				else
					values.putString("name", "");
				if (cursor.getString(cursor
						.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL)) != null)
					values.putString(
							"label",
							cursor.getString(cursor
									.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL))
									+ Html.fromHtml("<br/>"));
				else
					values.putString("label", Html.fromHtml("<br/>").toString());
				if (cursor.getString(cursor
						.getColumnIndex(CallLog.Calls.NUMBER)) != null)
					values.putString(
							"number",
							cursor.getString(cursor
									.getColumnIndex(CallLog.Calls.NUMBER))
									+ " ");
				else
					values.putString("number", "");
				if (cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)) != null) {
					Date date = new Date(Long.valueOf(cursor.getString(cursor
							.getColumnIndex(CallLog.Calls.DATE))));
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
							"d MMMM yyyy' 'HH:MM:ss");
					values.putString("date", Html.fromHtml("<br/>")
							+ simpleDateFormat.format(date) + " ");
				} else
					values.putString("date", "");
				if (Integer.toString(cursor.getInt(cursor
						.getColumnIndex(CallLog.Calls._ID))) != null)
					values.putString("id", cursor.getString(cursor
							.getColumnIndex(CallLog.Calls._ID)));
				else
					values.putString("id", "");
				bundle.putBundle(Integer.toString(i), values);
				i++;
			}
			message.setData(bundle);
		}
		handler.sendMessage(message);
	}

	/** Create the Call Log activity **/
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.calllog);
		super.onCreate(savedInstanceState);

		/** Find UI elements **/
		tabCallsDialled = (Button) findViewById(R.id.tabCallsDialled);
		tabCallsReceived = (Button) findViewById(R.id.tabCallsReceived);
		tabCallsMissed = (Button) findViewById(R.id.tabCallsMissed);
		tabCallsAll = (Button) findViewById(R.id.tabCallsAll);
		tabTime = (Button) findViewById(R.id.tabTime);
		callLogsListView = (ListView) findViewById(R.id.lstCallLogs);

		topBar = new String[] {
				getResources().getString(R.string.tabCallsDialled),
				getResources().getString(R.string.tabCallsReceived),
				getResources().getString(R.string.tabCallsMissed),
				getResources().getString(R.string.tabCallsAll),
				getResources().getString(R.string.totalTime) };

		// gridView = (GridView) findViewById(R.id.gridView);
		// gridView.setAdapter(new GridAdapter(getApplicationContext()));

		attachListener(tabCallsDialled);
		attachListener(tabCallsReceived);
		attachListener(tabCallsMissed);
		attachListener(tabCallsAll);
		attachListener(tabTime);

		this.records = new ArrayList<HashMap<String, String>>();

		/**
		 * If Dialed tab is pressed, display dialed calls; clicking on the
		 * contact name would dial the number
		 **/
		tabCallsDialled.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				runThread(TYPE_DIALED);
			}
		});

		/**
		 * If Received tab is pressed, display received calls; clicking on the
		 * contact name would dial the number
		 **/
		tabCallsReceived.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				runThread(TYPE_RECEIVED);
			}
		});

		/**
		 * If Missed tab is pressed, display missed calls; clicking on the
		 * contact name would dial the number
		 **/
		tabCallsMissed.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				runThread(TYPE_MISSED);
			}
		});

		/**
		 * If All tab is pressed, display all calls; clicking on the contact
		 * name would dial the number
		 **/
		tabCallsAll.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				runThread(TYPE_ALL);
			}
		});

		/**
		 * If Total Time tab is pressed, display the total incoming and outgoing
		 * minutes
		 **/
		tabTime.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				runThread(TYPE_TIME);
			}
		});

		/** Handle click events **/
		callLogsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				startNewActivity(position);
			}
		});

		callLogsListView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				ListView lstView = (ListView) view;
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						startNewActivity(currentSelection);
						break;
					case KeyEvent.KEYCODE_DPAD_DOWN:
						currentSelection++;
						if (currentSelection == lstView.getCount()) {
							currentSelection = 0;
						} else {
							giveFeedback(callLogsListView.getItemAtPosition(
									currentSelection).toString());
							lstView.setSelection(currentSelection);
						}
						break;
					case KeyEvent.KEYCODE_DPAD_UP:
						currentSelection--;
						if (currentSelection == -1) {
							currentSelection = callLogsListView.getCount() - 1;
						} else {
							giveFeedback(callLogsListView.getItemAtPosition(
									currentSelection).toString());
							lstView.setSelection(currentSelection);
						}
						break;
					}
				}
				return false;
			}
		});

		handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				ArrayList<String> strRecords = new ArrayList<String>();
				CallLogApp.this.records = new ArrayList<HashMap<String, String>>();
				HashMap<String, String> record;
				Bundle bundle = message.getData();
				if (bundle.size() <= 1) {
					// empty log
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak(getResources().getString(R.string.emptylog));
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.emptylog),
							Toast.LENGTH_SHORT).show();
				} else {
					if (bundle.getInt("type") == TYPE_TIME) {
						strRecords.add("Total duration of incoming calls: "
								+ bundle.getLong("intime") + " seconds");
						strRecords.add("Total duration of outgoing calls: "
								+ bundle.getLong("outtime") + " seconds");
						CallLogApp.this.records.clear();
					} else {
						for (int i = 0; i < bundle.size() - 1; i++) { // - 1 for
																		// excluding
																		// type
							Bundle tempBundle = bundle.getBundle(Integer
									.toString(i));
							// name ,number, date, label, id
							strRecords.add(tempBundle.getString("name")
									+ tempBundle.getString("label")
									+ tempBundle.getString("number")
									+ tempBundle.getString("date"));
							record = new HashMap<String, String>();
							record.put("name", tempBundle.getString("name"));
							record.put("number", tempBundle.getString("number"));
							record.put("id", tempBundle.getString("id"));
							CallLogApp.this.records.add(record);
						}
					}
				}

				if (bundle.getInt("type") == TYPE_TIME) {
					callLogsAdapter = new CommonAdapter(
							getApplicationContext(), strRecords, 0);
				} else {
					callLogsAdapter = new CommonAdapter(
							getApplicationContext(), strRecords, 1);
				}
				callLogsListView.setAdapter(callLogsAdapter);
				callLogsListView.setVisibility(View.VISIBLE);
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
			TTS.speak(getResources().getString(R.string.callLog));
		}
		runThread(TYPE_DIALED);
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.calllog);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}