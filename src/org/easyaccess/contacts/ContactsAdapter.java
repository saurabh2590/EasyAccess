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
import android.widget.SectionIndexer;
import android.widget.TextView;

@SuppressWarnings("rawtypes")
public class ContactsAdapter extends ArrayAdapter implements SectionIndexer {
	/**
	 * A custom adapter to display the contacts in a ListView. It implements
	 * quick letter navigation.
	 */

	/** Declare variables **/
	private final Context context;
	private ArrayList<String> values;

	/**
	 * Constructor to initialize the values for the Adapter.
	 * 
	 * @param context
	 * @param values
	 */
	@SuppressWarnings("unchecked")
	public ContactsAdapter(Context context, ArrayList values) {
		super(context, R.layout.row, values);
		this.context = context;
		this.values = new ArrayList();
		this.values = (values);
	}

	/**
	 * Constructs the view for the adapter from the view. (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row, parent, false);
		final TextView textView = (TextView) rowView
				.findViewById(R.id.textView1);
		textView.setText(values.get(position));
		textView.setContentDescription(values.get(position).replaceAll(
				".(?=[0-9])", "$0 "));
		SharedPreferences preferences = context.getSharedPreferences(context
				.getResources().getString(R.string.color), 0);
		if (preferences.getInt("bgcolor", 0) != 0
				|| preferences.getInt("fgcolor", 0) != 0) {
			int bgColor = preferences.getInt("bgcolor", 0);
			int fgColor = preferences.getInt("fgcolor", 0);
			try {
				context.getResources().getResourceName(bgColor);
				bgColor = context.getResources().getColor(bgColor);
			} catch (NotFoundException nfe) {
				bgColor = context.getResources().getColor(
						R.color.homescreen_background);
			}
			try {
				context.getResources().getResourceName(fgColor);
				fgColor = context.getResources().getColor(fgColor);
			} catch (NotFoundException nfe) {
				fgColor = context.getResources().getColor(
						R.color.card_textcolor_regular);
			}
			textView.setTextColor(fgColor);
			textView.setBackgroundColor(bgColor);
		}
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
			textView.setTypeface(null, Typeface.BOLD);
		}

		preferences = context.getSharedPreferences(context.getResources()
				.getString(R.string.size), 0);
		if (preferences.getFloat("size", 0) != 0) {
			float fontSize = preferences.getFloat("size", 0);
			textView.setTextSize(fontSize);
		} else {

			textView.setTextSize(Integer.valueOf(context.getResources()
					.getString(R.string.textSize)));
		}
		return rowView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.SectionIndexer#getPositionForSection(int)
	 */
	@Override
	public int getPositionForSection(int i) {
		return (int) (getCount() * ((float) i / (float) getSections().length));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.SectionIndexer#getSectionForPosition(int)
	 */
	@Override
	public int getSectionForPosition(int arg0) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.SectionIndexer#getSections()
	 */
	@Override
	public Object[] getSections() {
		String[] chars = new String[SideSelector.ALPHABET.length];
		for (int i = 0; i < SideSelector.ALPHABET.length; i++) {
			chars[i] = String.valueOf(SideSelector.ALPHABET[i]);
		}

		return chars;
	}
}
