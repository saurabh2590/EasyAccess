package org.easyaccess.simplemenus;

import android.os.Bundle;
import android.widget.LinearLayout;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class FileBrowsingSharingMenu extends EasyAccessActivity {

	/** Create the File Browsing & Sharing menu activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.filebrowsingsharing);
		super.onCreate(savedInstanceState);
		
		/** Launch respective app, depending on which button is pressed **/
		setButtonClickUri(R.id.btnESFileExplorer, "com.estrongs.android.pop");
		setButtonClickUri(R.id.btnTotalCommander, "com.ghisler.android.TotalCommander");
		setButtonClickUri(R.id.btnDropbox, "com.dropbox.android");
		setButtonClickUri(R.id.btnGoogleDrive, "com.google.android.apps.docs");
		
		/** Put most everything before here **/
	}	
	
	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.fileBrowsingSharingMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);

		super.onResume();
	}
	
}
