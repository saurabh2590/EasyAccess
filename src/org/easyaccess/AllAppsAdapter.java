package org.easyaccess;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class AllAppsAdapter extends ArrayAdapter<ApplicationInfo> {
	private List<ApplicationInfo> appsList = null;
	private Context context;
	private PackageManager packageManager;
	private int bgColor;
	private int fgColor;
	private SharedPreferences preferences;
	private Typeface typeface;
	private float textSize;

	AllAppsAdapter(Context context, int textViewResourceId, List<ApplicationInfo> appsList) {
		super(context, textViewResourceId, appsList);
		this.context = context;
		this.appsList = appsList;
		packageManager = context.getPackageManager();
		getFontSettings();
	}

	private void getFontSettings() {
		preferences = context.getSharedPreferences(context.getResources().getString(R.string.color), 0);
		bgColor = preferences.getInt("bgcolor", 0);
		fgColor = preferences.getInt("fgcolor", 0);
		try {
			if (bgColor != 0) {
				context.getResources().getResourceName(bgColor);
				bgColor = context.getResources().getColor(bgColor);
			}
		} catch (NotFoundException nfe) {
			// Ignore
		}
		try {
			context.getResources().getResourceName(fgColor);
			fgColor = context.getResources().getColor(fgColor);
		} catch (NotFoundException nfe) {
			fgColor = context.getResources().getColor(R.color.card_textcolor_regular);
		}

		preferences = context.getSharedPreferences(context.getResources().getString(R.string.fonttype), 0);
		if (preferences.getInt("typeface", -1) != -1) {
			switch (preferences.getInt("typeface", -1)) {
			case Utils.NONE:
				typeface = null;
				break;
			case Utils.SERIF:
				typeface = Typeface.SERIF;
				break;
			case Utils.MONOSPACE:
				typeface = Typeface.MONOSPACE;
				break;
			}
		} else {
			typeface = null;
		}

		preferences = context.getSharedPreferences(context.getResources().getString(R.string.size), 0);
		if (preferences.getFloat("size", 0) != 0) {
			float fontSize = preferences.getFloat("size", 0);
			textSize = fontSize;
		} else {
			textSize = Float.valueOf(context.getResources().getString(R.string.textSize));
		}

	}

	@Override
	public int getCount() {
		return ((null != appsList) ? appsList.size() : 0);
	}

	@Override
	public ApplicationInfo getItem(int position) {
		return ((null != appsList) ? appsList.get(position) : null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.row_all_apps, null);
			viewHolder = new ViewHolder();
			viewHolder.appNameTextView = (TextView) convertView.findViewById(R.id.textViewAppName);
			viewHolder.appIconImageView = (ImageView) convertView.findViewById(R.id.imageViewAppIcon);
			viewHolder.allAppsRowLayout = (LinearLayout) convertView.findViewById(R.id.linearLayoutAllAppsRow);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		ApplicationInfo applicationInfo = appsList.get(position);
		if (applicationInfo != null) {
			viewHolder.appNameTextView.setText(applicationInfo.loadLabel(packageManager));
			if (typeface == null) {
				viewHolder.appNameTextView.setTypeface(null, Typeface.NORMAL);
			} else {
				viewHolder.appNameTextView.setTypeface(typeface);
			}

			viewHolder.appNameTextView.setTextSize(textSize);

			if (fgColor != 0) {
				viewHolder.appNameTextView.setTextColor(fgColor);
			}
			if (bgColor != 0) {
				viewHolder.allAppsRowLayout.setBackgroundColor(bgColor);
			}
			viewHolder.appIconImageView.setImageDrawable(applicationInfo.loadIcon(packageManager));
		}
		return convertView;
	}

	private static class ViewHolder {
		private TextView appNameTextView;
		private ImageView appIconImageView;
		private LinearLayout allAppsRowLayout;
	}
}