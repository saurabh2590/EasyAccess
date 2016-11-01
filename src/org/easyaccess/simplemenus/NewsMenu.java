package org.easyaccess.simplemenus;

import android.os.Bundle;
import android.widget.LinearLayout;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;

public class NewsMenu extends EasyAccessActivity {
	
	/** Create the News activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.news);
		super.onCreate(savedInstanceState);
		
		/** Launch respective app, depending on which button is pressed **/
		setButtonClickUri(R.id.btnBBCNews, "bbc.mobile.news.ww");
		setButtonClickUri(R.id.btnNDTV, "com.july.ndtv");
		setButtonClickUri(R.id.btnTimesNow, "com.timesnowmobile.TimesNow");
		setButtonClickUri(R.id.btnAajTak, "in.AajTak.headlines");
		setButtonClickUri(R.id.btnZeeNews, "com.zeenews.news");
		
		/** Put most everything before here **/
	}
	
	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.newsMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
		
		super.onResume();
	}
	
	
}
