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

import org.easyaccess.calllog.CallLogApp;
import org.easyaccess.contacts.ContactsApp;
import org.easyaccess.phonedialer.PhoneDialerApp;
import org.easyaccess.status.StatusApp;
import org.easyaccess.textmessages.TextMessagesApp;

import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
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
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnPhoneDialer), PhoneDialerApp.class);
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnCallLog), CallLogApp.class);
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnTextMessages), TextMessagesApp.class);
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnContacts), ContactsApp.class);
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnStatus), StatusApp.class);
		attachListenerToOpenExternalApp((Button) v.findViewById(R.id.btnEmail), "com.google.android.gm");

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
