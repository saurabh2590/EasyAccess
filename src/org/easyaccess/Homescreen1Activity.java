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
import org.easyaccess.status.StatusApp;
import org.easyaccess.textmessages.TextMessagesApp;

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
		attachListenerToOpenActivity(
				(Button) v.findViewById(R.id.btnTextMessages),
				TextMessagesApp.class);
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnContacts),
				ContactsApp.class);
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnStatus),
				StatusApp.class);
		// attachListenerToOpenExternalApp((Button)
		// v.findViewById(R.id.btnEmail), "com.google.android.gm");
		Button btnEmail = (Button) v.findViewById(R.id.btnEmail);
		btnEmail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {

					/*
					 * Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
					 * .parse("mailto:")); startActivity(intent);
					 */
					Intent emailIntent = new Intent(Intent.ACTION_VIEW, Uri
							.parse("mailto:"));
					PackageManager pm = getActivity().getPackageManager();

					List<ResolveInfo> resInfo = pm.queryIntentActivities(
							emailIntent, 0);
					if (resInfo.size() > 0) {
						ResolveInfo ri = resInfo.get(0);
						// First create an intent with only the package name of
						// the first registered email app
						// and build a picked based on it
						Intent intentChooser = pm
								.getLaunchIntentForPackage(ri.activityInfo.packageName);
						Intent openInChooser = Intent.createChooser(
								intentChooser, "");

						// Then create a list of LabeledIntent for the rest of
						// the registered email apps
						List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
						for (int i = 1; i < resInfo.size(); i++) {
							// Extract the label and repackage it in a
							// LabeledIntent
							ri = resInfo.get(i);
							String packageName = ri.activityInfo.packageName;
							Intent intent = pm
									.getLaunchIntentForPackage(packageName);
							intentList.add(new LabeledIntent(intent,
									packageName, ri.loadLabel(pm), ri.icon));
						}

						LabeledIntent[] extraIntents = intentList
								.toArray(new LabeledIntent[intentList.size()]);
						// Add the rest of the email apps to the picker
						// selection
						openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
								extraIntents);
						startActivity(openInChooser);
					}
				} catch (Exception e) {
					launchOrDownloadFromFragment("com.google.android.gm");
				}
			}
		});

		/** Put most everything before here **/
		return v;
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
			startNewActivity(TextMessagesApp.class);
			break;
		case R.id.btnContacts:
			startNewActivity(ContactsApp.class);
			break;
		case R.id.btnStatus:
			startNewActivity(StatusApp.class);
			break;
		case R.id.btnEmail:
			launchOrDownloadFromFragment("com.google.android.gm");
			break;
		}
	}


}
