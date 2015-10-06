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
package org.easyaccess.contacts;

import java.util.ArrayList;

import org.easyaccess.R;
import org.easyaccess.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Creates a custom adapter for adding items in the spinner.
 */

@SuppressWarnings("rawtypes")
public class SpinnerAdapter extends ArrayAdapter {

	/** Declare variables **/
	private final Context context;
	private ArrayList<String> values;

	/**
	 * Constructor to initialize the spinner Adapter with values
	 * 
	 * @param context
	 * @param values
	 */
	@SuppressWarnings("unchecked")
	public SpinnerAdapter(Context context, ArrayList values) {
		super(context, R.layout.row, values);
		this.context = context;
		this.values = new ArrayList();
		this.values = (values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getDropDownView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@SuppressWarnings("deprecation")
	/**
	 * Generates a View for a list item.
	 * @param position The position at which the item is to be inserted.
	 * @param view The view that is to be generated for the list item.
	 * @param parent The parent of the view.
	 * @return Returns the generated View.
	 */
	public View getCustomView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row, parent, false);
		final TextView textView = (TextView) rowView
				.findViewById(R.id.textView1);
		textView.setText(values.get(position));
		textView.setContentDescription(values.get(position));
		SharedPreferences preferences = context.getSharedPreferences(context
				.getResources().getString(R.string.color), 0);
		int bgColor = preferences.getInt("bgcolor", 0);
		int fgColor = preferences.getInt("fgcolor", 0);
		try {
			if (bgColor != 0) {
				context.getResources().getResourceName(bgColor);
				bgColor = context.getResources().getColor(bgColor);
				textView.setBackgroundColor(bgColor);
			} else {
				textView.setBackgroundDrawable(null);
			}
		} catch (NotFoundException nfe) {
			textView.setBackgroundDrawable(null);
		}
		try {
			context.getResources().getResourceName(fgColor);
			fgColor = context.getResources().getColor(fgColor);
		} catch (NotFoundException nfe) {
			fgColor = context.getResources().getColor(
					R.color.card_textcolor_regular);
		}
		textView.setTextColor(fgColor);

		preferences = context.getSharedPreferences(context.getResources()
				.getString(R.string.fonttype), 0);
		if (preferences.getInt("typeface", -1) != -1) {
			switch (preferences.getInt("typeface", -1)) {
			case Utils.NONE:
				textView.setTypeface(null, Typeface.NORMAL);
				break;
			case Utils.SERIF:
				textView.setTypeface(Typeface.SERIF);
				break;
			case Utils.MONOSPACE:
				textView.setTypeface(Typeface.MONOSPACE);
				break;
			}
		} else {
			textView.setTypeface(null, Typeface.NORMAL);
		}

		preferences = context.getSharedPreferences(context.getResources()
				.getString(R.string.size), 0);
//		if (preferences.getFloat("size", 0) != 0) {
//			float fontSize = preferences.getFloat("size", 0);
//			textView.setTextSize(fontSize);
//		} else {
//			textView.setTextSize(Integer.valueOf(context.getResources()
//					.getString(R.string.textSize))
//					* context.getResources().getDisplayMetrics().density);
//		}

		return rowView;
	}
}
