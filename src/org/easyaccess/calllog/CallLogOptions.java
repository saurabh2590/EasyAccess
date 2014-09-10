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

package org.easyaccess.calllog;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;
import org.easyaccess.contacts.ContactsDetailsMenu;
import org.easyaccess.contacts.SaveContact;
import org.easyaccess.phonedialer.ContactManager;
import org.easyaccess.phonedialer.PhoneDialerApp;
import org.easyaccess.textmessages.TextMessagesComposerApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Lists the options associated with a call log such as call, send sms, view call history, 
 * delete call log, view contact, save contact. **/

public class CallLogOptions extends EasyAccessActivity {
	private Button btnCall, btnSendSMS, btnViewCallHistory, btnDeleteFromLog, btnContact;
	private TextView txtContactName;
	private String name, number, id;
	
	/** 
	* Attaches onLongClick listener to the button passed as parameter.
	* @param button The button with which the onLongClick listener is to be associated.
	**/
	void attachOnClickListener(Button button) {		
		button.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View view) {
				startNewActivity(view);
			}
		});
		
		button.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch(keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						startNewActivity(view);
						break;
					}
				}
				return false;
			}
		});
	}
	
	/**
	* Launches a new activity based on the parameter passed.
	* @param view The view that was clicked. Based on the ID of the view, the corresponding 
	* activity is launched. If the ID corresponds to the call button, the PhoneDialerApp activity 
	* is launched. If the ID corresponds to the send SMS button, the TextMessageComposerApp activity 
	* is launched. If the ID corresponds to the view call history button, the CallLogHistory activity 
	* is launched. If the ID corresponds to the delete from log button, that particular log is 
	* deleted. If the ID corresponds to the contact button, the ContactDetailsMenu activity is launched 
	* if the contact exists in the phone, otherwise the SaveContact activity is launched.
	*/
	void startNewActivity(View view) {
		Intent intent;
		switch(view.getId()) {
		case R.id.btnCall:
			//pass number to dialer app
			intent = new Intent(getApplicationContext(), PhoneDialerApp.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("call", CallLogOptions.this.number);
			startActivity(intent);
			finish();
			break;
		case R.id.btnSendSMS:
			intent = new Intent(getApplicationContext(), TextMessagesComposerApp.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("number", CallLogOptions.this.number);
			String type = (((new ContactManager(getApplicationContext())).
					getNameFromNumber(CallLogOptions.this.number)).get("type"));
			if(type != null) {
				intent.putExtra("name", (((new ContactManager(getApplicationContext())).
						getNameFromNumber(CallLogOptions.this.number)).get("name")));
				intent.putExtra("type", type);
			}
			startActivity(intent);
			break;
		case R.id.btnViewCallHistory:
			intent = new Intent(getApplicationContext(), CallLogHistory.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("number", CallLogOptions.this.number);
			intent.putExtra("name", (((new ContactManager(getApplicationContext())).
					getNameFromNumber(CallLogOptions.this.number)).get("name")));
			intent.putExtra("id", CallLogOptions.this.id);
			startActivity(intent);
			break;
		case R.id.btnDeleteFromLog:
			if(getContentResolver().delete(Uri.parse("content://call_log/calls"), 
					CallLog.Calls._ID +"=?", new String[]{CallLogOptions.this.id}) != 0) {
				if(TTS.isSpeaking())
					TTS.stop();
				//check if keyboard is connected but accessibility services are disabled
	        	if(!Utils.isAccessibilityEnabled(getApplicationContext()) && 
	        			getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(getResources().getString(R.string.logdeletesuccess));
				Toast.makeText(getApplicationContext(), 
						getResources().getString(R.string.logdeletesuccess), Toast.LENGTH_SHORT).show();
				finish();
			}
			else {
				//check if keyboard is connected but accessibility services are disabled
	        	if(!Utils.isAccessibilityEnabled(getApplicationContext()) && 
	        			getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(getResources().getString(R.string.logdeletefailure));
				Toast.makeText(getApplicationContext(), 
						getResources().getString(R.string.logdeletefailure), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btnContact:
			if(!CallLogOptions.this.name.equals("")) {				
				//view contact
				intent = new Intent(getApplicationContext(), ContactsDetailsMenu.class);
				intent.putExtra("number", CallLogOptions.this.number);
				intent.putExtra("name", CallLogOptions.this.name);
				intent.putExtra("id", new ContactManager(getApplicationContext()).
						getId(CallLogOptions.this.number));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			else {
				//save to contacts
				intent = new Intent(getApplicationContext(), SaveContact.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);						
				intent.putExtra("number", CallLogOptions.this.number);
				startActivity(intent);
			}
			finish();
			break;
		}
	}
	
	/** Create the Call Log activity **/
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.calllogoptions);
		super.onCreate(savedInstanceState);
		
		/** Find UI elements **/
		btnCall = (Button) findViewById(R.id.btnCall);
		btnSendSMS = (Button) findViewById(R.id.btnSendSMS);
		btnViewCallHistory = (Button) findViewById(R.id.btnViewCallHistory);
		btnDeleteFromLog = (Button) findViewById(R.id.btnDeleteFromLog);
		btnContact = (Button) findViewById(R.id.btnContact);
		txtContactName = (TextView) findViewById(R.id.txtContactsName);
		
		txtContactName.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View view, boolean arg1) {
				giveFeedback(((TextView)view).getText().toString());
			}
		});
		
		attachListener(btnCall);
		attachListener(btnSendSMS);
		attachListener(btnViewCallHistory);
		attachListener(btnDeleteFromLog);
		
		/** Retrieve the parameters passed to the activity **/
		this.name = getIntent().getExtras().getString("name");
		this.number = getIntent().getExtras().getString("number");
		this.number = this.number.trim();
		this.id = getIntent().getExtras().getString("id");
		
		if(this.name.toString().equals("")) {
			txtContactName.setText(this.number);
			txtContactName.setContentDescription(this.number.replaceAll(".(?=[0-9])", "$0 "));
		}
		else {
			txtContactName.setText(this.name.toString());
			txtContactName.setContentDescription(this.name.toString().replaceAll(".(?=[0-9])", "$0 "));
		}
		//Set the text of the last button depending on whether the number is saved in the phone
		if(this.name.trim().equals("")) {
			btnContact.setText(getResources().getString(R.string.btnAddToContact));
			btnContact.setContentDescription(getResources().getString(R.string.btnAddToContact));
		}
		else {
			btnContact.setText(getResources().getString(R.string.btnViewContact));
			btnContact.setContentDescription(getResources().getString(R.string.btnViewContact));
		}
		attachListener(btnContact);
		
		/** Handle long click events **/
		attachOnClickListener(btnCall);
		attachOnClickListener(btnSendSMS);
		attachOnClickListener(btnViewCallHistory);
		attachOnClickListener(btnDeleteFromLog);
		attachOnClickListener(btnContact);
	}	
	
	@Override
	public void onResume() {
		super.onResume();
		//check if keyboard is connected and accessibility services are disabled
    	if(!Utils.isAccessibilityEnabled(getApplicationContext()) &&
    			getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
    		TTS.speak(getResources().getString(R.string.callLogOptions));
    	}
		//get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.calllogoptions);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}