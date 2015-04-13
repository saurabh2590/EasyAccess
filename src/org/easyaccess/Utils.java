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

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.Ringtone;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

public class Utils {
	/**
	 * Consists of constants and static methods that would be used by various classes the constitute easyaccess.
	 **/

	public static HashMap<String, String> callingDetails = null;
	public static int off_hook = 0;
	public static int ringing = 0;
	public static int loudspeaker = 0;
	public static int INIT = 0;
	public static String CALL_ENDED = "org.easyaccess.easyaccess.CallStateService.CALL_ENDED";
	public static String INCOMING_CALL = "org.easyaccess.easyaccess.CallStateService.INCOMING_CALL";
	public static String END_CALL = "org.easyaccess.easyaccess.CallStateService.END_CALL";
	public static ArrayList<String> callers = new ArrayList<String>();
	public static ArrayList<String> numbers = new ArrayList<String>();
	@SuppressWarnings("serial")
	public static ArrayList<String> colorNames = new ArrayList<String>() {
		{
			add("Black");
			add("White");
			add("Blue");
			add("Cyan");
			add("Green");
			add("Grey");
			add("Magenta");
			add("Red");
			add("Yellow");
		}
	};
	@SuppressWarnings("serial")
	public static ArrayList<String> numberType = new ArrayList<String>() {
		{
			add("Mobile");
			add("Home");
			add("Work");
			add("Work Mobile");
			add("Home Fax");
			add("Pager");
			add("Other");
		}
	};
	@SuppressWarnings("serial")
	public static ArrayList<String> fontType = new ArrayList<String>() {
		{
			add("Normal");
			add("Serif");
			add("Monospace");
		}
	};

	public static final int AUTOMATIC = 1;
	public static final String THREEG = "3G";
	public static final String TWOG = "2G";
	public static final String WIFI = "Wi Fi";
	public static final int OUTGOING = 1;
	public static final int INCOMING = 2;
	public static final int NONE = 0;
	public static final int SERIF = 1;
	public static final int MONOSPACE = 2;

	public static Ringtone ringtone;
	public static AudioManager audioManager;

	/**
	 * Applies a background color and text color to the buttons, text views and radio buttons in the view passed as a parameter.
	 * 
	 * @param view The view whose child elements should be determined.
	 * @param bgColor The background color to be applied.
	 * @param fgColor The text color to be applied.
	 */
	@SuppressWarnings("deprecation")
	public static void iterateToApplyColor(View view, int bgColor, int fgColor) {
		if (view instanceof ViewGroup) {
			for (int index = 0; index < ((ViewGroup) view).getChildCount(); index++)
				iterateToApplyColor(((ViewGroup) view).getChildAt(index), bgColor, fgColor);
		} else if (view != null) {
			if (view.getClass() == Button.class && view.getId() != R.id.btnNavigationBack && view.getId() != R.id.btnNavigationHome) {
				if (bgColor != view.getContext().getResources().getColor(R.color.card_background_regular)) {
					((Button) view).setBackgroundColor(bgColor);
				} else {
					((Button) view).setBackgroundDrawable(((Button) view).getContext().getResources().getDrawable(R.drawable.card));
				}
				((Button) view).setTextColor(fgColor);
			} else if (view.getClass() == TextView.class && !(((TextView) view).getText().toString().trim().equals(""))) {
				if (bgColor != view.getContext().getResources().getColor(R.color.card_background_regular)) {
					((TextView) view).setBackgroundColor(bgColor);
				} else {
					((TextView) view).setBackgroundResource(Color.TRANSPARENT);
				}
				((TextView) view).setTextColor(fgColor);
			} else if (view.getClass() == RadioButton.class && !(((RadioButton) view).getText().toString().trim().equals(""))) {
				if (bgColor != view.getContext().getResources().getColor(R.color.card_background_regular)) {
					((RadioButton) view).setBackgroundColor(bgColor);
				} else {
					((RadioButton) view).setBackgroundResource(Color.TRANSPARENT);
				}
				((RadioButton) view).setTextColor(fgColor);
			}
		}
	}

	/**
	 * Applies a text size to the buttons, text views and radio buttons in the view passed as a parameter.
	 * 
	 * @param view The view whose child elements should be determined.
	 * @param fontSize The text size to be applied.
	 */
	public static void iterateToApplyFontSize(View view, float fontSize) {
		if (view instanceof ViewGroup) {
			for (int index = 0; index < ((ViewGroup) view).getChildCount(); index++)
				iterateToApplyFontSize(((ViewGroup) view).getChildAt(index), fontSize);
		} else if (view != null) {
			if (view.getClass() == Button.class && view.getId() != R.id.btnNavigationBack && view.getId() != R.id.btnNavigationHome) {
				((Button) view).setTextSize(fontSize);
			} else if (view.getClass() == TextView.class && !(((TextView) view).getText().toString().trim().equals(""))) {
				((TextView) view).setTextSize(fontSize);
			} else if (view.getClass() == RadioButton.class && !(((RadioButton) view).getText().toString().trim().equals(""))) {
				((RadioButton) view).setTextSize(fontSize);
			}
		}
	}

	/**
	 * Applies a font type to the buttons, text views and radio buttons in the view passed as a parameter.
	 * 
	 * @param view The view whose child elements should be determined.
	 * @param fontType The font type to be applied.
	 */
	public static void iterateToApplyFontType(View view, int fontType) {
		if (view instanceof ViewGroup) {
			for (int index = 0; index < ((ViewGroup) view).getChildCount(); index++)
				iterateToApplyFontType(((ViewGroup) view).getChildAt(index), fontType);
		} else if (view != null) {
			if (view.getClass() == Button.class && view.getId() != R.id.btnNavigationBack && view.getId() != R.id.btnNavigationHome) {
				switch (fontType) {
				case NONE:
					((Button) view).setTypeface(null, Typeface.BOLD);
					break;
				case SERIF:
					((Button) view).setTypeface(Typeface.SERIF);
					break;
				case MONOSPACE:
					((Button) view).setTypeface(Typeface.MONOSPACE);
					break;
				}
			} else if (view.getClass() == TextView.class && !(((TextView) view).getText().toString().trim().equals(""))) {
				switch (fontType) {
				case NONE:
					((TextView) view).setTypeface(null, Typeface.NORMAL);
					break;
				case SERIF:
					((TextView) view).setTypeface(Typeface.SERIF);
					break;
				case MONOSPACE:
					((TextView) view).setTypeface(Typeface.MONOSPACE);
					break;
				}
			} else if (view.getClass() == RadioButton.class && !(((RadioButton) view).getText().toString().trim().equals(""))) {
				switch (fontType) {
				case NONE:
					((RadioButton) view).setTypeface(null, Typeface.NORMAL);
					break;
				case SERIF:
					((RadioButton) view).setTypeface(Typeface.SERIF);
					break;
				case MONOSPACE:
					((RadioButton) view).setTypeface(Typeface.MONOSPACE);
					break;
				}
			}
		}
	}

	/**
	 * Retrieves the saved background and text colors and applies to the views in the layout.
	 * 
	 * @param context The context of the application that invokes the method.
	 * @param layout The layout in which the color changes are to be applied.
	 */
	public static void applyFontColorChanges(Context context, LinearLayout layout) {
		// get the values in SharedPreferences
		SharedPreferences preferences = context.getSharedPreferences(context.getResources().getString(R.string.color), 0);

		if (preferences.getInt("bgcolor", -1) != -1 || preferences.getInt("fgcolor", -1) != -1) {

			int bgColor = preferences.getInt("bgcolor", 0);
			int fgColor = preferences.getInt("fgcolor", 0);
			try {
				context.getResources().getResourceName(bgColor);
				bgColor = context.getResources().getColor(bgColor);
			} catch (NotFoundException nfe) {
				bgColor = context.getResources().getColor(R.color.card_background_regular);
			}
			try {
				context.getResources().getResourceName(fgColor);
				fgColor = context.getResources().getColor(fgColor);
			} catch (NotFoundException nfe) {
				fgColor = context.getResources().getColor(R.color.card_textcolor_regular);
			}
			Utils.iterateToApplyColor(layout, bgColor, fgColor);
		} else {
			Utils.iterateToApplyColor(layout, context.getResources().getColor(R.color.card_background_regular),
					context.getResources().getColor(R.color.card_textcolor_regular));
		}
	}

	public static void applyFontColorChanges(Context context, View view) {

		SharedPreferences preferences = context.getSharedPreferences(context.getResources().getString(R.string.color), 0);
		int fgColor = preferences.getInt("fgcolor", 0);
		int bgColor = preferences.getInt("bgcolor", 0);

		try {
			fgColor = context.getResources().getColor(fgColor);
		} catch (NotFoundException nfe) {
			fgColor = context.getResources().getColor(R.color.card_textcolor_regular);
		}

		try {
			if (bgColor != 0) {
				bgColor = context.getResources().getColor(bgColor);
			}
		} catch (NotFoundException nfe) {
			// Ignore
		}

		if (view instanceof TextView) {
			((TextView) view).setBackgroundColor(bgColor);
			((TextView) view).setTextColor(fgColor);
		} else if (view instanceof Button) {
			((Button) view).setBackgroundColor(bgColor);
			((Button) view).setTextColor(fgColor);
		}

	}

	/**
	 * Retrieves the saved text size and applies to the views in the layout.
	 * 
	 * @param context The context of the application that invokes the method.
	 * @param layout The layout in which the text size changes are to be applied.
	 */
	public static void applyFontSizeChanges(Context context, View view) {
		// get the values in SharedPreferences
		SharedPreferences preferences = context.getSharedPreferences(context.getResources().getString(R.string.size), 0);
		if (preferences.getFloat("size", 0) != 0) {
			float fontSize = preferences.getFloat("size", 0);
			Utils.iterateToApplyFontSize(view, fontSize);
		} else {
			Utils.iterateToApplyFontSize(view, Integer.valueOf(context.getResources().getString(R.string.defaultFontSize)));
		}
	}

	/**
	 * Retrieves the saved font type and applies to the views in the layout.
	 * 
	 * @param context The context of the application that invokes the method.
	 * @param layout The layout in which the font type changes are to be applied.
	 */
	public static void applyFontTypeChanges(Context context, View view) {
		// get the values in SharedPreferences
		SharedPreferences preferences = context.getSharedPreferences(context.getResources().getString(R.string.fonttype), 0);
		if (preferences.getInt("typeface", -1) != -1) {
			Utils.iterateToApplyFontType(view, preferences.getInt("typeface", -1));
		} else {
			Utils.iterateToApplyFontType(view, 0);
		}
	}

	/**
	 * Determines whether any accessibility service is enabled on the device.
	 * 
	 * @param context The context of the application that invoked the method.
	 * @return Returns true if an accessibility service is enabled, else, returns false.
	 */
	public static boolean isAccessibilityEnabled(Context context) {
		AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
		return am.isEnabled();
	}

	/**
	 * Announces the text passed as a parameter, and causes the device to vibrate for 300 milliseconds.
	 * 
	 * @param context The context of the application that invoked the method.
	 * @param text The text that is to be read aloud.
	 */
	public static void giveFeedback(Context context, String text) {
		// vibrate
		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(300);
		// TTS feedback
		if (!TTS.isSpeaking())
			TTS.speak(text);
	}

	/**
	 * Attaches onFocusChangeListener to the button passed as a parameter. When the button receives focus, giveFeedback method is called, that reads aloud the
	 * string passed to it as a parameter.
	 * 
	 * @param context The context of the application that invoked the method.
	 * @param button The button with which the onFocusChange listener is to be associated.
	 */
	public static void attachListener(final Context context, Button button) {
		final String text = button.getText().toString();

		button.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					giveFeedback(context, text);
				}
			}
		});
	}
}
