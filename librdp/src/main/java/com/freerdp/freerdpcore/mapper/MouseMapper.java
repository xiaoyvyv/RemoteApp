package com.freerdp.freerdpcore.mapper;

import android.content.Context;

import com.freerdp.freerdpcore.presentation.ApplicationSettingsActivity;

public class MouseMapper {

	private final static int PTR_FLAGS_LEFT_BUTTON = 0x1000;
	private final static int PTR_FLAGS_RIGHT_BUTTON = 0x2000;

	private final static int PTR_FLAGS_DOWN = 0x8000;
	private final static int PTR_FLAGS_MOVE = 0x0800;

	private final static int PTR_FLAGS_WHEEL = 0x0200;
	private final static int PTR_FLAGS_WHEEL_NEGATIVE = 0x0100;

	public static int getLeftButtonEvent(Context context, boolean down) {
		if (ApplicationSettingsActivity.getSwapMouseButtons(context)) {
			return (PTR_FLAGS_RIGHT_BUTTON | (down ? PTR_FLAGS_DOWN : 0));
		} else {
			return (PTR_FLAGS_LEFT_BUTTON | (down ? PTR_FLAGS_DOWN : 0));
		}
	}

	public static int getRightButtonEvent(Context context, boolean down) {
		if (ApplicationSettingsActivity.getSwapMouseButtons(context)) {
			return (PTR_FLAGS_LEFT_BUTTON | (down ? PTR_FLAGS_DOWN : 0));
		} else {
			return (PTR_FLAGS_RIGHT_BUTTON | (down ? PTR_FLAGS_DOWN : 0));
		}
	}

	public static int getMoveEvent() {
		return PTR_FLAGS_MOVE;
	}

	public static int getScrollEvent(Context context, boolean down) {
		int flags = PTR_FLAGS_WHEEL;

		// 反转滚动？
		if (ApplicationSettingsActivity.getInvertScrolling(context))
			down = !down;

		if (down) {
			flags |= (PTR_FLAGS_WHEEL_NEGATIVE | 0x0088);
		} else {
			flags |= 0x0078;
		}
		return flags;
	}
}
