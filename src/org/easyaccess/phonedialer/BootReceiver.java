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
