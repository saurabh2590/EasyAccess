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

/** Adapted from https://github.com/browep/AndroidAlphaIndexer/blob/master/src/com/github/browep/alphaindexer/SideSelector.java **/

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SectionIndexer;

/**
 * Enables quick letter navigation through the list of contacts.
 */
public class SideSelector extends View {

	public static char[] ALPHABET = new char[] { 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
	public static final int BOTTOM_PADDING = 10;

	private SectionIndexer selectionIndexer = null;
	private ListView list;
	private Paint paint;
	private String[] sections;

	/**
	 * Constructor to initialize the side selector
	 * 
	 * @param context
	 */
	public SideSelector(Context context) {
		super(context);
		init();
	}

	/**
	 * Constructor to initialize the side selector
	 * 
	 * @param context
	 * @param attrs
	 */
	public SideSelector(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * Constructor to initialize the side selector
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SideSelector(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * Initializes the paint object.
	 */

	private void init() {
		setBackgroundColor(0x44FFFFFF);
		paint = new Paint();
		paint.setColor(0xFFA6A9AA);
		paint.setTextSize(20);
		paint.setTextAlign(Paint.Align.CENTER);
	}

	/**
	 * Adds the section index to the ListView.
	 * 
	 * @param _list
	 *            The ListView with which the section index is to be associated.
	 */

	public void setListView(ListView _list) {
		list = _list;
		selectionIndexer = (SectionIndexer) _list.getAdapter();

		Object[] sectionsArr = selectionIndexer.getSections();
		sections = new String[sectionsArr.length];
		for (int i = 0; i < sectionsArr.length; i++) {
			sections[i] = sectionsArr[i].toString();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		int y = (int) event.getY();
		float selectedIndex = ((float) y / (float) getPaddedHeight())
				* ALPHABET.length;

		if (event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_MOVE) {
			if (selectionIndexer == null) {
				selectionIndexer = (SectionIndexer) list.getAdapter();
			}
			int position = selectionIndexer
					.getPositionForSection((int) selectedIndex);
			if (position == -1) {
				return true;
			}
			list.setSelection(position);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		int viewHeight = getPaddedHeight();
		float charHeight = ((float) viewHeight) / (float) sections.length;

		float widthCenter = getMeasuredWidth() / 2;
		for (int i = 0; i < sections.length; i++) {
			canvas.drawText(String.valueOf(sections[i]), widthCenter,
					charHeight + (i * charHeight), paint);
		}
		super.onDraw(canvas);
	}

	/**
	 * Calculates and returns the height after removing the padding from the
	 * bottom.
	 * 
	 * @return the height after removing the padding from the bottom.
	 */
	private int getPaddedHeight() {
		return getHeight() - BOTTOM_PADDING;
	}
}