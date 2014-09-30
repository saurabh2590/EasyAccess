/*
	
	Copyright 2014 IDEAL Group Inc.(http://www.ideal-group.org), http://easyaccess.org
	
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
package org.easyaccess.settings;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

/**
 * Adapted from
 * http://www.coderexception.com/CNz6HzB3UWUxQUUJ/listen-to-volume-buttons
 * -in-background-service
 **/
public class SettingsContentObserver extends ContentObserver {
	int previousVolume;
	Context context;

	public SettingsContentObserver(Context c, Handler handler) {
		super(handler);
		context = c;

		AudioManager audio = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	@Override
	public boolean deliverSelfNotifications() {
		return super.deliverSelfNotifications();
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);

		AudioManager audio = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		int delta = previousVolume - currentVolume;

		if (delta > 0) {
			previousVolume = currentVolume;
		} else if (delta < 0) {
			previousVolume = currentVolume;
		}
	}
}
