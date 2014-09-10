package org.easyaccess.textmessages;

import java.util.HashMap;

import org.easyaccess.TTS;
import org.easyaccess.Utils;
import org.easyaccess.phonedialer.ContactManager;

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

public class SmsReceiver extends BroadcastReceiver{
	private HashMap<String, String> senderDetails;
	
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String sender = null;
            if (bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        sender = msgs[i].getOriginatingAddress();
                        senderDetails = new ContactManager(context).getNameFromNumber(sender);
                        //play ringtone
                        Utils.ringtone = RingtoneManager.getRingtone(context, Settings.System.DEFAULT_NOTIFICATION_URI);
                        Utils.ringtone.play();
                        if(senderDetails.get("name") != null) {
                        	if(Utils.isAccessibilityEnabled(context))
                        		TTS.speak("Received text message from " + senderDetails.get("name") + " " + senderDetails.get("type"));
                        }
                        else {
                        	if(Utils.isAccessibilityEnabled(context))
                        		TTS.speak("Received text message from " + sender);
                        }
                      //if default app
                        if(android.os.Build.VERSION.SDK_INT >= 19) {
                			if (Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) {
		                        ContentValues values = new ContentValues();
		                        values.put("address", sender);
		                        values.put("date", msgs[i].getTimestampMillis());
		                        if(msgs[i].getPseudoSubject().equals("")) {
		                        	values.put("subject", " ");
		                        }
		                        else {
		                        	values.put("subject", msgs[i].getPseudoSubject());
		                        }
		                        values.put("read", 0);
		                        values.put("body", msgs[i].getDisplayMessageBody());
		                        context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
                			}
                        }
                        Intent intentObject  = new Intent(context, TextMessagesApp.class);
                        intentObject.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentObject);
                    }
                }catch(Exception e){
                	e.printStackTrace();
                }
                
                
            }
        }
    }
}