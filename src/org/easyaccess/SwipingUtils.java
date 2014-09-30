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
import java.util.List;

import org.easyaccess.settings.LayoutParamsAndViewUtils;
import org.easyaccess.settings.ScreenCurtainFunctions;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class SwipingUtils extends FragmentActivity {

	protected View curtainView;
	protected boolean curtainSet = false;

	MyPageAdapter pageAdapter;

	@Override
	public void onResume() {
		super.onResume();
		// check if keyboard is connected or accessibility services are enabled
		if (Utils.isAccessibilityEnabled(getApplicationContext())
				|| getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			TTS.speak(getResources()
					.getString(R.string.appNameTalkBackFriendly));
		}
		WindowManager windowManager = getWindowManager();
		LayoutInflater inflater = getLayoutInflater();
		LayoutParamsAndViewUtils layoutParamsAndView = ScreenCurtainFunctions
				.prepareForCurtainCheck(inflater);

		ScreenCurtainFunctions appState = ((ScreenCurtainFunctions) getApplicationContext());
		if (appState.getState() && !curtainSet) {
			curtainView = layoutParamsAndView.getView();
			windowManager.addView(curtainView,
					layoutParamsAndView.getLayoutParams());
			curtainSet = true;
		} else if (!appState.getState() && curtainSet) {
			windowManager.removeView(curtainView);
			curtainSet = false;
		}
	}

	@Override
	public void onBackPressed() {
		// do nothing
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swiping);

		ScreenCurtainFunctions appState = ((ScreenCurtainFunctions) getApplicationContext());
		appState.setState(false);

		/** Set Roboto as the standard font **/
		if (Build.VERSION.SDK_INT < 11) {
			ViewGroup godfatherView = (ViewGroup) this.getWindow()
					.getDecorView();
			FontUtils.setRobotoFont(this, godfatherView);
		}

		List<Fragment> fragments = getFragments();

		pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);

		ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
		pager.setAdapter(pageAdapter);

	}

	/**
	 * Adds a fragment to the list and returns the populatd list.
	 * 
	 * @return The populated list is returned.
	 */
	private List<Fragment> getFragments() {
		List<Fragment> fList = new ArrayList<Fragment>();

		fList.add(new HomescreenActivity());
		return fList;
	}

	private class MyPageAdapter extends FragmentPagerAdapter {

		/** Adapter for loading fragments **/

		private List<Fragment> fragments;

		public MyPageAdapter(FragmentManager fm, List<Fragment> fragments) {
			super(fm);
			this.fragments = fragments;
		}

		@Override
		public Fragment getItem(int position) {
			return this.fragments.get(position);
		}

		@Override
		public int getCount() {
			return this.fragments.size();
		}
	}
}
