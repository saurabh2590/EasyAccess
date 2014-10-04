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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.Toast;

public class EasyAccessFragment extends Fragment {

	/**
	 * Launch installed Android app or download from Google Play Store if
	 * missing
	 **/
	void launchOrDownloadFromFragment(String uriTarget) {
		Intent intent = getActivity().getPackageManager()
				.getLaunchIntentForPackage(uriTarget);
		if (intent != null) {
			//Start installed app
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} else {
			//If app is not installed, bring user to the Play Store
			intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(Uri.parse("market://details?id=" + uriTarget));
			// Error handling in case Play Store cannot be launched
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Context context = getActivity().getApplicationContext();
				CharSequence text = "Unable to launch the Google Play Store!";
				Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			}
		}
	}
}
