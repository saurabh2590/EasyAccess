package org.easyaccess.simplemenus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.Utils;
import org.easyaccess.settings.SettingsColor;
import org.easyaccess.settings.SettingsFont;

public class BooksNewsWeatherMenu extends EasyAccessActivity {
	
	/** Declare all the Buttons used in the activity **/
	private Button btnBooks, btnNews;
	
	/** Launches the Books menu. **/
	void startBooks() {
		Intent intent = new Intent(getApplicationContext(), BooksMenu.class);
		startActivity(intent);
	}

	/** Launches the News menu. **/
	void startNews() {
		Intent intent = new Intent(getApplicationContext(), NewsMenu.class);
		startActivity(intent);
	}	
	
	/** Create the Books, News & Weather activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.booksnewsweather);
		super.onCreate(savedInstanceState);

		btnBooks=(Button)findViewById(R.id.btnBooks);
		btnNews=(Button)findViewById(R.id.btnNews);

		btnBooks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startBooks();
			}
		});
		
		/** Launch respective app, depending on which button is pressed **/
		setButtonClickUri(R.id.btnAccuWeather, "com.accuweather.android");



		btnNews.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startNews();
			}
		});

		/** Put most everything before here **/
	}
	
	@Override
	protected void onResume() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.booksNewsWeatherMenu);
		// Apply the selected font color, font size and font type to the
		// activity

		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
		
		super.onResume();
	}
	
	
}
