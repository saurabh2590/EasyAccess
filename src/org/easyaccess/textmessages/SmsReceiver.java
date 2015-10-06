/*
	
	Copyright 2014 IDEAL Group Inc.(http://www.ideal-group.org), http://easyaccess.org
	
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

import java.util.HashMap;

import org.easyaccess.TTS;
import org.easyaccess.Utils;
import org.easyaccess.phonedialer.ContactManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SmsMessage;

/** Class that listens to incoming text messages **/

public class SmsReceiver extends BroadcastReceiver {
	private HashMap<String, String> senderDetails;
	private boolean isDefault = false;

	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction()
				.equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;
			String sender = null;
			if (bundle != null) {
				try {
					Object[] pdus = (Object[]) bundle.get("pdus");
					msgs = new SmsMessage[pdus.length];
					for (int i = 0; i < msgs.length; i++) {
						msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
						sender = msgs[i].getOriginatingAddress();
						senderDetails = new ContactManager(context)
								.getNameFromNumber(sender);
						// play ringtone
						Utils.ringtone = RingtoneManager.getRingtone(context,
								Settings.System.DEFAULT_NOTIFICATION_URI);
						Utils.ringtone.play();
						if (senderDetails.get("name") != null) {
							if (Utils.isAccessibilityEnabled(context))
								TTS.speak("Received text message from "
										+ senderDetails.get("name") + " "
										+ senderDetails.get("type"));
						} else {
							if (Utils.isAccessibilityEnabled(context))
								TTS.speak("Received text message from "
										+ sender);
						}
						// if default app
						if (android.os.Build.VERSION.SDK_INT >= 19) {
							if (Telephony.Sms.getDefaultSmsPackage(context)
									.equals(context.getPackageName())) {
								isDefault = true;
								ContentValues values = new ContentValues();
								values.put("address", sender);
								values.put("date", msgs[i].getTimestampMillis());
								if (msgs[i].getPseudoSubject().equals("")) {
									values.put("subject", " ");
								} else {
									values.put("subject",
											msgs[i].getPseudoSubject());
								}
								values.put("read", 0);
								values.put("body",
										msgs[i].getDisplayMessageBody());
								context.getContentResolver().insert(
										Uri.parse("content://sms/inbox"),
										values);
							}
						}
						else{
							isDefault = true;
						}
						
					
						if(isDefault){
							Intent intentObject = new Intent(context,
									TextMessagesApp.class);
							intentObject.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intentObject.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							
							context.startActivity(intentObject);
						}
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
}