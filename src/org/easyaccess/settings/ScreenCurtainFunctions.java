/*
	   _           _      _           _     _ 
	  (_)         | |    | |         (_)   | |
	   _ _   _ ___| |_ __| |_ __ ___  _  __| |
	  | | | | / __| __/ _` | '__/ _ \| |/ _` |
	  | | |_| \__ \ || (_| | | | (_) | | (_| |
	  | |\__,_|___/\__\__,_|_|  \___/|_|\__,_|
	 _/ |                                     
	|__/ 
	
	Copyright 2013 Caspar Isemer and and Eva Krueger, http://justdroid.org
	
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

import org.easyaccess.R;

import android.app.Application;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;


public class ScreenCurtainFunctions extends Application {
	
	  private boolean screenCurtainOn;

	  public boolean getState(){
	    return screenCurtainOn;
	  }
	  
	  public void setState(boolean s){
		  screenCurtainOn = s;
	  }
	  
	  /**
	  * 
	  * @param inflater The instance of LayoutInflater using which layout parameters are applied to 
	  * the view.
	  * @return the instance of LayoutParamsAndViewUtils that can be used to implement screen curtain.
	  */
	  public static LayoutParamsAndViewUtils prepareForCurtainCheck(LayoutInflater inflater) {
		  
			View view = inflater.inflate(R.layout.screencurtain, null, false);
			
	    	view.setFocusable(false);
	    	view.setClickable(false);
	    	view.setKeepScreenOn(false);
	    	view.setLongClickable(false);
	    	view.setFocusableInTouchMode(false);
	    	
	    	LayoutParams layoutParams = new LayoutParams();
	    	
	    	layoutParams.height = LayoutParams.MATCH_PARENT; 
	    	layoutParams.width = LayoutParams.MATCH_PARENT;
	    	layoutParams.flags = 280;
	    	layoutParams.format = PixelFormat.TRANSLUCENT;
	    	layoutParams.windowAnimations = android.R.style.Animation_Toast;
	    	layoutParams.type = LayoutParams.TYPE_SYSTEM_OVERLAY;
	    	layoutParams.gravity = Gravity.BOTTOM;
	    	layoutParams.x = 0;
	    	layoutParams.y = 0;
	    	layoutParams.verticalWeight = 1.0F;
	    	layoutParams.horizontalWeight = 1.0F;
	    	layoutParams.verticalMargin = 0.0F;
	    	layoutParams.horizontalMargin = 0.0F;
	    	
	    	LayoutParamsAndViewUtils layoutParamsAndView = new LayoutParamsAndViewUtils(layoutParams, view);
	    	
	    	return layoutParamsAndView;
	  }
}
