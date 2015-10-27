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

import org.easyaccess.textmessages.TextMessagesApp;
import org.easyaccess.textmessages.TextMessagesViewerApp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

@SuppressWarnings("rawtypes")
public class CommonAdapter extends ArrayAdapter {

	/**
	 * A custom adapter to display the items in a ListView.
	 */
	private static final int CALL_LOG = 1;
	private static final int CALL_LOG_HISTORY = 2;
	private static final int TEXT_MESSAGES = 3;

	private final Context context;
	private ArrayList<String> values;
	private int callLogActivity = 0;
	public SparseBooleanArray mCheckStates;
	private int typeOfMessage;
	@SuppressWarnings("unchecked")
	public CommonAdapter(Context context, ArrayList values, int callLog, int typeOfMsg) {
		super(context, R.layout.row, values);
		this.context = context;
		this.values = new ArrayList();
		this.values = (values);
		this.typeOfMessage = typeOfMsg;
		callLogActivity =callLog;
		mCheckStates = new SparseBooleanArray(values.size());
	}

	@Override
	// this method generates a View for a list item
	public View getView(final int position, View view, ViewGroup parent) {
		
		 AppInfoHolder holder= null;
		//View 
		View rowView =view;
		 if (rowView == null){
			 LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			 rowView = inflater.inflate(R.layout.row, parent, false);
			 
			 holder = new AppInfoHolder();
			 
			 holder.textView = (TextView) rowView
						.findViewById(R.id.textView1);
			 holder.cb_delete = (CheckBox)rowView
						.findViewById(R.id.cb_delete);
			 rowView.setTag(holder);
		 }
		 else{
			 
			 holder = (AppInfoHolder)rowView.getTag();
		 }
	
		 holder.cb_delete.setTag(position);
	
		 holder.textView.setText(values.get(position));
		
		
		if (this.callLogActivity == CALL_LOG) {
			
			int secondOccurrenceOfNewLineCharacter = values.get(position)
					.indexOf("\n", values.get(position).indexOf("\n") + 1);
			holder.textView.setContentDescription(values.get(position).substring(0,
					values.get(position).indexOf("\n"))
					+ "\n"
					+ values.get(position)
							.substring(values.get(position).indexOf("\n") + 1,
									secondOccurrenceOfNewLineCharacter)
							.replaceAll(".(?=[0-9])", "$0 ")
					+ values.get(position)
							.substring(secondOccurrenceOfNewLineCharacter)
							.replaceAll(".(?=[:])", "$0 "));
		} else if (this.callLogActivity == CALL_LOG_HISTORY) {
			
			holder.textView.setContentDescription(values.get(position).replaceAll(
					".(?=[:])", "$0 "));
		} else if (this.callLogActivity == TEXT_MESSAGES) {
			
			holder.cb_delete.setVisibility(View.VISIBLE);
			
			holder.textView.setContentDescription(values.get(position)
					.substring(0, values.get(position).indexOf("\n"))
					.replaceAll(".(?=[0-9])", "$0 ")
					+ values.get(position)
							.substring(values.get(position).indexOf("\n"))
							.replaceAll(".(?=[:])", "$0 "));
			
			
			holder.textView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					
					Intent intent = new Intent(context,
							TextMessagesViewerApp.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("address", values.get(position).substring(values.get(position).lastIndexOf("*")+1, values.get(position).length()));
					intent.putExtra("typeOfMessage", typeOfMessage);
					context.startActivity(intent);
					//((Activity) context).finish();
					((TextMessagesApp)context).finish();
					
				}
			});
			
			
			
			
			holder.cb_delete.setChecked(mCheckStates.get(position, false));
			holder.cb_delete.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					  mCheckStates.put((Integer) buttonView.getTag(), isChecked);    
					
				}
			});
		} else {
			
			if (values.get(position).contains(":")) {
				android.util.Log.e("here", "here");
				holder.textView.setContentDescription(values.get(position)
						.replaceAll(".(?=[:])", "$0 ")
						.replaceAll(".(?=[0-9])", "$0 "));
			} else {
				holder.textView.setContentDescription(values.get(position).replaceAll(
						".(?=[0-9])", "$0 "));
			}
		}
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
			holder.textView.setTextColor(fgColor);
			holder.textView.setBackgroundColor(bgColor);
		}
		preferences = context.getSharedPreferences(context.getResources()
				.getString(R.string.fonttype), 0);
		if (preferences.getInt("typeface", -1) != -1) {
			switch (preferences.getInt("typeface", -1)) {
			case Utils.NONE:
				holder.textView.setTypeface(null, Typeface.NORMAL);
				break;
			case Utils.SERIF:
				holder.textView.setTypeface(Typeface.SERIF);
				break;
			case Utils.MONOSPACE:
				holder.textView.setTypeface(Typeface.MONOSPACE);
				break;
			}
		} else {
			holder.textView.setTypeface(null, Typeface.NORMAL);
		}

		preferences = context.getSharedPreferences(context.getResources()
				.getString(R.string.size), 0);
		if (preferences.getFloat("size", 0) != 0) {
			float fontSize = preferences.getFloat("size", 0);
			holder.textView.setTextSize(fontSize);
		} else {
			holder.textView.setTextSize(Integer.valueOf(context.getResources()
					.getString(R.string.textSize)));
		}
		return rowView;
	}
	
	
	
	 public boolean isChecked(int position) {
	        return mCheckStates.get(position, false);
	    }

	    public void setChecked(int position, boolean isChecked) {
	        mCheckStates.put(position, isChecked);

	    }
	    
	    static class AppInfoHolder
	    {
	        
	        TextView textView;
	        CheckBox cb_delete;

	    }
}
