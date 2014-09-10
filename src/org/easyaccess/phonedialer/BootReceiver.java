package org.easyaccess.phonedialer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The class that starts the CallStateService when the phone is booted.
 */
public class BootReceiver extends BroadcastReceiver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent bootIntent = new Intent(context, CallStateService.class);
		bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(bootIntent);
	}
}
