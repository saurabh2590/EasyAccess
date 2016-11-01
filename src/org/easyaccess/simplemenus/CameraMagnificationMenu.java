package org.easyaccess.simplemenus;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.LinearLayout;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;
import org.easyaccess.settings.SettingsColor;
import org.easyaccess.settings.SettingsFont;

public class CameraMagnificationMenu extends EasyAccessActivity {
	
	/** Declare all the Buttons used in the activity **/
	private Button btnOCR, btnOtherCameraUtilities;
	
	/** Launches the OCR menu. **/
	void startOCR() {
		Intent intent = new Intent(getApplicationContext(), OCRMenu.class);
		startActivity(intent);
	}

	/** Launches the Other Camera Utilities menu. **/
	void startOtherCameraUtilities() {
		Intent intent = new Intent(getApplicationContext(), OtherCameraUtilitiesMenu.class);
		startActivity(intent);
	}	
	
	/** Create the Camera & Magnification activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.cameramagnification);
		super.onCreate(savedInstanceState);
		
		/** Launch respective app, depending on which button is pressed **/

		setButtonClickIntent(String.valueOf(R.id.btnPhoneCamera), MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
		setButtonClickUri(R.id.btnDigitalMagnifier, "com.app2u.magnifier");
		
		/** Put most everything before here **/
	}
	
	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.cameraMagnificationMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
		
		super.onResume();
	}
	
	
}
