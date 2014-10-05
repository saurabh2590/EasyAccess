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

import org.easyaccess.CommonAdapter;
import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The Call Log History activity is used for listing the outgoing, incoming and
 * missed calls from a particular number.
 */

public class CallLogHistory extends EasyAccessActivity {

	/** Declare variables and UI elements **/

	private TextView txtContactName;
	private ListView callLogHistoryListView;
	private CommonAdapter callLogHistoryAdapter;
	private Runnable runnable;
	@SuppressWarnings("unused")
	private String number, id;
	private Handler handler;

	/**
	 * Runs a thread to retrieve the call history associated with a number.
	 * 
	 * @param number
	 *            The number whose call history is to be fetched.
	 **/
	void runThread(final String number) {
		runnable = new Runnable() {
			@Override
			public void run() {
				getCallHistory(number);
			}
		};
		new Thread(runnable).start();
	}

	@SuppressLint("SimpleDateFormat")
	/** 
	 * Retrieves the call history associated with a number.
	 * @number The number whose call history is to be fetched.
	 **/
	void getCallHistory(String number) {
		Cursor cursor = getContentResolver().query(
				Uri.parse("content://call_log/calls"), null,
				CallLog.Calls.NUMBER + " = ?", new String[] { number },
				"date DESC");
		int i = 0;
		Message message = new Message();
		Bundle bundle = new Bundle();
		while (cursor.moveToNext()) {
			Bundle values = new Bundle();
			if (cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)) != null) {
				Date date = new Date(Long.valueOf(cursor.getString(cursor
						.getColumnIndex(CallLog.Calls.DATE))));
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
						"d MMMM yyyy' 'HH:MM:ss");
				values.putString("date", Html.fromHtml("<br/>")
						+ simpleDateFormat.format(date) + " ");
			} else
				values.putString("date", "");
			if (cursor.getString(cursor
					.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL)) != null) {
				String type = cursor.getString(cursor
						.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
				values.putString("type", type);
			} else
				values.putString("type", "");
			values.putInt("status",
					cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)));
			bundle.putBundle(Integer.toString(i), values);
			i++;
		}
		message.setData(bundle);
		handler.sendMessage(message);
	}

	/** Create the Call Log activity **/
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.callloghistory);
		super.onCreate(savedInstanceState);

		// Find UI elements
		txtContactName = (TextView) findViewById(R.id.txtContactName);
		callLogHistoryListView = (ListView) findViewById(R.id.lstCallLogHistory);

		// Handle click events
		// retrieve the passed number
		this.number = getIntent().getExtras().getString("number");
		this.id = getIntent().getExtras().getString("id");
		if (getIntent().getExtras().getString("name") != null) {
			txtContactName.setText(getIntent().getExtras().getString("name"));
			txtContactName.setContentDescription(getIntent().getExtras()
					.getString("name").replaceAll(".(?=[0-9])", "$0 "));
		} else {
			txtContactName.setText(getIntent().getExtras().getString("number"));
			txtContactName.setContentDescription(getIntent().getExtras()
					.getString("number").replaceAll(".(?=[0-9])", "$0 "));
		}

		runThread(this.number);

		handler = new Handler() {
			@Override
			public void handleMessage(Message message) {

				ArrayList<String> strRecords = new ArrayList<String>();
				Bundle bundle = message.getData();
				if (bundle.size() == 0) {
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
					for (int i = 0; i < bundle.size(); i++) {
						Bundle tempBundle = bundle.getBundle(Integer
								.toString(i));
						String status = "";
						switch (tempBundle.getInt("status")) {
						case CallLog.Calls.INCOMING_TYPE:
							status = getString(R.string.received);
							break;
						case CallLog.Calls.MISSED_TYPE:
							status = getString(R.string.missed);
							break;
						case CallLog.Calls.OUTGOING_TYPE:
							status = getString(R.string.dialed);
							break;
						}
						strRecords.add(tempBundle.getString("date")
								+ tempBundle.getString("type") + " " + status);
					}
				}
				callLogHistoryAdapter = new CommonAdapter(
						getApplicationContext(), strRecords, 2);
				callLogHistoryListView.setAdapter(callLogHistoryAdapter);
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
			TTS.speak(getResources().getString(R.string.callHistory));
		}
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.callloghistory);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}