package org.easyaccess.simplemenus;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class EmailMenu extends EasyAccessActivity {

	/** Create the Email menu activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.email);
		super.onCreate(savedInstanceState);
		
		/** Launch respective app, depending on which button is pressed **/
		setButtonClickUri(R.id.btnAquaMail, "org.kman.AquaMail");
		
		Button btnEmail = (Button)findViewById(R.id.btnGmail);
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
					PackageManager pm = getApplicationContext().getPackageManager();

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
					//launchOrDownloadFromFragment("com.google.android.gm");
				}
			}
		});		
		
		/** Put most everything before here **/
	}	
	
	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.emailMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);

		super.onResume();
	}
	
}
