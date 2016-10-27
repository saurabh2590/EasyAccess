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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import org.easyaccess.alarms.AlarmCalendarMenu;
import org.easyaccess.alarms.DailyUtilities;
import org.easyaccess.simplemenus.BooksMenu;
import org.easyaccess.simplemenus.BooksNewsWeatherMenu;
import org.easyaccess.simplemenus.BooksNewspapersMenu;
import org.easyaccess.simplemenus.InternetMenu;
import org.easyaccess.simplemenus.MapsMenu;
import org.easyaccess.simplemenus.EntertainmentMenu;
import org.easyaccess.simplemenus.MusicVideoMenu;

public class Homescreen2Activity extends AbstractHomescreenActivity implements
		KeyListener {
	/**
	 * The HomeScreenActivity displays the options available in the app.
	 */

	/** Create the Main activity showing home screen #2 **/
	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.homescreen2, container, false);
		this.view = v;

		// Launch respective easyaccess app, depending on which button is
		// pressed
		// attachListenerToOpenExternalApp((Button)
		// v.findViewById(R.id.btnInternet), "com.android.chrome");

		attachListenerToOpenActivity(
				(Button) v.findViewById(R.id.btnDailyUtilities),
				DailyUtilities.class);
		
		attachListenerToOpenActivity(
				(Button) v.findViewById(R.id.btnInternet),
				InternetMenu.class);		
		
		attachListenerToOpenExternalApp((Button) v.findViewById(R.id.btnRadio),
				"tunein.player");

		attachListenerToOpenActivity(
				(Button) v.findViewById(R.id.btnEntertainment),
				EntertainmentMenu.class);

		/*
		 * Button btnMusicVideo = (Button) v.findViewById(R.id.btnMusicVideo);
		 * btnMusicVideo.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { try { Intent intent = new
		 * Intent(); //intent.setType("audio/video");
		 * intent.setType("video/mp4");
		 * intent.setAction(Intent.ACTION_GET_CONTENT);
		 * startActivity(Intent.createChooser(intent, "Open Audio (mp3) file"));
		 * } catch (Exception e) {
		 * 
		 * } } });
		 */attachListenerToOpenActivity(
				(Button) v.findViewById(R.id.btnBooksNewsWeather),
				BooksNewsWeatherMenu.class);

		attachListenerToOpenActivity(
				(Button) v.findViewById(R.id.btnBooksNewspapers),
				BooksNewspapersMenu.class);
		attachListenerToOpenActivity(
				(Button) v.findViewById(R.id.btnAlarmCalendar),
				AlarmCalendarMenu.class);
		attachListenerToOpenActivity(
				(Button) v.findViewById(R.id.btnMusicVideo),
				MusicVideoMenu.class);
		attachListenerToOpenActivity((Button) v.findViewById(R.id.btnMaps),
				MapsMenu.class);

		/** Put most everything before here **/
		return v;
	}

	@Override
	void startSelectedActivity(View view) {
		switch (view.getId()) {
		case R.id.btnInternet:
			launchOrDownloadFromFragment("com.android.chrome");
			break;
		case R.id.btnAlarmCalendar:
			startNewActivity(DailyUtilities.class);
			break;
		case R.id.btnRadio:
			launchOrDownloadFromFragment("tunein.player");
			break;
		case R.id.btnMusicVideo:
			startNewActivity(EntertainmentMenu.class);
			break;
		case R.id.btnBooksNewspapers:
			startNewActivity(BooksNewsWeatherMenu.class);
			break;
		case R.id.btnMaps:
			startNewActivity(MapsMenu.class);
			break;
		}
	}

}
