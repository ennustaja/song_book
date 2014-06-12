package com.songbook;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SearchLayout extends LinearLayout {
	public SearchLayout(Context context) {
		super(context);
	}

	public SearchLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SearchLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean fitSystemWindows(Rect insets) {
		LinearLayout.LayoutParams params = ((LinearLayout.LayoutParams)this.getLayoutParams());
		int bottom = params.bottomMargin;
		int left = params.leftMargin;
		int right = params.rightMargin;
		params.setMargins(left, insets.top, right, bottom);
		return super.fitSystemWindows(insets);
	}
}
