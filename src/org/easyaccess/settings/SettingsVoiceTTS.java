package org.easyaccess.settings;

import android.os.Bundle;
import android.widget.LinearLayout;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class SettingsVoiceTTS extends EasyAccessActivity {

	/** Create the Voice TTS menu activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.settingsvoicetts);
		super.onCreate(savedInstanceState);
		
		/** Launch respective app, depending on which button is pressed **/
		setButtonClickUri(R.id.btnAssistant, "com.speaktoit.assistant");
		setButtonClickUri(R.id.btnEspeak, "com.reecedunn.espeak");
		setButtonClickUri(R.id.btnGoogleTTS, "com.google.android.tts");
		setButtonClickUri(R.id.btnAutoTTS, "com.vnspeak.ttsengine.autotts");
		
		/** Put most everything before here **/
	}	
	
	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.settingsVoiceTTSMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);

		super.onResume();
	}
	
}
