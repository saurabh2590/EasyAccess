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
package org.easyaccess;

import java.util.ArrayList;
import java.util.List;

import org.easyaccess.calllog.CallLogApp;
import org.easyaccess.contacts.ContactsApp;
import org.easyaccess.phonedialer.PhoneDialerApp;
import org.easyaccess.simplemenus.EmailMenu;
import org.easyaccess.status.StatusApp;
import org.easyaccess.textmessages.TextMessagesApp;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class Homescreen1Activity extends AbstractHomescreenActivity implements
		KeyListener {
	/**
	 * The HomeScreenActivity displays the options available in the app.
	 */

	/** Create the Main activity showing home screen #1 **/
	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.homescreen1, container, false);
		this.view = v;

		// Launch respective easyaccess app, depending on which button is
		// pressed
		attachListenerToOpenActivity(
				(Button) v.findViewById(R.id.btnPhoneDialer),
				PhoneDialerApp.class);
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnCallLog),
				CallLogApp.class);
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnEmail),
				EmailMenu.class);		
		
		
		// uncommented to allow opening of default SMS application.
		Button btnMsg = (Button) v.findViewById(R.id.btnTextMessages);
		btnMsg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*Intent sendIntent = new Intent(Intent.ACTION_VIEW);         
				sendIntent.setData(Uri.parse("sms:"));
				startActivity(sendIntent);*/
				//openInbox();
				openTextMessageInbox();
			}
		});



		// Commented to allow opening of default SMS application.
//		attachListenerToOpenActivity(
//				(Button) v.findViewById(R.id.btnTextMessages),
//				TextMessagesApp.class);
		
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnContacts),
				ContactsApp.class);
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnStatus),
				StatusApp.class);
		// attachListenerToOpenExternalApp((Button)
		// v.findViewById(R.id.btnEmail), "com.google.android.gm");
		

		/** Put most everything before here **/
		return v;
	}
	
	private void openTextMessageInbox() {
		try {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.setType("vnd.android-dir/mms-sms");
			if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
				startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	void startSelectedActivity(View view) {
		switch (view.getId()) {
		case R.id.btnPhoneDialer:
			startNewActivity(PhoneDialerApp.class);
			break;
		case R.id.btnCallLog:
			startNewActivity(CallLogApp.class);
			break;
		case R.id.btnTextMessages:
			// Commented to allow opening of default SMS application.
			// startNewActivity(TextMessagesApp.class);
			break;
		case R.id.btnContacts:
			startNewActivity(ContactsApp.class);
			break;
		case R.id.btnStatus:
			startNewActivity(StatusApp.class);
			break;
		case R.id.btnEmail:
			//launchOrDownloadFromFragment("com.google.android.gm");
			break;
		}
	}


	public void openInbox() {String application_name = "com.android.mms";
	try {
	Intent intent = new Intent("android.intent.action.MAIN");
	intent.addCategory("android.intent.category.LAUNCHER");

	intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	List<ResolveInfo> resolveinfo_list = getActivity().getPackageManager()
	.queryIntentActivities(intent, 0);

	for (ResolveInfo info : resolveinfo_list) {
	if (info.activityInfo.packageName
	.equalsIgnoreCase(application_name)) {
	launchComponent(info.activityInfo.packageName,
	info.activityInfo.name);
	break;
	}
	}
	} catch (ActivityNotFoundException e) {
	Toast.makeText(
			getActivity().getApplicationContext(),
	"There was a problem loading the application: "
	+ application_name, Toast.LENGTH_SHORT).show();
	}
	}

		private void launchComponent(String packageName, String name) {
		Intent launch_intent = new Intent("android.intent.action.MAIN");
		launch_intent.addCategory("android.intent.category.LAUNCHER");
		launch_intent.setComponent(new ComponentName(packageName, name));
		launch_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(launch_intent);
		}
}
