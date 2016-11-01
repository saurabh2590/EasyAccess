package org.easyaccess.simplemenus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class InternetMenu extends EasyAccessActivity {

	/** Create the Email menu activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.internet);
		super.onCreate(savedInstanceState);
		
		/** Launch respective app, depending on which button is pressed **/
		/**
		Button btnChrome = (Button) v.findViewById(R.id.btnChrome);
		btnChrome.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {

					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("http://www.google.com"));
					startActivity(intent);

				} catch (Exception e) {
					launchOrDownloadFromFragment("com.android.chrome");
				}
			}
		});
		**/
		
		setButtonClickUri(R.id.btnChrome, "com.android.chrome");
		setButtonClickUri(R.id.btnFirefox, "org.mozilla.firefox_beta");
		
		/** Put most everything before here **/
	}	
	
	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.internetMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);

		super.onResume();
	}
	
}

